package myClient;

import myClient.factory.MyConnectionOpenerFactory;
import myClient.factory.MyConnectionReceiverFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Closeable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MyConnectionTest {
    private MyConnection myConnection;
    private String[] uris;
    @Mock
    MySubscriber mySubscriber;

    private AtomicReference<MyDriverAdapter> myDriverReference;

    @Mock
    MyConnectionReceiver myConnectionReceiver;

    @Mock
    MyConnectionOpener myConnectionOpener;
    @Mock
    Thread thread;
    @Mock
    ThreadFactory threadFactory;
    @Mock
    MyConnectionOpenerFactory myConnectionOpenerFactory;
    @Mock
    MyConnectionReceiverFactory myConnectionReceiverFactory;


    @Before
    public void setUp() throws Exception {
        myDriverReference = new AtomicReference<MyDriverAdapter>();
        uris = new String[]{};
        when(myConnectionOpenerFactory.newMyConnectionOpener(uris, MyConnection.RECONNECT_INTERVAL, myDriverReference)).thenReturn(myConnectionOpener);

        myConnection = new MyConnection(uris, myConnectionOpenerFactory, myDriverReference);

        myConnection.setThreadFactory(threadFactory);
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(thread);


        myConnection.setMyConnectionReceiverFactory(myConnectionReceiverFactory);
        when(myConnectionReceiverFactory.newMyConnectionReceiver(myDriverReference, myConnectionOpener)).thenReturn(myConnectionReceiver);
    }

    @Test
    public void testOpen() throws Exception {
        myConnection.open();

        verify(myConnectionOpenerFactory).newMyConnectionOpener(uris, MyConnection.RECONNECT_INTERVAL, myDriverReference);
        verify(threadFactory).newThread(myConnectionOpener);
        verify(thread).start();
    }

    @Test
    public void testSubscribe() throws Exception {
        int queryId = 123;
        Closeable closeable = myConnection.subscribe(queryId, mySubscriber);

        verify(threadFactory).newThread(myConnectionReceiver);
        verify(thread).start();
        verify(myConnectionReceiverFactory).newMyConnectionReceiver(myDriverReference, myConnectionOpener);
        verify(myConnectionReceiver).addSubscriber(queryId, mySubscriber);

        //该方法返回一个IDisposable对象，Dispose后即取消订阅，subscriber不会继续收到数据。
        closeable.close();
        verify(myConnectionReceiver).removeSubscriber(queryId);
    }

    @Test
    public void testClose() throws Exception {

    }

    @Test
    public void testAddConnectionListener() throws Exception {

    }

    @Test
    public void testRemoveConnectionListener() throws Exception {

    }
}
