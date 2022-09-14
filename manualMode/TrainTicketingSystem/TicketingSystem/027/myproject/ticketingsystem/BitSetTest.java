package ticketingsystem;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 965087276@qq.com
 * @date 2020/1/4 19:41
 */
public class BitSetTest {
    public static void main(String[] args) {
        int len = 100000;
        AtomicBitSet bitSet = new AtomicBitSet(len);
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < len; i++) {
            int x = new Random().nextInt(len);
            bitSet.set(x, true);
            set.add(x);
        }
        List<Integer> list1 = set.stream().collect(Collectors.toList());
        List<Integer> list2 = new ArrayList<>();
        Set<Integer> set2 = new HashSet<>();
        list1.sort(Comparator.comparingInt(a -> a));
        int x = 0;
        while ((x = bitSet.nextZeroBit(x)) != -1) set2.add(x++);
        for (int i = 0; i < 100000; i++) {
            if (!set2.contains(i)) list2.add(i);
        }
        System.out.println(list1.equals(list2));
    }


}
