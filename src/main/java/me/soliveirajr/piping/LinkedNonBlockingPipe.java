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
package me.soliveirajr.piping;

import java.util.concurrent.ConcurrentLinkedQueue;

import me.soliveirajr.piping.util.Builder;

/**
 * A pipe that never blocks. If there isn't a message to be received, the receive method returns false.
 * And if there isn't space to send a message the dispatch method returns false.
 */
public class LinkedNonBlockingPipe<E extends Transferable<E>> implements Pipe<E> {

    private final ConcurrentLinkedQueue<E> queue;
    private final ConcurrentLinkedQueue<E> pool;
    
    public LinkedNonBlockingPipe(int capacity, Class<E> transferable) {
        this.queue = new ConcurrentLinkedQueue<E>();
        this.pool = new ConcurrentLinkedQueue<E>();
        init(capacity, transferable);
    }
    
    public LinkedNonBlockingPipe(int capacity, Builder<E> transferable) {
        this.queue = new ConcurrentLinkedQueue<E>();
        this.pool = new ConcurrentLinkedQueue<E>();
        init(capacity, transferable);
    }
    
    private void init(int capacity, Class<E> transferable) {
        try {
            for(int i = 0; i < capacity; i++) {
                pool.add(transferable.newInstance());
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void init(int capacity, Builder<E> transferable) {
        try {
            for(int i = 0; i < capacity; i++) {
                pool.add(transferable.newInstance());
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public boolean dispatch(E message) {
        E toDispatch;
        if ((toDispatch = pool.poll()) == null) return false;
        message.transferTo(toDispatch);
        queue.add(toDispatch); // add to tail...
        return true;
    }

    @Override
    public boolean receive(E message) {
        E toReceive;
        if ((toReceive = queue.poll()) == null) return false;
        toReceive.transferTo(message);
        pool.add(toReceive); // add to tail...
        return true;
    }
}