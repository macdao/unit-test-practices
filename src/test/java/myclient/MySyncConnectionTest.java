package myclient;

import myclient.factory.MyDriverFactory;
import mydriver.MyDriverException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MySyncConnectionTest {

    private MySyncConnection mySyncConnection;
    private String uri1;
    private String uri2;
    @Mock
    MyDriverFactory myDriverFactory;
    @Mock
    MyDriverAdapter myDriver1;
    @Mock
    MyDriverAdapter myDriver2;
    @Mock
    MyConnectionEventListener listener;
    private int queryId;
    @Mock
    MySubscriber mySubscriber;
    private Map<Integer, MySubscriber> mySubscriberMap;

    @Before
    public void setUp() throws Exception {
        mySubscriberMap = new HashMap<Integer, MySubscriber>();
        queryId = 123;
        uri1 = "a";
        uri2 = "b";
        String[] uris = new String[]{"a", "b"};
        when(myDriverFactory.newMyDriver(uri1)).thenReturn(myDriver1);
        when(myDriverFactory.newMyDriver(uri2)).thenReturn(myDriver2);

        mySyncConnection = new MySyncConnection(uris, myDriverFactory, mySubscriberMap);
        mySyncConnection.addConnectionListener(listener);
    }

    @Test
    public void testOpen() throws Exception {
        mySyncConnection.open();

        verify(myDriverFactory).newMyDriver(uri1);
        verify(myDriver1).connect();
        ArgumentCaptor<EventObject> argument = ArgumentCaptor.forClass(EventObject.class);
        verify(listener).connected(argument.capture());
        assertThat((MyDriverAdapter) argument.getValue().getSource(), is(myDriver1));
        assertThat(mySyncConnection.getMyDriverAdapter(), is(myDriver1));
    }

    @Test
    public void testOpen2() throws Exception {
        MyDriverException exception = new MyDriverException("Error occurred when connect.");
        doThrow(exception).when(myDriver1).connect();

        try {
            mySyncConnection.open();
            fail();
        } catch (MyDriverException e) {
            assertThat(e, is(exception));
        }

        verify(myDriverFactory).newMyDriver(uri1);
        verify(myDriver1).connect();
        assertThat(mySyncConnection.getMyDriverAdapter(), nullValue());
        ArgumentCaptor<EventObject> argument1 = ArgumentCaptor.forClass(EventObject.class);
        verify(listener).connectionFailed(argument1.capture());
        assertThat((MyDriverAdapter) argument1.getValue().getSource(), is(myDriver1));
    }

    @Test
    public void testOpen3() throws Exception {
        MyDriverException exception1 = new MyDriverException("Error occurred when connect.");
        doThrow(exception1).doNothing().when(myDriver1).connect();
        MyDriverException exception2 = new MyDriverException("Error occurred when connect.");
        doThrow(exception2).when(myDriver2).connect();

        try {
            mySyncConnection.open();fail();
        } catch (MyDriverException e) {
            assertThat(e, is(exception1));
        }

        verify(myDriverFactory).newMyDriver(uri1);
        verify(myDriver1).connect();
        assertThat(mySyncConnection.getMyDriverAdapter(), nullValue());
        ArgumentCaptor<EventObject> argument1 = ArgumentCaptor.forClass(EventObject.class);
        verify(listener).connectionFailed(argument1.capture());
        assertThat((MyDriverAdapter) argument1.getValue().getSource(), is(myDriver1));

        try {
            mySyncConnection.open();
            fail();
        } catch (MyDriverException e) {
            assertThat(e, is(exception2));
        }

        verify(myDriverFactory).newMyDriver(uri2);
        verify(myDriver2).connect();
        assertThat(mySyncConnection.getMyDriverAdapter(), nullValue());
        ArgumentCaptor<EventObject> argument2 = ArgumentCaptor.forClass(EventObject.class);
        verify(listener, times(2)).connectionFailed(argument2.capture());
        assertThat((MyDriverAdapter) argument2.getValue().getSource(), is(myDriver2));

        mySyncConnection.open();

        verify(myDriverFactory, times(2)).newMyDriver(uri1);
        verify(myDriver1, times(2)).connect();
        ArgumentCaptor<EventObject> argument3 = ArgumentCaptor.forClass(EventObject.class);
        verify(listener).connected(argument3.capture());
        assertThat((MyDriverAdapter) argument3.getValue().getSource(), is(myDriver1));
        assertThat(mySyncConnection.getMyDriverAdapter(), is(myDriver1));
    }

    @Test
    public void testSubscribe() throws Exception {
        mySyncConnection.open();
        final Closeable closeable = mySyncConnection.subscribe(queryId, mySubscriber);

        verify(myDriver1).addQuery(queryId);
        assertThat(mySubscriberMap.size(), is(1));
        assertThat(mySubscriberMap.get(queryId), is(mySubscriber));

        closeable.close();

        verify(myDriver1).removeQuery(queryId);
        assertThat(mySubscriberMap.isEmpty(), is(true));
    }

    @Test
    public void testSubscribeAndAddQueryThrowException() throws Exception {
        final MyDriverException exception = new MyDriverException("Error occurred when add query.");
        doThrow(exception).when(myDriver1).addQuery(queryId);

        mySyncConnection.open();
        try {
            mySyncConnection.subscribe(queryId, mySubscriber);
            fail();
        } catch (MyDriverException e) {
            assertThat(e, is(exception));
        }

        verify(myDriver1).addQuery(queryId);
        assertThat(mySubscriberMap.isEmpty(), is(true));
        ArgumentCaptor<EventObject> argument = ArgumentCaptor.forClass(EventObject.class);
        verify(listener).disconnected(argument.capture());
        assertThat((MyDriverAdapter) argument.getValue().getSource(), is(myDriver1));
    }

    @Test
    public void testSubscribeAndRemoveQueryThrowException() throws Exception {
        MyDriverException exception = new MyDriverException("Error occurred when receive query.");
        doThrow(exception).when(myDriver1).removeQuery(queryId);

        mySyncConnection.open();
        final Closeable closeable = mySyncConnection.subscribe(queryId, mySubscriber);

        try {
            closeable.close();
            fail();
        } catch (IOException e) {
            assertThat((MyDriverException) e.getCause(), is(exception));
        }

        verify(myDriver1).removeQuery(queryId);
        assertThat(mySubscriberMap.isEmpty(), is(true));
        ArgumentCaptor<EventObject> argument = ArgumentCaptor.forClass(EventObject.class);
        verify(listener).disconnected(argument.capture());
        assertThat((MyDriverAdapter) argument.getValue().getSource(), is(myDriver1));
    }

    @Test
    public void testClose() throws Exception {
        mySyncConnection.open();

        mySyncConnection.close();

        verify(myDriver1).close();
        assertThat(mySyncConnection.getMyDriverAdapter(), nullValue());
        ArgumentCaptor<EventObject> argument = ArgumentCaptor.forClass(EventObject.class);
        verify(listener).disconnected(argument.capture());
        assertThat((MyDriverAdapter) argument.getValue().getSource(), is(myDriver1));
    }
}
