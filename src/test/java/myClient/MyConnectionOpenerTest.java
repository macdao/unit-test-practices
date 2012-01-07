package myClient;

import myDriver.MyDriver;
import myDriver.MyDriverException;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA.
 * User: Macdao
 * Date: 12-1-7
 * Time: 下午9:32
 * To change this template use File | Settings | File Templates.
 */
@RunWith(MockitoJUnitRunner.class)
public class MyConnectionOpenerTest {

    private MyConnectionOpener myConnectionOpener;
    private String[] uris;
    private String uri1;
    private String uri2;
    private String uri3;
    private AtomicReference<MyDriver> myDriverReference;
    @Mock
    MyDriverFactory myDriverFactory;
    @Mock
    MyDriver myDriver1;
    @Mock
    MyDriver myDriver2;
    @Mock
    MyDriver myDriver3;
    @Mock CommonUtility commonUtility;
    private int reconnectInterval;

    @Before
    public void setUp() throws Exception {
        uri1 = "a";
        uri2 = "b";
        uri3 = "c";
        uris = new String[]{uri1, uri2, uri3};
        reconnectInterval = 100;
        myDriverReference = new AtomicReference<MyDriver>();

        myConnectionOpener = new MyConnectionOpener(uris, reconnectInterval, myDriverReference);
        myConnectionOpener.setMyDriverFactory(myDriverFactory);
        myConnectionOpener.setCommonUtility(commonUtility);
    }

    @Test
    public void testRun() throws Exception {
        when(myDriverFactory.newMyDriver(uri1)).thenReturn(myDriver1);

        myConnectionOpener.run();

        verify(myDriver1).connect();
        assertThat(myConnectionOpener.getMyDriverReference().get(), is(myDriver1));
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
        assertThat(myConnectionOpener.getMyDriverReference().get(), is(myDriver2));
    }
}
