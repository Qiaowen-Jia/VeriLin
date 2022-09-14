package ticketingsystem;

import java.util.concurrent.atomic.AtomicMarkableReference;

class LockFreeList<T> {
    Node<T> head;

    LockFreeList() {
        head = new Node<>(Long.MIN_VALUE);
        Node<T> tail = new Node<>(Long.MAX_VALUE);
        head.next = new AtomicMarkableReference<>(tail, false);
        tail.next = new AtomicMarkableReference<>(null, false);
    }

    boolean add(long key, T item) {
        while (true) {
            Window<T> window = find(key);
            Node<T> pred = window.pred, curr = window.curr;
            if (curr.key == key) {
                return false;
            } else {
                Node<T> node = new Node<>(key, item);
                node.next = new AtomicMarkableReference<>(curr, false);
                if (pred.next.compareAndSet(curr, node, false, false)) {
                    return true;
                }
            }
        }
    }

    boolean add(T item) {
        if (item == null) {
            return false;
        }
        return add(item.hashCode(), item);
    }

    T getAndRemoveHead() {
        while (true) {
            T item = head.item;
            if (item == null) {
                return null;
            }
            if (remove(item)) {
                return item;
            }
        }
    }

    boolean remove(long key) {
        boolean snip;
        while (true) {
            Window<T> window = find(key);
            Node<T> pred = window.pred, curr = window.curr;
            if (curr.key != key) {
                return false;
            } else {
                Node<T> succ = curr.next.getReference();
                snip = curr.next.compareAndSet(succ, succ, false, true);    // 逻辑删除
                if (!snip) {
                    continue;
                }
                pred.next.compareAndSet(curr, succ, false, false);      // 失败了 find 会帮助删除
                return true;
            }
        }
    }

    boolean remove(T item) {
        if (item == null) {
            return false;
        }
        return remove(item.hashCode());
    }

    boolean contains(T item) {
        if (item == null) {
            return false;
        }
        boolean[] marked = {false};
        int key = item.hashCode();
        Node<T> curr = head;
        while (curr.key < key) {
            curr = curr.next.getReference();
            curr.next.get(marked);
        }
        return curr.key == key && !marked[0];
    }

    Window<T> find(long key) {
        Node<T> pred;
        Node<T> curr;
        Node<T> succ;
        boolean[] marked = {false};
        boolean snip;

        retry:
        while (true) {
            pred = head;
            curr = pred.next.getReference();
            while (true) {      // 向后找到第一个没有被逻辑删除的节点
                succ = curr.next.get(marked);   // succ 为后继结点，marked 为 curr 结点的标记位
                while (marked[0]) {
                    snip = pred.next.compareAndSet(curr, succ, false, false);   // pred 的后继结点为 succ，pred 的标记位不变
                    if (!snip) {    // 赋值失败
                        continue retry;
                    }
                    curr = succ;
                    succ = curr.next.get(marked);
                }
                if (curr.key >= key) {
                    return new Window<>(pred, curr);
                }
                pred = curr;
                curr = succ;
            }
        }
    }

    static class Node<T> {
        long key;
        T item;
        AtomicMarkableReference<Node<T>> next;

        Node(long key) {
            this(key, null);
        }

        Node(long key, T item) {
            this.key = key;
            this.item = item;
            next = new AtomicMarkableReference<>(null, false);
        }
    }

    static class Window<T> {
        Node<T> pred, curr;

        Window(Node<T> pred, Node<T> curr) {
            this.pred = pred;
            this.curr = curr;
        }
    }

    public static void main(String[] args) {
        int x = 0x80000000;
        long y = x;
        System.out.println(y);
    }
}
