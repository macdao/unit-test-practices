package myClient.factory;

import myClient.MyConnectionEventListener;
import myClient.MyConnectionOpener;
import myClient.MyConnectionReceiver;
import myClient.MyDriverAdapter;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MyConnectionReceiverFactory {
    public MyConnectionReceiver newMyConnectionReceiver(AtomicReference<MyDriverAdapter> myDriverReference, MyConnectionOpener myConnectionOpener, List<MyConnectionEventListener> listeners) {
        return new MyConnectionReceiver(myDriverReference, myConnectionOpener, listeners);
    }
}
