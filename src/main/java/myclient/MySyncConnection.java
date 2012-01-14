package myclient;

import com.google.common.collect.Maps;
import myclient.factory.MyDriverFactory;
import mydriver.MyDriverException;

import java.io.Closeable;
import java.io.IOException;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

public class MySyncConnection implements MyConnectionInterface, MyConnectionEventListener {
    private final String[] uris;
    private final MyDriverFactory myDriverFactory;
    private final int reconnectInterval;
    private final CommonUtility commonUtility;
    private final List<MyConnectionEventListener> listeners;
    private final Map<Integer, MySubscriber> mySubscribers = Maps.newHashMap();
    private MyDriverAdapter myDriverAdapter;

    public MySyncConnection(String[] uris, MyDriverFactory myDriverFactory, int reconnectInterval, CommonUtility commonUtility, List<MyConnectionEventListener> listeners) {
        this.uris = uris;
        this.myDriverFactory = myDriverFactory;
        this.reconnectInterval = reconnectInterval;
        this.commonUtility = commonUtility;
        listeners.add(this);
        this.listeners = listeners;
    }

    @Override
    public void open() {
        int i = 0;
        while (true) {
            if (i == uris.length) {
                i = 0;
            }

            String uri = uris[i++];
            MyDriverAdapter myDriver = myDriverFactory.newMyDriver(uri);
            try {
                myDriver.connect();
                for (MyConnectionEventListener listener : listeners) {
                    listener.connected(new EventObject(myDriver));
                }
                return;
            } catch (MyDriverException e) {
                for (MyConnectionEventListener listener : listeners) {
                    listener.connectionFailed(new EventObject(myDriver));
                }
            }
            commonUtility.threadSleep(reconnectInterval);
        }
    }

    @Override
    public Closeable subscribe(final int queryId, MySubscriber subscriber) {
        try {
            myDriverAdapter.addQuery(queryId);
        } catch (MyDriverException e) {
            handleTransferException(myDriverAdapter, e);
        }
        mySubscribers.put(queryId, subscriber);
        return new Closeable() {
            @Override
            public void close() throws IOException {
                cancelSubscribe(queryId);
            }
        };
    }

    private void cancelSubscribe(int queryId) {
        try {
            myDriverAdapter.removeQuery(queryId);
        } catch (MyDriverException e) {
            handleTransferException(myDriverAdapter, e);
        }
        mySubscribers.remove(queryId);
    }

    @Override
    public void close() {
        for (MyConnectionEventListener listener : listeners) {
            listener.disconnected(new EventObject(myDriverAdapter));
        }
    }

    @Override
    public void addConnectionListener(MyConnectionEventListener listener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeConnectionListener(MyConnectionEventListener listener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void connected(EventObject event) {
        myDriverAdapter = (MyDriverAdapter) event.getSource();
    }

    @Override
    public void connectionFailed(EventObject event) {
    }

    @Override
    public void disconnected(EventObject event) {
        myDriverAdapter.close();
        myDriverAdapter = null;
    }

    public Map<Integer, MySubscriber> getMySubscribers() {
        return mySubscribers;
    }

    public MyDriverAdapter getMyDriverAdapter() {
        return myDriverAdapter;
    }

    private void handleTransferException(MyDriverAdapter myDriverAdapter, MyDriverException e) {
        for (MyConnectionEventListener listener : listeners) {
            listener.disconnected(new EventObject(myDriverAdapter));
        }
        throw new RuntimeException(e);
    }
}
