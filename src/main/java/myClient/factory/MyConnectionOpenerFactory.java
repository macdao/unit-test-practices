package myClient.factory;

import myClient.MyConnectionEventListener;
import myClient.MyConnectionOpener;

import java.util.List;

public class MyConnectionOpenerFactory {
    public MyConnectionOpener newMyConnectionOpener(String[] uris, int reconnectInterval, List<MyConnectionEventListener> listeners) {
        return new MyConnectionOpener(uris, reconnectInterval, listeners);
    }
}
