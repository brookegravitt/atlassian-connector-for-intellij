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

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Action;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleChangeStateWorker;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

@Deprecated
public abstract class AbstractTransitionReviewAction extends AnAction {
    protected abstract Action getRequestedTransition();

    private ReviewAdapter rd;

    @Override
	public void actionPerformed(final AnActionEvent event) {
		new CrucibleChangeStateWorker(IdeaHelper.getCurrentProject(event), rd, getRequestedTransition()).run();
    }

    @Override
	public void update(AnActionEvent event) {
        super.update(event);
        if (IdeaHelper.getCrucibleToolWindowPanel(event) != null) {

            if (IdeaHelper.getCrucibleToolWindowPanel(event).getSelectedReview() == null) {
                event.getPresentation().setEnabled(false);
            } else {
                rd = IdeaHelper.getCrucibleToolWindowPanel(event).getSelectedReview();
                try {
                    if (rd.getTransitions().isEmpty()) {
                        event.getPresentation().setEnabled(false);
                        event.getPresentation().setVisible(false);
                    } else {
                        for (Action transition : rd.getTransitions()) {
                            if (transition.equals(getRequestedTransition())) {
                                event.getPresentation().setEnabled(true);
                                event.getPresentation().setVisible(true);
                                break;
                            } else {
                                event.getPresentation().setEnabled(false);
                                event.getPresentation().setVisible(false);
                            }
                        }
                    }
                } catch (ValueNotYetInitialized valueNotYetInitialized) {
					LoggerImpl.getInstance().error(valueNotYetInitialized);
                }
            }
        } else {
            event.getPresentation().setEnabled(false);
            event.getPresentation().setVisible(false);
        }
    }
}