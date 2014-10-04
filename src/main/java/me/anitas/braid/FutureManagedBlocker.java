package me.anitas.braid;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class FutureManagedBlocker<V> implements ForkJoinPool.ManagedBlocker {

    private final Future<V> future;

    public FutureManagedBlocker(Future<V> future) {
        this.future = future;
    }

    @Override
    public boolean block() throws InterruptedException {
        try {
            future.get();
        } catch (ExecutionException e) {
            // TODO(anita): Manage!
        }
        return true;
    }

    @Override
    public boolean isReleasable() {
        return future.isDone() || future.isCancelled();
    }

    public V getResult() throws ExecutionException, InterruptedException {
        return future.get();
    }
}