package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Aug 1, 2008
 * Time: 4:21:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShowDiffEvent extends CrucibleEvent {
	private CrucibleFileInfo file;

	public ShowDiffEvent(final CrucibleReviewActionListener caller, final CrucibleFileInfo file) {
		super(caller);
		this.file = file;

	}

	protected void notify(final CrucibleReviewActionListener listener) {
		listener.showDiff(file);
	}
}
