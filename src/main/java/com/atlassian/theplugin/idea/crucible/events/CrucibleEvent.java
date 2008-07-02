package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.comments.ReviewActionEventBroker;
import com.atlassian.theplugin.idea.IdeaHelper;

/**
 * Created by IntelliJ IDEA.
* User: lguminski
* Date: Jun 17, 2008
* Time: 8:39:49 PM
* To change this template use File | Settings | File Templates.
*/
public abstract class CrucibleEvent implements Runnable {
	protected CrucibleReviewActionListener caller;

	protected CrucibleEvent(CrucibleReviewActionListener caller) {
		this.caller = caller;
	}

	protected abstract void notify(CrucibleReviewActionListener listener);

	public void run() {
		ReviewActionEventBroker broker = IdeaHelper.getReviewActionEventBroker();
		for (CrucibleReviewActionListener listener : broker.getListeners()) {
			if (listener == caller) {
				continue;
			}
			notify(listener);
		}
	}
}
