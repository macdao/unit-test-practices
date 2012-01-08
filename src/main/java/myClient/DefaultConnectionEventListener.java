package myClient;

import java.util.EventObject;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultConnectionEventListener implements MyConnectionEventListener {
    private final AtomicReference<MyDriverAdapter> myDriverReference;

    public DefaultConnectionEventListener(AtomicReference<MyDriverAdapter> myDriverReference) {
        this.myDriverReference = myDriverReference;
    }

    @Override
    public void connected(EventObject event) {
        MyDriverAdapter myDriverAdapter = (MyDriverAdapter) event.getSource();
        myDriverReference.set(myDriverAdapter);
    }

    @Override
    public void connectionFailed(EventObject event) {
    }

    @Override
    public void disconnected(EventObject event) {
        MyDriverAdapter myDriverAdapter = (MyDriverAdapter) event.getSource();
        if (myDriverAdapter != null) {
            myDriverAdapter.close();
        }
        myDriverReference.set(null);
    }
}