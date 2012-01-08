package myClient;

import myClient.factory.MyDriverFactory;
import myDriver.MyDriver;
import myDriver.MyDriverException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MyConnectionOpenerTest {

    private MyConnectionOpener myConnectionOpener;
    private String uri1;
    private String uri2;
    private AtomicReference<MyDriverAdapter> myDriverReference;
    @Mock
    MyDriverFactory myDriverFactory;
    @Mock
    MyDriverAdapter myDriver1;
    @Mock
    MyDriverAdapter myDriver2;
    @Mock
    CommonUtility commonUtility;
    private int reconnectInterval;

    @Before
    public void setUp() throws Exception {
        uri1 = "a";
        uri2 = "b";
        reconnectInterval = 100;
        myDriverReference = new AtomicReference<MyDriverAdapter>();

        myConnectionOpener = new MyConnectionOpener(new String[]{uri1, uri2}, reconnectInterval, myDriverReference);
        myConnectionOpener.setMyDriverFactory(myDriverFactory);
        myConnectionOpener.setCommonUtility(commonUtility);
    }

    @Test
    public void testRun() throws Exception {
        when(myDriverFactory.newMyDriver(uri1)).thenReturn(myDriver1);

        myConnectionOpener.run();

        verify(myDriver1).connect();
        assertThat(myDriverReference.get(), is(myDriver1));
    }

    @Test
    public void testRun2() throws Exception {
        when(myDriverFactory.newMyDriver(uri1)).thenReturn(myDriver1);
        when(myDriverFactory.newMyDriver(uri2)).thenReturn(myDriver2);
        doThrow(new MyDriverException("Error occurred when connect.")).when(myDriver1).connect();

        myConnectionOpener.run();

        verify(myDriver1).connect();
        verify(myDriver2).connect();
        verify(commonUtility).threadSleep(reconnectInterval);
        assertThat(myDriverReference.get(), is(myDriver2));
    }
}
