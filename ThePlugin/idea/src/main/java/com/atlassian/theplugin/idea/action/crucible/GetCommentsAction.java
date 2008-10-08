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

package com.atlassian.theplugin.idea.action.crucible;


import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.idea.CrucibleReviewWindow;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.CrucibleTableToolWindowPanel;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.ShowReviewEvent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class GetCommentsAction extends TableSelectedAction {

	@Override
	public void actionPerformed(final AnActionEvent e) {
		Project project = IdeaHelper.getCurrentProject(e);


		 if (project != null && e.getPlace().equals(CrucibleTableToolWindowPanel.PLACE_PREFIX + project.getName())) {
            if (!VcsIdeaHelper.isUnderVcsControl(e)) {
				Messages.showInfoMessage(project, CrucibleConstants.CRUCIBLE_MESSAGE_NOT_UNDER_VCS,
							CrucibleConstants.CRUCIBLE_TITLE_NOT_UNDER_VCS);

			} else {
				CrucibleReviewWindow.getInstance(project);
				super.actionPerformed(e);
			}
        }

	}

	@Override
	protected void itemSelected(final Project project, Object row) {
		CrucibleReviewWindow.getInstance(project).showCrucibleReviewWindow(((Review) row));

		IdeaHelper.getReviewActionEventBroker(project).trigger(new ShowReviewEvent(
				CrucibleReviewActionListener.ANONYMOUS, (Review) row));

	}	
}
