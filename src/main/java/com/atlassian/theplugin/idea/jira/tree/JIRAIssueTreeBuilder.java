package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.idea.BasicWideNodeTreeUI;
import com.atlassian.theplugin.idea.jira.CachedIconLoader;
import com.atlassian.theplugin.idea.jira.JiraIssueGroupBy;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.FrozenModel;
import com.atlassian.theplugin.jira.model.FrozenModelListener;
import com.atlassian.theplugin.jira.model.JIRAIssueListModel;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelListener;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

public class JIRAIssueTreeBuilder {

	private JiraIssueGroupBy groupBy;
	private final JIRAIssueListModel issueModel;
	private DefaultTreeModel treeModel;
	private static final TreeCellRenderer TREE_RENDERER = new JIRAIssueTreeRenderer();
	private JTree lastTree;
	private boolean isGroupSubtasksUnderParent;

	public enum UpdateGroup {
		UPDATED_TODAY("Today"),
		UPDATED_YESTERDAY("Yesterday"),
		UPDATED_TWO_DAYS_AGO("2 Days Ago"),
		UPDATED_THIS_WEEK("This Week"),
		UPDATED_LAST_WEEK("Last Week"),
		UPDATED_THIS_MONTH("This Month"),
		UPDATED_LAST_MONTH("Last Month"),
		UPDATED_EARLIER("Older"),
		UPDATED_INVALID("Invalid or Unknown");

		private String name;

		UpdateGroup(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private Map<String, String> projectKeysToNames;

	public JIRAIssueTreeBuilder(JiraIssueGroupBy groupBy, boolean groupSubtasksUnderParent, JIRAIssueListModel model) {
		this.groupBy = groupBy;
		isGroupSubtasksUnderParent = groupSubtasksUnderParent;
		this.issueModel = model;
		lastTree = null;

		issueModel.addModelListener(new JIRAIssueListModelListener() {

			public void modelChanged(JIRAIssueListModel aModel) {
			}

			public void issuesLoaded(JIRAIssueListModel aModel, int loadedIssues) {
			}

		});

		issueModel.addFrozenModelListener(new FrozenModelListener() {
			public void modelFrozen(FrozenModel aModel, boolean frozen) {
				if (lastTree != null) {
					lastTree.setEnabled(!frozen);
				}
			}
		});

	}

	public void setGroupBy(JiraIssueGroupBy groupBy) {
		this.groupBy = groupBy;
	}

	public void setProjectKeysToNames(Map<String, String> projectKeysToNames) {
		this.projectKeysToNames = projectKeysToNames;
	}

	public synchronized void rebuild(JTree tree, JComponent treeParent) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		if (groupBy == JiraIssueGroupBy.LAST_UPDATED) {
			createUpdateGroups(root);
		}
		reCreateTree(tree, treeParent, root);
		if (isGroupSubtasksUnderParent) {
			for (JIRAIssue issue : issueModel.getIssuesNoSubtasks()) {
				JIRAIssueTreeNode node = new JIRAIssueTreeNode(issueModel, issue);
				getPlace(issue, root).add(node);
				if (!issue.getSubTaskKeys().isEmpty()) {
					for (JIRAIssue sub : issueModel.getSubtasks(issue)) {
						node.add(new JIRAIssueTreeNode(issueModel, sub));
					}
				}
			}
		} else {
			for (JIRAIssue issue : issueModel.getIssues()) {
				getPlace(issue, root).add(new JIRAIssueTreeNode(issueModel, issue));
			}
		}
		if (groupBy == JiraIssueGroupBy.LAST_UPDATED) {
			pruneEmptyUpdateGroups(root);
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

	public void setGroupSubtasksUnderParent(boolean groupSubtasksUnderParent) {
		isGroupSubtasksUnderParent = groupSubtasksUnderParent;
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
			case LAST_UPDATED:
				name = updatedDate2Name(issue).toString();
				break;
			default:
				return root;
		}
		if (name == null) {
			name = "None";
		}
		DefaultMutableTreeNode n = findGroupNode(root, name);
		if (n == null) {
			n = new JIRAIssueGroupTreeNode(issueModel, name, CachedIconLoader.getIcon(iconUrl),
					CachedIconLoader.getDisabledIcon(iconUrl));
			root.add(n);
		}
		return n;
	}

	// isn't this constant defined somewhere?
	private static final int DAYS_IN_WEEK = 7;

	private UpdateGroup updatedDate2Name(JIRAIssue issue) {
		DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		UpdateGroup groupName;
		try {
			DateMidnight midnight = new DateMidnight();
			DateTime updated = new DateTime(df.parse(issue.getUpdated()).getTime());

			if (updated.isAfter(midnight)) {
				groupName = UpdateGroup.UPDATED_TODAY;
			} else if (updated.isAfter(midnight.minusDays(1))) {
				groupName = UpdateGroup.UPDATED_YESTERDAY;
			} else if (updated.isAfter(midnight.minusDays(2))) {
				groupName = UpdateGroup.UPDATED_TWO_DAYS_AGO;
			} else if (updated.isAfter(midnight.minusDays(midnight.getDayOfWeek()))) {
				groupName = UpdateGroup.UPDATED_THIS_WEEK;
			} else if (updated.isAfter(midnight.minusDays(midnight.getDayOfWeek() + DAYS_IN_WEEK))) {
				groupName = UpdateGroup.UPDATED_LAST_WEEK;
			} else if (updated.isAfter(midnight.minusDays(midnight.getDayOfMonth()))) {
				groupName = UpdateGroup.UPDATED_THIS_MONTH;
			} else if (updated.isAfter(midnight.minusMonths(1))) {
				groupName = UpdateGroup.UPDATED_LAST_MONTH;
			} else {
				groupName = UpdateGroup.UPDATED_EARLIER;
			}
		} catch (java.text.ParseException e) {
			groupName = UpdateGroup.UPDATED_INVALID;
		}
		return groupName;
	}

	private void createUpdateGroups(DefaultMutableTreeNode root) {
		for (UpdateGroup g : UpdateGroup.values()) {
			root.add(new JIRAIssueGroupTreeNode(issueModel, g.toString(), null, null));
		}
	}

	private void pruneEmptyUpdateGroups(DefaultMutableTreeNode root) {
		boolean haveEmptyNodes;
		do {
			haveEmptyNodes = false;
			for (int i = 0; i < root.getChildCount(); ++i) {
				if (root.getChildAt(i).getChildCount() == 0) {
					root.remove(i);
					haveEmptyNodes = true;
					break;
				}
			}
		} while (haveEmptyNodes);
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
