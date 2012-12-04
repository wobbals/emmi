package emmi.executor;

import java.util.concurrent.ThreadPoolExecutor;

public abstract class ExecutorQueue {
	static {
		//initialize global queues
		//start operator
	}
	
	public static ExecutorQueue getGlobalInstance(int priority) {
		return null;
	}
	
	public static ExecutorQueue createQueue(boolean concurrent) {
		return null;
	}
	
	private static ThreadPoolExecutor executor;
	protected Operator operator;
	
	protected static class Operator {

		public void signal(SerialExecutorQueue serialExecutorQueue) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	protected abstract Runnable pump();
	
	public abstract void submitAsync(Runnable runnable);
	public abstract void submitSync(Runnable runnable) throws InterruptedException;
	public abstract void submitAsyncBarrier(Runnable runnable);
	public abstract void submitSyncBarrier(Runnable runnable) throws InterruptedException;
	
	public void suspend() {
		
	}
	
	public void resume() {
		
	}
}
