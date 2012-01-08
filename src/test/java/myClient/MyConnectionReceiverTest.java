package myClient;

import myDriver.MyData;
import myDriver.MyDriverException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.concurrent.ThreadFactory;
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
    Thread thread;
    @Mock
    ThreadFactory threadFactory;
    @Mock
    MyConnectionOpener myConnectionOpener;

    @Before
    public void setUp() throws Exception {
        myDriverReference = new AtomicReference<MyDriverAdapter>();
        queryId = 123;
        myConnectionReceiver = new MyConnectionReceiver(myDriverReference, myConnectionOpener);
        myConnectionReceiver.setThreadFactory(threadFactory);

        when(threadFactory.newThread(any(Runnable.class))).thenReturn(thread);
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

    @Test
    public void testRunAddQuery() throws Exception {
        myDriverReference.set(myDriver);
        doThrow(new MyDriverException("Error occurred when add query.")).when(myDriver).addQuery(queryId);
        myConnectionReceiver.setQueryIdAdded(true);
        myConnectionReceiver.getMySubscriberMap().put(queryId, mySubscriber);

        new Thread(myConnectionReceiver).start();
        Thread.sleep(100);

        verify(myDriver).addQuery(queryId);
        verify(myDriver).close();
        assertThat(myDriverReference.get(), nullValue());
        verify(threadFactory).newThread(myConnectionOpener);
        verify(thread).start();
        verifyNoMoreInteractions(mySubscriber);
        verifyNoMoreInteractions(myDriver);
    }

    @Test
    public void testRun3() throws Exception {
        String message = "else";
        when(myDriver.receive()).thenReturn(new MyData(queryId, "begin"), new MyData(queryId, message), null);
        myConnectionReceiver.addSubscriber(queryId, mySubscriber);

        new Thread(myConnectionReceiver).start();
        myDriverReference.set(myDriver);
        Thread.sleep(100);

        verify(myDriver).addQuery(queryId);
        verify(mySubscriber).onBegin();
        verify(mySubscriber).onMessage(message);

        verifyNoMoreInteractions(mySubscriber);
    }

    /**
     * 如果抛出了MyDriverException异常，则关闭当前的MyDriver对象，
     * 并“立即”重新开始连接下一个uri，如果失败，则再尝试下一个（此时需要有一定间隔），
     * 连接成功后重新向各subscriber发送数据。
     *
     * @throws Exception e
     */
    @Test
    public void testRun2() throws Exception {
        myDriverReference.set(myDriver);
        when(myDriver.receive()).thenThrow(new MyDriverException("Error occurred when receive."));
        myConnectionReceiver.addSubscriber(queryId, mySubscriber);

        new Thread(myConnectionReceiver).start();
        Thread.sleep(100);

        verify(myDriver, times(1)).receive();
        verify(myDriver).close();
        assertThat(myDriverReference.get(), nullValue());
        verify(threadFactory).newThread(myConnectionOpener);
        verify(thread).start();
        verifyNoMoreInteractions(mySubscriber);

        final MyDriverAdapter myDriverAdapter2 = mock(MyDriverAdapter.class);
        myDriverReference.set(myDriverAdapter2);
        Thread.sleep(100);
        verify(myDriverAdapter2, times(1)).receive();
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

    @Test
    public void testRunAndClose() throws Exception {
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
        doThrow(new MyDriverException("Error occurred when receive query.")).when(myDriver).removeQuery(queryId);
        myDriverReference.set(myDriver);

        myConnectionReceiver.removeSubscriber(queryId);

        verify(myDriver).removeQuery(queryId);
        verify(myDriver).close();
        assertThat(myDriverReference.get(), nullValue());
        verify(threadFactory).newThread(myConnectionOpener);
        verify(thread).start();
        verifyNoMoreInteractions(mySubscriber);
    }
}
