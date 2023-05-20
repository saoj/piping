# Thread Piping
A different way to do multithreading programming using messages through pipes.

## Synchronized Way
Using the `synchronized` keyword to lock around shared variables (i.e. shared state).

```java
public class ThreadA extends Thread {
	
	private long counter;
	private long operations = 0;
	private final long iterations;
	
	public ThreadA(long iterations) {
		this.iterations = iterations;
	}
	
	public synchronized void incrementBy(long x) {
		counter += x;
		operations++;
	}
	
	public synchronized void decrementBy(long x) {
		counter -= x;
		operations++;
	}
	
	@Override
	public void run() {
		long i = 0;
		while(i++ < iterations) {
			long x = i % 10;
			if (x % 2 == 0) {
				incrementBy(2 * x);
			} else {
				decrementBy(x);
			}
		}
	}

	@Override
	public String toString() {
		return ThreadA.class.getSimpleName() + " with counter=" + counter + " after " + operations + " operations";
	}
}
```
```java
public class ThreadB extends Thread {
	
	private final ThreadA threadA;
	private final long iterations;
	
	public ThreadB(long iterations, ThreadA threadA) {
		this.iterations = iterations;
		this.threadA = threadA;
	}
	
	@Override
	public void run() {
		long i = 0;
		while(i++ < iterations) {
			long x = i % 10;
			if (x % 2 == 0) {
				threadA.decrementBy(x);
			} else {
				threadA.incrementBy(2 * x);
			}
		}
	}
}
```
```java
public static void main(String[] args) throws InterruptedException {
	
	final long iterations = 10_000_000;
	
	ThreadA threadA = new ThreadA(iterations);
	ThreadB threadB = new ThreadB(iterations, threadA);
	
	threadA.start();
	threadB.start();
	
	threadA.join();
	threadB.join();
	
	System.out.println(threadA);
}
```

## Pipe Asynchronous Way
Using a single pipe to send messages from thread B to thread A. No synchronization around shared variables is needed.

```java
public class OpMessage implements Transferable<OpMessage> {

	public static enum Op { ADD, SUB }
	
	public Op op;
	public long value;
	
	@Override
	public void transferTo(OpMessage dest) {
		dest.op = this.op;
		dest.value = this.value;
	}
}
```
```java
public class ThreadA extends Thread {
	
	private long counter;
	private long operations = 0;
	private final long iterations;
	private final Pipe<OpMessage> pipe;
	private final OpMessage toReceive = new OpMessage();
	
	public ThreadA(long iterations, Pipe<OpMessage> pipe) {
		this.iterations = iterations;
		this.pipe = pipe;
	}
	
	public void incrementBy(long x) {
		counter += x;
		operations++;
	}
	
	public void decrementBy(long x) {
		counter -= x;
		operations++;
	}
	
	@Override
	public void run() {
		long i = 0;
		while(i++ < iterations) {
			long x = i % 10;
			if (x % 2 == 0) {
				incrementBy(2 * x);
			} else {
				decrementBy(x);
			}
			if (pipe.receive(toReceive)) {
				if (toReceive.op == OpMessage.Op.ADD) {
					incrementBy(toReceive.value);
				} else if (toReceive.op == OpMessage.Op.SUB) {
					decrementBy(toReceive.value);
				} else {
					throw new IllegalStateException("Don't know this op: " + toReceive.op);
				}
			}
			
		}
	}

	@Override
	public String toString() {
		return ThreadA.class.getSimpleName() + " with counter=" + counter + " after " + operations + " operations";
	}
}
```
```java
public class ThreadB extends Thread {
	
	private final long iterations;
	private final Pipe<OpMessage> pipe;
	private final OpMessage toSend = new OpMessage();
	
	public ThreadB(long iterations, Pipe<OpMessage> pipe) {
		this.iterations = iterations;
		this.pipe = pipe;
	}
	
	@Override
	public void run() {
		long i = 0;
		while(i++ < iterations) {
			long x = i % 10;
			if (x % 2 == 0) {
				toSend.op = OpMessage.Op.SUB;
				toSend.value = x;
			} else {
				toSend.op = OpMessage.Op.ADD;
				toSend.value = 2 *x;
			}
			if (!pipe.dispatch(toSend)) {
				throw new RuntimeException("Cannot send operation!");
			}
		}
	}
}
```
```java
public static void main(String[] args) throws InterruptedException {
	
	final long iterations = 10_000_000;
	
	{
	
		Pipe<OpMessage> pipe = new LinkedBlockingPipe<OpMessage>(1024, OpMessage.class);
		
		ThreadA threadA = new ThreadA(iterations, pipe);
		ThreadB threadB = new ThreadB(iterations, pipe);
		
		threadA.start();
		threadB.start();
		
		threadA.join();
		threadB.join();
		
		System.out.println(threadA);
	}
	
	System.out.println("\nNow if you use a non-blocking pipe of course it fails...\n");
	
	{
		
		Pipe<OpMessage> pipe = new LinkedNonBlockingPipe<OpMessage>(1024, OpMessage.class);
		
		ThreadA threadA = new ThreadA(iterations, pipe);
		ThreadB threadB = new ThreadB(iterations, pipe);
		
		threadA.start();
		threadB.start();
		
		threadA.join();
		threadB.join();
		
		System.out.println(threadA);
	}
	
}
```

