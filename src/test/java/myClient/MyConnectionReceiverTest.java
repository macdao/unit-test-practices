package myClient;

import myDriver.MyData;
import myDriver.MyDriver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MyConnectionReceiverTest {

    private int queryId;
    private MyConnectionReceiver myConnectionReceiver;
    AtomicReference<MyDriverAdapter> myDriverReference=  new AtomicReference<MyDriverAdapter>();
    @Mock
    MySubscriber mySubscriber;
    @Mock MyDriverAdapter myDriver;


    @Before
    public void setUp() throws Exception {
        queryId = 123;
        myConnectionReceiver = new MyConnectionReceiver(myDriverReference);
    }

    @Test
    public void testRun() throws Exception {
        String message = "else";
        myDriverReference.set(myDriver);
        when(myDriver.receive()).thenReturn(new MyData(queryId, "begin"), new MyData(queryId, message), null);
        myConnectionReceiver.addSubscriber(queryId,mySubscriber);

        myConnectionReceiver.run();

        verify(mySubscriber).onBegin();
        verify(mySubscriber).onMessage(message);

        verifyNoMoreInteractions(mySubscriber);
    }

    /**
     * queryId为不重复int数据，如果已经存在，则抛出异常。
     */
    @Test(expected = QueryIdDuplicateException.class)
    public void testAddSubscriberDuplicate(){
        myConnectionReceiver.addSubscriber(queryId,mySubscriber);
        myConnectionReceiver.addSubscriber(queryId,mySubscriber);


    }
}
