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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.atlassian.theplugin.idea.crucible.CrucibleRevisionReviewCreator;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;

public class PostCommitReviewAction extends Crucible16RepositoryAction {
    public void actionPerformed(AnActionEvent event) {
        final ChangeList[] changes = DataKeys.CHANGE_LISTS.getData(event.getDataContext());

        new Thread(new Runnable() {
            public void run() {
                        ApplicationManager.getApplication().invokeAndWait(
                        new CrucibleRevisionReviewCreator(CrucibleServerFacadeImpl.getInstance(), changes),
                        ModalityState.defaultModalityState());
            }
        }).start();
    }
}
