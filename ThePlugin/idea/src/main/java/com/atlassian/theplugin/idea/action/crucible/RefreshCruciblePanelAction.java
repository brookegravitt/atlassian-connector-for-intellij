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
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusChecker;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.commons.util.Logger;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class RefreshCruciblePanelAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
        ThePluginProjectComponent projectComponent = IdeaHelper.getCurrentProjectComponent(e);
        if (projectComponent != null) {
			CrucibleStatusChecker checker = projectComponent.getCrucibleStatusChecker();
            IdeaHelper.getCrucibleToolWindowPanel(e).refreshReviews(checker);
        }
    }
}
