package myclient;

import myclient.factory.MyConnectionOpenerFactory;
import myclient.factory.MyConnectionReceiverFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

public class MyConnection {

    public final static int RECONNECT_INTERVAL = 3000;

    private final ThreadFactory threadFactory;
    private final MyConnectionReceiver myConnectionReceiver;
    private final MyConnectionOpener myConnectionOpener;
    private final List<MyConnectionEventListener> listeners;

    public MyConnection(String[] uris) {
        this(uris, new MyConnectionOpenerFactory(), new AtomicReference<MyDriverAdapter>(), new MyConnectionReceiverFactory(), Executors.defaultThreadFactory(), new ArrayList<MyConnectionEventListener>());
    }

    public MyConnection(String[] uris, MyConnectionOpenerFactory myConnectionOpenerFactory, final AtomicReference<MyDriverAdapter> myDriverReference, MyConnectionReceiverFactory myConnectionReceiverFactory, ThreadFactory threadFactory, List<MyConnectionEventListener> listeners) {
        this.threadFactory = threadFactory;
        listeners.add(new DefaultConnectionEventListener(myDriverReference));
        this.listeners = listeners;
        myConnectionOpener = myConnectionOpenerFactory.newMyConnectionOpener(uris, RECONNECT_INTERVAL, listeners);
        myConnectionReceiver = myConnectionReceiverFactory.newMyConnectionReceiver(myDriverReference, listeners);
        threadFactory.newThread(myConnectionReceiver).start();
    }

    public void open() {
        threadFactory.newThread(myConnectionOpener).start();
    }

    public Closeable subscribe(final int queryId, MySubscriber subscriber) {
        myConnectionReceiver.addSubscriber(queryId, subscriber);
        return new Closeable() {
            @Override
            public void close() throws IOException {
                myConnectionReceiver.removeSubscriber(queryId);
            }
        };
    }

    public void close() {
        myConnectionReceiver.close();
        myConnectionOpener.close();
    }

    public synchronized void addConnectionListener(MyConnectionEventListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeConnectionListener(MyConnectionEventListener listener) {
        listeners.remove(listener);
    }

}
