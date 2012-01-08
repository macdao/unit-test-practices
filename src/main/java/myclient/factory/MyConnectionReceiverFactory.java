package myclient.factory;

import myclient.MyConnectionEventListener;
import myclient.MyConnectionOpener;
import myclient.MyConnectionReceiver;
import myclient.MyDriverAdapter;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MyConnectionReceiverFactory {
    public MyConnectionReceiver newMyConnectionReceiver(AtomicReference<MyDriverAdapter> myDriverReference, MyConnectionOpener myConnectionOpener, List<MyConnectionEventListener> listeners) {
        return new MyConnectionReceiver(myDriverReference, myConnectionOpener, listeners);
    }
}
