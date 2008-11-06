package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.idea.jira.JIRAIssueGroupBy;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.JIRAIssueListModel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.util.Map;

public class JIRAIssueTreeBuilder {

	private JIRAIssueGroupBy groupBy;
	private final JIRAIssueListModel issueModel;
	private DefaultTreeModel treeModel;

	private Map<String, String> projectKeysToNames;

	public void setProjectKeysToNames(final Map<String, String> projectKeysToNames) {
		this.projectKeysToNames = projectKeysToNames;
	}

	public JIRAIssueTreeBuilder(JIRAIssueGroupBy groupBy, JIRAIssueListModel model) {
		this.groupBy = groupBy;
		this.issueModel = model;
	}

	public void setGroupBy(JIRAIssueGroupBy groupBy) {
		this.groupBy = groupBy;
	}

	public void setProjectNamesMapping(Map<String, String> projectKeysToNames) {
		setProjectKeysToNames(projectKeysToNames);	
	}

	public void rebuild(JTree tree) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		reCreateTree(tree, root);
		for (JIRAIssue issue : issueModel.getIssues()) {
			getPlace(issue, root).add(new JIRAIssueTreeNode(issue));
		}
		treeModel.nodeStructureChanged(root);
	}

	private void reCreateTree(JTree tree, DefaultMutableTreeNode root) {
		tree.removeAll();
		treeModel = new DefaultTreeModel(root);
		tree.setModel(treeModel);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setRootVisible(false);
	}

	private DefaultMutableTreeNode getPlace(JIRAIssue issue, DefaultMutableTreeNode root) {
		String name;
		switch (groupBy) {
			case PRIORITY:
				name = issue.getPriority();
				break;
			case PROJECT:
				name = getProjectName(issue.getProjectKey());
				break;
			case STATUS:
				name = issue.getStatus();
				break;
			case TYPE:
				name = issue.getType();
				break;
			default:
				return root;
		}
		DefaultMutableTreeNode n = findGroupNode(root, name);
		if (n == null) {
			n = new DefaultMutableTreeNode(name);
			root.add(n);
		}
		return n;
	}

	private String getProjectName(String key) {
		if (projectKeysToNames == null || !projectKeysToNames.containsKey(key)) {
			// bummer
			return key;
		}
		return projectKeysToNames.get(key);
	}

	private DefaultMutableTreeNode findGroupNode(DefaultMutableTreeNode root, String name) {
		for (int i = 0; i < treeModel.getChildCount(root); ++i) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeModel.getChild(root, i);
			if (node.toString().equals(name)) {
				return node;
			}
		}
		return null;
	}
}
