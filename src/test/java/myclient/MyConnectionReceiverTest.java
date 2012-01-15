package myclient;

import mydriver.MyDriverException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MyConnectionReceiverTest {
    private MyConnectionReceiver myConnectionReceiver;
    @Mock
    MySyncConnection mySyncConnection;

    @Before
    public void setUp() throws Exception {
        myConnectionReceiver = new MyConnectionReceiver(mySyncConnection);
    }

    @Test
    public void testReceiveSuccess() throws Exception {
        myConnectionReceiver.setOpened(true);

        boolean b = myConnectionReceiver.oneLoop();

        verify(mySyncConnection).receive();
        assertThat(b, is(true));
    }

    @Test
    public void testReceiveFailed() throws Exception {
        myConnectionReceiver.setOpened(true);
        doThrow(new MyDriverException("Error occurred when receive.")).when(mySyncConnection).receive();

        boolean b = myConnectionReceiver.oneLoop();

        verify(mySyncConnection).receive();
        assertThat(b, is(true));
    }

    @Test
    public void testClosed() throws Exception {
        boolean b = myConnectionReceiver.oneLoop();

        assertThat(b, is(false));

    }
}
