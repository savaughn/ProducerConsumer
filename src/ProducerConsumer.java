/*************************************************
 * Spencer Vaughn                            	 *
 * 010290651	  								 *
 * Producer-Consumer threads with Bounded Buffer *
 *************************************************/

import java.util.concurrent.*;
import java.util.Random;

public class ProducerConsumer { 
	
	public static class Buffer{
		
		private final int bufferSize = 5;
		private final int criticalSectionCount = 1;
		private int count;
		private int bufferIn;
		private int bufferOut;
		private int[] buffer;
		private Semaphore mutex; 
		private Semaphore full; 
		private Semaphore empty; 
		
		public Buffer() {
			count = 0;
			bufferIn = 0;
			bufferOut = 0;
			buffer = new int[bufferSize];
			mutex = new Semaphore(criticalSectionCount); 
			empty = new Semaphore(bufferSize);
			full = new Semaphore(0); 
		}
		
		public void produce(int randInt) {
			while (count == bufferSize) {};
			
			System.out.println("Buffer count: " + count);
			try {
				empty.acquire();
				mutex.acquire();
			}	 catch (InterruptedException e) {}
		
			++count;
			buffer[bufferIn] = randInt;
			System.out.println("Producer: " +randInt);
			bufferIn = (bufferIn + 1)% bufferSize;
			
			
			mutex.release();
			full.release();
		}
		
		public int consume() {
			
			int bufferRead = 0;
			while (count == 0) {};
			
			try {
				full.acquire();
				mutex.acquire();
			}	 catch (InterruptedException e) {}
			
			--count;
			bufferRead = buffer[bufferOut];
			bufferOut = (bufferOut + 1)% bufferSize;
			
			mutex.release();
			empty.release();	
			
			return bufferRead;
		}
			
	}

	public static class Producer implements Runnable {
		
		private Buffer buffer;
		private int sleep;
		
		public Producer(Buffer buffer) {
			this.buffer = buffer;	
		}
		
		public void run() {
			Random rand;
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {}
				
				rand = new Random();
				int randInt = rand.nextInt(99999);
				buffer.produce(randInt);
		}		
	}
	
	public static class Consumer implements Runnable {
		
		private Buffer buffer;
       
		public Consumer(Buffer buffer) { 
			this.buffer = buffer;
		}
   
       public void run() {
			int bufferInt;
			for(int i = 0; i < 100; i++) {
				Random r;
				int randSleepTime = r.nextInt(500);
				try {
						Thread.sleep(randSleepTime);
					} catch (InterruptedException e) {}
					
					bufferInt = (int)buffer.consume();
					System.out.println("Consumer: " + bufferInt);
				}
			}
    	}
	
	
	public static void main(String args[]) throws InterruptedException {
		
		//parse args
		int sleep = Integer.parseInt(args[0]);
		int producerThreadCount = Integer.parseInt(args[1]);
		int consumerThreadCount = Integer.parseInt(args[2]);
		
		//Create buffer
		Buffer buffer = new Buffer();
		
		Thread producers[] = new Thread[producerThreadCount];
		Thread consumers[] = new Thread[consumerThreadCount];
		
		for(int i=0; i < producerThreadCount; i++) {
			producers[i] = new Thread(new Producer(buffer));
			producers[i].start();
		}
		
		for(int i = 0; i < consumerThreadCount; i++) {
			consumers[i] = new Thread(new Consumer(buffer));
			consumers[i].start();
		}	
		
		try {
			Thread.sleep(sleep*1000);
		} catch(InterruptedException e) {}
	}	
			
}