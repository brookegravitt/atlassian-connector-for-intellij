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

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.config.GenericComboBoxItemWrapper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FilterByAction extends AbstractBambooComboBoxAction {
	
	public FilterByAction() {
		super("Filter By");
	}

	private static class BambooFilterTypeWrapper extends GenericComboBoxItemWrapper<BambooFilterType> {
		public BambooFilterTypeWrapper(final BambooFilterType wrapped) {
			super(wrapped);
		}

		@Override
		public String toString() {
			if (wrapped != null) {
				return wrapped.getName();
			} else {
				return "None";
			}
		}
	}


	public static final BambooFilterTypeWrapper NONE = new BambooFilterTypeWrapper(null);
	@Override
	protected void execute(@NotNull final BambooToolWindowPanel panel, @Nullable final Object selectedItem) {
		if (selectedItem instanceof BambooFilterTypeWrapper) {
			BambooFilterTypeWrapper bambooFilterWrapper = (BambooFilterTypeWrapper) selectedItem;
			panel.setBambooFilterType(bambooFilterWrapper.getWrapped());
		}
	}

	@Override
	public void update(final AnActionEvent e) {
		Project p = IdeaHelper.getCurrentProject(e);
		final Object clientProperty = e.getPresentation().getClientProperty(getComboKey());
		if (clientProperty instanceof JComboBox) {
			final JComboBox jComboBox = (JComboBox) clientProperty;
			updateSelection(p, jComboBox);
		}
	}

	@Override
	protected DefaultComboBoxModel createComboBoxModel() {
		final BambooFilterTypeWrapper[] model = new BambooFilterTypeWrapper[BambooFilterType.values().length + 1];
		model[0] = NONE;
		for (int i = 0; i < BambooFilterType.values().length; i++) {
			BambooFilterType bambooFilterType = BambooFilterType.values()[i];
			model[i + 1] = new BambooFilterTypeWrapper(bambooFilterType);
		}
		return new DefaultComboBoxModel(model);
	}

	protected void updateSelection(Project project, JComboBox combo) {
		final BambooToolWindowPanel panel = IdeaHelper.getProjectComponent(project, BambooToolWindowPanel.class);
		if (panel == null) {
			return;
		}
		if (((BambooFilterTypeWrapper) combo.getSelectedItem()).getWrapped() != panel.getBambooFilterType()) {
			combo.setSelectedItem(new BambooFilterTypeWrapper(panel.getBambooFilterType()));
		}
	}
}
