package myclient;

import mydriver.MyDriverException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyConnectionOpener implements MyOneLoop {
    private final MySyncConnection mySyncConnection;
    private volatile boolean opened;
    private final CommonUtility commonUtility;
    private final int reconnectInterval;
    private final Map<Integer, MySubscriber> mySubscriberMap = new ConcurrentHashMap<Integer, MySubscriber>();
    private final Map<Integer, MySubscriber> effectedSubscriberMap;

    public MyConnectionOpener(MySyncConnection mySyncConnection, CommonUtility commonUtility, int reconnectInterval, Map<Integer, MySubscriber> effectedSubscriberMap) {
        this.mySyncConnection = mySyncConnection;
        this.commonUtility = commonUtility;
        this.reconnectInterval = reconnectInterval;
        this.effectedSubscriberMap = effectedSubscriberMap;
    }

    public boolean oneLoop() {
        if (!opened) {
            mySyncConnection.close();
            return false;
        }

        try {
            mySyncConnection.open();
        } catch (Exception e) {
            commonUtility.threadSleep(reconnectInterval);
            return true;
        }

        for (Map.Entry<Integer, MySubscriber> entry : mySubscriberMap.entrySet()) {
            if (!effectedSubscriberMap.containsKey(entry.getKey())) {
                try {
                    mySyncConnection.subscribe(entry.getKey(), entry.getValue());
                } catch (MyDriverException e) {
                    return true;
                }
            }
        }


        for (Map.Entry<Integer, MySubscriber> entry : effectedSubscriberMap.entrySet()) {
            if (!mySubscriberMap.containsKey(entry.getKey())) {
                try {
                    mySyncConnection.cancelSubscribe(entry.getKey());
                } catch (MyDriverException e) {
                    return true;
                }
            }
        }

        return true;
    }

    public void addSubscribe(int queryId, MySubscriber subscriber) {
        if (mySubscriberMap.containsKey(queryId)) {
            throw new QueryIdDuplicateException(queryId);
        }
        mySubscriberMap.put(queryId, subscriber);
    }

    public void removeSubscribe(int queryId) {
        if (mySubscriberMap.containsKey(queryId)) {
            mySubscriberMap.remove(queryId);
        }
    }

    public Map<Integer, MySubscriber> getMySubscriberMap() {
        return mySubscriberMap;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }
}
