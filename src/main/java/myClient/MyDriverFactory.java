package myClient;

import myDriver.MyDriver;

public class MyDriverFactory {
    public MyDriver newMyDriver(String uri){
        return new MyDriver(uri);
    }
}
