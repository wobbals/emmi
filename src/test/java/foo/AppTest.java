package foo;

import java.util.concurrent.ConcurrentLinkedQueue;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import emmi.executor.ExecutorQueue;

/**
 * Unit test for simple App.
 */
public class AppTest 
extends TestCase
{
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public AppTest( String testName )
	{
		super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite()
	{
		return new TestSuite( AppTest.class );
	}

	public void testConcurrentQueue() {
		final ConcurrentLinkedQueue<Integer> intQueue = new ConcurrentLinkedQueue<Integer>();
		ExecutorQueue queue = ExecutorQueue.createQueue(true);
		for (int i = 1; i <= 10; i++) {
			final int output = i;
			queue.submitAsync(new Runnable() {
				public void run() {
					intQueue.offer(output);
					System.out.println("hello "+output);
				}});
		}
		System.out.println("counting tasks queued");
		queue.submitSync(new Runnable() {

			public void run() {
				Integer currentInt = intQueue.poll();
				Integer nextInt;
				while (null != currentInt) {
					nextInt = intQueue.poll();
					System.out.printf("dequeue %d", currentInt);
					currentInt = nextInt;
				}

				System.out.println("super duper");
				
			}});
		System.out.println("all done!");
	}
		
	public void testSerialQueue()
	{
		final ConcurrentLinkedQueue<Integer> intQueue = new ConcurrentLinkedQueue<Integer>();
		ExecutorQueue queue = ExecutorQueue.createQueue(false);
		for (int i = 1; i <= 10; i++) {
			final int output = i;
			queue.submitAsync(new Runnable() {
				public void run() {
					intQueue.offer(output);
					System.out.println("hello "+output);
				}});
		}
		System.out.println("counting tasks queued");
		queue.submitSync(new Runnable() {

			public void run() {
				Integer currentInt = intQueue.poll();
				Integer nextInt;
				while (null != currentInt) {
					nextInt = intQueue.poll();
					if (null != nextInt) {
						assertTrue(nextInt > currentInt);
					}
					currentInt = nextInt;
				}

				System.out.println("super duper");
				
			}});
		System.out.println("all done!");
	}
}
