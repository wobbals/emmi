package emmi.test;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import emmi.executor.ExecutorQueue;

/**
 * Unit test for simple App.
 */
public class QueueTest 
extends TestCase
{
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public QueueTest( String testName )
	{
		super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite()
	{
		return new TestSuite( QueueTest.class );
	}

	public void testConcurrentQueue() {
		final ConcurrentLinkedQueue<Integer> intQueue = new ConcurrentLinkedQueue<Integer>();
		ExecutorQueue queue = ExecutorQueue.createQueue(true);
		final AtomicInteger totalInts = new AtomicInteger(0);
		final int testSize = 100;
		final int barrierIncrement = 20;
		for (int i = 1; i <= testSize; i++) {
			final int output = i;
			if (i % barrierIncrement == 0) {
				queue.submitAsyncBarrier(new Runnable() {
					public void run() {
						intQueue.offer(output);
						totalInts.incrementAndGet();
						System.out.println("barrier "+output);
						Integer currentInt = intQueue.poll();
						while (null != currentInt) {
							System.out.printf("%d is greater than %d\n", output, currentInt);
							assertTrue((output >= currentInt));
							assertTrue(currentInt > output-20);
							currentInt = intQueue.poll();
						}
					}});
			} else {
				queue.submitAsync(new Runnable() {
					public void run() {
						intQueue.offer(output);
						totalInts.incrementAndGet();
						System.out.println("hello "+output);
					}});
			}			
		}
		queue.submitSyncBarrier(new Runnable() {
			public void run() {
				assertTrue(intQueue.isEmpty());
				assertTrue(totalInts.get() == testSize);
				System.out.printf("all done! in=%d out=%d\n", testSize, totalInts.get());
			}
		});
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
