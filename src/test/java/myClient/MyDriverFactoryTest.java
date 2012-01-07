package myClient;

import myDriver.MyDriver;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by IntelliJ IDEA.
 * User: Macdao
 * Date: 12-1-7
 * Time: 下午11:02
 * To change this template use File | Settings | File Templates.
 */
public class MyDriverFactoryTest {
    private MyDriverFactory myDriverFactory;

    @Before
    public void setUp() throws Exception {
        myDriverFactory = new MyDriverFactory();
    }

    @Test
    public void testNewMyDriver() throws Exception {
        MyDriver myDriver = myDriverFactory.newMyDriver("url");

        assertThat(myDriver, notNullValue());
    }
}
