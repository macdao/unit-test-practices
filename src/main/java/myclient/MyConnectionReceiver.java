package myclient;

import mydriver.MyDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyConnectionReceiver implements MyOneLoop {
    private final MySyncConnection mySyncConnection;
    private boolean opened;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MyConnectionReceiver(MySyncConnection mySyncConnection) {
        this.mySyncConnection = mySyncConnection;
    }

    @Override
    public boolean oneLoop() {
        if (!opened) {
            return false;
        }

        try {
            mySyncConnection.receive();
        } catch (MyDriverException e) {
            logger.warn("Receive failed:{}", e.getMessage());
            return true;
        }
        return true;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }
}
