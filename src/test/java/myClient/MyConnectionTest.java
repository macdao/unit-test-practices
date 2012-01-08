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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

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
    Thread receiverThread;
    @Mock
    Thread openerThread;
    @Mock
    ThreadFactory threadFactory;
    @Mock
    MyConnectionOpenerFactory myConnectionOpenerFactory;
    @Mock
    MyConnectionReceiverFactory myConnectionReceiverFactory;
    private int queryId;


    @Before
    public void setUp() throws Exception {
        myDriverReference = new AtomicReference<MyDriverAdapter>();
        uris = new String[]{};
        when(myConnectionOpenerFactory.newMyConnectionOpener(uris, MyConnection.RECONNECT_INTERVAL, myDriverReference)).thenReturn(myConnectionOpener);
        when(threadFactory.newThread(myConnectionOpener)).thenReturn(openerThread);
        when(threadFactory.newThread(myConnectionReceiver)).thenReturn(receiverThread);
        when(myConnectionReceiverFactory.newMyConnectionReceiver(myDriverReference, myConnectionOpener)).thenReturn(myConnectionReceiver);

        myConnection = new MyConnection(uris, myConnectionOpenerFactory, myDriverReference, myConnectionReceiverFactory, threadFactory);
        queryId = 123;
    }

    @Test
    public void testOpen() throws Exception {
        myConnection.open();

        verify(myConnectionOpenerFactory).newMyConnectionOpener(uris, MyConnection.RECONNECT_INTERVAL, myDriverReference);
        verify(threadFactory).newThread(myConnectionOpener);
        verify(openerThread).start();
    }

    @Test
    public void testSubscribe() throws Exception {
        Closeable closeable = myConnection.subscribe(queryId, mySubscriber);

        verify(threadFactory).newThread(myConnectionReceiver);
        verify(receiverThread).start();
        verify(myConnectionReceiverFactory).newMyConnectionReceiver(myDriverReference, myConnectionOpener);
        verify(myConnectionReceiver).addSubscriber(queryId, mySubscriber);

        //该方法返回一个IDisposable对象，Dispose后即取消订阅，subscriber不会继续收到数据。
        closeable.close();
        verify(myConnectionReceiver).removeSubscriber(queryId);
    }

    /**
     * 断开与服务器的连接（即调用MyDriver的Close方法）。
     * 该方法“不需要”清除所有订阅，换言之可以再次Open，每个已注册的subscriber会重新开始接受数据。
     *
     * @throws Exception e
     */
    @Test
    public void testClose() throws Exception {
        MyDriverAdapter myDriverAdapter = mock(MyDriverAdapter.class);
        myDriverReference.set(myDriverAdapter);
        myConnection.subscribe(queryId, mySubscriber);

        myConnection.close();

        verify(myDriverAdapter).close();
        myConnection.subscribe(queryId, mySubscriber);
        verify(myConnectionReceiver,times(2)).addSubscriber(queryId,mySubscriber);
    }

    @Test
    public void testAddConnectionListener() throws Exception {

    }

    @Test
    public void testRemoveConnectionListener() throws Exception {

    }
}
