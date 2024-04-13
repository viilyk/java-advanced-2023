package info.kgeorgiy.ja.ilyk.concurrent;
import info.kgeorgiy.java.advanced.concurrent.*;
import info.kgeorgiy.java.advanced.mapper.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class IterativeParallelism implements ListIP{

    private static ParallelMapper parallelMapper;

    /**
     * Creates {@code IterativeParallelism} that will use {@code parallelMapper} for running parallel tasks.
     * @param parallelMapper {@link ParallelMapper} instance
     */
    public IterativeParallelism(ParallelMapper parallelMapper) {
        IterativeParallelism.parallelMapper = parallelMapper;
    }

    /**
     * Creates default {@code IterativeParallelism}.
     */
    public IterativeParallelism() {
        parallelMapper = null;
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return parallelize(threads, values, a -> maxFunc(a, comparator), a -> maxFunc(a, comparator));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelize(threads, values, p -> allFunc(p, predicate), l -> allFunc(l, Predicate.isEqual(true)));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, Predicate.not(predicate));
    }

    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<Integer>, Integer> sum = a -> countFunc(a, x -> x, x -> true);
        return IterativeParallelism.<T, Integer, Integer>parallelize(threads, values, l -> countFunc(l, a -> 1, predicate), sum);
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return IterativeParallelism.<Object, List<String>, String>parallelize(threads, values,
                a -> mapFilterFunc(a, Object::toString, x -> true),
                a -> String.join("", concate(a)));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return IterativeParallelism.<T, List<T>, List<T>>parallelize(threads, values,
                a -> mapFilterFunc(a, x -> x, predicate),
                this::concate);
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return IterativeParallelism.<T, List<U>, List<U>>parallelize(threads, values,
                a -> mapFilterFunc(a, f, x -> true),
                this::concate);
    }

    private static <T, R, S> S parallelize(int threads, List<? extends T> values,
                             Function<List<? extends T>, R> function1,
                             Function<List<R>, S> function2) throws InterruptedException {
        int size = values.size() / threads;
        int remainder = values.size() % threads;
        int l = 0, r;
        List<List<? extends T>> s = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            r = l + size + (i < remainder ? 1 : 0);
            if (r > l) {
                s.add(values.subList(l, r));
            }
            l = r;
        }
        List<R> results;
        if (parallelMapper == null) {
            List<Thread> threadList = new ArrayList<>();
            results = new ArrayList<>(Collections.nCopies(s.size(), null));
            for (int i = 0; i < s.size(); i++) {
                final int index = i;
                Thread thread = new Thread(() -> results.set(index, function1.apply(s.get(index))));
                threadList.add(thread);
                thread.start();
            }
            for (Thread i : threadList) {
                i.join();
                if (i.isInterrupted()) {
                    throw new InterruptedException("Thread was interrapted.");
                }
            }
        } else {
            results = parallelMapper.map(function1, s);
        }
        return function2.apply(results);
    }

    private <T> T maxFunc(List<? extends T> a, Comparator<? super T> comparator) {
        T max = a.get(0);
        for(int i = 1; i < a.size(); i++) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return max;
            }
            if (comparator.compare(a.get(i), max) > 0) {
                max = a.get(i);
            }
        }
        return max;
    }

    private <T> boolean allFunc(List<? extends T> a, Predicate<? super T> predicate) {
        for (T t : a) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return false;
            }
            if (!predicate.test(t)) {
                return false;
            }
        }
        return true;
    }

    private <T> int countFunc(List<? extends T> a, Function<? super T, Integer> f, Predicate<? super T> predicate) {
        int res = 0;
        for (T t : a) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return res;
            }
            if (predicate.test(t)) {
                res += f.apply(t);
            }
        }
        return res;
    }

    private <T, U> List<U> mapFilterFunc(List<? extends T> a, Function<? super T, ? extends U> f, Predicate<? super T> predicate) {
        List<U> res = new ArrayList<>();
        for (T t : a) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return res;
            }
            if (predicate.test(t)) {
                res.add(f.apply(t));
            }
        }
        return res;
    }

    private <T> List<T> concate(List<List<T>> a) {
        List<T> res = new ArrayList<>();
        for (List<T> t : a) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return res;
            }
            res.addAll(t);
        }
        return res;
    }
}
