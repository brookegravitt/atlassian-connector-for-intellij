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

package com.atlassian.theplugin.idea.action.crucible.comment;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.*;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.Nullable;

public class AddGeneralCommentAction extends AbstractCommentAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		if (getReview(e) != null) {
			addGeneralComment(e, getReview(e));
		}
	}

	@Override
	public void update(AnActionEvent e) {
		boolean enabled = getReview(e) != null && checkIfAuthorized(getReview(e));

		e.getPresentation().setEnabled(enabled);
		if (e.getPlace().equals(CrucibleConstants.MENU_PLACE) || (e.getPlace().equals(ReviewItemTreePanel.MENU_PLACE))) {
			e.getPresentation().setVisible(enabled);
		}
	}

	@Nullable
	private ReviewAdapter getReview(final AnActionEvent e) {
		CrucibleToolWindow crucibleDetailsWindow = IdeaHelper.getProjectComponent(e, CrucibleToolWindow.class);
		if (crucibleDetailsWindow != null) {
			return crucibleDetailsWindow.getReview();
		}

		return null;
	}

	private boolean checkIfAuthorized(final ReviewAdapter review) {
		if (review == null) {
			return false;
		}
		if (!review.getActions().contains(CrucibleAction.COMMENT)) {
			return false;
		}
		return true;
	}

	private void addGeneralComment(AnActionEvent event, final ReviewAdapter review) {
		final GeneralCommentBean newComment = new GeneralCommentBean();

        event.getPresentation().putClientProperty(CommentTooltipPanel.JBPOPUP_PARENT_COMPONENT, getTree(event));
        CommentTooltipPanel.showCommentTooltipPopup(
                event,
                new CommentTooltipPanelWithRunners(event, review, null, newComment, null, CommentTooltipPanel.Mode.ADD),
                null, null);
	}
}