package myclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyRunnable implements Runnable {
    private MyOneLoop myOneLoop;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MyRunnable(MyOneLoop myOneLoop) {
        this.myOneLoop = myOneLoop;
    }

    @Override
    public void run() {
        while (true) {
//            logger.info("Loop {}", myOneLoop);
            boolean oneLoop = myOneLoop.oneLoop();
            if (!oneLoop) {
                return;
            }
        }
    }
}
