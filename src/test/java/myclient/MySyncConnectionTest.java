package myclient;

import com.google.common.collect.Lists;
import myclient.factory.MyDriverFactory;
import mydriver.MyDriverException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MySyncConnectionTest {

    private MySyncConnection mySyncConnection;
    private int reconnectInterval = 10;
    private String uri1;
    private String uri2;
    @Mock
    CommonUtility commonUtility;
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
    private ArrayList<MyConnectionEventListener> listeners;

    @Before
    public void setUp() throws Exception {
        queryId = 123;
        uri1 = "a";
        uri2 = "b";
        String[] uris = new String[]{"a", "b"};
        when(myDriverFactory.newMyDriver(uri1)).thenReturn(myDriver1);
        when(myDriverFactory.newMyDriver(uri2)).thenReturn(myDriver2);

        listeners = Lists.newArrayList(listener);
        mySyncConnection = new MySyncConnection(uris, myDriverFactory, reconnectInterval, commonUtility, listeners);
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
        doThrow(new MyDriverException("Error occurred when connect.")).when(myDriver1).connect();

        mySyncConnection.open();

        verify(myDriverFactory).newMyDriver(uri1);
        verify(myDriver1).connect();
        verify(myDriverFactory).newMyDriver(uri2);
        verify(myDriver2).connect();
        verify(commonUtility).threadSleep(reconnectInterval);
        ArgumentCaptor<EventObject> argument1 = ArgumentCaptor.forClass(EventObject.class);
        verify(listener).connectionFailed(argument1.capture());
        assertThat((MyDriverAdapter) argument1.getValue().getSource(), is(myDriver1));
        ArgumentCaptor<EventObject> argument2 = ArgumentCaptor.forClass(EventObject.class);
        verify(listener).connected(argument2.capture());
        assertThat((MyDriverAdapter) argument2.getValue().getSource(), is(myDriver2));
        assertThat(mySyncConnection.getMyDriverAdapter(), is(myDriver2));
    }

    @Test
    public void testOpen3() throws Exception {
        doThrow(new MyDriverException("Error occurred when connect.")).doNothing().when(myDriver1).connect();
        doThrow(new MyDriverException("Error occurred when connect.")).when(myDriver2).connect();

        mySyncConnection.open();

        verify(myDriverFactory, times(2)).newMyDriver(uri1);
        verify(myDriver1, times(2)).connect();
        verify(myDriverFactory).newMyDriver(uri2);
        verify(myDriver2).connect();
        verify(commonUtility, times(2)).threadSleep(reconnectInterval);
        ArgumentCaptor<EventObject> argument1 = ArgumentCaptor.forClass(EventObject.class);
        verify(listener, times(2)).connectionFailed(argument1.capture());
        List<EventObject> allValues = argument1.getAllValues();
        assertThat((MyDriverAdapter) allValues.get(0).getSource(), is(myDriver1));
        assertThat((MyDriverAdapter) allValues.get(1).getSource(), is(myDriver2));
        ArgumentCaptor<EventObject> argument2 = ArgumentCaptor.forClass(EventObject.class);
        verify(listener).connected(argument2.capture());
        assertThat((MyDriverAdapter) argument2.getValue().getSource(), is(myDriver1));
        assertThat(mySyncConnection.getMyDriverAdapter(), is(myDriver1));
    }

    @Test
    public void testSubscribe() throws Exception {
        mySyncConnection.open();
        final Closeable closeable = mySyncConnection.subscribe(queryId, mySubscriber);

        verify(myDriver1).addQuery(queryId);
        assertThat(mySyncConnection.getMySubscribers().size(), is(1));
        assertThat(mySyncConnection.getMySubscribers().get(queryId), is(mySubscriber));

        closeable.close();

        verify(myDriver1).removeQuery(queryId);
        assertThat(mySyncConnection.getMySubscribers().isEmpty(), is(true));
    }

    @Test
    public void testSubscribeAndAddQueryThrowException() throws Exception {
        final MyDriverException exception = new MyDriverException("Error occurred when add query.");
        doThrow(exception).when(myDriver1).addQuery(queryId);

        mySyncConnection.open();
        try {
            mySyncConnection.subscribe(queryId, mySubscriber);
            fail();
        } catch (RuntimeException e) {
            assertThat((MyDriverException) e.getCause(), is(exception));
        }

        verify(myDriver1).addQuery(queryId);
        assertThat(mySyncConnection.getMySubscribers().isEmpty(), is(true));
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
        } catch (RuntimeException e) {
            assertThat((MyDriverException) e.getCause(), is(exception));
        }

        verify(myDriver1).removeQuery(queryId);
        assertThat(mySyncConnection.getMySubscribers().size(), is(1));
        assertThat(mySyncConnection.getMySubscribers().get(queryId), is(mySubscriber));
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

    @Test
    public void testAddConnectionListener() throws Exception {
        MyConnectionEventListener mock = mock(MyConnectionEventListener.class);

        mySyncConnection.addConnectionListener(mock);

        assertThat(listeners.contains(mock), is(true));
    }

    @Test
    public void testRemoveConnectionListener() throws Exception {
        mySyncConnection.removeConnectionListener(listener);

        assertThat(listeners.contains(listener), is(false));
    }
}
