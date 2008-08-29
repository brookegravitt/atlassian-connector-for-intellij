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

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.ShowFileEvent;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class ShowFileAction extends ReviewTreeAction {

	@Override
	protected void executeTreeAction(final Project project, final AtlassianTreeWithToolbar tree) {
		ReviewActionData actionData = new ReviewActionData(tree);
		if (actionData.review != null && actionData.file != null) {
			IdeaHelper.getReviewActionEventBroker(project)
					.trigger(new ShowFileEvent(CrucibleReviewActionListener.ANONYMOUS, actionData.review, actionData.file));
		}
	}

	@Override
	public void update(final AnActionEvent e) {
		e.getPresentation().setEnabled(e.getData(Constants.CRUCIBLE_FILE_NODE_KEY) != null);
	}
}