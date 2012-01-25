package myclient;

import com.google.common.collect.Lists;
import myclient.factory.MyDriverFactory;
import mydriver.MyData;
import mydriver.MyDriverException;

import java.io.Closeable;
import java.io.IOException;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

public class MySyncConnection implements MyConnectionInterface, MyConnectionEventListener {
    private final String[] uris;
    private final MyDriverFactory myDriverFactory;
    private final List<MyConnectionEventListener> listeners;
    private final Map<Integer, MySubscriber> mySubscriberMap;
    private volatile MyDriverAdapter myDriverAdapter;
    private int urisIndex;

    public MySyncConnection(String[] uris, MyDriverFactory myDriverFactory, Map<Integer, MySubscriber> mySubscriberMap) {
        this.uris = uris;
        this.myDriverFactory = myDriverFactory;
        listeners = Lists.newArrayList();
        listeners.add(this);
        this.mySubscriberMap = mySubscriberMap;
    }

    @Override
    public void open() throws MyDriverException {
        if (myDriverAdapter != null) {
            return;
        }

        if (urisIndex == uris.length) {
            urisIndex = 0;
        }

        String uri = uris[urisIndex++];
        MyDriverAdapter myDriver = myDriverFactory.newMyDriver(uri);
        try {
            myDriver.connect();
            for (MyConnectionEventListener listener : listeners) {
                listener.connected(new EventObject(myDriver));
            }
        } catch (MyDriverException e) {
            for (MyConnectionEventListener listener : listeners) {
                listener.connectionFailed(new EventObject(myDriver));
            }
            throw e;
        }
    }

    @Override
    public Closeable subscribe(final int queryId, MySubscriber subscriber) throws MyDriverException {
        final MyDriverAdapter driver = myDriverAdapter;
        if (driver == null) {
            return new Closeable() {

                @Override
                public void close() throws IOException {

                }
            };
        }

        try {
            driver.addQuery(queryId);
        } catch (MyDriverException e) {
            handleTransferException(driver);
            throw e;
        }
        mySubscriberMap.put(queryId, subscriber);
        return new Closeable() {
            @Override
            public void close() throws IOException {
                try {
                    cancelSubscribe(queryId);
                } catch (MyDriverException e) {
                    throw new IOException(e);
                }
            }
        };
    }

    public void cancelSubscribe(int queryId) throws MyDriverException {
        final MyDriverAdapter driver = myDriverAdapter;
        if (driver == null) {
            return;
        }

        try {
            driver.removeQuery(queryId);
        } catch (MyDriverException e) {
            handleTransferException(driver);
            throw e;
        }
        mySubscriberMap.remove(queryId);
    }

    @Override
    public void close() {
        MyDriverAdapter driver = myDriverAdapter;
        if (driver != null) {
            for (MyConnectionEventListener listener : listeners) {
                listener.disconnected(new EventObject(driver));
            }
        }
    }

    public void receive() throws MyDriverException {
        final MyDriverAdapter driver = myDriverAdapter;
        if (driver == null) {
            return;
        }

        final MyData myData;
        try {
            myData = driver.receive();
        } catch (MyDriverException e) {
            handleTransferException(driver);
            throw e;
        }

        if (myData != null) {
            if (mySubscriberMap.containsKey(myData.queryId)) {
                MySubscriber mySubscriber = mySubscriberMap.get(myData.queryId);
                if ("begin".equals(myData.value)) {
                    mySubscriber.onBegin();
                } else {
                    mySubscriber.onMessage(myData.value);
                }
            }
        }
    }

    @Override
    public void addConnectionListener(MyConnectionEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeConnectionListener(MyConnectionEventListener listener) {
        listeners.remove(listener);
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
        mySubscriberMap.clear();
        myDriverAdapter.close();
        myDriverAdapter = null;
    }

    public MyDriverAdapter getMyDriverAdapter() {
        return myDriverAdapter;
    }

    private void handleTransferException(MyDriverAdapter myDriverAdapter) {
        for (MyConnectionEventListener listener : listeners) {
            listener.disconnected(new EventObject(myDriverAdapter));
        }
    }
}
