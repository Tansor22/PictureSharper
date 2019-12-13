package core;

import utils.ConcurrencyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.IntStream;

public class ParallelConvolution extends ConvolutionTool {
    public enum ParallelModes {
        NATIVE_JAVA_THREADS,
        OPEN_MP,
        STREAM_PARALLEL,
        FORK_JOIN_POOL,
        NATIVE_C_THREADS,
        MPJ
    }

    public class ForkJoinTask extends RecursiveAction {
        private final static long THRESHOLD = 10_000_000_000L;
        private int begin;
        private int end;
        private int[] rCanal;
        private int[] gCanal;
        private int[] bCanal;

        public ForkJoinTask(int begin, int end, int[] rCanal, int[] gCanal, int[] bCanal) {
            this.begin = begin;
            this.end = end;
            this.rCanal = rCanal;
            this.gCanal = gCanal;
            this.bCanal = bCanal;
        }


        @Override
        protected void compute() {
            // greater than 100 000 points
            if (end - begin >= THRESHOLD) {
                // splitting
                List<ForkJoinTask> tasks = createForkJoinTasks();
                // starting
                for (ForkJoinTask task : tasks)
                    task.fork();
                // waiting
                for (ForkJoinTask task : tasks)
                    task.join();
            } else {
                // do the job
                for (int i = begin; i < end; i++) {
                    int tmpY = (i / width) + gap;
                    int tmpX = (i % width) + gap;
                    kernel.apply(tmpX, tmpY, tmpRCanal, tmpW, rCanal, func, reducer);
                    kernel.apply(tmpX, tmpY, tmpGCanal, tmpW, gCanal, func, reducer);
                    kernel.apply(tmpX, tmpY, tmpBCanal, tmpW, bCanal, func, reducer);
                    pixels[i] = buildRGB(rCanal[i], gCanal[i], bCanal[i]);
                }

            }

        }

        private List<ForkJoinTask> createForkJoinTasks() {
            List<ForkJoinTask> tasks = new ArrayList<>();
            int half = (end - begin) / 2;
            int middle = this.begin + half;
            ForkJoinTask one = new ForkJoinTask(begin, middle, rCanal, gCanal, bCanal);
            ForkJoinTask another = new ForkJoinTask(middle, end, rCanal, gCanal, bCanal);
            tasks.add(one);
            tasks.add(another);
            return tasks;
        }
    }

    public ParallelModes getMode() {
        return mode;
    }

    private ParallelModes mode = ParallelModes.NATIVE_JAVA_THREADS;

    public ParallelConvolution(int width, int height, int[] src, Properties conf) {
        super(width, height, src, conf);
    }

    @Override
    public int[] process() {
        switch (mode) {
            case NATIVE_JAVA_THREADS:
                return processNativeJDK();
            case STREAM_PARALLEL:
                return processStreamAPI();
            case OPEN_MP:
                return processOpenMP();
            case FORK_JOIN_POOL:
                return processForkJoinPool();
            case MPJ:
                return processMPJ();
            case NATIVE_C_THREADS:
                return processNativeJNA();
            default:
                return processNativeJDK();
        }
    }

    private int[] processNativeJNA() {
        throw new UnsupportedOperationException("Unsupported yet");
    }

    private int[] processMPJ() {
        throw new UnsupportedOperationException("Unsupported yet");
    }

    private int[] processForkJoinPool() {
        int[] rCanal = new int[dimension];
        int[] gCanal = new int[dimension];
        int[] bCanal = new int[dimension];
        ForkJoinTask task = new ForkJoinTask(0, dimension, rCanal, gCanal, bCanal);
        ForkJoinPool pool = new ForkJoinPool(4);
        pool.execute(task);
        task.join();
        return pixels;
    }

    private int[] processOpenMP() {
        int[] rCanal = new int[dimension];
        int[] gCanal = new int[dimension];
        int[] bCanal = new int[dimension];
        /* === OMP CONTEXT === */
        class OMPContext {
            public int local_dimension;
        }
        final OMPContext ompContext = new OMPContext();
        ompContext.local_dimension = dimension;
        final org.omp4j.runtime.IOMPExecutor ompExecutor = new org.omp4j.runtime.DynamicExecutor(Runtime.getRuntime().availableProcessors());
        /* === /OMP CONTEXT === */
        for (int i_oNV = 0; i_oNV < ompContext.local_dimension; i_oNV++) {
            final int i = i_oNV;
            ompExecutor.execute(() -> {
                int tmpY = (i / width) + gap;
                int tmpX = (i % width) + gap;
                kernel.apply(tmpX, tmpY, tmpRCanal, tmpW, rCanal, func, reducer);
                kernel.apply(tmpX, tmpY, tmpGCanal, tmpW, gCanal, func, reducer);
                kernel.apply(tmpX, tmpY, tmpBCanal, tmpW, bCanal, func, reducer);
                pixels[i] = buildRGB(rCanal[i], gCanal[i], bCanal[i]);
            });
        }
        ompExecutor.waitForExecution();

        return pixels;
    }

    private int[] processStreamAPI() {
        int[] rCanal = new int[dimension];
        int[] gCanal = new int[dimension];
        int[] bCanal = new int[dimension];
        IntStream
                .iterate(0, (i) -> i + 1)
                .parallel()
                .limit(dimension)
                .forEach((i) -> {
                    int tmpY = (i / width) + gap;
                    int tmpX = (i % width) + gap;
                    kernel.apply(tmpX, tmpY, tmpRCanal, tmpW, rCanal, func, reducer);
                    kernel.apply(tmpX, tmpY, tmpGCanal, tmpW, gCanal, func, reducer);
                    kernel.apply(tmpX, tmpY, tmpBCanal, tmpW, bCanal, func, reducer);
                    pixels[i] = buildRGB(rCanal[i], gCanal[i], bCanal[i]);
                });
        return pixels;
    }

    private int[] processNativeJDK() {
        int[] rCanal = new int[dimension];
        int[] gCanal = new int[dimension];
        int[] bCanal = new int[dimension];
        Thread[] convolutionThreads = new Thread[3];

        convolutionThreads[0] = createConvolutionThread(tmpRCanal, rCanal);
        convolutionThreads[1] = createConvolutionThread(tmpGCanal, gCanal);
        convolutionThreads[2] = createConvolutionThread(tmpBCanal, bCanal);

        // start with border
        ConcurrencyUtils.startAndWaitMultipleThreads(convolutionThreads);
        int cores = Runtime.getRuntime().availableProcessors();
        int batchSize = dimension / cores;
        List<Thread> assemblingThreads = new ArrayList<>();

        for (int i = 0; i <= dimension - batchSize; i += batchSize)
            assemblingThreads.add(createAssemblingThread(i, i + batchSize, rCanal, gCanal, bCanal));

        // start with border
        ConcurrencyUtils.startAndWaitMultipleThreads(assemblingThreads);
        return pixels;
    }

    private Thread createConvolutionThread(int[] temp, int[] target) {
        return ConcurrencyUtils.createThread(() -> {
            for (int tmpY = gap; tmpY < height + gap; tmpY++)
                for (int tmpX = gap; tmpX < width + gap; tmpX++) {
                    kernel.apply(tmpX, tmpY, temp, tmpW, target, func, reducer);
                }
        });
    }

    private Thread createAssemblingThread(int begin, int end, int[] rCanal, int[] gCanal, int[] bCanal) {
        return ConcurrencyUtils.createThread(() -> {
            for (int i = begin; i < end; i++) {
                pixels[i] = buildRGB(rCanal[i], gCanal[i], bCanal[i]);
            }
        });
    }
}
