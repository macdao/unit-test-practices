package myclient.factory;

import myclient.MyConnectionEventListener;
import myclient.MyConnectionReceiver;
import myclient.MyDriverAdapter;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MyConnectionReceiverFactory {
    public MyConnectionReceiver newMyConnectionReceiver(AtomicReference<MyDriverAdapter> myDriverReference, List<MyConnectionEventListener> listeners) {
        return new MyConnectionReceiver(myDriverReference, listeners);
    }
}