## Pipe Synchronous Way
Using two pipes: one to send operations and another one to receive acks. No synchronization around shared variables is needed.
```java
public class OpMessage implements Transferable<OpMessage> {

	public static enum Op { ADD, SUB }
	
	public Op op;
	public long value;
	
	@Override
	public void transferTo(OpMessage dest) {
		dest.op = this.op;
		dest.value = this.value;
	}
}
```
```java
public class AckMessage implements Transferable<AckMessage> {

	@Override
	public void transferTo(AckMessage dest) {
		// NOOP
	}
}
```
```java
public class ThreadA extends Thread {
	
	private long counter;
	private long operations = 0;
	private final long iterations;
	private final Pipe<OpMessage> inPipe;
	private final Pipe<AckMessage> outPipe;
	private final OpMessage toReceive = new OpMessage();
	private final AckMessage ack = new AckMessage();
	
	public ThreadA(long iterations, Pipe<AckMessage> outPipe, Pipe<OpMessage> inPipe) {
		this.iterations = iterations;
		this.outPipe = outPipe;
		this.inPipe = inPipe;
	}
	
	public void incrementBy(long x) {
		counter += x;
		operations++;
	}
	
	public void decrementBy(long x) {
		counter -= x;
		operations++;
	}
	
	@Override
	public void run() {
		long i = 0;
		while(i++ < iterations) {
			long x = i % 10;
			if (x % 2 == 0) {
				incrementBy(2 * x);
			} else {
				decrementBy(x);
			}
			if (inPipe.receive(toReceive)) {
				if (toReceive.op == OpMessage.Op.ADD) {
					incrementBy(toReceive.value);
				} else if (toReceive.op == OpMessage.Op.SUB) {
					decrementBy(toReceive.value);
				} else {
					throw new IllegalStateException("Don't know this op: " + toReceive.op);
				}
			}
			if (!outPipe.dispatch(ack)) {
				throw new RuntimeException("Cannot send ack!");
			}
		}
	}

	@Override
	public String toString() {
		return ThreadA.class.getSimpleName() + " with counter=" + counter + " after " + operations + " operations";
	}
}
```
```java
public class ThreadB extends Thread {
	
	private final long iterations;
	private final Pipe<OpMessage> outPipe;
	private final Pipe<AckMessage> inPipe;
	private final OpMessage toSend = new OpMessage();
	private final AckMessage ack = new AckMessage();
	
	public ThreadB(long iterations, Pipe<OpMessage> outPipe, Pipe<AckMessage> inPipe) {
		this.iterations = iterations;
		this.outPipe = outPipe;
		this.inPipe = inPipe;
	}
	
	@Override
	public void run() {
		long i = 0;
		while(i++ < iterations) {
			long x = i % 10;
			if (x % 2 == 0) {
				toSend.op = OpMessage.Op.SUB;
				toSend.value = x;
			} else {
				toSend.op = OpMessage.Op.ADD;
				toSend.value = 2 *x;
			}
			if (!outPipe.dispatch(toSend)) {
				throw new RuntimeException("Cannot send operation!");
			}
			if (!inPipe.receive(ack)) {
				throw new RuntimeException("Cannot receive ack!");
			}
		}
	}
}
```
```java
public static void main(String[] args) throws InterruptedException {
	
	final long iterations = 10_000_000;
	
	{
	
		Pipe<OpMessage> inPipe = new LinkedBlockingPipe<OpMessage>(1024, OpMessage.class);
		Pipe<AckMessage> outPipe = new LinkedBlockingPipe<AckMessage>(1, AckMessage.class);
		
		ThreadA threadA = new ThreadA(iterations, outPipe, inPipe);
		ThreadB threadB = new ThreadB(iterations, inPipe, outPipe);
		
		threadA.start();
		threadB.start();
		
		threadA.join();
		threadB.join();
		
		System.out.println(threadA);
	}
	
	System.out.println("\nNow if you use a non-blocking pipe of course it fails...\n");
	
	{
		
		Pipe<OpMessage> inPipe = new LinkedNonBlockingPipe<OpMessage>(1024, OpMessage.class);
		Pipe<AckMessage> outPipe = new LinkedNonBlockingPipe<AckMessage>(1, AckMessage.class);
		
		ThreadA threadA = new ThreadA(iterations, outPipe, inPipe);
		ThreadB threadB = new ThreadB(iterations, inPipe, outPipe);
		
		threadA.start();
		threadB.start();
		
		threadA.join();
		threadB.join();
		
		System.out.println(threadA);
	}
	
}
```
