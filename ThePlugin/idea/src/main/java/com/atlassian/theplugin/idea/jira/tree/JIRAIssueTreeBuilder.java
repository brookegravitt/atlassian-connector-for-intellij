package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.idea.BasicWideNodeTreeUI;
import com.atlassian.theplugin.idea.jira.CachedIconLoader;
import com.atlassian.theplugin.idea.jira.JIRAIssueGroupBy;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.JIRAIssueListModel;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Map;

public class JIRAIssueTreeBuilder {

	private JIRAIssueGroupBy groupBy;
	private final JIRAIssueListModel issueModel;
	private DefaultTreeModel treeModel;
	private static final TreeCellRenderer TREE_RENDERER = new JIRAIssueTreeRenderer();
	private JTree lastTree;

	private Map<String, String> projectKeysToNames;

	public JIRAIssueTreeBuilder(JIRAIssueGroupBy groupBy, JIRAIssueListModel model) {
		this.groupBy = groupBy;
		this.issueModel = model;
		lastTree = null;
	}

	public void setGroupBy(JIRAIssueGroupBy groupBy) {
		this.groupBy = groupBy;
	}

	public void setProjectKeysToNames(Map<String, String> projectKeysToNames) {
		this.projectKeysToNames = projectKeysToNames;
	}

	public synchronized void rebuild(JTree tree, JComponent treeParent) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		reCreateTree(tree, treeParent, root);
		for (JIRAIssue issue : issueModel.getIssues()) {
			getPlace(issue, root).add(new JIRAIssueTreeNode(issueModel, issue));
		}
		treeModel.nodeStructureChanged(root);
	}

	private void reCreateTree(final JTree tree, JComponent treeParent, DefaultMutableTreeNode root) {
		tree.removeAll();
		treeModel = new DefaultTreeModel(root);
		tree.setModel(treeModel);
		registerUI(tree);
		if (this.lastTree != tree) {
			this.lastTree = tree;
			tree.setShowsRootHandles(true);
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					final TreePath selectionPath = tree.getSelectionModel().getSelectionPath();
					if (selectionPath != null && selectionPath.getLastPathComponent() != null) {
						((JIRAAbstractTreeNode) selectionPath.getLastPathComponent()).onSelect();
					}
				}
			});
			initializeUI(tree, treeParent);
			tree.setRootVisible(false);
		}
	}

	//
	// voodoo magic below - makes the lastTree node as wide as the whole panel. Somehow. Like I said - it is magic.
	//

	public void initializeUI(final JTree tree, final JComponent treeParent) {
		registerUI(tree);
		treeParent.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (tree.isVisible()) {
					registerUI(tree);
				}
			}
		});
	}

	private void registerUI(JTree tree) {
		tree.setUI(new MyTreeUI());
	}

	private class MyTreeUI extends BasicWideNodeTreeUI {
		@Override
		protected TreeCellRenderer createDefaultCellRenderer() {
			return TREE_RENDERER;
		}
	}

	//
	// end of voodoo magic
	//

	private DefaultMutableTreeNode getPlace(JIRAIssue issue, DefaultMutableTreeNode root) {
		String name;
		String iconUrl = null;
		switch (groupBy) {
			case PRIORITY:
				name = issue.getPriority();
				iconUrl = issue.getPriorityIconUrl();
				break;
			case PROJECT:
				name = getProjectName(issue.getProjectKey());
				break;
			case STATUS:
				name = issue.getStatus();
				iconUrl = issue.getStatusTypeUrl();
				break;
			case TYPE:
				name = issue.getType();
				iconUrl = issue.getTypeIconUrl();
				break;
			default:
				return root;
		}
		if (name == null) {
			name = "None";
		}
		DefaultMutableTreeNode n = findGroupNode(root, name);
		if (n == null) {
			n = new JIRAIssueGroupTreeNode(issueModel, name, CachedIconLoader.getIcon(iconUrl));
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
			JIRAIssueGroupTreeNode node = (JIRAIssueGroupTreeNode) treeModel.getChild(root, i);
			if (node.toString().equals(name)) {
				return node;
			}
		}
		return null;
	}
}
