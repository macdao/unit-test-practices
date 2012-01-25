package myclient;

import mydriver.MyDriverException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MyConnectionOpenerTest {

    private MyConnectionOpener myConnectionOpener;
    @Mock
    MySyncConnection mySyncConnection;
    @Mock
    CommonUtility commonUtility;
    private int reconnectInterval;
    @Mock
    MySubscriber mySubscriber;
    private final int queryId = 123;
    private Map<Integer, MySubscriber> effectedMySubscriberMap;

    @Before
    public void setUp() throws Exception {
        reconnectInterval = 10;
        effectedMySubscriberMap = new HashMap<Integer, MySubscriber>();
        myConnectionOpener = new MyConnectionOpener(mySyncConnection, commonUtility, reconnectInterval, effectedMySubscriberMap);
    }

    @Test
    public void testClosed() throws Exception {
        myConnectionOpener.setOpened(false);

        boolean b = myConnectionOpener.oneLoop();

        assertThat(b, is(false));
    }

    @Test
    public void testConnectFailed() throws Exception {
        myConnectionOpener.setOpened(true);
        doThrow(new MyDriverException("Error occurred when connect.")).when(mySyncConnection).open();

        boolean b = myConnectionOpener.oneLoop();

        verify(mySyncConnection).open();
        verify(commonUtility).threadSleep(reconnectInterval);
        verifyNoMoreInteractions(mySyncConnection);
        assertThat(b, is(true));
    }

    @Test
    public void testConnectSuccess() throws Exception {
        myConnectionOpener.setOpened(true);

        boolean b = myConnectionOpener.oneLoop();

        verify(mySyncConnection).open();
        assertThat(b, is(true));
    }

    @Test
    public void testConnectedAndAddSubscribeSuccess() throws Exception {
        myConnectionOpener.setOpened(true);
        myConnectionOpener.addSubscribe(queryId, mySubscriber);

        boolean b = myConnectionOpener.oneLoop();

        verify(mySyncConnection).open();
        verify(mySyncConnection).subscribe(queryId, mySubscriber);
        assertThat(myConnectionOpener.getMySubscriberMap().size(), is(1));
        assertThat(myConnectionOpener.getMySubscriberMap().get(queryId), is(mySubscriber));

        assertThat(b, is(true));
    }

    @Test
    public void testConnectedAndAddSubscribeFailed() throws Exception {
        myConnectionOpener.setOpened(true);
        myConnectionOpener.addSubscribe(queryId, mySubscriber);
        doThrow(new MyDriverException("Error occurred when add query.")).when(mySyncConnection).subscribe(queryId, mySubscriber);

        boolean b = myConnectionOpener.oneLoop();

        verify(mySyncConnection).open();
        verify(mySyncConnection).subscribe(queryId, mySubscriber);
        verifyNoMoreInteractions(mySyncConnection);
        assertThat(myConnectionOpener.getMySubscriberMap().size(), is(1));
        assertThat(myConnectionOpener.getMySubscriberMap().get(queryId), is(mySubscriber));
        assertThat(effectedMySubscriberMap.isEmpty(), is(true));

        assertThat(b, is(true));
    }

    @Test
    public void testConnectedAndRemoveSubscribeSuccess() throws Exception {
        myConnectionOpener.setOpened(true);
        effectedMySubscriberMap.put(queryId, mySubscriber);
        myConnectionOpener.removeSubscribe(queryId);

        boolean b = myConnectionOpener.oneLoop();

        verify(mySyncConnection).open();
        verify(mySyncConnection).cancelSubscribe(queryId);
        assertThat(myConnectionOpener.getMySubscriberMap().isEmpty(), is(true));

        assertThat(b, is(true));
    }

    @Test
    public void testConnectedAndRemoveSubscribeFailed() throws Exception {
        myConnectionOpener.setOpened(true);
        effectedMySubscriberMap.put(queryId, mySubscriber);
        int queryId2 = 110;
        MySubscriber mySubscriber2 = mock(MySubscriber.class);
        effectedMySubscriberMap.put(queryId2, mySubscriber2);
        myConnectionOpener.addSubscribe(queryId2, mySubscriber2);
        myConnectionOpener.removeSubscribe(queryId);
        doThrow(new MyDriverException("Error occurred when receive query.")).when(mySyncConnection).cancelSubscribe(queryId);

        boolean b = myConnectionOpener.oneLoop();

        verify(mySyncConnection).open();
        verify(mySyncConnection).cancelSubscribe(queryId);
        verifyNoMoreInteractions(mySyncConnection);
        assertThat(myConnectionOpener.getMySubscriberMap().size(), is(1));
        assertThat(myConnectionOpener.getMySubscriberMap().get(queryId2), is(mySubscriber2));

        assertThat(b, is(true));
    }

    @Test
    public void testAddSubscribe() throws Exception {
        myConnectionOpener.addSubscribe(queryId, mySubscriber);

        try {
            myConnectionOpener.addSubscribe(queryId, mySubscriber);
            fail();
        } catch (QueryIdDuplicateException e) {
        }
    }
}
