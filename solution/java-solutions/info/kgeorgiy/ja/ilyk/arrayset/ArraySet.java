package info.kgeorgiy.ja.ilyk.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements SortedSet<T> {

    private final List<T> data;
    private final Comparator<? super T> comparator;

    public ArraySet() {
        data = List.of();
        comparator = null;
    }

    public ArraySet(Collection<? extends T> data, Comparator<? super T> comparator) {
        this.comparator = comparator;
        TreeSet<T> t = new TreeSet<>(comparator);
        t.addAll(data);
        this.data = new ArrayList<>(t);
    }

    public ArraySet(Comparator<? super T> comparator) {
        this(List.of(), comparator);
    }

    public ArraySet(Collection<? extends T> data) {
        this(data, null);
    }

    private ArraySet(List<T> data, Comparator<? super T> comparator) {
        this.data = data;
        this.comparator = comparator;
    }

    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if (compareElements(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("fromElement is greater than toElement.");
        }
        return getSubSet(getIndex(fromElement), getIndex(toElement));
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return getSubSet(0, getIndex(toElement));
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return getSubSet(getIndex(fromElement), size());
    }

    @Override
    public T first() {
        checkEmpty();
        return data.get(0);
    }

    @Override
    public T last() {
        checkEmpty();
       return data.get(size() - 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (T) o, comparator) >= 0;
    }

    private void checkEmpty() {
        if (isEmpty()) {
            throw new NoSuchElementException("ArraySet is empty.");
        }
    }

    private int compareElements(T fromElement, T toElement) {
        return comparator == null ? -Collections.reverseOrder().compare(fromElement, toElement) :
                comparator.compare(fromElement, toElement);
    }

    private ArraySet<T> getSubSet(int fromIndex, int toIndex) {
        return new ArraySet<>(fromIndex <= toIndex ? data.subList(fromIndex, toIndex) :
                List.of(), comparator);
    }

    private int getIndex(T element) {
        int res =  Collections.binarySearch(data, element, comparator);
        return res >= 0 ? res : -res - 1;
    }
 }
