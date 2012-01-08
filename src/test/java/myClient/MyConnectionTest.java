package myClient;

import myDriver.MyDriver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

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

    private AtomicReference<MyDriver> myDriverReference = new AtomicReference<MyDriver>();
    @Mock
    MyConnectionReceiverFactory myConnectionReceiverFactory;
    @Mock
    MyConnectionReceiver myConnectionReceiver;
    @Mock
    MyConnectionOpenerFactory myConnectionOpenerFactory;
    @Mock
    MyConnectionOpener myConnectionOpener;

    @Before
    public void setUp() throws Exception {
        uris = new String[]{};
        myConnection = new MyConnection(uris);
        myConnection.setExecutorService(executorService);
        myConnection.setMyDriverReference(myDriverReference);
        myConnection.setMyConnectionReceiverFactory(myConnectionReceiverFactory);
        myConnection.setMyConnectionOpenerFactory(myConnectionOpenerFactory);

        when(myConnectionReceiverFactory.newMyConnectionReceiver(myDriverReference)).thenReturn(myConnectionReceiver);
        when(myConnectionOpenerFactory.newMyConnectionOpener(uris, MyConnection.RECONNECT_INTERVAL, myDriverReference)).thenReturn(myConnectionOpener);
    }

    @Test
    public void testOpen() throws Exception {
        myConnection.open();

        verify(myConnectionOpenerFactory).newMyConnectionOpener(uris, MyConnection.RECONNECT_INTERVAL, myDriverReference);
        verify(executorService).submit(myConnectionOpener);
    }

    @Test
    public void testSubscribe() throws Exception {
        int queryId = 123;
        myConnection.subscribe(queryId, mySubscriber);

        verify(executorService).submit(myConnectionReceiver);
        verify(myConnectionReceiverFactory).newMyConnectionReceiver(myDriverReference);
        verify(myConnectionReceiver).addSubscriber(queryId, mySubscriber);
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
