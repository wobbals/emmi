package emmi.executor;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SerialExecutorQueue extends ExecutorQueue {
	private ReentrantLock serialLock = new ReentrantLock();
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
				serialLock.unlock();
			}
		}
	}

	@Override
	public void submitSync(Runnable runnable) {
		SyncTask syncRunnable = new SyncTask(runnable);
		taskQueue.offer(syncRunnable);
		operator.signal(this);
		syncRunnable.await();
	}
	
	private class SyncTask implements Runnable {
		private Runnable runnable;
		private ReentrantLock syncLock = new ReentrantLock();
		private Condition notDone = syncLock.newCondition();
		private AtomicBoolean taskFinished = new AtomicBoolean(false);
		public SyncTask(Runnable runnable) { this.runnable = runnable; }
		
		public void run() {
			runnable.run();
			taskFinished.set(true);
			notDone.signal();
		}
		
		public void await() {
			syncLock.lock();
			try {
				while (!taskFinished.compareAndSet(true, true)) {
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
		if (!serialLock.tryLock()) {
			return null;
		}
		Runnable runnable = taskQueue.poll();
		if (null == runnable) {
			serialLock.unlock();
		}
		return runnable;
	}

}
