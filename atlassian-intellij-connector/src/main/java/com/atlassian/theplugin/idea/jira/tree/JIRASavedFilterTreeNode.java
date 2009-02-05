package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class JIRASavedFilterTreeNode extends AbstractTreeNode {
	private JIRASavedFilter savedFilter;
	private JiraServerCfg jiraServerCfg;


	public JIRASavedFilterTreeNode(final JIRASavedFilter savedFilter, final JiraServerCfg jiraServerCfg) {
		super(savedFilter.getName(), null, null);
		this.savedFilter = savedFilter;
		this.jiraServerCfg = jiraServerCfg;
	}

	public String toString() {
		return savedFilter.getName();
	}

	public JComponent getRenderer(final JComponent c, final boolean selected,
			final boolean expanded, final boolean hasFocus) {


		return new JLabel("Incorrect renderer");
	}

	public void onSelect() {
//		onSelect (listModel != null && savedFilter != null) {
//			listModel.selectSavedFilter(((JIRAServerTreeNode) getParent()).getJiraServer(), savedFilter);
//		}
	}

	public JiraServerCfg getJiraServerCfg() {
		return jiraServerCfg;
	}

	public JIRASavedFilter getSavedFilter() {
		return savedFilter;
	}


}
