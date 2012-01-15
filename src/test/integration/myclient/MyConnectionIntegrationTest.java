package myclient;

import org.junit.Test;

import java.io.Closeable;
import java.util.EventObject;

public class MyConnectionIntegrationTest {
    @Test
    public void test() throws Exception {
        MyConnection connection = new MyConnection(new String[]{"a", "b"});
        connection.addConnectionListener(new MyConnectionEventListener() {
            @Override
            public void connected(EventObject event) {
                System.out.println("connected");
            }

            @Override
            public void connectionFailed(EventObject event) {
                System.out.println("connectionFailed");
            }

            @Override
            public void disconnected(EventObject event) {
                System.out.println("disconnected");
            }
        });
        Closeable closeable = connection.subscribe(1024, new MySubscriber() {
            @Override
            public void onBegin() {
                System.out.println("onBegin");
            }

            @Override
            public void onMessage(String message) {
                System.out.println("omMessage:" + message);
            }
        });
        connection.open();

        connection.subscribe(1023, new MySubscriber() {
            @Override
            public void onBegin() {
                System.out.println("onBegin2");
            }

            @Override
            public void onMessage(String message) {
                System.out.println("omMessage2:" + message);
            }
        });

        Thread.sleep(5000);
        closeable.close();
        Thread.sleep(5000);

        connection.close();

    }
}
