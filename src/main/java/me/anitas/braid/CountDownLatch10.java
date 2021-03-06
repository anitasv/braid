package me.anitas.braid;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CountDownLatch10 {

    private int count;

    private ReentrantLock reentrantLock = new ReentrantLock();

    private Condition condition = reentrantLock.newCondition();

    public CountDownLatch10(int count) {
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
        long future = System.nanoTime() + unit.toNanos(timeout);

        boolean gotLock = reentrantLock.tryLock(timeout, unit);
        if (gotLock) {
            try {
                if (count <= 0) {
                    return true;
                }
                long pending = future - System.nanoTime();
                condition.await(pending, TimeUnit.NANOSECONDS);
                return count <= 0;
            } finally {
                reentrantLock.unlock();
            }
        } else {
            return false;
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
