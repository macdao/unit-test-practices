package myclient;

import myclient.factory.MyDriverFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class MyConnection implements MyConnectionInterface, Runnable {

    public final static int RECONNECT_INTERVAL = 3000;

    private final MyConnectionInterface myConnection;

    private final Map<Integer, MySubscriber> mySubscriberMap;

    private final ThreadFactory threadFactory;

    private boolean opened;

    private final List<MyConnectionEventListener> listeners;

    public MyConnection(String[] uris) {
        this(uris, new MyConnectionFactory(), new HashMap<Integer, MySubscriber>(), Executors.defaultThreadFactory(), new ArrayList<MyConnectionEventListener>(), new MyDriverFactory(), new CommonUtility());
    }

    public MyConnection(String[] uris, MyConnectionFactory myConnectionFactory, Map<Integer, MySubscriber> mySubscriberMap, ThreadFactory threadFactory, List<MyConnectionEventListener> listeners, MyDriverFactory myDriverFactory, CommonUtility commonUtility) {
        myConnection = myConnectionFactory.newMyConnection(uris, myDriverFactory, RECONNECT_INTERVAL, commonUtility, listeners);
        this.mySubscriberMap = mySubscriberMap;
        this.threadFactory = threadFactory;
        this.listeners = listeners;
    }

    public void open() {
        opened = true;
        threadFactory.newThread(this).start();
    }

    public Closeable subscribe(final int queryId, MySubscriber subscriber) {
        mySubscriberMap.put(queryId, subscriber);
        return new Closeable() {
            @Override
            public void close() throws IOException {
                mySubscriberMap.remove(queryId);
            }
        };
    }

    public void close() {
        opened = false;
    }

    public synchronized void addConnectionListener(MyConnectionEventListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeConnectionListener(MyConnectionEventListener listener) {
        listeners.remove(listener);
    }

    public boolean isOpened() {
        return opened;
    }

    @Override
    public void run() {
        while (true) {
            boolean oneLoop = oneLoop();
            if (!oneLoop) {
                return;
            }
        }
    }

    private boolean oneLoop() {
        //todo
        return false;  //To change body of created methods use File | Settings | File Templates.
    }
}
