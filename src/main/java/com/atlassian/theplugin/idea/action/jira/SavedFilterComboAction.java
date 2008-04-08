package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Iterator;

public class SavedFilterComboAction extends ComboBoxAction {
	public static final String QF_NAME = "SavedFilter";
	private static final String NO_SAVED_FILTER_TEXT = "Select saved filter";
	private ComboBoxButton button = null;

	@NotNull
	protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {
		if (!(jComponent instanceof ComboBoxButton)) {
			throw new UnsupportedOperationException("This action can only be used as a combobox");
		}

		DefaultActionGroup group = new DefaultActionGroup();
		button = (ComboBoxButton) jComponent;

		JIRAServer server = IdeaHelper.getCurrentJIRAServer();
		for (Iterator iterator = server.getSavedFilters().iterator(); iterator.hasNext();) {
			final JIRASavedFilter filter = (JIRASavedFilter) iterator.next();
			group.add(new AnAction(filter.getName()) {
				public void actionPerformed(AnActionEvent event) {
					button.setText(event.getPresentation().getText());
					IdeaHelper.getJIRAToolWindowPanel(event).addQueryFragment(filter);
					IdeaHelper.getJIRAToolWindowPanel(event).refreshIssues();
				}
			});
		}

		return group;
	}

	public void update(AnActionEvent event) {
		super.update(event);
		if (IdeaHelper.getJIRAToolWindowPanel(event) != null) {
			if (!IdeaHelper.getJIRAToolWindowPanel(event).getFilters().getSavedFilterUsed()) {
				setComboText(event, NO_SAVED_FILTER_TEXT, false);
			} else {
				if (IdeaHelper.getJIRAToolWindowPanel(event).getFilters().getSavedFilter() != null) {
					setComboText(event,
							IdeaHelper.getJIRAToolWindowPanel(event).
									getFilters().getSavedFilter().getFilterEntry().get("name"), true);
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