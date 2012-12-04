package emmi.io;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import emmi.executor.ExecutorQueue;

public class SocketHelper {
	private Listener listener = new DummyListener();

	private ExecutorQueue queue;

	public static SocketHelper create(SocketAddress remote) {
		return null;
	}

	public abstract class Listener {
		public abstract void onConnected();
		public abstract void onDisconnected();
		public abstract void onReadDone(ByteBuffer data);
		public abstract void onWriteDone(ByteBuffer data);
	}

	public Listener getListener() {
		return listener;
	}

	public void setListener(Listener listener) {
		if (null == listener) {
			this.listener = new DummyListener();
		} else {
			this.listener = listener;
		}
	}

	public ExecutorQueue getQueue() {
		return queue;
	}

	public void setQueue(ExecutorQueue queue) {
		this.queue = queue;
	}

	public void connect() {
		
	}
	
	public void disconnect() {
		
	}
	
	public void write(ByteBuffer data) {

	}
	
	private class DummyListener extends Listener {

		@Override
		public void onConnected() { }

		@Override
		public void onDisconnected() { }

		@Override
		public void onReadDone(ByteBuffer data) { }

		@Override
		public void onWriteDone(ByteBuffer data) { }
	}

}
