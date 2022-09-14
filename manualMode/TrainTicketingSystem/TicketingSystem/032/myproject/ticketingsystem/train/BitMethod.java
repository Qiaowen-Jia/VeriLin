package ticketingsystem.train;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BitMethod {
	private static HashMap<Long, Integer> binaryLocateMap;
	private static final int SIZE = Long.SIZE;
    private static long[] binary;
    
    static {
    	binary = new long[SIZE];
    	binaryLocateMap = new HashMap<>(SIZE);
        for (int i = 0; i < SIZE; ++i) {
        	binary[i] = (long) (1L << i);
        	binaryLocateMap.put(binary[i], i);
        }
    }
    public static List<Integer> get_one_Locate(long tem) {
        ArrayList<Integer> one_locate = new ArrayList<>(SIZE);
        while (tem != 0) {
            long ret = tem & (tem - 1);
            one_locate.add(binaryLocateMap.get(tem ^ ret));
            tem = ret;
        }
        return one_locate;
    }
    public static List<Integer> get_zero_Locate(long tem) {
        return get_one_Locate(~tem);
    }
    public static long set(long tem, int i) {
        return tem | binary[i];
    }
    public static long reset(long tem, int i) {
        return tem & (~binary[i]);
    }
    public static long setRange(long tem, int s, int e) {
        for (int i = s; i < e; ++i)
        	tem = set(tem, i);
        return tem;
    }
    

}