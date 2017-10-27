/*
 * Copyright 2014-2016 Media for Mobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.idealsee.sdk.server;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ISARHttpRequestQueue implements Iterable<ISARHttpRequest> {
    protected ConcurrentLinkedQueue<ISARHttpRequest> queue = new ConcurrentLinkedQueue<ISARHttpRequest>();

    public ISARHttpRequestQueue() {
    }

    public ISARHttpRequestQueue(ISARHttpRequestQueue commandQueue) {
        for (ISARHttpRequest httpRequest : commandQueue) {
            queue(httpRequest);
        }
    }

    public void queue(ISARHttpRequest httpRequest) {
        queue.add(httpRequest);
    }

    public ISARHttpRequest dequeue() {
        return queue.poll();
    }

    @Override
    public Iterator<ISARHttpRequest> iterator() {
        return queue.iterator();
    }

    public ISARHttpRequest first() {
        if (queue.isEmpty()) {
            return null;
        }

        return queue.peek();
    }

    public boolean contains(ISARHttpRequest request) {
        if (queue.isEmpty()) return false;

        return queue.contains(request);
    }

    public boolean containsByARCommand(ISARCommand command) {
        if (queue.isEmpty()) return false;
        for (ISARHttpRequest request : queue) {
            if (ISARCommand.START_AR_SEARCH == request.getARCommand()) {
                return true;
            }
        }
        return false;
    }

    public boolean remove(ISARHttpRequest request) {
        boolean result = false;
        if (queue.isEmpty()) return false;
        if (queue.contains(request)) {
            result = queue.remove(request);
        }
        return result;
    }

    /**
     * 根据ARCommand删除列表中所有的request，返回删除的个数，如果没有删除，则返回0.
     *
     * @param command
     * @return
     */
    public int removeAllByARCommand(ISARCommand command) {
        int count = 0;
        for (ISARHttpRequest request : queue) {
            if (ISARCommand.START_AR_SEARCH == request.getARCommand()) {
                queue.remove(request);
                count++;
            }
        }
        return count;
    }

    public void clear() {
        queue.clear();
    }

    public int size() {
        return queue.size();
    }

    public enum ISARCommand {
        START_AR_SEARCH,
        ISARCommand, STOP_AR_SEARCH
    }

}
