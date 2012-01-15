package myclient;

import com.google.common.collect.ImmutableMap;
import mydriver.MyDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MyConnectionOpener implements MyOneLoop {
    private final MySyncConnection mySyncConnection;
    private boolean opened;
    private CommonUtility commonUtility;
    private int reconnectInterval;
    private final Map<Integer, MySubscriber> mySubscriberMap = new HashMap<Integer, MySubscriber>();
    private final Map<Integer, MySubscriber> effectedSubscriberMap;
    private final Logger logger = LoggerFactory.getLogger(getClass());

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

        if (mySubscriberMap.size() != effectedSubscriberMap.size()) {
            logger.info("{}-{}", mySubscriberMap, effectedSubscriberMap);
        }

        final Map<Integer, MySubscriber> subscriberMap = ImmutableMap.copyOf(mySubscriberMap);
        final Map<Integer, MySubscriber> effectedMap = ImmutableMap.copyOf(effectedSubscriberMap);
        for (Map.Entry<Integer, MySubscriber> entry : subscriberMap.entrySet()) {
            if (!effectedMap.containsKey(entry.getKey())) {
                try {
                    logger.info("Subscribe {}", entry.getKey());
                    mySyncConnection.subscribe(entry.getKey(), entry.getValue());
                } catch (MyDriverException e) {
                    logger.warn("Subscribe failed:{}", entry.getKey());
                    return true;
                }
            }
        }


        for (Map.Entry<Integer, MySubscriber> entry : effectedMap.entrySet()) {
            if (!subscriberMap.containsKey(entry.getKey())) {
                try {
                    logger.info("Cancel subscribe {}", entry.getKey());
                    mySyncConnection.cancelSubscribe(entry.getKey());
                } catch (MyDriverException e) {
                    logger.warn("Cancel subscribe failed:{}", entry.getKey());
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
