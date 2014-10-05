package me.anitas.timeout;

import java.util.concurrent.*;

public class InterruptingExecutor implements Executor {

    private final long timeoutNanos;

    private final ScheduledExecutorService scheduledExecutorService;

    private class Interrupter implements Callable<Boolean> {

        private final Thread thread;

        private Interrupter(Thread thread) {
            this.thread = thread;
        }

        @Override
        public Boolean call() throws Exception {
            if (Thread.interrupted()) {
                return false;
            } else {
                thread.interrupt();
                return true;
            }
        }
    }

    public InterruptingExecutor(int corePoolSize, long timeout, TimeUnit timeUnit) {
        this.timeoutNanos = timeUnit.toNanos(timeout);
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(corePoolSize);
    }

    @Override
    public void execute(Runnable command) {
        Future<Boolean> interrupter =
                scheduledExecutorService.schedule(new Interrupter(Thread.currentThread()),
                        timeoutNanos, TimeUnit.NANOSECONDS);
        try {
            command.run();
        } finally {
            // If interrupter doesn't clear this status scheduledExecutorService will
            interrupter.cancel(true);
            try {
                boolean interrupted = interrupter.get();
                if (interrupted) {
                    // Clear interrupt if still interrupted, it is possible some other thread
                    // interrupted this thread, which we are not going to be responsible for
                    // clearing, but we may clear theirs if we also caused an interrupt.
                    Thread.interrupted();
                }
            } catch (InterruptedException e) {
                // The interrupt appeared after cancel was issued, but before interrupter could
                // detect that it was interrupted.
                // throwing an interrupted exception automatically must clear interruption status
                // so this is also taken care of.
            } catch (ExecutionException e) {
                // Must not happen
            }
        }
    }
}
