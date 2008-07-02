package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ui.AtlassianTableView;
import com.atlassian.theplugin.idea.ui.UserTableContext;
import com.atlassian.theplugin.idea.crucible.events.FocusOnVersionedCommentEvent;
import com.atlassian.theplugin.idea.crucible.events.ShowReviewedFileItemEvent;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.tree.CrucibleTreeRootNode;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemDataNode;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 19, 2008
 * Time: 9:09:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class BackToRevisionCommentsAction extends TableSelectedAction implements CrucibleReviewActionListener {
	private AnActionEvent event;

	@Override
	public void actionPerformed(AnActionEvent event) {
		AtlassianTableView table = identifyTable(event);
		if (table != null) {
			UserTableContext context = table.getStateContext();

			IdeaHelper.getReviewActionEventBroker().trigger(
					new ShowReviewedFileItemEvent(
							this,
							(ReviewDataInfoAdapter) CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.getValue(context),
							(ReviewItem) CrucibleConstants.CrucibleTableState.REVIEW_ITEM.getValue(context)));

		}
	}

	protected void itemSelected(Object row) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnReview(ReviewDataInfoAdapter reviewDataInfoAdapter) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnFile(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnGeneralComment(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnGeneralCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnVersionedComment(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem, Collection<VersionedComment> versionedComments, VersionedComment versionedComment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnVersionedCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void showReview(ReviewDataInfoAdapter reviewDataInfoAdapter) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void showReviewedFileItem(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void showGeneralComment(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void showGeneralCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void showVersionedComment(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem, Collection<VersionedComment> versionedComments, VersionedComment versionedComment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void showVersionedCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
