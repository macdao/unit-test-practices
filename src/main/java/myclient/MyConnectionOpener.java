package myclient;

import myclient.factory.MyDriverFactory;
import mydriver2.MyDriverException;

import java.util.EventObject;
import java.util.List;

public class MyConnectionOpener implements Runnable {
    private String[] uris;
    private int reconnectInterval;
    private MyDriverFactory myDriverFactory;
    private CommonUtility commonUtility;
    private final List<MyConnectionEventListener> listeners;

    public MyConnectionOpener(String[] uris, int reconnectInterval, List<MyConnectionEventListener> listeners) {
        this.uris = uris;
        this.reconnectInterval = reconnectInterval;
        this.listeners = listeners;
    }

    @Override
    public void run() {
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
                break;
            } catch (MyDriverException e) {
                for (MyConnectionEventListener listener : listeners) {
                    listener.connectionFailed(new EventObject(myDriver));
                }
            }
            commonUtility.threadSleep(reconnectInterval);
        }
    }

    public void setMyDriverFactory(MyDriverFactory myDriverFactory) {
        this.myDriverFactory = myDriverFactory;
    }

    public void setCommonUtility(CommonUtility commonUtility) {
        this.commonUtility = commonUtility;
    }
}
