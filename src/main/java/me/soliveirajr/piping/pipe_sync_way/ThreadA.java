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