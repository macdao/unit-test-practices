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

    private ThreadFactory threadFactory = Executors.defaultThreadFactory();
    private AtomicReference<MyDriverAdapter> myDriverReference;
    private MyConnectionReceiver myConnectionReceiver;
    private MyConnectionReceiverFactory myConnectionReceiverFactory;
    private final MyConnectionOpener myConnectionOpener;

    public MyConnection(String[] uris) {
        this(uris, new MyConnectionOpenerFactory(), new AtomicReference<MyDriverAdapter>());
    }

    public MyConnection(String[] uris, MyConnectionOpenerFactory myConnectionOpenerFactory, AtomicReference<MyDriverAdapter> myDriverReference) {
        this.myDriverReference = myDriverReference;
        myConnectionOpener = myConnectionOpenerFactory.newMyConnectionOpener(uris, RECONNECT_INTERVAL, myDriverReference);
    }

    public void open() {
        threadFactory.newThread(myConnectionOpener).start();
    }

    public Closeable subscribe(final int queryId, MySubscriber subscriber) {
        if (myConnectionReceiver == null) {
            synchronized (this) {
                if (myConnectionReceiver == null) {
                    myConnectionReceiver = myConnectionReceiverFactory.newMyConnectionReceiver(myDriverReference, myConnectionOpener);
                    threadFactory.newThread(myConnectionReceiver).start();
                }
            }
        }
        myConnectionReceiver.addSubscriber(queryId, subscriber);
        return new Closeable() {
            @Override
            public void close() throws IOException {
                myConnectionReceiver.removeSubscriber(queryId);
            }
        };
    }

    public void close() {
        throw new RuntimeException("Not implemented");
    }

    public synchronized void addConnectionListener(MyConnectionEventListener listener) {
        throw new RuntimeException("Not implemented");
    }

    public synchronized void removeConnectionListener(MyConnectionEventListener listener) {
        throw new RuntimeException("Not implemented");
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    public void setMyConnectionReceiverFactory(MyConnectionReceiverFactory myConnectionReceiverFactory) {
        this.myConnectionReceiverFactory = myConnectionReceiverFactory;
    }
}
