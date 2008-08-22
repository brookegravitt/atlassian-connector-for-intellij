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

package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.TestResultsToolWindow;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;

public class ToggleAllTestsAction extends ToggleAction {
	@Override
	public boolean isSelected(AnActionEvent event) {
		TestResultsToolWindow window = IdeaHelper.getProjectComponent(event, TestResultsToolWindow.class);
		if (window == null) {
			return false;
		}

		TestResultsToolWindow.TestTree tree = window.getTestTree(event.getPlace());
		if (tree == null) {
			return !TestResultsToolWindow.TestTree.PASSED_TESTS_VISIBLE_DEFAULT;
		}
		return !tree.isPassedTestsVisible();
	}

	@Override
	public void setSelected(AnActionEvent event, boolean b) {
		TestResultsToolWindow window = IdeaHelper.getProjectComponent(event, TestResultsToolWindow.class);
		TestResultsToolWindow.TestTree tree = window.getTestTree(event.getPlace());
		tree.setPassedTestsVisible(!b);
	}
}
