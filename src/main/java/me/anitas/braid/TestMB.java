package me.anitas.braid;

import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class TestMB {

    public static void main(String[] args) throws InterruptedException {

        ForkJoinPool forkJoinPool = new ForkJoinPool(1);

        final SettableFuture<String> test1 = SettableFuture.create();

        final FutureManagedBlocker<String> mb = new FutureManagedBlocker<String>(test1);

        test1.setException(new Throwable());

        forkJoinPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ForkJoinPool.managedBlock(mb);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    System.out.println(mb.getResult());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


        forkJoinPool.awaitTermination(1, TimeUnit.SECONDS);
    }
}
