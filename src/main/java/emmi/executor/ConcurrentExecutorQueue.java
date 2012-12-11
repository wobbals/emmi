package emmi.executor;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class ConcurrentExecutorQueue extends ExecutorQueue {
	private Semaphore barrierLock = new Semaphore(1);
	private ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<Runnable>();
	
	@Override
	public void submitAsync(Runnable runnable) {
		taskQueue.offer(new AsyncTask(runnable));
		operator.signal(this);
	}
	
	private class AsyncTask implements Runnable {
		private Runnable runnable;
		public AsyncTask(Runnable runnable) { this.runnable = runnable; }
		
		public void run() {
			try {
				runnable.run();
			} finally {
				operator.signal(ConcurrentExecutorQueue.this);
			}
		}
	}

	@Override
	public void submitSync(Runnable runnable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void submitAsyncBarrier(Runnable runnable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void submitSyncBarrier(Runnable runnable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Runnable pump() {
		// TODO Auto-generated method stub
		return null;
	}

}
