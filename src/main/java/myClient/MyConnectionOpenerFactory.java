package myClient;

import myDriver.MyDriver;

import java.util.concurrent.atomic.AtomicReference;

public class MyConnectionOpenerFactory {
    public MyConnectionOpener newMyConnectionOpener(String[] uris, int reconnectInterval, AtomicReference<MyDriver> myDriverReference) {
        return new MyConnectionOpener(uris, reconnectInterval, myDriverReference);
    }
}
