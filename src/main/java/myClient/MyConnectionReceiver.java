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
    private boolean queryIdAdded;

    public MyConnectionReceiver(AtomicReference<MyDriverAdapter> myDriverReference, MyConnectionOpener myConnectionOpener) {
        this.myDriverReference = myDriverReference;
        this.myConnectionOpener = myConnectionOpener;
    }

    @Override
    public void run() {
        while (true) {
            final MyDriverAdapter myDriverAdapter = myDriverReference.get();
            if (myDriverAdapter == null) {
                continue;
            }

            synchronized (this) {
                if (queryIdAdded) {
                    try {
                        addQuery(myDriverAdapter);
                    } catch (MyDriverException e) {
                        handleTransferException(myDriverAdapter);
                        continue;
                    }
                    queryIdAdded = false;
                }
            }

            final MyData myData;
            try {
                myData = myDriverAdapter.receive();
            } catch (MyDriverException e) {
                //事情大条了
                handleTransferException(myDriverAdapter);
                continue;
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
        synchronized (mySubscriberMap) {
            if (mySubscriberMap.containsKey(queryId)) {
                throw new QueryIdDuplicateException(queryId);
            }
            mySubscriberMap.put(queryId, subscriber);
            synchronized (this) {
                queryIdAdded = true;
            }
        }
    }

    public void removeSubscriber(int queryId) {
        synchronized (mySubscriberMap) {
            mySubscriberMap.remove(queryId);

            MyDriverAdapter myDriverAdapter = myDriverReference.get();
            if (myDriverAdapter != null) {
                try {
                    myDriverAdapter.removeQuery(queryId);
                } catch (MyDriverException e) {
                    handleTransferException(myDriverAdapter);
                }
            }
        }
    }

    private void addQuery(MyDriverAdapter myDriverAdapter) throws MyDriverException {
        for (int key : mySubscriberMap.keySet()) {
            myDriverAdapter.addQuery(key);
        }
    }

    private void handleTransferException(MyDriverAdapter myDriverAdapter) {
        myDriverAdapter.close();
        myDriverReference.set(null);
        threadFactory.newThread(myConnectionOpener).start();
        queryIdAdded = true;
    }

    public boolean isQueryIdAdded() {
        return queryIdAdded;
    }

    public void setQueryIdAdded(boolean queryIdAdded) {
        this.queryIdAdded = queryIdAdded;
    }

    public Map<Integer, MySubscriber> getMySubscriberMap() {
        return mySubscriberMap;
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }
}
