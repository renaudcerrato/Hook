package com.mypopsy.hook.internal;

import java.util.Iterator;

public class OrderedLinkedList<T extends OrderedLinkedList.Node> implements Iterable<T> {

    private T first;

    @SuppressWarnings("unchecked")
    public T insert(T newEntry) {

        if (first == null) {
            return first = newEntry;
        }

        T current = first;

        // Nodes are stored in ascending priority order.
        while (newEntry.compareTo(current) < 0) {
            // end reached?
            if (current.next == null) {
                current.next = newEntry;
                newEntry.previous = current;
                return newEntry;
            }
            current = (T) current.next;
        }

        newEntry.previous = current.previous;
        newEntry.next = current;
        current.previous = newEntry;

        if (newEntry.previous != null)
            newEntry.previous.next = newEntry;
        else
            first = newEntry;

        return newEntry;
    }

    public T first() {
        return first;
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedIterator(first);
    }

    public abstract class Node implements Comparable<Node> {

        public T previous, next;

        public void remove() {
            if (previous != null)
                previous.next = next;
            else
                first = next;

            if (next != null)
                next.previous = previous;
        }
    }

    @SuppressWarnings("unchecked")
    private class LinkedIterator implements Iterator<T> {
        private T current;

        public LinkedIterator(T item) {
            this.current = item;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public T next() {
            T next = current;
            current = (T) current.next;
            return next;
        }

        @Override
        public void remove() {
            current.remove();
        }
    }
}