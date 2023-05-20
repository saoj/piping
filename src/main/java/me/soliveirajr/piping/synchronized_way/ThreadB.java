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
package me.soliveirajr.piping.synchronized_way;

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