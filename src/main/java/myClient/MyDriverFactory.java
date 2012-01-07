package myClient;

import myDriver.MyDriver;

/**
 * Created by IntelliJ IDEA.
 * User: Macdao
 * Date: 12-1-7
 * Time: 下午9:35
 * To change this template use File | Settings | File Templates.
 */
public class MyDriverFactory {
    public MyDriver newMyDriver(String uri){
        return new MyDriver(uri);
    }
}
