package myClient;

import myDriver.MyDriver;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class MyConnection {

    public final static int RECONNECT_INTERVAL = 3000;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String[] uris;
    private AtomicReference<MyDriver> myDriverReference = new AtomicReference<MyDriver>();
    private MyConnectionReceiver myConnectionReceiver;
    private MyConnectionReceiverFactory myConnectionReceiverFactory;
    private MyConnectionOpenerFactory myConnectionOpenerFactory;

    public MyConnection(String[] uris) {
        this.uris = uris;
    }

    public void open() {
        MyConnectionOpener myConnectionOpener = myConnectionOpenerFactory.newMyConnectionOpener(uris, RECONNECT_INTERVAL, myDriverReference);
        executorService.submit(myConnectionOpener);
    }

    public Closeable subscribe(int queryId, MySubscriber subscriber) {
        if (myConnectionReceiver == null) {
            synchronized (this) {
                if (myConnectionReceiver == null) {
                    myConnectionReceiver = myConnectionReceiverFactory.newMyConnectionReceiver(myDriverReference);
                    executorService.submit(myConnectionReceiver);
                }
            }
        }
        myConnectionReceiver.addSubscriber(queryId, subscriber);
        return null;
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

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setMyDriverReference(AtomicReference<MyDriver> myDriverReference) {
        this.myDriverReference = myDriverReference;
    }

    public void setMyConnectionReceiverFactory(MyConnectionReceiverFactory myConnectionReceiverFactory) {
        this.myConnectionReceiverFactory = myConnectionReceiverFactory;
    }

    public void setMyConnectionOpenerFactory(MyConnectionOpenerFactory myConnectionOpenerFactory) {
        this.myConnectionOpenerFactory = myConnectionOpenerFactory;
    }
}
