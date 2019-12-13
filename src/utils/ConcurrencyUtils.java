package utils;

import java.util.List;

public class ConcurrencyUtils {
    public static void waitForMultipleThreads(Thread... threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void waitForMultipleThreads(List<Thread> threads) {
        waitForMultipleThreads(threads.toArray(new Thread[]{}));
    }

    public static Thread createThread(Runnable r) {
        return new Thread(r);
    }

    public static void startMultipleThreads(List<Thread> threads) {
        startMultipleThreads(threads.toArray(new Thread[]{}));
    }

    public static void startMultipleThreads(Thread... threads) {
        for (Thread thread : threads)
            thread.start();
    }

    public static void startAndWaitMultipleThreads(Thread... threads) {
        startMultipleThreads(threads);
        waitForMultipleThreads(threads);
    }

    public static void startAndWaitMultipleThreads(List<Thread> threads) {
        startMultipleThreads(threads);
        waitForMultipleThreads(threads);
    }
}
