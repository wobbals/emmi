package foo;

import java.net.InetSocketAddress;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import emmi.executor.ExecutorQueue;
import emmi.io.SocketHelper;

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

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
        
        SocketHelper socketHelper = SocketHelper.create(new InetSocketAddress("localhost", 22));
        
        ExecutorQueue queue = ExecutorQueue.getGlobalInstance(0);
    }
}
