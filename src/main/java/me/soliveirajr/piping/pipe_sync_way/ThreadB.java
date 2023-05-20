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

import me.soliveirajr.piping.Pipe;

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