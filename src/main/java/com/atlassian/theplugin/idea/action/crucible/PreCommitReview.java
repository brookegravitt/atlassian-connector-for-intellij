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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.project.Project;
import com.atlassian.theplugin.idea.crucible.CruciblePatchSubmitCommitSession;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;

public class PreCommitReview extends AnAction {

    public void actionPerformed(AnActionEvent event) {
        final ChangeList[] changes = DataKeys.CHANGE_LISTS.getData(event.getDataContext());
        final Project project = DataKeys.PROJECT.getData(event.getDataContext());

        new Thread(new Runnable() {
            public void run() {
                new CruciblePatchSubmitCommitSession(project, CrucibleServerFacadeImpl.getInstance()).execute(
                        changes[0].getChanges(), changes[0].getName());
            }
        }).start();
    }

    public void update(AnActionEvent event) {
        super.update(event);
        final ChangeList[] changes = DataKeys.CHANGE_LISTS.getData(event.getDataContext());
        if (changes != null && changes.length == 1) {
            event.getPresentation().setEnabled(true);
        } else {
            event.getPresentation().setEnabled(false);
        }
    }
}

