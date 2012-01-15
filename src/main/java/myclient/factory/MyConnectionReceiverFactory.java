package myclient.factory;

import myclient.MyConnectionReceiver;
import myclient.MySyncConnection;

public class MyConnectionReceiverFactory {
    public MyConnectionReceiver newMyConnectionReceiver(MySyncConnection mySyncConnection) {
        return new MyConnectionReceiver(mySyncConnection);
    }
}
