package emmi.executor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentExecutorQueue extends ExecutorQueue {
	private Semaphore barrierLock = new Semaphore(1);
	private LinkedBlockingQueue<ConcurrentQueueTask> taskQueue = new LinkedBlockingQueue<ConcurrentQueueTask>();
	private AtomicInteger liveTaskCounter = new AtomicInteger(0);

	private abstract class ConcurrentQueueTask implements Runnable {
		protected Runnable runnable; 
		private boolean barrierTask;
		public ConcurrentQueueTask(Runnable runnable) {
			this.runnable = runnable;
		}

		final public void setIsBarrierTask(boolean isBarrierTask) {
			this.barrierTask = isBarrierTask;
		}

		final public boolean isBarrierTask() {
			return barrierTask;
		}

		public abstract void doPreTask();
		public abstract void doPostTask();

		public final void run() {
			doPreTask();
			try {
				runnable.run();
			} finally {
				doPostTask();
				liveTaskCounter.decrementAndGet();
				if (isBarrierTask()) {
					barrierLock.release();
				}				
			}
		}
	}

	@Override
	public void submitAsync(Runnable runnable) {
		taskQueue.offer(new AsyncTask(runnable));
		operator.signal(this);
	}

	private class AsyncTask extends ConcurrentQueueTask {
		public AsyncTask(Runnable runnable) { super(runnable); }

		@Override
		public void doPreTask() { }

		@Override
		public void doPostTask() { }
	}

	@Override
	public void submitSync(Runnable runnable) {
		SyncTask syncRunnable = new SyncTask(runnable);
		taskQueue.offer(syncRunnable);
		signalOperator();
		syncRunnable.await();
	}

	private class SyncTask extends ConcurrentQueueTask {
		private ReentrantLock syncLock = new ReentrantLock();
		private Condition notDone = syncLock.newCondition();
		private volatile boolean taskFinished = false;
		public SyncTask(Runnable runnable) { super(runnable); }

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

		@Override
		public void doPreTask() {
			syncLock.lock();
		}

		@Override
		public void doPostTask() {
			taskFinished = true;
			notDone.signal();
			syncLock.unlock();
		}
	}

	@Override
	public void submitAsyncBarrier(Runnable runnable) {
		AsyncTask task = new AsyncTask(runnable);
		task.setIsBarrierTask(true);
		taskQueue.offer(task);
		signalOperator();
	}

	@Override
	public void submitSyncBarrier(Runnable runnable) {
		SyncTask task = new SyncTask(runnable);
		task.setIsBarrierTask(true);
		taskQueue.offer(task);
		signalOperator();
		task.await();
	}

	/**
	 * TODO: make this so it can run without the synchronized keyword. We're violating the "don't block, asshole" principle here.
	 * Hint: you'll need a deque to accomplish this task.
	 */
	@Override
	protected synchronized Runnable pump() {
		ConcurrentQueueTask task = taskQueue.peek();
		if (null == task) {
			return null;
		}
		if (task.isBarrierTask() && liveTaskCounter.compareAndSet(0, 1) && barrierLock.tryAcquire()) {
			Runnable myTask = taskQueue.poll();
			return myTask;
		} else if (!task.isBarrierTask() && barrierLock.tryAcquire()) {
			liveTaskCounter.incrementAndGet();
			barrierLock.release();
			return taskQueue.poll();
		}
		return null;
	}

}
