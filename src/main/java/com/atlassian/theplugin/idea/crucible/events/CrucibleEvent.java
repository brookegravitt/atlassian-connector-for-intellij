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

package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.comments.ReviewActionEventBroker;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 17, 2008
 * Time: 8:39:49 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class CrucibleEvent {
	protected CrucibleReviewActionListener caller;

	protected CrucibleEvent(CrucibleReviewActionListener caller) {
		this.caller = caller;
	}

	protected abstract void notify(CrucibleReviewActionListener listener);

	public void run(ReviewActionEventBroker broker) {
		for (CrucibleReviewActionListener listener : broker.getListeners()) {
			if (listener == caller) {
				continue;
			}
			notify(listener);
		}
	}
}
