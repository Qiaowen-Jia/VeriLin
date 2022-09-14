package ticketingsystem;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BuyRefundLock {
	volatile int buycount;
	volatile int refundcount;
	ReentrantLock lock;
	BuyLock buylock;
	RefundLock refundlock;
	Condition condition;
	public BuyRefundLock()
	{
		buycount = 0;
		refundcount = 0;
		lock = new ReentrantLock();
		buylock = new BuyLock();
		refundlock = new RefundLock();
		condition = lock.newCondition();
	}
	class BuyLock{
		public void lock() {
			// TODO Auto-generated method stub
			lock.lock();
			try
			{
				while(refundcount>0)
				{
					condition.await();
				}
				buycount++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}
		public void unlock() {
			// TODO Auto-generated method stub
			lock.lock();
			try {
				buycount--;
				if(buycount==0)
					condition.signalAll();
			}finally
			{
				lock.unlock();
			}
		}
	}
	class RefundLock{

		public void lock() {
			// TODO Auto-generated method stub
			lock.lock();
			try
			{
				while(buycount>0)
				{
					condition.await();
				}
				refundcount++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}

		public void unlock() {
			// TODO Auto-generated method stub
			lock.lock();
			try {
				refundcount--;
				if(refundcount==0)
					condition.signalAll();
			}finally {
				lock.unlock();
			}
		}
	}


}
