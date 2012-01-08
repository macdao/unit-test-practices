package myClient.factory;

import myClient.MyConnectionReceiver;
import myClient.MyDriverAdapter;

import java.util.concurrent.atomic.AtomicReference;

public class MyConnectionReceiverFactory {
    public MyConnectionReceiver newMyConnectionReceiver(AtomicReference<MyDriverAdapter> myDriverReference){
        return new MyConnectionReceiver(myDriverReference);
    }
}
