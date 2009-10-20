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

package com.atlassian.theplugin.idea.action.tree.file;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewDetailsToolWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Aug 5, 2008
 * Time: 4:11:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class ToggleFilterFilesAction extends AnAction {
	static final Icon FILTER_ON_ICON = IconLoader.getIcon("/actions/unselectall.png");
	static final Icon FILTER_OFF_ICON = IconLoader.getIcon("/actions/selectall.png");
	static final String TEXT_FILTER_ON = "Show Files with comments only";
	static final String TEXT_FILTER_OFF = "Show all files";

	public ToggleFilterFilesAction() {
		getTemplatePresentation().setIcon(FILTER_ON_ICON);
		getTemplatePresentation().setText(TEXT_FILTER_ON);
	}

	@Override
	public void actionPerformed(final AnActionEvent e) {

		ReviewDetailsToolWindow window = IdeaHelper.getReviewDetailsToolWindow(e);

		if (window != null) {
			window.switchFilter();
			switchIcons(e.getPresentation());
		}
	}

	private void switchIcons(final Presentation presentation) {
		if (presentation.getIcon().equals(FILTER_ON_ICON)) {
			presentation.setIcon(FILTER_OFF_ICON);
			presentation.setText(TEXT_FILTER_OFF);
		} else {
			presentation.setIcon(FILTER_ON_ICON);
			presentation.setText(TEXT_FILTER_ON);
		}
	}
}
