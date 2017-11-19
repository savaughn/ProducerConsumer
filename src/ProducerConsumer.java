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
			bufferCount = 0;
			bufferIn = 0;
			bufferOut = 0;
			buffer = new int[bufferSize];
			mutex = new Semaphore(criticalSectionCount); 
			empty = new Semaphore(bufferSize);
			full = new Semaphore(0); 
		}
		
		// Producer call in buffer
		public void produce(int randInt) {
			
			try {
				empty.acquire();
				mutex.acquire();
			}	 catch (InterruptedException e) {
				System.out.println("error produce buffer");
				System.exit(1);
			}
			while (bufferCount == bufferSize) {};  //when buffer is full wait
			++bufferCount;
			buffer[bufferIn] = randInt;
			System.out.println("Producer: " +randInt);
			bufferIn = (bufferIn + 1)% bufferSize;			
			mutex.release();
			full.release();
		}
		
		//consumer call in buffer
		public int consume() {
			
			int bufferRead = 0;			
			try {
				full.acquire();
				mutex.acquire();
			}	 catch (InterruptedException e) {
				System.out.println("error consume buffer");
				System.exit(1);
			}
			
			while (bufferCount == 0) {};  //when buffer is empty wait
			--bufferCount;
			bufferRead = buffer[bufferOut];
			bufferOut = (bufferOut + 1)% bufferSize;			
			mutex.release();
			empty.release();	
			
			return bufferRead;
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
				
				randBufferInt = r.nextInt(99999);
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
					System.out.println("Consumer: " + bufferInt);
				}
			}
    	}
	
	
	public static void main(String args[]) throws InterruptedException {
		
		//parse arguments
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
