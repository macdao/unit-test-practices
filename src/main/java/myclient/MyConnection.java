package myclient;

import myclient.factory.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class MyConnection implements MyConnectionInterface {

    public final static int RECONNECT_INTERVAL = 3000;

    private final MySyncConnection mySyncConnection;

    private final ThreadFactory threadFactory;

    private final MyConnectionOpener myConnectionOpener;
    private final MyConnectionReceiver myConnectionReceiver;
    private final MyRunnable openerRunner;
    private final MyRunnable receiverRunner;

    public MyConnection(String[] uris) {
        this(uris, new MySyncConnectionFactory(), new HashMap<Integer, MySubscriber>(), Executors.defaultThreadFactory(), new MyDriverFactory(), new CommonUtility(), new MyConnectionOpenerFactory(), new MyConnectionReceiverFactory(), new MyRunnableFactory());
    }

    public MyConnection(String[] uris, MySyncConnectionFactory mySyncConnectionFactory, Map<Integer, MySubscriber> mySubscriberMap, ThreadFactory threadFactory, MyDriverFactory myDriverFactory, CommonUtility commonUtility, MyConnectionOpenerFactory myConnectionOpenerFactory, MyConnectionReceiverFactory myConnectionReceiverFactory, MyRunnableFactory myRunnableFactory) {
        mySyncConnection = mySyncConnectionFactory.newMySyncConnection(uris, myDriverFactory, mySubscriberMap);
        this.threadFactory = threadFactory;
        myConnectionOpener = myConnectionOpenerFactory.newMyConnectionOpener(mySyncConnection, commonUtility, RECONNECT_INTERVAL, mySubscriberMap);
        myConnectionReceiver = myConnectionReceiverFactory.newMyConnectionReceiver(mySyncConnection);
        openerRunner = myRunnableFactory.newMyRunnable(myConnectionOpener);
        receiverRunner = myRunnableFactory.newMyRunnable(myConnectionReceiver);
    }

    public void open() {
        myConnectionOpener.setOpened(true);
        myConnectionReceiver.setOpened(true);
        threadFactory.newThread(openerRunner).start();
        threadFactory.newThread(receiverRunner).start();
    }

    public Closeable subscribe(final int queryId, MySubscriber subscriber) {
        myConnectionOpener.addSubscribe(queryId, subscriber);
        return new Closeable() {
            @Override
            public void close() throws IOException {
                myConnectionOpener.removeSubscribe(queryId);
            }
        };
    }

    public void close() {
        myConnectionOpener.setOpened(false);
        myConnectionReceiver.setOpened(false);
    }

    public synchronized void addConnectionListener(MyConnectionEventListener listener) {
        mySyncConnection.addConnectionListener(listener);
    }

    public synchronized void removeConnectionListener(MyConnectionEventListener listener) {
        mySyncConnection.removeConnectionListener(listener);
    }
}
