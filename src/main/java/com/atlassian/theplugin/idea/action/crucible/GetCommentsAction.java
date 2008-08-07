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


import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleTableToolWindowPanel;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.ShowReviewEvent;

import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;

public class GetCommentsAction extends TableSelectedAction {	

	public void actionPerformed(final AnActionEvent e) {
		 if (e != null && e.getPlace() != null && IdeaHelper.getCurrentProject(e) != null
                && e.getPlace().equals(CrucibleTableToolWindowPanel.PLACE_PREFIX + IdeaHelper.getCurrentProject(e).getName())) {
            if (!VcsIdeaHelper.isUnderVcsControl(e)) {
				JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), "You can use this action if VCS is enabled",
						"Action not available", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
			} else {
        		super.actionPerformed(e);
			}
        }

	}

	protected void itemSelected(Object row) {

		IdeaHelper.getReviewActionEventBroker().trigger(new ShowReviewEvent(
				CrucibleReviewActionListener.ANONYMOUS, (ReviewData) row));

	}	
}
