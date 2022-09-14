package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * @author 965087276@qq.com
 * @date 2019/12/9 9:20
 */
public class AtomicBitSet {
    private final static int ADDRESS_BITS_PER_WORD = 6;
    private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
    /* Used to shift left or right for a partial word mask */
    private static final long WORD_MASK = 0xffffffffffffffffL;
    private int wordsInUse;

    /**
     * The number of words in the logical size of this BitSet.
     */
//    private AtomicLong[] words;
    private AtomicLongArray words;

    /**
     * 初始化时的最大长度
     */
    private int maxBit;

    /**
     * Given a bit index, return word index containing it.
     */
    private static int wordIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }

    public AtomicBitSet(int nbits) {
        if (nbits < 0) {
            throw new NegativeArraySizeException("nbits < 0: " + nbits);
        }
        initWords(nbits);
    }

    private void initWords(int nbits) {
        int len = wordIndex(nbits - 1) + 1;
        wordsInUse = len;
        words = new AtomicLongArray(len);
        maxBit = nbits;
//        for (int i = 0; i < len; i++) {
//            words[i] = new AtomicLong(0L);
//        }
    }

    /**
     * 将bitIndex位置为1
     * @param bitIndex
     */
    private void set(int bitIndex) {
        if (bitIndex < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        }
        int wordIndex = wordIndex(bitIndex);
        while (true) {
            long oldValue = words.get(wordIndex);
            if (words.compareAndSet(wordIndex, oldValue, oldValue | (1L << bitIndex))) {
                return;
            }
        }
    }

    /**
     * 将bitIndex位置为value
     * @param bitIndex
     * @param value
     */
    public void set(int bitIndex, boolean value) {
        if (value) {
            set(bitIndex);
        }
        else {
            clear(bitIndex);
        }
    }

    /**
     * 将bitIndex位置为0
     * @param bitIndex
     */
    private void clear(int bitIndex) {
        if (bitIndex < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        }
        int wordIndex = wordIndex(bitIndex);
        while (true) {
            long oldValue = words.get(wordIndex);
            if (words.compareAndSet(wordIndex, oldValue, oldValue & ~(1L << bitIndex))) {
                return;
            }
        }
    }

    /**
     * 求从fromIndex开始下一个0的位置（不需要考虑并发）
     * @param fromIndex
     * @return 返回0的位置
     */
    public int nextZeroBit(int fromIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        }
        int u = wordIndex(fromIndex);
        if (u >= wordsInUse) {
            return -1;
        }
        long word = (~words.get(u)) & (WORD_MASK << fromIndex);
        while (true) {
            if (word != 0) {
                int v = (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
                return v < maxBit ? v : -1;
            }
            if (++u == wordsInUse) {
                return -1;
            }
            word = ~words.get(u);
        }
    }

//    /**
//     * 获取所有0的位置
//     * @return
//     */
//    public List<Integer> getAllBits() {
//        List<Integer> list = new ArrayList<>();
//        for (int i = 0; i < wordsInUse; i++) {
//            int start = i * BITS_PER_WORD;
//            long value = ~words[i].longValue();
//            int u = -1;
//            while (++u < 64) {
//                if (((1L << u) & value) != 0) {
//                    if (start + u < maxBit)
//                        list.add(start + u);
//                }
//            }
//        }
//        return list;
//    }

    /**
     * 求并集（不需要考虑并发）
     * @param bitSet
     */
    public void or(AtomicBitSet bitSet) {
        if (bitSet == this) {
            return;
        }
        for (int i = 0; i < words.length(); i++) {
            words.set(i, words.get(i) | bitSet.words.get(i));
        }
    }

//    /**
//     * 求交集：这里没有并发的场景
//     * @param bitSet
//     */
//    public void and(AtomicBitSet bitSet) {
//        if (bitSet == this) {
//            return;
//        }
//        for (int i = 0; i < words.length; i++) {
//            words[i].set(words[i].longValue() & bitSet.words[i].longValue());
//        }
//    }

    /**
     * 0的个数（不需要考虑并发）
     * @return
     */
    public int cardinality() {
        int sum = 0;
        for (int i = 0; i < this.words.length(); i++) {
            sum += Long.bitCount(words.get(i));
        }
        return maxBit - sum;
    }

}
