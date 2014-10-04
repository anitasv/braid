package me.anitas.braid;

import java.util.concurrent.TimeUnit;

public class CountDownLatch6 {

    private int count;

    public CountDownLatch6(int count) {
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
        if (getCount() <= 0) {
            return true;
        }
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
