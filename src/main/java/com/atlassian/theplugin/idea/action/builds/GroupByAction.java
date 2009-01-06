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
package com.atlassian.theplugin.idea.action.builds;

import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.BuildGroupBy;
import com.atlassian.theplugin.idea.bamboo.BuildsToolWindowPanel;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Jacek Jaroczynski
 */
// todo add client property handling to all actions
public class GroupByAction extends AnAction implements CustomComponentAction {
	@Override
	public void actionPerformed(final AnActionEvent e) {
	}

	public JComponent createCustomComponent(Presentation presentation) {
		final JComboBox combo = new JComboBox(createModel());
//		presentation.putClientProperty(COMBOBOX_KEY, combo);
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final Project currentProject = IdeaHelper.getCurrentProject(DataManager.getInstance().getDataContext(combo));
				if (currentProject != null) {
					BuildsToolWindowPanel panel = IdeaHelper.getBuildsToolWindowPanel(currentProject);
					if (panel != null) {
						panel.setGroupBy((BuildGroupBy) combo.getSelectedItem());
					} else {
						LoggerImpl.getInstance().error(GroupByAction.class.getName() + ": cannot find "
								+ BuildsToolWindowPanel.class);
					}
				} else {
					LoggerImpl.getInstance().error(GroupByAction.class.getName() + ": cannot determine current project");
				}

			}
		});
		return combo;
}

	private ComboBoxModel createModel() {
		return new DefaultComboBoxModel(BuildGroupBy.values());
	}

	}
