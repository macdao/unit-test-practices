package myClient;

import myClient.factory.MyDriverFactory;
import myDriver.MyDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class MyConnectionOpener implements Runnable {
    private String[] uris;
    private int reconnectInterval;
    private AtomicReference<MyDriverAdapter> myDriverReference;
    private MyDriverFactory myDriverFactory;
    private CommonUtility commonUtility;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public MyConnectionOpener(String[] uris, int reconnectInterval, AtomicReference<MyDriverAdapter> myDriverReference) {
        this.uris = uris;
        this.reconnectInterval = reconnectInterval;
        this.myDriverReference = myDriverReference;
    }

    @Override
    public void run() {
        int i = 0;
        while (true) {
            if (i == uris.length) {
                i = 0;
            }

            String uri = uris[i++];
            MyDriverAdapter myDriver = myDriverFactory.newMyDriver(uri);
            try {
                myDriver.connect();
                myDriverReference.set(myDriver);
                break;
            } catch (MyDriverException e) {
                logger.warn("Failed to connect to uri {} and got '{}'", uri, e.getMessage());
            }
            commonUtility.threadSleep(reconnectInterval);
        }
    }

    public void setMyDriverFactory(MyDriverFactory myDriverFactory) {
        this.myDriverFactory = myDriverFactory;
    }

    public void setCommonUtility(CommonUtility commonUtility) {
        this.commonUtility = commonUtility;
    }
}
