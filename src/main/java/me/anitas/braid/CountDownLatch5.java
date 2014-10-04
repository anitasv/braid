package me.anitas.braid;

import java.util.concurrent.TimeUnit;

/**
 * Created by anita on 10/4/2014.
 */
public class CountDownLatch5 {

    private volatile int count;

    public CountDownLatch5(int count) {
        this.count = count;
    }

    public synchronized void countDown() {
        count--;
        if (count == 0) {
            this.notifyAll();
        }
    }

    public synchronized void await() throws InterruptedException {
        while (getCount() > 0) {
            this.wait();
        }
    }

    public synchronized boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        this.wait(unit.toMillis(timeout));
        return getCount() <= 0;
    }

    public synchronized int getCount() {
        return count;
    }

    public String toString() {
        return super.toString() + ":[" + getCount() + "]";
    }

}
