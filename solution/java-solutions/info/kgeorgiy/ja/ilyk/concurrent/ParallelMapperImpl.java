package info.kgeorgiy.ja.ilyk.concurrent;

import info.kgeorgiy.java.advanced.mapper.*;

import java.util.*;
import java.util.function.Function;

/**
 * Class implementing {@link ParallelMapper}.
 */
public class ParallelMapperImpl implements ParallelMapper{

    private final List<Thread> producers;
    private final Queue<Runnable> tasks;
    private volatile boolean interrapted;

    /**
     * The constructor that creates threads which can be used for parallelization.
     * @param threads number of producer threads
     */
    public ParallelMapperImpl(int threads) {
        producers = new ArrayList<>();
        tasks = new ArrayDeque<>();
        interrapted = false;
        Runnable r = () -> {
            try {
                while (!Thread.interrupted()) {
                    Runnable task;
                    synchronized (tasks) {
                        while(tasks.isEmpty()) {
                            tasks.wait();
                        }
                        task = tasks.poll();
                        tasks.notify();
                    }
                    task.run();
                }
            } catch (InterruptedException ignored) {
            } finally {
                Thread.currentThread().interrupt();
                interrapted = true;
            }
        };
        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(r);
            producers.add(thread);
            thread.start();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        Container<R> results = new Container<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            final int index = i;
            synchronized (tasks) {
                tasks.add(() -> results.set(index, f.apply(args.get(index))));
                tasks.notify();
            }
        }
        synchronized (results) {
            while(!results.isCompleted() && !interrapted) {
                results.wait();
            }
        }
        if (interrapted) {
            throw new InterruptedException("Some producer thread was interrapted.");
        }
        return results.getAll();
    }


    @Override
    public void close() {
        for (Thread thread : producers) {
            thread.interrupt();
        }
        for (Thread thread : producers) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private static class Container<R> {
        private int count;
        private final List<R> data;

        Container(int size) {
            this.count = size;
            this.data = new ArrayList<>(Collections.nCopies(size, null));
        }

        public synchronized void set(int index, R value) {
            data.set(index, value);
            count--;
            notify();
        }

        public List<R> getAll() {
            return data;
        }

        public boolean isCompleted() {
            return count == 0;
        }
    }
}
