package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.CrucibleChangeSet;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.events.ShowReviewEvent;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;


public class GetCommentsAction extends TableSelectedAction  {

	protected void itemSelected(Object row) {
		IdeaHelper.getReviewActionEventBroker().trigger(new ShowReviewEvent(
				CrucibleReviewActionListener.I_DONT_CARE, (CrucibleChangeSet) row));
	}
}
