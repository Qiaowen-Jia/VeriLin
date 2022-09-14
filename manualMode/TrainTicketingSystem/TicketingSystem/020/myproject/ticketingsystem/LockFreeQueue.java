package ticketingsystem;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeQueue<T> {
    private volatile AtomicReference<Node> head, tail;

    LockFreeQueue() {
        head = new AtomicReference<>(new Node(null));
        tail = head;
    }

    void enq(T value) {
        Node node = new Node(value);
        while (true) {
            Node last = tail.get();     // 尾节点
            Node next = last.next.get();    // 尾节点的下一个节点
            if (last == tail.get()) {   // tail 没有被修改
                if (next == null) {     // last 的下一个节点为空
                    if (last.next.compareAndSet(null, node)) {      // 试图接入 last 的下一个节点
                        tail.compareAndSet(last, node);     // tail 向后指
                        return;
                    }
                } else {    // 另一个线程已经加到了末尾，但还没有修改尾节点，帮助 tail 向后指
                    tail.compareAndSet(last, next);
                }
            }   // tail 被修改，进入下一个循环
        }
    }

    T deq() {
        while (true) {
            Node first = head.get();
            Node last = tail.get();
            Node next = first.next.get();
            if (first == head.get()) {      // 前面执行期间，没有其他线程捣乱
                if (first == last) {        // 为空
                    if (next == null) {     // 只能代表在获取 next 的时候为空，并不能代表当前为空
                        return null;
                    }
                    // 如果 next 不为 null，则 next 已经加入链表，而 tail 还没有被修改
                    tail.compareAndSet(last, next);
                } else {    // 不为空
                    T value = next.value;
                    if (head.compareAndSet(first, next)) {  // head 还没有被修改
                        return value;
                    }
                }
            }
        }
    }

    class Node {
        T value;
        AtomicReference<Node> next;

        Node(T value) {
            this.value = value;
            next = new AtomicReference<>(null);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LockFreeQueue<Integer> queue1 = new LockFreeQueue<>();
        ConcurrentLinkedQueue<Integer> queue2 = new ConcurrentLinkedQueue<>();
        long startTime = 0, endTime = 0;
        Random random = new Random();
        Thread[] threads = new Thread[100];
        startTime = System.currentTimeMillis();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100000; j++) {
                    if (random.nextInt(100) < 50) {
                        queue1.enq(1);
                    } else {
                        queue1.deq();
                    }
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100000; j++) {
                    if (random.nextInt(100) < 50) {
                        queue2.add(1);
                    } else {
                        queue2.poll();
                    }
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
    }
}
