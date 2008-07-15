package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.CrucibleChangeSet;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 18, 2008
 * Time: 10:48:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShowGeneralCommentEvent extends CrucibleEvent {
	private CrucibleChangeSet crucibleChangeSet;
	private GeneralComment comment;

	public ShowGeneralCommentEvent(CrucibleReviewActionListener caller,
            CrucibleChangeSet crucibleChangeSet, GeneralComment generalComment) {
		super(caller);
		this.crucibleChangeSet = crucibleChangeSet;
		this.comment = generalComment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.showGeneralComment(crucibleChangeSet, comment);
	}
}
