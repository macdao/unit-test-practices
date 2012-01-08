package myClient;

import com.google.common.collect.ImmutableList;
import myClient.factory.MyDriverFactory;
import myDriver.MyDriverException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.EventObject;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MyConnectionOpenerTest {

    private MyConnectionOpener myConnectionOpener;
    private String uri1;
    private String uri2;
    @Mock
    MyDriverFactory myDriverFactory;
    @Mock
    MyDriverAdapter myDriver1;
    @Mock
    CommonUtility commonUtility;
    private int reconnectInterval;
    @Mock
    MyConnectionEventListener listener;

    @Before
    public void setUp() throws Exception {
        uri1 = "a";
        uri2 = "b";
        reconnectInterval = 100;

        myConnectionOpener = new MyConnectionOpener(new String[]{uri1, uri2}, reconnectInterval, ImmutableList.of(listener));
        myConnectionOpener.setMyDriverFactory(myDriverFactory);
        myConnectionOpener.setCommonUtility(commonUtility);
    }

    @Test
    public void testRun() throws Exception {
        when(myDriverFactory.newMyDriver(uri1)).thenReturn(myDriver1);

        myConnectionOpener.run();

        verify(myDriver1).connect();
        verify(listener).connected(any(EventObject.class));
    }

    /**
     * 从第一个uri开始连接，如果连接失败，则尝试连接下一个uri，
     * 如果最后一个uri也失败则重新尝试第一个uri，直至成功。
     * 两次尝试之间都需要等待一段时间（ReconnectInterval）。
     *
     * @throws Exception e
     */
    @Test
    public void testRun2() throws Exception {
        MyDriverAdapter myDriver2 = mock(MyDriverAdapter.class);

        when(myDriverFactory.newMyDriver(uri1)).thenReturn(myDriver1);
        when(myDriverFactory.newMyDriver(uri2)).thenReturn(myDriver2);
        doThrow(new MyDriverException("Error occurred when connect.")).when(myDriver1).connect();

        myConnectionOpener.run();

        verify(myDriver1).connect();
        verify(myDriver2).connect();
        verify(commonUtility).threadSleep(reconnectInterval);
        verify(listener).connectionFailed(any(EventObject.class));
        verify(listener).connected(any(EventObject.class));
    }
}
