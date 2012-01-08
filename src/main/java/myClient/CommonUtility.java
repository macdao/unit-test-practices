package myClient;

public class CommonUtility {
    void threadSleep(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //
        }
    }
}
