package myclient;

import myclient.factory.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MyConnectionTest {
    private MyConnection myConnection;
    private String[] uris;
    @Mock
    MySubscriber mySubscriber;

    @Mock
    MySyncConnectionFactory mySyncConnectionFactory;
    private int queryId;
    @Mock
    MySyncConnection mySyncConnection;
    @Mock
    Closeable closeable;
    @Mock
    ThreadFactory threadFactory;
    @Mock
    Thread openerThread;
    @Mock
    Thread receiverThread;
    private final MyDriverFactory myDriverFactory = new MyDriverFactory();
    private final CommonUtility commonUtility = new CommonUtility();
    private Map<Integer, MySubscriber> mySubscriberMap;
    @Mock
    MyConnectionOpenerFactory myConnectionOpenerFactory;
    @Mock
    MyConnectionOpener myConnectionOpener;
    @Mock
    MyConnectionReceiverFactory myConnectionReceiverFactory;
    @Mock
    MyRunnableFactory myRunnableFactory;
    @Mock
    MyRunnable openerRunner;
    @Mock
    MyRunnable receiverRunner;
    @Mock
    MyConnectionReceiver myConnectionReceiver;

    @Before
    public void setUp() throws Exception {
        uris = new String[]{"a", "b"};
        mySubscriberMap = new HashMap<Integer, MySubscriber>();

        when(mySyncConnectionFactory.newMySyncConnection(uris, myDriverFactory, mySubscriberMap)).thenReturn(mySyncConnection);
        when(mySyncConnection.subscribe(queryId, mySubscriber)).thenReturn(closeable);
        when(myConnectionOpenerFactory.newMyConnectionOpener(mySyncConnection, commonUtility, MyConnection.RECONNECT_INTERVAL, mySubscriberMap)).thenReturn(myConnectionOpener);
        when(myConnectionReceiverFactory.newMyConnectionReceiver(mySyncConnection)).thenReturn(myConnectionReceiver);
        when(myRunnableFactory.newMyRunnable(myConnectionOpener)).thenReturn(openerRunner);
        when(myRunnableFactory.newMyRunnable(myConnectionReceiver)).thenReturn(receiverRunner);


        myConnection = new MyConnection(uris, mySyncConnectionFactory, mySubscriberMap, threadFactory, myDriverFactory, commonUtility, myConnectionOpenerFactory, myConnectionReceiverFactory, myRunnableFactory);

        when(threadFactory.newThread(openerRunner)).thenReturn(openerThread);
        when(threadFactory.newThread(receiverRunner)).thenReturn(receiverThread);
        queryId = 123;
    }

    @Test
    public void testNew() throws Exception {
        verify(mySyncConnectionFactory).newMySyncConnection(uris, myDriverFactory, mySubscriberMap);
        verify(myConnectionOpenerFactory).newMyConnectionOpener(mySyncConnection, commonUtility, MyConnection.RECONNECT_INTERVAL, mySubscriberMap);
        verify(myConnectionReceiverFactory).newMyConnectionReceiver(mySyncConnection);
        verify(myRunnableFactory).newMyRunnable(myConnectionOpener);
        verify(myRunnableFactory).newMyRunnable(myConnectionReceiver);
    }

    @Test
    public void testOpen() throws Exception {
        myConnection.open();

        verify(threadFactory).newThread(openerRunner);
        verify(threadFactory).newThread(receiverRunner);
        verify(openerThread).start();
        verify(receiverThread).start();
        verify(myConnectionOpener).setOpened(true);
        verify(myConnectionReceiver).setOpened(true);
    }

    @Test
    public void testSubscribe() throws Exception {
        Closeable closeable = myConnection.subscribe(queryId, mySubscriber);

        verify(myConnectionOpener).addSubscribe(queryId, mySubscriber);

        closeable.close();

        verify(myConnectionOpener).removeSubscribe(queryId);
    }

    /**
     * 断开与服务器的连接（即调用MyDriver的Close方法）。
     * 该方法“不需要”清除所有订阅，换言之可以再次Open，每个已注册的subscriber会重新开始接受数据。
     *
     * @throws Exception e
     */
    @Test
    public void testClose() throws Exception {
        myConnection.close();

        verify(myConnectionOpener).setOpened(false);
        verify(myConnectionReceiver).setOpened(false);
    }
}
