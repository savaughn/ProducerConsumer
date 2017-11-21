/*************************************************
 * Spencer Vaughn                                *
 * 010290651                                     *
 * Producer-Consumer threads with Bounded Buffer *
 *************************************************/

import java.util.concurrent.*;
import java.util.Random;

public class ProducerConsumer { 
	
/***************     Buffer     *******************/	
	public static class Buffer{
		
		private final int bufferSize = 5;
		private final int criticalSectionCount = 1;
		
		private int bufferCount;
		private int bufferIn;
		private int bufferOut;
		
		private int[] buffer = {};
		private Semaphore mutex; 
		private Semaphore full; 
		private Semaphore empty; 
		
		public Buffer() {
			setBufferCount(0);
			bufferIn = 0;
			bufferOut = 0;
			buffer = new int[bufferSize];					// Buffer size is 5
			mutex = new Semaphore(criticalSectionCount);    // Mutex gets 1 permit
			empty = new Semaphore(bufferSize);				// Empty gets 5 permits
			full = new Semaphore(0); 						// Fulls gets 0 permits
		}
		
		// Producer call in buffer
		public void produce(int randInt) {
			
			try {
				empty.acquire(); //stops producer when buffer is full 
				mutex.acquire(); //locks critical section
			}	 catch (InterruptedException e) {
				System.out.println("error produce buffer");
				System.exit(1);
			}
			
			setBufferCount(getBufferCount() + 1);
			buffer[bufferIn] = randInt;
			System.out.println("Producer produced " +randInt);
			bufferIn = (bufferIn + 1)% bufferSize;			
			mutex.release();	//unlocks critical section 
			full.release(); 	//signals consumer 
		}
		
		//consumer call in buffer
		public int consume() {
			
			int bufferRead = 0;			
			try {
				full.acquire(); //stops consumer when buffer is empty
				mutex.acquire(); //gets access to critical
			}	 catch (InterruptedException e) {
				System.out.println("error consume buffer");
				System.exit(1);
			}
			
			setBufferCount(getBufferCount() - 1);
			bufferRead = buffer[bufferOut];
			bufferOut = (bufferOut + 1)% bufferSize;			
			mutex.release();  //unlocks critical section
			empty.release();  //signals producer
			
			return bufferRead;
		}

		public int getBufferCount() {
			return bufferCount;
		}

		public void setBufferCount(int bufferCount) {
			this.bufferCount = bufferCount;
		}
			
	}

/***********************     Producer     *****************************
 * 	Fills buffer with random integers.                                *
 *	100 iterations with random sleep time between 0 and 0.5 seconds.  *
 **********************************************************************/

	public static class Producer implements Runnable {
		
		private Buffer buffer;
		private int randSleepTime;
		private int randBufferInt;
		
		public Producer(Buffer buffer) {
			this.buffer = buffer;	
		}
		
		public void run() {
			
			Random r = new Random();
			
			for(int i = 0; i < 100; i++) {
				randSleepTime = r.nextInt(500);
				try {
					Thread.sleep(randSleepTime);
				} catch (InterruptedException e) {
					System.out.println("error producer can't sleep");
					System.exit(1);
				}
				
				randBufferInt = r.nextInt(89999)+10000;
				buffer.produce(randBufferInt);
				
			}
		}		
	}

/************************     Consumer     **********************************
 *  Returns random integer stored in buffer by producer.                    *
 *	100 iterations with random sleep time between 0 and 0.5 seconds.        *
 ****************************************************************************/

	public static class Consumer implements Runnable {
		
		private Buffer buffer;
       
		public Consumer(Buffer buffer) { 
			this.buffer = buffer;
		}
   
       public void run() {
    	   
			int bufferInt;
			Random r = new Random();
			
			for(int i = 0; i < 100; i++) {				
				int randSleepTime = r.nextInt(500);
				try {
						Thread.sleep(randSleepTime);
					} catch (InterruptedException e) {
						System.out.println("error producer can't sleep");
						System.exit(1);
					}					
				
				bufferInt = (int)buffer.consume();
				System.out.println("Consumer consumed " + bufferInt);
				}
			}
    	}
	
	
	public static void main(String args[]) throws InterruptedException {
				
		if(args.length != 3 ) {
			System.out.println("'Check arguments' usage:ProducerConsumer <sleep> <producer thread count> <consumer thread count>");
			System.exit(1);
		}
		
		int sleep = Integer.parseInt(args[0]);
		int producerThreadCount = Integer.parseInt(args[1]);
		int consumerThreadCount = Integer.parseInt(args[2]);
		
		//Instantiate buffer
		Buffer buffer = new Buffer();
		
		//Instantiate thread intArrays
		Thread producers[] = new Thread[producerThreadCount];
		Thread consumers[] = new Thread[consumerThreadCount];
		
		//Create producer threads
		for(int i=0; i < producerThreadCount; i++) {
			producers[i] = new Thread(new Producer(buffer));
			producers[i].start();
		}
		
		//Create consumer threads
		for(int i = 0; i < consumerThreadCount; i++) {
			consumers[i] = new Thread(new Consumer(buffer));
			consumers[i].start();
		}	
		
		//Sleep (ms) for specified time while threads modify buffer
		try {
			Thread.sleep(sleep*1000);
		} catch(InterruptedException e) {
			System.out.println("main thread can't sleep");
			System.exit(1);
		}
		
		//Exit after sleep
		System.exit(0);
		
	}	
			
}
