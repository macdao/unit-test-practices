package myclient;

import java.io.Closeable;

public interface MyConnectionInterface {
    void open();

    Closeable subscribe(int queryId, MySubscriber subscriber);

    public void close();

    void addConnectionListener(MyConnectionEventListener listener);

    void removeConnectionListener(MyConnectionEventListener listener);
}
