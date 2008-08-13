/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.idea.crucible.events.CrucibleEvent;
import com.atlassian.theplugin.util.PluginUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 17, 2008
 * Time: 10:53:01 AM
 * To change this template use File | Settings | File Templates.
 */
public final class ReviewActionEventBroker {
	private Set<CrucibleReviewActionListener> listeners =
			new HashSet<CrucibleReviewActionListener>();
	private Queue<CrucibleEvent> events = new LinkedBlockingQueue<CrucibleEvent>();
	public static final Logger LOGGER = PluginUtil.getLogger();

	public ReviewActionEventBroker() {
        new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						CrucibleEvent event = ((LinkedBlockingQueue<CrucibleEvent>) events).take();
						event.run(ReviewActionEventBroker.this);
					}
				} catch (InterruptedException e) {
					//swallowed
				}
			}
		}, "atlassian-idea-plugin Crucible events processor"
		).start();
	}

	public void registerListener(CrucibleReviewActionListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void unregisterListener(CrucibleReviewActionListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	public void trigger(CrucibleEvent event) {
		events.add(event);
	}

	public Iterable<? extends CrucibleReviewActionListener> getListeners() {
		synchronized (listeners) {
			return Collections.unmodifiableSet(new HashSet<CrucibleReviewActionListener>(listeners));
		}
	}
}
