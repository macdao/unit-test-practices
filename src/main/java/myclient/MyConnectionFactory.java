package myclient;

import myclient.factory.MyDriverFactory;

import java.util.List;

public class MyConnectionFactory {
    public MyConnectionInterface newMyConnection(String[] uris, MyDriverFactory myDriverFactory, int reconnectInterval, CommonUtility commonUtility, List<MyConnectionEventListener> listeners) {
        return new MySyncConnection(uris, myDriverFactory, reconnectInterval, commonUtility, listeners);
    }
}
