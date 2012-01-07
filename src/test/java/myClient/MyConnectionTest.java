package myClient;

import myDriver.MyData;
import myDriver.MyDriver;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MyConnectionTest {
    private MyConnection myConnection;
    private String[] uris;
    @Mock
    ExecutorService executorService;
    @Mock
    MySubscriber mySubscriber;
    @Mock
    MyDriver myDriver;
    private AtomicReference<MyDriver> myDriverReference = new AtomicReference<MyDriver>();

    @Before
    public void setUp() throws Exception {
        uris = new String[]{};
        myConnection = new MyConnection(uris);
        myConnection.setExecutorService(executorService);
        myConnection.setMyDriverReference(myDriverReference);
    }

    @Test
    public void testOpen() throws Exception {
        myConnection.open();

        ArgumentCaptor<MyConnectionOpener> argument = ArgumentCaptor.forClass(MyConnectionOpener.class);
        verify(executorService).submit(argument.capture());
        assertThat(argument.getValue().getUris(), equalTo(uris));
        assertThat(argument.getValue().getReconnectInterval(), equalTo(MyConnection.RECONNECT_INTERVAL));
        assertThat(argument.getValue().getMyDriverReference(), is(myDriverReference));
    }

    @Test
    public void testSubscribe() throws Exception {
        int queryId = 123;
        myDriverReference.set(myDriver);

        myConnection.subscribe(queryId, mySubscriber);

        ArgumentCaptor<MyConnectionReceiver> argument = ArgumentCaptor.forClass(MyConnectionReceiver.class);
        verify(executorService).submit(argument.capture());
        assertThat(argument.getValue().getQueryId(), is(queryId));
        assertThat(argument.getValue().getMySubscriber(), is(mySubscriber));
        assertThat(argument.getValue().getMyDriver(), is(myDriver));

    }

    @Test
    public void testClose() throws Exception {

    }

    @Test
    public void testAddConnectionListener() throws Exception {

    }

    @Test
    public void testRemoveConnectionListener() throws Exception {

    }
}
