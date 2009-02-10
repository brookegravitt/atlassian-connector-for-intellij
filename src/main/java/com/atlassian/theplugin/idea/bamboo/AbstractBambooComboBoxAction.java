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
package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ui.ComboWithLabel;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class AbstractBambooComboBoxAction extends AnAction implements CustomComponentAction {
	private final String comboLabel;

	protected AbstractBambooComboBoxAction(String comboLabel) {
		super();
		this.comboLabel = comboLabel;
	}

	protected String getComboKey() {
		return getClass().getName() + ".combo";
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
	}

	protected abstract DefaultComboBoxModel createComboBoxModel();

	protected abstract void execute(@NotNull final BambooToolWindowPanel panel, @Nullable final Object selectedItem);

	protected abstract void updateSelection(Project project, JComboBox combo);

	public JComponent createCustomComponent(Presentation presentation) {
		final JComboBox combo = new JComboBox(createComboBoxModel());
		ComboWithLabel cwl = new ComboWithLabel(combo, comboLabel);

		Project project = IdeaHelper.getCurrentProject(DataManager.getInstance().getDataContext());
		updateSelection(project, combo);

		presentation.putClientProperty(getComboKey(), combo);
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final Project currentProject = IdeaHelper.getCurrentProject(DataManager.getInstance().getDataContext(combo));
				if (currentProject != null) {
					final BambooToolWindowPanel panel = IdeaHelper
							.getProjectComponent(currentProject, BambooToolWindowPanel.class);
					if (panel != null) {
						execute(panel, combo.getSelectedItem());
					} else {
						LoggerImpl.getInstance().error(getClass().getName() + ": cannot find "
								+ BambooToolWindowPanel.class);
					}
				} else {
					LoggerImpl.getInstance().error(getClass().getName() + ": cannot determine current project");
				}

			}
		});
		return cwl;
	}
}
