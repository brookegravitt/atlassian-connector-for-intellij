package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.ShowReviewedFileItemEvent;
import com.atlassian.theplugin.idea.ui.AtlassianTableView;
import com.atlassian.theplugin.idea.ui.UserTableContext;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 19, 2008
 * Time: 9:09:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class BackToRevisionCommentsAction extends TableSelectedAction {

	@Override
	public void actionPerformed(AnActionEvent event) {
		AtlassianTableView table = identifyTable(event);
		if (table != null) {
			UserTableContext context = table.getStateContext();

			IdeaHelper.getReviewActionEventBroker().trigger(
					new ShowReviewedFileItemEvent(
							CrucibleReviewActionListener.I_DONT_CARE,
							(ReviewData) CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.getValue(context),
							(CrucibleFileInfo) CrucibleConstants.CrucibleTableState.REVIEW_ITEM.getValue(context)));

		}
	}

	protected void itemSelected(Object row) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

}
