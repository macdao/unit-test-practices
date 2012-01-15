package myclient;

import myclient.factory.MyDriverFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MyConnectionTest {
    private MyConnection myConnection;
    private String[] uris;
    @Mock
    MySubscriber mySubscriber;

    @Mock
    MyConnectionFactory myConnectionFactory;
    private int queryId;
    @Mock
    MyConnectionInterface myConnectionInterface;
    @Mock
    Closeable closeable;
    @Mock
    ThreadFactory threadFactory;
    @Mock
    Thread thread;
    private ArrayList<MyConnectionEventListener> listeners;
    private final MyDriverFactory myDriverFactory = new MyDriverFactory();
    private final CommonUtility commonUtility = new CommonUtility();
    private Map<Integer, MySubscriber> mySubscriberMap;

    @Before
    public void setUp() throws Exception {
        uris = new String[]{"a", "b"};
        listeners = new ArrayList<MyConnectionEventListener>();
        when(myConnectionFactory.newMyConnection(uris, myDriverFactory, MyConnection.RECONNECT_INTERVAL, commonUtility, listeners)).thenReturn(myConnectionInterface);
        when(myConnectionInterface.subscribe(queryId, mySubscriber)).thenReturn(closeable);

        mySubscriberMap = new HashMap<Integer, MySubscriber>();
        myConnection = new MyConnection(uris, myConnectionFactory, mySubscriberMap, threadFactory, new ArrayList<MyConnectionEventListener>(), myDriverFactory, commonUtility);

        when(threadFactory.newThread(myConnection)).thenReturn(thread);
        queryId = 123;
    }

    @Test
    public void testNew() throws Exception {
        verify(myConnectionFactory).newMyConnection(uris, myDriverFactory, MyConnection.RECONNECT_INTERVAL, commonUtility, listeners);
    }

    @Test
    public void testOpen() throws Exception {
        myConnection.open();

        verify(threadFactory).newThread(myConnection);
        verify(thread).start();
        assertThat(myConnection.isOpened(),is(true));
    }

    @Test
    public void testSubscribe() throws Exception {
        Closeable closeable = myConnection.subscribe(queryId, mySubscriber);

        assertThat(mySubscriberMap.size(), is(1));
        assertThat(mySubscriberMap.get(queryId), is(mySubscriber));
        assertThat(closeable, is(closeable));

        closeable.close();

        assertThat(mySubscriberMap.isEmpty(), is(true));
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
        
        assertThat(myConnection.isOpened(),is(false));
    }
}
