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
                // We are able to detect a cancellation early enough to prevent an interrupt
                // the check also clears the interrupt on this thread, so we are okay
                return false;
            } else {
                // We have not yet received cancellation, even though it might be cancelled by
                // now, there are three cases
                //    a) either we return true, in which case the original thread will
                //       clear interrupt status
                //    b) the get() call pending itself got interrupted, in that case scheduledExecutorService
                //       will clear the interrupt status when creating the InterruptedException.
                //    c) The cancel appeared after processing, but interrupt on this thread was raised, this
                //       will also be cleared by the scheduledExecutorService when it sees callable has
                //       returned and status has not changed.
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
                Boolean interrupted = interrupter.get();
                if (interrupted != null && interrupted) {
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
            } catch (CancellationException e) {
                // Cancelled before scheduling.
            }
        }
    }
}
