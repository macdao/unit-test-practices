package myclient;

import mydriver.MyData;
import mydriver.MyDriverException;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class MyConnectionReceiver implements Runnable {
    private AtomicReference<MyDriverAdapter> myDriverReference;
    private final Map<Integer, MySubscriber> mySubscriberMap = new HashMap<Integer, MySubscriber>();
    private boolean queryIdAdded;
    private final Set<Integer> toBeRemovedQueryIds = new HashSet<Integer>();
    private final List<MyConnectionEventListener> listeners;
    private boolean closed;

    public MyConnectionReceiver(AtomicReference<MyDriverAdapter> myDriverReference, List<MyConnectionEventListener> listeners) {
        this.myDriverReference = myDriverReference;
        this.listeners = listeners;
    }

    @Override
    public void run() {
        while (true) {
            if (closed) {
                return;
            }
            final MyDriverAdapter myDriverAdapter = myDriverReference.get();
            if (myDriverAdapter == null) {
                continue;
            }

            if (queryIdAdded) {
                synchronized (this) {
                    try {
                        addQuery(myDriverAdapter);
                    } catch (MyDriverException e) {
                        handleTransferException(myDriverAdapter);
                        continue;
                    }
                    queryIdAdded = false;
                }
            }

            if (!toBeRemovedQueryIds.isEmpty()) {
                synchronized (toBeRemovedQueryIds) {
                    try {
                        removeQuery(myDriverAdapter);
                    } catch (MyDriverException e) {
                        handleTransferException(myDriverAdapter);
                        toBeRemovedQueryIds.clear();
                        queryIdAdded = true;
                        continue;
                    }
                }
                toBeRemovedQueryIds.clear();
            }


            final MyData myData;
            try {
                myData = myDriverAdapter.receive();
            } catch (MyDriverException e) {
                //事情大条了
                handleTransferException(myDriverAdapter);
                synchronized (this) {
                    queryIdAdded = true;
                }
                continue;
            }
            if (myData == null) {
                break;
            }
            if (mySubscriberMap.containsKey(myData.queryId)) {
                MySubscriber mySubscriber = mySubscriberMap.get(myData.queryId);
                if ("begin".equals(myData.value)) {
                    mySubscriber.onBegin();
                } else {
                    mySubscriber.onMessage(myData.value);
                }
            }

        }
    }

    private void removeQuery(MyDriverAdapter myDriverAdapter) throws MyDriverException {
        for (int queryId : toBeRemovedQueryIds) {
            myDriverAdapter.removeQuery(queryId);
        }
    }

    public void addSubscriber(int queryId, MySubscriber subscriber) throws QueryIdDuplicateException {
        synchronized (mySubscriberMap) {
            if (mySubscriberMap.containsKey(queryId)) {
                throw new QueryIdDuplicateException(queryId);
            }
            mySubscriberMap.put(queryId, subscriber);
            synchronized (this) {
                queryIdAdded = true;
            }
        }
    }

    public void removeSubscriber(int queryId) {
        synchronized (mySubscriberMap) {
            if (mySubscriberMap.containsKey(queryId)) {
                mySubscriberMap.remove(queryId);
                synchronized (toBeRemovedQueryIds) {
                    toBeRemovedQueryIds.add(queryId);
                }
            }
        }
    }

    private void addQuery(MyDriverAdapter myDriverAdapter) throws MyDriverException {
        for (int key : mySubscriberMap.keySet()) {
            myDriverAdapter.addQuery(key);
        }
    }

    private void handleTransferException(MyDriverAdapter myDriverAdapter) {
        for (MyConnectionEventListener listener : listeners) {
            listener.disconnected(new EventObject(myDriverAdapter));
        }
    }

    public boolean isQueryIdAdded() {
        return queryIdAdded;
    }

    public void setQueryIdAdded(boolean queryIdAdded) {
        this.queryIdAdded = queryIdAdded;
    }

    public Set<Integer> getToBeRemovedQueryIds() {
        return toBeRemovedQueryIds;
    }

    public Map<Integer, MySubscriber> getMySubscriberMap() {
        return mySubscriberMap;
    }

    public void close() {
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }
}
