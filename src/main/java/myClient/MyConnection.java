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

    public MyConnection(String[] uris) {
        this.uris = uris;
    }

    public void open() {
        executorService.submit(new MyConnectionOpener(uris, RECONNECT_INTERVAL, myDriverReference));
    }

    public Closeable subscribe(int queryId, MySubscriber subscriber) {
        executorService.submit(new MyConnectionReceiver(getMyDriver(),subscriber,queryId));
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

    public MyDriver getMyDriver(){
        return myDriverReference.get();
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setMyDriverReference(AtomicReference<MyDriver> myDriverReference) {
        this.myDriverReference = myDriverReference;
    }
}
