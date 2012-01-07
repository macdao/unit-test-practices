package myClient;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.ExecutorService;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MyConnectionTest {
    private MyConnection myConnection;
    private String[] uris;
    @Mock
    ExecutorService executorService;

    @Before
    public void setUp() throws Exception {
        uris = new String[]{};
        myConnection = new MyConnection(uris);
        myConnection.setExecutorService(executorService);
    }

    @Test
    public void testOpen() throws Exception {
        myConnection.open();

        ArgumentCaptor<MyConnectionOpener> argument = ArgumentCaptor.forClass(MyConnectionOpener.class);
        verify(executorService).submit(argument.capture());
        assertThat(argument.getValue().getUris(), CoreMatchers.equalTo(uris));
    }

    @Test
    public void testClose() throws Exception {

    }

    @Test
    public void testSubscribe() throws Exception {

    }

    @Test
    public void testAddConnectionListener() throws Exception {

    }

    @Test
    public void testRemoveConnectionListener() throws Exception {

    }
}
