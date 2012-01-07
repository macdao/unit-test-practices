package myClient;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyConnection {

    public final static int RECONNECT_INTERVAL = 3000;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String[] uris;

    public MyConnection(String[] uris) {
        this.uris = uris;
    }

    public void open() {
        executorService.submit(new MyConnectionOpener(uris, RECONNECT_INTERVAL));
    }

    public void close() {
        throw new RuntimeException("Not implemented");
    }

    public Closeable subscribe(int queryId, MySubscriber subscriber) {
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
}
