package myClient.factory;

import myClient.MyDriverAdapter;

public class MyDriverFactory {
    public MyDriverAdapter newMyDriver(String uri) {
        return new MyDriverAdapter(uri);
    }
}
