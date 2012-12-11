package emmi.executor;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * For getting work done. All the reasons you'd choose GCD over your own thread management
 * in the Cocoa context apply here.
 * @author charley
 *
 */
public abstract class ExecutorQueue {
	static {
		//initialize global queues

		//start operator
		executor = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(),
				new ThreadFactory() {

			public Thread newThread(Runnable runnable) {
				return new Thread(runnable, "emmi");
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
		public void signal(final ExecutorQueue queue) {
			Runnable task = queue.pump();
			while (null != task) {
				final Runnable myTask = task;
				executor.submit(new Runnable() {
					public void run() {
						try {
							myTask.run();
						} finally {
							signal(queue);
						}
					}});
				task = queue.pump();
			}
		}

	}

	/**
	 * The contract here is that if pump returns a non-null runnable, that runnable
	 * MUST execute, in order to prevent the queue from being orphaned.
	 * This call gets invoked more times than necessary. Sometimes a lot more.
	 * It should not block. Subclassers: do not block for anything in this function.
	 * @return null if you have nothing to do right now. Don't block, asshole.
	 */
	protected abstract Runnable pump();
	
	protected void signalOperator() {
		operator.signal(this);
	}

	/**
	 * Returns immediately, runnable is scheduled for later execution.
	 * @param runnable
	 */
	public abstract void submitAsync(Runnable runnable);
	
	/**
	 * Returns after runnable completes, regardless of result.
	 * TODO: make these run on the caller thread
	 * @param runnable
	 */
	public abstract void submitSync(Runnable runnable);
	
	/**
	 * Executes alone, regardless of queue width.
	 * @param runnable
	 */
	public abstract void submitAsyncBarrier(Runnable runnable);
	public abstract void submitSyncBarrier(Runnable runnable);

	public void suspend() {

	}

	public void resume() {

	}
}
