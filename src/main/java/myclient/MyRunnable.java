package myclient;

public class MyRunnable implements Runnable {
    private final MyOneLoop myOneLoop;

    public MyRunnable(MyOneLoop myOneLoop) {
        this.myOneLoop = myOneLoop;
    }

    @Override
    public void run() {
        while (true) {
            boolean oneLoop = myOneLoop.oneLoop();
            if (!oneLoop) {
                return;
            }
        }
    }
}
