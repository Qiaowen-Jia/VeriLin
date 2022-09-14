package ticketingsystem;

public class BucketList<T> extends LockFreeList<T> {
    private static final long HI_MASK = 0x4000000000000000L;      // 将 bit62 置 1
    private static final long MASK = 0x3FFFFFFFFFFFFFFFL;

    BucketList() {
        super();
        head.key = 0;
    }

    private BucketList(Node<T> head) {
        this.head = head;
    }

    long hashCode(int key) {
        return Long.parseLong(Integer.toHexString(key), 16);
    }

    /**
     * 生成普通结点的 key 值
     *
     * @param key
     * @return 最低位为 1，保证当 key 值与哨兵结点的相同时，可以跟在哨兵结点之后
     */
    private long makeOrdinaryKey(long key) {
        return reverse((key & MASK) | HI_MASK);
    }

    /**
     * 生成哨兵结点的 key 值
     *
     * @param key 哨兵结点原来的 key 值
     * @return 最低位为 0
     */
    private static long makeSentinelKey(long key) {
        return reverse(key & MASK);
    }

    /**
     * @param n
     * @return 反转低 63 位，保证返回值非负
     */
    private static long reverse(long n) {
        long res = 0;
        for (int i = 0; i < 63; i++, n >>= 1) {
            res = (res << 1) | (n & 1);
        }
        return res;
    }

    @Override
    boolean add(T item) {
        if (item == null) {
            return false;
        }
        return add(makeOrdinaryKey(hashCode(item.hashCode())), item);
    }

    boolean addWithKey(long key, T item) {
        return add(makeOrdinaryKey(key), item);
    }

    @Override
    boolean remove(T item) {
        if (item == null) {
            return false;
        }
        return remove(makeOrdinaryKey(hashCode(item.hashCode())));
    }

    boolean removeWithKey(long key) {
        return remove(makeOrdinaryKey(key));
    }

    @Override
    public boolean contains(T item) {
        long key = makeOrdinaryKey(hashCode(item.hashCode()));
        return find(key).curr.key == key;
    }

    boolean containsWithKey(long key) {
        key = makeOrdinaryKey(key);
        return find(key).curr.key == key;
    }

    T findWithKey(long key) {
        key = makeOrdinaryKey(key);
        Node<T> node = find(key).curr;
        return node.key == key ? node.item: null;
    }

    BucketList<T> getSentinel(long index) {
        long key = makeSentinelKey(index);
        while (true) {
            Window<T> window = find(key);
            Node<T> pred = window.pred, curr = window.curr;
            if (curr.key == key) {
                return new BucketList<>(curr);
            } else {
                Node<T> node = new Node<>(key);
                node.next.set(pred.next.getReference(), false);
                if (pred.next.compareAndSet(curr, node, false, false)) {
                    return new BucketList<>(node);
                }
            }
        }
    }

    public static void main(String[] args) {
        int x = 0x80000000;
        System.out.println(Long.parseLong(Integer.toHexString(x), 16));
    }
}
