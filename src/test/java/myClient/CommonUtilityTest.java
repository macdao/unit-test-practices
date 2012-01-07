package myClient;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import sun.font.CoreMetrics;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by IntelliJ IDEA.
 * User: Macdao
 * Date: 12-1-7
 * Time: 下午10:57
 * To change this template use File | Settings | File Templates.
 */
public class CommonUtilityTest {
    private CommonUtility commonUtility;
    @Before
    public void setUp() throws Exception {
commonUtility = new CommonUtility();
    }

    @Test
    public void testThreadSleep() throws Exception {
        long time1 = System.currentTimeMillis();
        commonUtility.threadSleep(10);
        long time2 = System.currentTimeMillis();
        long time = time2 - time1;
        assertThat(time >= 10 && time <=15, is(true));
    }
}
