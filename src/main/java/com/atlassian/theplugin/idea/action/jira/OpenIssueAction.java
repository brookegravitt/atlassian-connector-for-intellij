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

package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.jira.editor.vfs.MemoryVirtualFile;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;

public class OpenIssueAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
		JIRAIssue issue = IdeaHelper.getJIRAToolWindowPanel(event).getCurrentIssue();
        String fileName = issue.getKey();
        FileEditorManager manager =
                FileEditorManager.getInstance(DataKeys.PROJECT.getData(event.getDataContext()));
        VirtualFile[] files = manager.getOpenFiles();
        VirtualFile vf = null;
        for (VirtualFile f : files) {
            if (f.getName().equals(fileName) && (f instanceof MemoryVirtualFile)) {
                vf = f;
                break;
            }
        }

		if (vf == null) {
            vf = new MemoryVirtualFile(fileName);
        }
		// either opens a new editor, or focuses the already open one
		manager.openFile(vf, true);
    }
}