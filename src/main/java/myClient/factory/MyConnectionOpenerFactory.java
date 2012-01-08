package myClient.factory;

import myClient.MyConnectionOpener;
import myClient.MyDriverAdapter;

import java.util.concurrent.atomic.AtomicReference;

public class MyConnectionOpenerFactory {
    public MyConnectionOpener newMyConnectionOpener(String[] uris, int reconnectInterval, AtomicReference<MyDriverAdapter> myDriverReference) {
        return new MyConnectionOpener(uris, reconnectInterval, myDriverReference);
    }
}
