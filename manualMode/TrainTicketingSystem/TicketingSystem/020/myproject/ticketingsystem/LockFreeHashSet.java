package ticketingsystem;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LockFreeHashSet<T> {
    private BucketList<T>[] bucket;
    private AtomicInteger bucketSize;
    private AtomicInteger setSize;
    private static final int THRESHOLD = 4;
    private int capacity;

    LockFreeHashSet(int capacity) {
        this.capacity = capacity;
        bucket = (BucketList<T>[]) new BucketList[capacity];
        bucket[0] = new BucketList<>();
        bucketSize = new AtomicInteger(Math.min(capacity, 1024));
        setSize = new AtomicInteger(0);
    }

    int size() {
        return setSize.get();
    }

    private int hashCode(Object o) {
        return o.hashCode() & Integer.MAX_VALUE;
    }

    boolean addWithKey(long key, T x) {
        int myBucket = (int) (key % bucketSize.get());
        BucketList<T> b = getBucketList(myBucket);
        if (!b.addWithKey(key, x)) {
            return false;
        }
        int setSizeNow = setSize.getAndIncrement();
        int bucketSizeNow = bucketSize.get();
        if (setSizeNow / bucketSizeNow > THRESHOLD && 2 * bucketSizeNow <= capacity) {
            bucketSize.compareAndSet(bucketSizeNow, 2 * bucketSizeNow);     // 如果没成功，则其他线程已经做了
        }
        return true;
    }

    boolean add(T x) {
        int myBucket = hashCode(x) % bucketSize.get();
        BucketList<T> b = getBucketList(myBucket);
        if (!b.add(x)) {
            return false;
        }
        int setSizeNow = setSize.getAndIncrement();
        int bucketSizeNow = bucketSize.get();
        if (setSizeNow / bucketSizeNow > THRESHOLD && 2 * bucketSizeNow <= capacity) {
            bucketSize.compareAndSet(bucketSizeNow, 2 * bucketSizeNow);     // 如果没成功，则其他线程已经做了
        }
        return true;
    }

    boolean removeWithKey(long key) {
        int myBucket = (int) (key % bucketSize.get());
        BucketList<T> b = getBucketList(myBucket);
        return b.removeWithKey(key);
    }

    boolean remove(T x) {
        int myBucket = hashCode(x) % bucketSize.get();
        BucketList<T> b = getBucketList(myBucket);
        return b.remove(x);
    }

    boolean containsWithKey(long key) {
        int myBucket = (int) (key % bucketSize.get());
        BucketList<T> b = getBucketList(myBucket);
        return b.containsWithKey(key);
    }

    T find(long key) {
        int myBucket = (int) (key % bucketSize.get());
        BucketList<T> b = getBucketList(myBucket);
        return b.findWithKey(key);
    }

    boolean contains(T x) {
        int myBucket = hashCode(x) % bucketSize.get();
        BucketList<T> b = getBucketList(myBucket);
        return b.contains(x);
    }

    private BucketList<T> getBucketList(int myBucket) {
        if (bucket[myBucket] == null) {
            initializeBucket(myBucket);
        }
        return bucket[myBucket];
    }

    private void initializeBucket(int myBucket) {
        int parent = getParent(myBucket);
        if (bucket[parent] == null) {
            initializeBucket(parent);
        }
        bucket[myBucket] = bucket[parent].getSentinel(myBucket);    // getSentinel 返回非空
    }

    /**
     * 该桶是从哪个桶衍生过来的，比如 3 号桶的父桶为 1
     *
     * @param myBucket
     * @return
     */
    private int getParent(int myBucket) {
        int parent = bucketSize.get();
        do {
            parent = parent >> 1;
        } while (parent > myBucket);
        return myBucket - parent;
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        LockFreeHashSet<Long> set = new LockFreeHashSet<>(8192);
//        HashSet<String> set = new HashSet<>();
//        CopyOnWriteArraySet<String> set = new CopyOnWriteArraySet<>();
        ExecutorService service = Executors.newFixedThreadPool(50);
        int times = 1000000;
        AtomicInteger flag = new AtomicInteger(0);
        for (int i = 0; i < times; i++) {
            long tmp = flag.getAndIncrement();
            service.execute(() -> {
//                set.addWithKey(tmp, tmp);
                if (!set.addWithKey(tmp, tmp)) {
                    System.out.println(tmp);
                }
            });
        }
        service.shutdown();
        service = Executors.newFixedThreadPool(12);
        flag.set(0);
        for (int i = 0; i < times; i++) {
            long tmp = flag.getAndIncrement();
            service.execute(() -> {
                if (!set.containsWithKey(tmp)) {
                    System.out.println(tmp);
                }
            });
        }
        service.shutdown();
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println(set.size());
//        System.out.println(set.size1());
        System.out.println((endTime - startTime) + "ms");
//        for (int i = 0; i < times; i++) {
//            if (!set.containsWithKey(i)) {
//                System.out.println(i);
//            }
//        }
    }
}
