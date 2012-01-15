package myclient.factory;

import myclient.MySubscriber;
import myclient.MySyncConnection;

import java.util.Map;

public class MySyncConnectionFactory {
    public MySyncConnection newMySyncConnection(String[] uris, MyDriverFactory myDriverFactory, Map<Integer, MySubscriber> mySubscriberMap) {
        return new MySyncConnection(uris, myDriverFactory, mySubscriberMap);
    }
}
