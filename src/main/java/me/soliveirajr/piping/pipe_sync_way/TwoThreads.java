/* 
 * Copyright 2023 (c) Sergio Oliveira Jr. - https://github.com/saoj
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package me.soliveirajr.piping.pipe_sync_way;

import me.soliveirajr.piping.LinkedBlockingPipe;
import me.soliveirajr.piping.LinkedNonBlockingPipe;
import me.soliveirajr.piping.Pipe;

public class TwoThreads {
	
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
}
