package myclient;

import com.google.common.collect.ImmutableList;
import mydriver.MyData;
import mydriver.MyDriverException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.EventObject;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MyConnectionReceiverTest {

    private int queryId;
    private MyConnectionReceiver myConnectionReceiver;
    private AtomicReference<MyDriverAdapter> myDriverReference;
    @Mock
    MySubscriber mySubscriber;
    @Mock
    MyDriverAdapter myDriver;
    @Mock
    MyConnectionEventListener listener;

    @Before
    public void setUp() throws Exception {
        myDriverReference = new AtomicReference<MyDriverAdapter>();
        queryId = 123;
        myConnectionReceiver = new MyConnectionReceiver(myDriverReference, ImmutableList.of(new DefaultConnectionEventListener(myDriverReference), listener));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                myConnectionReceiver.close();
                return null;
            }
        }).when(listener).disconnected(any(EventObject.class));
    }

    @Test
    public void testRun() throws Exception {
        String message = "else";
        myDriverReference.set(myDriver);
        when(myDriver.receive()).thenReturn(new MyData(queryId, "begin"), new MyData(queryId, message), null);
        myConnectionReceiver.addSubscriber(queryId, mySubscriber);

        myConnectionReceiver.run();

        verify(myDriver).addQuery(queryId);
        verify(mySubscriber).onBegin();
        verify(mySubscriber).onMessage(message);

        verifyNoMoreInteractions(mySubscriber);
    }

    /**
     * 增加订阅者会向MyDriver发送消息
     *
     * @throws Exception e
     */
    @Test
    public void testRunAndAddQuery() throws Exception {
        String message = "else";
        when(myDriver.receive()).thenReturn(new MyData(queryId, "begin"), new MyData(queryId, message), null);
        myConnectionReceiver.addSubscriber(queryId, mySubscriber);
        myDriverReference.set(myDriver);

        myConnectionReceiver.run();


        verify(myDriver).addQuery(queryId);
        verify(mySubscriber).onBegin();
        verify(mySubscriber).onMessage(message);

        verifyNoMoreInteractions(mySubscriber);
    }

    @Test
    public void testRunAndAddQueryThrowException() throws Exception {
        myDriverReference.set(myDriver);
        final MyDriverAdapter myDriverAdapter2 = mock(MyDriverAdapter.class);
        doThrow(new MyDriverException("Error occurred when add query.")).when(myDriver).addQuery(queryId);

        when(myDriverAdapter2.receive()).thenReturn(null);
        myConnectionReceiver.setQueryIdAdded(true);
        myConnectionReceiver.getMySubscriberMap().put(queryId, mySubscriber);

        myConnectionReceiver.run();

        verify(myDriver).addQuery(queryId);
        verify(myDriver).close();
        verifyNoMoreInteractions(mySubscriber);
        verifyNoMoreInteractions(myDriver);
        verify(listener).disconnected(any(EventObject.class));
    }

    /**
     * receive 如果抛出了MyDriverException异常，则关闭当前的MyDriver对象，
     * 并“立即”重新开始连接下一个uri，如果失败，则再尝试下一个（此时需要有一定间隔），
     * 连接成功后重新向各subscriber发送数据。
     *
     * @throws Exception e
     */
    @Test
    public void testRunAndReceiveThrowException() throws Exception {
        myDriverReference.set(myDriver);
        when(myDriver.receive()).thenThrow(new MyDriverException("Error occurred when receive."));
        myConnectionReceiver.addSubscriber(queryId, mySubscriber);

        myConnectionReceiver.run();

        verify(myDriver).receive();
        verify(myDriver).close();
        assertThat(myDriverReference.get(), nullValue());
        verifyNoMoreInteractions(mySubscriber);

        /*
        final MyDriverAdapter myDriverAdapter2 = mock(MyDriverAdapter.class);
        myDriverReference.set(myDriverAdapter2);
        Thread.sleep(10);
        verify(myDriverAdapter2).receive();
        verify(listener).disconnected(any(EventObject.class));
        */
    }

    @Test
    public void testRunWith2Subscribers() throws Exception {
        String message = "else";
        myDriverReference.set(myDriver);
        int queryId2 = 444;
        when(myDriver.receive()).thenReturn(new MyData(queryId, "begin"), new MyData(queryId2, "begin"), null);
        myConnectionReceiver.addSubscriber(queryId, mySubscriber);
        MySubscriber mySubscriber2 = mock(MySubscriber.class);
        myConnectionReceiver.addSubscriber(queryId2, mySubscriber2);

        myConnectionReceiver.run();

        verify(mySubscriber).onBegin();
        verify(mySubscriber, never()).onMessage(message);
        verify(mySubscriber2).onBegin();


        verifyNoMoreInteractions(mySubscriber);
    }

    /**
     * 删除订阅者会向MyDriver发送消息
     *
     * @throws Exception e
     */
    @Test
    public void testRunAndRemoveSubscriber() throws Exception {
        final String message = "else";
        myDriverReference.set(myDriver);

        when(myDriver.receive()).thenReturn(new MyData(queryId, "begin")).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                myConnectionReceiver.removeSubscriber(queryId);
                return new MyData(queryId, message);
            }
        }).thenReturn(null);
        myConnectionReceiver.addSubscriber(queryId, mySubscriber);

        myConnectionReceiver.run();

        verify(mySubscriber).onBegin();

        verifyNoMoreInteractions(mySubscriber);
        verify(myDriver).removeQuery(queryId);
    }

    @Test
    public void testRunAndRemoveSubscriberThrowException() throws Exception {
        final String message = "else";
        myDriverReference.set(myDriver);

        when(myDriver.receive()).thenReturn(new MyData(queryId, "begin")).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                myConnectionReceiver.removeSubscriber(queryId);
                return new MyData(queryId, message);
            }
        }).thenReturn(null);
        doThrow(new MyDriverException("Error occurred when receive query.")).when(myDriver).removeQuery(queryId);
        myConnectionReceiver.addSubscriber(queryId, mySubscriber);

        myConnectionReceiver.run();

        verify(mySubscriber).onBegin();

        verifyNoMoreInteractions(mySubscriber);

        verify(myDriver).removeQuery(queryId);
        verifyNoMoreInteractions(mySubscriber);
        verify(listener).disconnected(any(EventObject.class));
        assertThat(myConnectionReceiver.isQueryIdAdded(), is(true));
    }

    /**
     * queryId为不重复int数据，如果已经存在，则抛出异常。
     */
    @Test(expected = QueryIdDuplicateException.class)
    public void testAddSubscriberDuplicate() {
        myConnectionReceiver.addSubscriber(queryId, mySubscriber);
        myConnectionReceiver.addSubscriber(queryId, mySubscriber);
    }

    @Test
    public void testAddSubscriberDuplicate2() {
        myConnectionReceiver.addSubscriber(queryId, mySubscriber);
        myConnectionReceiver.removeSubscriber(queryId);
        myConnectionReceiver.addSubscriber(queryId, mySubscriber);
    }

    /**
     * 其中Connect，AddQuery、RemoveQuery和Receive由于都要和服务器端进行通信，
     * 因此都有可能会抛出MyDriverException。这些方法一旦抛出异常之后，
     * 该MyDriver对象则需要被视为不可用，但我们依然需要调用Close方法将其关闭。
     *
     * @throws Exception e
     */
    @Test
    public void testAddQuery() throws Exception {
        myConnectionReceiver.addSubscriber(queryId, mySubscriber);

        assertThat(myConnectionReceiver.isQueryIdAdded(), is(true));
        assertThat(myConnectionReceiver.getMySubscriberMap().containsKey(queryId), is(true));
    }

    @Test
    public void testRemoveQuery() throws Exception {
        myDriverReference.set(myDriver);
        myConnectionReceiver.addSubscriber(queryId, mySubscriber);

        myConnectionReceiver.removeSubscriber(queryId);

        assertThat(myConnectionReceiver.getToBeRemovedQueryIds().contains(queryId), is(true));
        verify(myDriver, never()).removeQuery(queryId);
    }

    @Test
    public void testClose() throws Exception {
        myConnectionReceiver.close();

        assertThat(myConnectionReceiver.isClosed(), is(true));
    }
}
