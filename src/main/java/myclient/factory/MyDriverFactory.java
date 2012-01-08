package myclient.factory;

import myclient.MyDriverAdapter;

public class MyDriverFactory {
    public MyDriverAdapter newMyDriver(String uri) {
        return new MyDriverAdapter(uri);
    }
}
