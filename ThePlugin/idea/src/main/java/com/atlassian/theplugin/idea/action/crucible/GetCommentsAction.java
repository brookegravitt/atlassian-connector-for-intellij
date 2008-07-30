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
import com.atlassian.theplugin.idea.crucible.events.ShowReviewEvent;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.CrucibleTableToolWindowPanel;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class GetCommentsAction extends TableSelectedAction  {    
    public GetCommentsAction(){
		ActionManager actionManager = ActionManager.getInstance();
	}

	protected void itemSelected(Object row) {
        IdeaHelper.getReviewActionEventBroker().trigger(new ShowReviewEvent(
				CrucibleReviewActionListener.ANONYMOUS, (ReviewData) row));
	}

	public void update(AnActionEvent e) {
		super.update(e);
        if (e != null && e.getPlace() != null && IdeaHelper.getCurrentProject(e) != null
                && e.getPlace().equals(CrucibleTableToolWindowPanel.PLACE_PREFIX + IdeaHelper.getCurrentProject(e).getName())) {
            if (!VcsIdeaHelper.isUnderVcsControl(e)) {
                getTemplatePresentation().setEnabled(false);
                e.getPresentation().setEnabled(false);
            } else {
                getTemplatePresentation().setEnabled(true);
                e.getPresentation().setEnabled(true);
            }
        }
    }
}
