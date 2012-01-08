package myClient;

import myClient.factory.MyConnectionOpenerFactory;
import myClient.factory.MyConnectionReceiverFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

public class MyConnection {

    public final static int RECONNECT_INTERVAL = 3000;

    private final ThreadFactory threadFactory;
    private final MyConnectionReceiver myConnectionReceiver;
    private final MyConnectionOpener myConnectionOpener;
    private final AtomicReference<MyDriverAdapter> myDriverReference;

    public MyConnection(String[] uris) {
        this(uris, new MyConnectionOpenerFactory(), new AtomicReference<MyDriverAdapter>(), new MyConnectionReceiverFactory(), Executors.defaultThreadFactory());
    }

    public MyConnection(String[] uris, MyConnectionOpenerFactory myConnectionOpenerFactory, AtomicReference<MyDriverAdapter> myDriverReference, MyConnectionReceiverFactory myConnectionReceiverFactory, ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        this.myDriverReference = myDriverReference;
        myConnectionOpener = myConnectionOpenerFactory.newMyConnectionOpener(uris, RECONNECT_INTERVAL, myDriverReference);
        myConnectionReceiver = myConnectionReceiverFactory.newMyConnectionReceiver(myDriverReference, myConnectionOpener);
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
        MyDriverAdapter myDriverAdapter = myDriverReference.get();
        if (myDriverAdapter != null) {
            myDriverAdapter.close();
        }
    }

    public synchronized void addConnectionListener(MyConnectionEventListener listener) {
        throw new RuntimeException("Not implemented");
    }

    public synchronized void removeConnectionListener(MyConnectionEventListener listener) {
        throw new RuntimeException("Not implemented");
    }
}
