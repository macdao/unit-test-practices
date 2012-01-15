package myclient.factory;

import myclient.CommonUtility;
import myclient.MyConnectionOpener;
import myclient.MySubscriber;
import myclient.MySyncConnection;

import java.util.Map;

public class MyConnectionOpenerFactory {
    public MyConnectionOpener newMyConnectionOpener(MySyncConnection mySyncConnection, CommonUtility commonUtility, int reconnectInterval, Map<Integer, MySubscriber> effectedSubscriberMap) {
        return new MyConnectionOpener(mySyncConnection, commonUtility, reconnectInterval, effectedSubscriberMap);
    }
}
