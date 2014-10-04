package me.anitas.braid;

import java.util.concurrent.TimeUnit;

public class CountDownLatch1 {

    private int count;

    public CountDownLatch1(int count) {
        this.count = count;
    }

    public synchronized void countDown() {
        count--;
        if (count == 0) {
            this.notifyAll();
        }
    }

    public void await() throws InterruptedException {
        this.wait();
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        this.wait(unit.toMillis(timeout));
        return getCount() <= 0;
    }

    public int getCount() {
        return count;
    }

    public String toString() {
        return super.toString() + ":[" + getCount() + "]";
    }

}
