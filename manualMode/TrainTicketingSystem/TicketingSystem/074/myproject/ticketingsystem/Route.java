package ticketingsystem;

public class Route {
    private Coach[] coachs;
	public Route(int coachnum,int seatnum) {
        coachs=new Coach[coachnum+1];
        for(int i=1;i<(coachnum+1);i++){
            coachs[i]=new Coach(seatnum);
        }
    }
    public Coach[] getCoachs() {
        return coachs;
    }
}
