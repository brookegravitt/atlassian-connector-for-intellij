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

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Deprecated
public class FilterTypeAction extends ToggleAction implements CustomComponentAction {
	private static final String MY_CHECKBOX_DIRTY_HACK = "my-checkbox-dirty-hack";

	public FilterTypeAction() {
		getTemplatePresentation().setText("Use Server Filters");
	}

	@Override
	public void update(final AnActionEvent event) {
		final JIRAToolWindowPanel toolWindowPanel = IdeaHelper.getJIRAToolWindowPanel(event);
		event.getPresentation().setEnabled(toolWindowPanel != null);
		update2(event);
	}

	@Override
	public boolean isSelected(AnActionEvent event) {
		final JIRAToolWindowPanel toolWindowPanel = IdeaHelper.getJIRAToolWindowPanel(event);
		return toolWindowPanel != null && toolWindowPanel.getFilters().getSavedFilterUsed();
	}

	@Override
	public void setSelected(AnActionEvent event, boolean b) {
		JIRAToolWindowPanel toolWindowPanel = IdeaHelper.getJIRAToolWindowPanel(event);
		if (toolWindowPanel != null) {
			toolWindowPanel.getFilters().setSavedFilterUsed(b);
			toolWindowPanel.refreshIssues();
		}
	}

	public JComponent createCustomComponent(Presentation presentation) {
		final JCheckBox myCheckBox = new JCheckBox(presentation.getText());
		myCheckBox.setToolTipText(presentation.getDescription());
		myCheckBox.setMnemonic(presentation.getMnemonic());
		myCheckBox.setDisplayedMnemonicIndex(presentation.getDisplayedMnemonicIndex());
		presentation.putClientProperty(MY_CHECKBOX_DIRTY_HACK, myCheckBox);

		myCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FilterTypeAction.this
						.actionPerformed(new AnActionEvent(null, DataManager.getInstance().getDataContext(myCheckBox),
								ActionPlaces.UNKNOWN, FilterTypeAction.this.getTemplatePresentation(),
								ActionManager.getInstance(), 0));
			}
		});

		return myCheckBox;
	}

	public void update2(final AnActionEvent e) {
		JCheckBox myCheckBox = (JCheckBox) e.getPresentation().getClientProperty(MY_CHECKBOX_DIRTY_HACK);
		if (myCheckBox != null) {
			myCheckBox.setSelected(isSelected(e));
			myCheckBox.updateUI();
		}
	}
}
