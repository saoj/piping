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

import me.soliveirajr.piping.Transferable;

public class OpMessage implements Transferable<OpMessage> {

	public static enum Op { ADD, SUB }
	
	public Op op;
	public long value;
	
	@Override
	public void transferTo(OpMessage dest) {
		dest.op = this.op;
		dest.value = this.value;
	}
}