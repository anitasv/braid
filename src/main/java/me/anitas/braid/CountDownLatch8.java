package me.anitas.braid;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by anita on 10/4/2014.
 */
public class CountDownLatch8 {
    private int count;

    private ReentrantLock reentrantLock = new ReentrantLock();

    private Condition condition = reentrantLock.newCondition();

    public CountDownLatch8(int count) {
        this.count = count;
    }

    public void countDown() {
        reentrantLock.lock();
        try {
            count--;
            if (count == 0) {
                condition.signal();
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    public void await() throws InterruptedException {
        reentrantLock.lock();
        try {
            while (getCount() > 0) {
                condition.wait();
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        reentrantLock.lock();
        try {
            if (count <= 0) {
                return true;
            }
            condition.await(timeout, unit);
            return count <= 0;
        } finally {
            reentrantLock.unlock();
        }
    }

    public int getCount() {
        reentrantLock.lock();
        try {
            return count;
        } finally {
            reentrantLock.unlock();
        }
    }

    public String toString() {
        return super.toString() + ":[" + getCount() + "]";
    }

}
