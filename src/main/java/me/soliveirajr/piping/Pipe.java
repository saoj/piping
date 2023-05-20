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

/**
 * A pipe that works kind of like a socket to send and receive messages 
 *
 * @param <E> the message you want to send
 */
public interface Pipe<E extends Transferable<E>> {
	
    /**
     * Dispatch a message through the pipe
     * 
     * @param message the message to dispatch
     * @return true if the message was able to be sent or false if the pipe is full
     */
	public boolean dispatch(E message);
	
    /**
     * Receive a message through the pipe
     * 
     * @param message the message to receive
     * @return true if the message was received or false if the pipe is empty
     */
	public boolean receive(E message);
}