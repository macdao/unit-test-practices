package myclient;

import java.io.Closeable;

public class MyConnectionIntegrationTest {
//    @Test
    public void test() throws Exception {
        MyConnection connection = new MyConnection(new String[]{"a"});
        connection.open();
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