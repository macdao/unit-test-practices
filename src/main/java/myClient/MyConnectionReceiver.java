package myClient;

import myDriver.MyData;
import myDriver.MyDriver;
import myDriver.MyDriverException;

/**
 * Created by IntelliJ IDEA.
 * User: Macdao
 * Date: 12-1-7
 * Time: 下午11:51
 * To change this template use File | Settings | File Templates.
 */
public class MyConnectionReceiver implements Runnable {
    private MyDriver myDriver;
    private MySubscriber mySubscriber;
    private int queryId;

    public MyConnectionReceiver(MyDriver myDriver, MySubscriber mySubscriber, int queryId) {
        this.myDriver = myDriver;
        this.mySubscriber = mySubscriber;
        this.queryId = queryId;
    }

    @Override
    public void run() {
        while (true) {
            MyData myData = null;
            try {
                myData = myDriver.receive();
            } catch (MyDriverException e) {
                //todo
            }
            if (myData != null) {
                if (queryId == myData.queryId) {
                    if ("begin".equals(myData.value)) {
                        mySubscriber.onBegin();
                    }else{
                        mySubscriber.onMessage(myData.value);
                    }
                }
            } else {
                //todo
            }
        }
    }

    public MyDriver getMyDriver() {
        return myDriver;
    }

    public MySubscriber getMySubscriber() {
        return mySubscriber;
    }

    public int getQueryId() {
        return queryId;
    }
}
