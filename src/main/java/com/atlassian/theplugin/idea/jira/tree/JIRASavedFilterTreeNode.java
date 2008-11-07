package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class JIRASavedFilterTreeNode extends JIRAAbstractTreeNode {
	private static final Icon JIRA_FILTER_ICON = IconLoader.getIcon("/actions/showSource.png");
	private JIRASavedFilter savedFilter;
	private JIRAFilterListModel listModel;


	public JIRASavedFilterTreeNode(final JIRAFilterListModel listModel, final JIRASavedFilter savedFilter) {
		this.listModel = listModel;
		this.savedFilter = savedFilter;
	}

	public String toString() {
		return savedFilter.getName();
	}

	public JComponent getRenderer(final JComponent c, final boolean selected,
	                              final boolean expanded, final boolean hasFocus) {
		SimpleColoredComponent component = new SimpleColoredComponent();
		component.append(savedFilter.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

		component.setIcon(JIRA_FILTER_ICON);
		component.setOpaque(true);
		component.setBackground(selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground());
		component.setForeground(selected ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground());
		return component;
	}

	public void onSelect() {
		
		listModel.selectSavedFilter(((JIRAServerTreeNode) getParent()).getJiraServer(), savedFilter);
	}


}
