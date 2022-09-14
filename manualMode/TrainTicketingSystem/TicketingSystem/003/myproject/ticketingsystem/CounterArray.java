package ticketingsystem;

public class CounterArray {
    private static int stationnum;
    private static int lenth;
    private static int nanos = (int) 2e3;
    private Counter[] counter;

    public CounterArray (int stationnum1, int N, int nanos1, int range1) {
        nanos = nanos1;
        stationnum = stationnum1;
        lenth = (stationnum*(stationnum-1))>>1;
        this.counter = new Counter[lenth];
        for (int i = 0; i < lenth; i++) {
            this.counter[i] = new Counter(N, range1);
        }
    }

    public int f_decrement(int dep, int arr, int[] mark) {
        for (int i = mark[0]; i < arr; i++) {
            int flag = Math.max(i + 1, dep + 1);
            for (int j = flag; j <= mark[1]; j++) {
                int k = Node.f_cvt_idx(i, j, stationnum);
                this.counter[k].decrement(nanos);
            }
        }
        return 0;
    }

    public int f_increment(int dep, int arr, int[] mark) {
        for (int i = mark[0]; i < arr; i++) {
            int flag = Math.max(i + 1, dep + 1);
            for (int j = flag; j <= mark[1]; j++) {
                int k = Node.f_cvt_idx(i, j, stationnum);
                this.counter[k].increment(nanos);
            }
        }
        return 0;
    }

    public int f_get_left(int nth){
        return this.counter[nth].get();
    }
}