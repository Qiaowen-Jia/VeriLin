package ticketingsystem;

/**
 * @author Jianyong Feng
 **/
public class BitSetTest {
    public static void main(String[] args) {
        BitSet bitSet = new BitSet(100);
        // 第一位long 0-63
        bitSet.set(0);
        bitSet.set(1);
        bitSet.set(63);

        // 第二位long 64-127
        bitSet.set(64);
        bitSet.set(127);
        System.out.println(bitSet);
    }
}
