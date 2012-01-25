package myclient;

import mydriver.MyDriverException;

public class MyConnectionReceiver implements MyOneLoop {
    private final MySyncConnection mySyncConnection;
    private boolean opened;

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
            return true;
        }
        return true;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }
}
