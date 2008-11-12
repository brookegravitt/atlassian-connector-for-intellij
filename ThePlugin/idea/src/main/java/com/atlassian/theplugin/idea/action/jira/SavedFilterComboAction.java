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
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.util.Iterator;
import java.util.List;

@Deprecated
public class SavedFilterComboAction extends ComboBoxAction {
	public static final String QF_NAME = "SavedFilter";
	private static final String SELECT_SAVED_FILTER_TEXT = "Select Saved Filter";
	private static final String NO_SAVED_FILTER_AVAILABLE_TEXT = "No Saved Filters";
	private ComboBoxButton button = null;
	private Project project;

	private boolean haveSavedFilters = true;

	public SavedFilterComboAction(Project project) {
		this.project = project;
	}
	
	protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {
		if (!(jComponent instanceof ComboBoxButton)) {
			throw new UnsupportedOperationException("This action can only be used as a combobox");
		}

		DefaultActionGroup group = new DefaultActionGroup();
		button = (ComboBoxButton) jComponent;

		JIRAServer server = IdeaHelper.getCurrentJIRAServer(project);
		if (server != null) {
			List filters = server.getSavedFilters();
			if (filters != null) {
				for (Iterator iterator = filters.iterator(); iterator.hasNext();) {
					if (!haveSavedFilters) {
						haveSavedFilters = true;
					}
					final JIRASavedFilter filter = (JIRASavedFilter) iterator.next();
					group.add(new AnAction(filter.getName()) {
						public void actionPerformed(AnActionEvent event) {
							button.setText(event.getPresentation().getText());
							IdeaHelper.getJIRAToolWindowPanel(event).addQueryFragment(filter);
							IdeaHelper.getJIRAToolWindowPanel(event).refreshIssues();
						}
					});
				}
			}
			if (filters == null || filters.size() == 0) {
				haveSavedFilters = false;
				button.setText(SELECT_SAVED_FILTER_TEXT);
				IdeaHelper.getJIRAToolWindowPanel(project).removeSavedFilter();
				IdeaHelper.getJIRAToolWindowPanel(project).refreshIssues();
			}
		}
		return group;
	}

	public void update(AnActionEvent event) {
		super.update(event);
		if (IdeaHelper.getJIRAToolWindowPanel(event) != null) {
			if (!IdeaHelper.getJIRAToolWindowPanel(event).getFilters().getSavedFilterUsed()) {
				setComboText(event, SELECT_SAVED_FILTER_TEXT, false);
			} else {
				if (IdeaHelper.getJIRAToolWindowPanel(event).getFilters().getSavedFilter() != null) {
					setComboText(event,
							IdeaHelper.getJIRAToolWindowPanel(event).
									getFilters().getSavedFilter().getFilterEntry().get("name"), true);
				} else {
					setComboText(event, haveSavedFilters
							? SELECT_SAVED_FILTER_TEXT : NO_SAVED_FILTER_AVAILABLE_TEXT, true);
				}
			}
		}
	}

	private void setComboText(AnActionEvent event, String label, boolean enabled) {
		if (button != null) {
			button.setText(label);
			button.setEnabled(enabled);
		} else {
			event.getPresentation().setText(label);
			event.getPresentation().setEnabled(enabled);
		}
	}
}