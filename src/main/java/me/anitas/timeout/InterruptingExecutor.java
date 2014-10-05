package me.anitas.timeout;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class InterruptingExecutor implements Executor {

    private final long timeoutNanos;

    private final ScheduledExecutorService scheduledExecutorService;

    private class Interrupter implements Runnable {

        private final Thread thread;

        private final AtomicBoolean done = new AtomicBoolean(false);

        private Interrupter(Thread thread) {
            this.thread = thread;
        }

        @Override
        public void run() {
            if (done.compareAndSet(false, true)) {
                thread.interrupt();
            }
        }
    }


    public InterruptingExecutor(int corePoolSize, long timeout, TimeUnit timeUnit) {
        this.timeoutNanos = timeUnit.toNanos(timeout);
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(corePoolSize);
    }

    @Override
    public void execute(Runnable command) {

        Interrupter actualInterrupter = new Interrupter(Thread.currentThread());

        Future<?> interrupter =
                scheduledExecutorService.schedule(actualInterrupter,
                        timeoutNanos, TimeUnit.NANOSECONDS);
        try {
            command.run();
        } finally {
            // If interrupter doesn't clear this status scheduledExecutorService will
            interrupter.cancel(true);

            // Make sure you eat the capsule so that no additional interrupt is possible
            actualInterrupter.run();

            // Claer now.
            Thread.interrupted();
        }
    }
}
