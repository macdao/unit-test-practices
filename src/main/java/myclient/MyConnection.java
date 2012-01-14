package myclient;

import java.io.Closeable;

public class MyConnection implements MyConnectionInterface {

    public final static int RECONNECT_INTERVAL = 3000;

    private final MyConnectionInterface myConnection;

    public MyConnection(String[] uris) {
        this(uris, new MyConnectionFactory());
    }

    public MyConnection(String[] uris, MyConnectionFactory myConnectionFactory) {
        myConnection = myConnectionFactory.newMyConnection();
    }

    public void open() {
        myConnection.open();
    }

    public Closeable subscribe(int queryId, MySubscriber subscriber) {
        return myConnection.subscribe(queryId, subscriber);
    }

    public void close() {
        myConnection.close();
    }

    public synchronized void addConnectionListener(MyConnectionEventListener listener) {
        myConnection.addConnectionListener(listener);
    }

    public synchronized void removeConnectionListener(MyConnectionEventListener listener) {
        myConnection.removeConnectionListener(listener);
    }

}
