/*************************************************
 * Spencer Vaughn                            	 *
 * 010290651	  								 *
 * Producer-Consumer threads with Bounded Buffer *
 *************************************************/

import java.util.concurrent.*;
import java.util.Random;

public class ProducerConsumer { 
	
	private static Producer producers[];
	private static Consumer consumers[];


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
		
		public synchronized void produce(int randInt) {
			try {
				empty.acquire();
				mutex.acquire();
			}	 catch (InterruptedException e) {}
		
			count++;
			buffer[bufferIn] = randInt;
			bufferIn++;
			mutex.release();
			full.release();
		}
		
		public synchronized int consume() {
			int bufferRead;
			try {
				full.acquire();
				mutex.acquire();
			}	 catch (InterruptedException e) {}
			
			count--;
			bufferRead = buffer[bufferOut];
			bufferOut++;
			mutex.release();
			empty.release();	
			
			return bufferRead;
		}
			
	}

	public static class Producer implements Runnable {
		
		private Buffer buffer;
		private int sleep;
		
		public Producer(Buffer buffer, int sleep, int producerThreadCount) {
			this.buffer = buffer;
			this.sleep = sleep;			
		}
		
		public void run() {
			Random rand;
			while(true) {
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {}
				
				rand = new Random();
				int randInt = rand.nextInt(99999);
				System.out.println("Producer "+randInt);
				buffer.produce(randInt);
			}
		}
		
	}
	
	public static class Consumer implements Runnable {
		
		private Buffer buffer;
		private int sleep;
       
		public Consumer(Buffer buffer, int sleep) { 
			this.buffer = buffer;
			this.sleep = sleep;
		}
   
       public void run() {
			int bufferInt;
			while(true){
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {}
				
				System.out.println("consumer ready");
				bufferInt = (int)buffer.consume();
			}
		}
    }
	
	
	public static void main(String args[]) throws InterruptedException {
		
		//parse args
		int sleep = Integer.parseInt(args[0]);
		int producerThreadCount = Integer.parseInt(args[1]);
		int consumerThreadCount = Integer.parseInt(args[2]);
		
		//Create Bounded buffer
		Buffer buffer = new Buffer();
		
		Thread producers;
		Thread consumers;
		
		//for(int i=0; i < producerThreadCount; i++) {
			producers = new Thread(new Producer(buffer, sleep, producerThreadCount));
			producers.run();
		//}
		
		//for(int i = 0; i < consumerThreadCount; i++) {
			consumers = new Thread(new Consumer(buffer, sleep));
			consumers.run();
		//}	
	}	
			
}