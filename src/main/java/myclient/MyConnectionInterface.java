package myclient;

import mydriver.MyDriverException;

import java.io.Closeable;

public interface MyConnectionInterface {
    void open() throws MyDriverException;

    Closeable subscribe(int queryId, MySubscriber subscriber) throws MyDriverException;

    public void close();

    void addConnectionListener(MyConnectionEventListener listener);

    void removeConnectionListener(MyConnectionEventListener listener);
}
