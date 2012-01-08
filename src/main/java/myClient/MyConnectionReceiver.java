package myClient;

import myDriver.MyData;
import myDriver.MyDriverException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class MyConnectionReceiver implements Runnable {
    private AtomicReference<MyDriverAdapter> myDriverReference;
    private final Map<Integer, MySubscriber> mySubscriberMap = new HashMap<Integer, MySubscriber>();

    public MyConnectionReceiver(AtomicReference<MyDriverAdapter> myDriverReference) {
        this.myDriverReference = myDriverReference;
    }

    @Override
    public void run() {
        while (true) {
            if (myDriverReference.get() == null) {
                return;
            }
            MyData myData = null;
            try {
                myData = myDriverReference.get().receive();
            } catch (MyDriverException e) {
                //todo
            }
            if (myData == null) {
                break;
            } else {
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
    }

    public synchronized void addSubscriber(int queryId, MySubscriber subscriber) throws QueryIdDuplicateException {
        if (mySubscriberMap.containsKey(queryId)) {
            throw new QueryIdDuplicateException(queryId);
        }

        mySubscriberMap.put(queryId, subscriber);
    }
}
