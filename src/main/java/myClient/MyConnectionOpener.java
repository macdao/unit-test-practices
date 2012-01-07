package myClient;

import myDriver.MyDriver;
import myDriver.MyDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Macdao
 * Date: 12-1-7
 * Time: 下午9:22
 * To change this template use File | Settings | File Templates.
 */
public class MyConnectionOpener implements Runnable {
    private String[] uris;
    private int reconnectInterval;
    private MyDriverFactory myDriverFactory;
    private CommonUtility commonUtility;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public MyConnectionOpener(String[] uris, int reconnectInterval) {
        this.uris = uris;
        this.reconnectInterval = reconnectInterval;
    }

    @Override
    public void run() {
        int i = 0;
        while (true) {
            if (i == uris.length) {
                i = 0;
            }

            String uri = uris[i++];
            MyDriver myDriver = myDriverFactory.newMyDriver(uri);
            try {
                myDriver.connect();
                break;
            } catch (MyDriverException e) {
                logger.warn("Failed to connect to uri {} and got '{}'", uri, e.getMessage());
            }
            commonUtility.threadSleep(reconnectInterval);
        }
    }

    public String[] getUris() {
        return uris;
    }

    public void setMyDriverFactory(MyDriverFactory myDriverFactory) {
        this.myDriverFactory = myDriverFactory;
    }

    public void setCommonUtility(CommonUtility commonUtility) {
        this.commonUtility = commonUtility;
    }
}
