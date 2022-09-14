package ticketingsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrainTest {
    public static void main(String[] args) {
        int size = 20;
        Train1 train1 = new Train1(1, 1, size);
        Train3 train3 = new Train3(1, 1, size);
        Random random = new Random();
        int[] coach = new int[1];
        int[] seat = new int[1];
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 1000000; i++) {
            int r = random.nextInt(100);
            if (r < 50) {
                int s = random.nextInt(size - 1) + 1;
                int e = Math.min(s + 2, size);
                boolean flag1 = train1.get(s, e, coach, seat);
                boolean flag3 = train3.get(s, e, coach, seat);
                if (flag1 != flag3) {
                    System.out.println("put error");
                }
                if (flag1) {
//                    System.out.println("get " + s + ", " + e);
                    list.add(s);
                }
            } else {
                if (list.size() != 0) {
                    int t = random.nextInt(list.size());
                    int s = list.get(t);
                    list.remove(t);
                    int e = Math.min(s + 2, size);
                    train1.put(s, e, coach[0], seat[0]);
                    train3.put(s, e, coach[0], seat[0]);
//                    System.out.println("put " + s + ", " + e);
                }
            }
            for (int j = 1; j < size; j++) {
                for (int k = j; k < size; k++) {
                    if (train1.inquiry(j, k) != train3.inquiry(j, k)) {
                        System.out.println("=======error " + j + ", " + k);
                    }
                }
            }
        }
        System.out.println("done");
    }
}
