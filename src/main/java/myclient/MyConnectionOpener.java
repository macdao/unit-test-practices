package myclient;

import myclient.factory.MyDriverFactory;
import mydriver.MyDriverException;

import java.util.EventObject;
import java.util.List;

public class MyConnectionOpener implements Runnable {
    private String[] uris;
    private int reconnectInterval;
    private MyDriverFactory myDriverFactory;
    private CommonUtility commonUtility;
    private final List<MyConnectionEventListener> listeners;
    private boolean closed;
    private int i;

    public MyConnectionOpener(String[] uris, int reconnectInterval, List<MyConnectionEventListener> listeners) {
        this.uris = uris;
        this.reconnectInterval = reconnectInterval;
        this.listeners = listeners;
        i = 0;
    }

    @Override
    public void run() {
        while (true) {
            if (!oneLoop()) {
                return;
            }
        }
    }

    private boolean oneLoop() {
        if (closed) {
            return false;
        }
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
            return false;
        } catch (MyDriverException e) {
            for (MyConnectionEventListener listener : listeners) {
                listener.connectionFailed(new EventObject(myDriver));
            }
        }
        commonUtility.threadSleep(reconnectInterval);
        return true;
    }

    public void setMyDriverFactory(MyDriverFactory myDriverFactory) {
        this.myDriverFactory = myDriverFactory;
    }

    public void setCommonUtility(CommonUtility commonUtility) {
        this.commonUtility = commonUtility;
    }

    public void close() {
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }
}
