package emmi.executor;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SerialExecutorQueue extends ExecutorQueue {
	private Semaphore serialLock = new Semaphore(1);
	private ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<Runnable>();
	
	
	@Override
	public void submitAsync(Runnable runnable) {
		taskQueue.offer(new AsyncTask(runnable));
		signalOperator();
	}
	
	private class AsyncTask implements Runnable {
		private Runnable runnable;
		public AsyncTask(Runnable runnable) { this.runnable = runnable; }
		
		public void run() {
			try {
				runnable.run();
			} finally {
				serialLock.release();
			}
		}
	}

	@Override
	public void submitSync(Runnable runnable) {
		SyncTask syncRunnable = new SyncTask(runnable);
		taskQueue.offer(syncRunnable);
		signalOperator();
		syncRunnable.await();
	}
	
	private class SyncTask implements Runnable {
		private Runnable runnable;
		private ReentrantLock syncLock = new ReentrantLock();
		private Condition notDone = syncLock.newCondition();
		private volatile boolean taskFinished = false;
		public SyncTask(Runnable runnable) { this.runnable = runnable; }
		
		public void run() {
			syncLock.lock();
			try {
				runnable.run();
			} finally {
				serialLock.release();
				taskFinished = true;
				notDone.signal();
				syncLock.unlock();
			}
		}
		
		public void await() {
			syncLock.lock();
			try {
				while (!taskFinished) {
					notDone.awaitUninterruptibly();
				}
			} finally {
				syncLock.unlock();
			}
		}
	}

	@Override
	public void submitAsyncBarrier(Runnable runnable) {
		submitAsync(runnable);
	}

	@Override
	public void submitSyncBarrier(Runnable runnable) {
		submitSync(runnable);
	}

	@Override
	protected Runnable pump() {
		// do not wait if queue is already executing a task
		if (!serialLock.tryAcquire()) {
			return null;
		}
		Runnable runnable = taskQueue.poll();
		if (null == runnable) {
			serialLock.release();
		}
		return runnable;
	}

}
