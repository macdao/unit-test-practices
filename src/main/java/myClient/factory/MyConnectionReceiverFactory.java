package myClient.factory;

import myClient.MyConnectionOpener;
import myClient.MyConnectionReceiver;
import myClient.MyDriverAdapter;

import java.util.concurrent.atomic.AtomicReference;

public class MyConnectionReceiverFactory {
    public MyConnectionReceiver newMyConnectionReceiver(AtomicReference<MyDriverAdapter> myDriverReference, MyConnectionOpener myConnectionOpener) {
        return new MyConnectionReceiver(myDriverReference, myConnectionOpener);
    }
}
