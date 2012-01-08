package myClient;

import myDriver.MyDriver;

import java.util.concurrent.atomic.AtomicReference;

public class MyConnectionReceiverFactory {
    public MyConnectionReceiver newMyConnectionReceiver(AtomicReference<MyDriver> myDriverReference){
        return new MyConnectionReceiver(myDriverReference);
    }
}
