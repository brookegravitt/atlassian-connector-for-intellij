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

package com.atlassian.theplugin.idea.action.bamboo.changes;

import com.atlassian.theplugin.idea.ui.tree.file.BambooFileNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;

public class GoToSourceAction extends AbstractBambooFileActions {


    @Override
    public void actionPerformed(AnActionEvent e) {
        Project currentProject = e.getData(DataKeys.PROJECT);
        BambooFileNode bfn = getBambooFileNode(e);
        if (bfn != null && currentProject != null) {
            jumpToSource(currentProject, bfn);
        }

    }


}
