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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;

public class FilterByAction extends AbstractBambooComboBoxAction {
	@Override
	protected void execute(@NotNull final BambooToolWindowPanel panel, @Nullable final Object selectedItem) {
		if (selectedItem instanceof BambooFilterType) {
			BambooFilterType bambooFilter = (BambooFilterType) selectedItem;
			panel.setBambooFilterType(bambooFilter);
		}
	}

	@Override
	public void update(final AnActionEvent e) {
		final BambooToolWindowPanel panel = IdeaHelper.getProjectComponent(e, BambooToolWindowPanel.class);
		if (panel == null) {
			return;
		}
		final Object clientProperty = e.getPresentation().getClientProperty(getComboKey());
		if (clientProperty instanceof JComboBox) {
			JComboBox jComboBox = (JComboBox) clientProperty;
			if (jComboBox.getSelectedItem() != panel.getBambooFilterType()) {
				jComboBox.setSelectedItem(panel.getBambooFilterType());
			}
		}
	}

	@Override
	protected DefaultComboBoxModel createComboBoxModel() {
		return new DefaultComboBoxModel(BambooFilterType.values());
	}

}
