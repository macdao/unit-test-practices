package myclient;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Closeable;

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
    @Mock Closeable closeable;

    @Before
    public void setUp() throws Exception {
        uris = new String[]{"a", "b"};
        when(myConnectionFactory.newMyConnection()).thenReturn(myConnectionInterface);
        when(myConnectionInterface.subscribe(queryId,mySubscriber)).thenReturn(closeable);

        myConnection = new MyConnection(uris, myConnectionFactory);
        queryId = 123;
    }

    @Test
    public void testNew() throws Exception {
        verify(myConnectionFactory).newMyConnection();
    }

    @Test
    public void testOpen() throws Exception {
        myConnection.open();

        verify(myConnectionInterface).open();
    }

    @Test
    public void testSubscribe() throws Exception {
        Closeable closeable = myConnection.subscribe(queryId, mySubscriber);

        verify(myConnectionInterface).subscribe(queryId, mySubscriber);
        
        assertThat(closeable, is(closeable));


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

        verify(myConnectionInterface).close();
    }
}
