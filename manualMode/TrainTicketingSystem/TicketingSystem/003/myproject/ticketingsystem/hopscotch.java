package ticketingsystem;

public class hopscotch {
    static int HOP_RANGE = 32; // 最大回跳值。
    static int ADD_RANGE = 256; // 加入新元素时搜索空闲表项的最大边界
    static int CAPACITY = 1048576;
    Bucket table[];
    final static int BUSY = -1;

    class Bucket {
        volatile long hop_info = 0;
        volatile Ticket1 ticket = new Ticket1();
        java.util.concurrent.locks.ReentrantLock lock = new java.util.concurrent.locks.ReentrantLock();

        Bucket() {
        }
    }

    hopscotch() {
        table = new Bucket[CAPACITY + ADD_RANGE];
        for (int i = 0; i < CAPACITY + ADD_RANGE; i++) {
            table[i] = new Bucket();
        }
    }

    hopscotch(int HOP_RANGE1, int ADD_RANGE1, int CAPACITY1) {
        HOP_RANGE = HOP_RANGE1;
        ADD_RANGE = ADD_RANGE1;
        CAPACITY = CAPACITY1;
        table = new Bucket[CAPACITY + ADD_RANGE];
        for (int i = 0; i < CAPACITY + ADD_RANGE; i++) {
            table[i] = new Bucket();
        }
    }

    // remove() 方法相对简单，删除元素并修改对应的 hop_info 信息
    Ticket remove(Ticket ticket) {
        int hash = (int) ((ticket.tid) & ((-1) >>> 1)) % CAPACITY;
        Bucket start_bucket = table[hash]; // 表项加锁
        start_bucket.lock.lock();
        long hop_info = start_bucket.hop_info;
        long mask = 1;
        for (int i = 0; i < HOP_RANGE; ++i, mask <<= 1) {
            if ((mask & hop_info) >= 1) {
                Bucket check_bucket = table[hash + i];
                if (Node.f_equals(ticket, check_bucket.ticket)) { //
                    // 找到数据相同的元素
                    Ticket rtn = check_bucket.ticket;
                    check_bucket.ticket.tid = BUSY;
                    start_bucket.hop_info &= ~(1 << i);
                    start_bucket.lock.unlock();
                    return rtn;
                }
            }
        }
        start_bucket.lock.unlock();
        return null; // 未找到元素
    }

    int[] find_closer_bucket(int free_index, int hop_dist, int val) {
        int[] result = new int[3];
        int move_index = free_index - (HOP_RANGE - 1);
        // 回跳的初始目标位置
        Bucket move_bucket = table[move_index];
        for (int free_dist = (HOP_RANGE - 1); free_dist > 0; --free_dist) {
            long start_hop_info = move_bucket.hop_info;
            int move_hop_dist = -1;
            long mask = 1;
            for (int i = 0; i < free_dist; ++i, mask <<= 1) {
                if ((mask & start_hop_info) >= 1) {
                    move_hop_dist = i;
                    break;
                }
            }
            if (-1 != move_hop_dist) {
                move_bucket.lock.lock();
                if (start_hop_info == move_bucket.hop_info) {
                    // 加锁后确认 move_bucket
                    int new_free_index = move_index + move_hop_dist;
                    Bucket new_free_bucket = table[new_free_index];
                    // new_free_bucket 置换空表项
                    move_bucket.hop_info |= (1 << free_dist);
                    table[free_index].ticket.f_set(new_free_bucket.ticket);
                    // new_free_bucket 置空
                    new_free_bucket.ticket.tid = BUSY;
                    move_bucket.hop_info &= ~(1 << move_hop_dist);
                    hop_dist = hop_dist - free_dist + move_hop_dist;
                    move_bucket.lock.unlock();
                    result[0] = hop_dist;
                    result[1] = val;
                    result[2] = new_free_index;
                    return result;
                }
                move_bucket.lock.unlock();
            }
            ++move_index;
            move_bucket = table[move_index];
        }
        table[free_index].ticket.tid = -1;
        result[0] = 0;
        result[1] = 0;
        result[2] = 0;
        return result; // 置换失败
    }

    boolean contains(long tid) {
        int hash = (int) ((tid) & ((-1) >>> 1)) % CAPACITY;
        Bucket start_bucket = table[hash];

        long hop_info = start_bucket.hop_info;
        long mask = 1;
        for (int i = 0; i < HOP_RANGE; ++i, mask <<= 1) {
            if ((mask & hop_info) >= 1) {
                Bucket check_bucket = table[hash + i];
                if (tid == check_bucket.ticket.tid) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean add(Ticket ticket) {
        int val = 1;
        int hash = (int) ((ticket.tid) & ((-1) >>> 1)) % CAPACITY;
        Bucket start_bucket = table[hash];
        start_bucket.lock.lock();
        // 寻找空表项
        int free_index = hash;
        Bucket free_bucket = table[free_index];
        int hop_dist = 0;
        for (; hop_dist < ADD_RANGE; ++hop_dist) {
            if (-1 == free_bucket.ticket.tid) {
                free_bucket.ticket.tid = BUSY;
                break;
            }
            ++free_index;
            free_bucket = table[free_index];
        }
        int[] closest_bucket_info = new int[3];
        if (hop_dist < ADD_RANGE) {
            do {
                if (hop_dist < HOP_RANGE) { // 直接加入元素
                    start_bucket.hop_info |= (1 << hop_dist);
                    free_bucket.ticket.f_set(ticket);
                    start_bucket.lock.unlock();
                    return true;
                } else {
                    closest_bucket_info = find_closer_bucket(free_index, hop_dist, val);
                    hop_dist = closest_bucket_info[0];
                    val = closest_bucket_info[1];
                    free_index = closest_bucket_info[2];
                    free_bucket = table[free_index];
                }
            } while (0 != val);
        }
        start_bucket.lock.unlock();
        return false;
    }
}
