package emmi.executor;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class ExecutorQueue {
	static {
		//initialize global queues

		//start operator
		executor = new ThreadPoolExecutor(1, 16, 60, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(),
				new ThreadFactory() {

			public Thread newThread(Runnable runnable) {
				return new Thread(runnable, "himom");
			}
		});

		operator = new Operator();
	}

	public static ExecutorQueue getGlobalInstance(int priority) {
		return null;
	}

	public static ExecutorQueue createQueue(boolean concurrent) {
		if (concurrent) {
			return new ConcurrentExecutorQueue();
		} else {
			return new SerialExecutorQueue();
		}
	}

	private static ThreadPoolExecutor executor;
	protected static Operator operator;

	protected static class Operator {

		/**
		 * Invoked by a queue implementation when queue tasks may be available for execution
		 * @param serialExecutorQueue
		 */
		public void signal(ExecutorQueue queue) {
			Runnable task = queue.pump();
			if (null != task) {
				executor.submit(task);
			}
		}

	}

	/**
	 * The contract here is that if pump returns a non-null runnable, that runnable
	 * MUST execute, in order to prevent the queue from being orphaned.
	 * @return
	 */
	protected abstract Runnable pump();

	public abstract void submitAsync(Runnable runnable);
	public abstract void submitSync(Runnable runnable);
	public abstract void submitAsyncBarrier(Runnable runnable);
	public abstract void submitSyncBarrier(Runnable runnable);

	public void suspend() {

	}

	public void resume() {

	}
}
