package myClient;

import myDriver.MyData;
import myDriver.MyDriverException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

public class MyConnectionReceiver implements Runnable {
    private AtomicReference<MyDriverAdapter> myDriverReference;
    private final Map<Integer, MySubscriber> mySubscriberMap = new HashMap<Integer, MySubscriber>();
    private ThreadFactory threadFactory = Executors.defaultThreadFactory();
    private MyConnectionOpener myConnectionOpener;

    public MyConnectionReceiver(AtomicReference<MyDriverAdapter> myDriverReference, MyConnectionOpener myConnectionOpener) {
        this.myDriverReference = myDriverReference;
        this.myConnectionOpener = myConnectionOpener;
    }

    @Override
    public void run() {
        while (true) {
            MyDriverAdapter myDriverAdapter = myDriverReference.get();
            if (myDriverAdapter == null) {
                return;
            }
            MyData myData = null;
            try {
                myData = myDriverAdapter.receive();
            } catch (MyDriverException e) {
                //事情大条了
                myDriverAdapter.close();
                myDriverReference.set(null);
                threadFactory.newThread(myConnectionOpener).start();
            }
            if (myData == null) {
                break;
            }
            if (mySubscriberMap.containsKey(myData.queryId)) {
                MySubscriber mySubscriber = mySubscriberMap.get(myData.queryId);
                if ("begin".equals(myData.value)) {
                    mySubscriber.onBegin();
                } else {
                    mySubscriber.onMessage(myData.value);
                }
            }

        }
    }

    public void addSubscriber(int queryId, MySubscriber subscriber) throws QueryIdDuplicateException {
        if (mySubscriberMap.containsKey(queryId)) {
            throw new QueryIdDuplicateException(queryId);
        }

        synchronized (mySubscriberMap) {
            mySubscriberMap.put(queryId, subscriber);
        }
    }

    public void removeSubscriber(int queryId) {
        synchronized (mySubscriberMap) {
            mySubscriberMap.remove(queryId);
        }
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }
}
