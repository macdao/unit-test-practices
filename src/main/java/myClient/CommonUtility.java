package myClient;

/**
 * Created by IntelliJ IDEA.
 * User: Macdao
 * Date: 12-1-7
 * Time: 下午10:09
 * To change this template use File | Settings | File Templates.
 */
public class CommonUtility {
    void threadSleep(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //
        }
    }
}
