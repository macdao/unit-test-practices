package myclient.factory;

import myclient.MyOneLoop;
import myclient.MyRunnable;

public class MyRunnableFactory {
    public MyRunnable newMyRunnable(MyOneLoop myOneLoop) {
        return new MyRunnable(myOneLoop);
    }
}
