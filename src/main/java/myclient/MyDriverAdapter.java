package myclient;

import mydriver2.MyData;
import mydriver2.MyDriver;
import mydriver2.MyDriverException;

public class MyDriverAdapter {

    private final MyDriver myDriver;

    public MyDriverAdapter(String uri) {
        myDriver = new MyDriver(uri);
    }

    public void connect() throws MyDriverException {
        myDriver.connect();
    }

    public void addQuery(int id) throws MyDriverException {
        myDriver.addQuery(id);
    }

    public void removeQuery(int id) throws MyDriverException {
        myDriver.removeQuery(id);
    }

    public void close() {
        myDriver.close();
    }


    public MyData receive() throws MyDriverException {
        return myDriver.receive();
    }
}
