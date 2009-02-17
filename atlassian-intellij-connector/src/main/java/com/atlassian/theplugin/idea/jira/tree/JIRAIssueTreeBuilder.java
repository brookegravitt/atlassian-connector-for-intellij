package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.idea.jira.CachedIconLoader;
import com.atlassian.theplugin.idea.jira.JiraIssueGroupBy;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeRenderer;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class JIRAIssueTreeBuilder {

	private JiraIssueGroupBy groupBy;
	private final JIRAIssueListModel issueModel;
	private SortableGroupsTreeModel treeModel;
	private static final TreeCellRenderer TREE_RENDERER = new TreeRenderer();
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

		private UpdateGroup(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private final class GroupByDateTreeNode extends JIRAIssueGroupTreeNode {
		private final UpdateGroup group;

		private GroupByDateTreeNode(UpdateGroup group) {
			super(issueModel, group.toString(), null, null);
			this.group = group;
		}

		private final Comparator<JIRAIssueGroupTreeNode> comparator = new Comparator<JIRAIssueGroupTreeNode>() {
			public int compare(JIRAIssueGroupTreeNode lhs, JIRAIssueGroupTreeNode rhs) {
				if (lhs instanceof GroupByDateTreeNode && rhs instanceof GroupByDateTreeNode) {
					return ((GroupByDateTreeNode) lhs).group.ordinal() - ((GroupByDateTreeNode) rhs).group.ordinal();
				}
				return lhs.getComparator().compare(lhs, rhs);
			}
		};

		public Comparator<JIRAIssueGroupTreeNode> getComparator() {
			return comparator;
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

		JIRAIssue selectedIsse = getSelectedIssue(tree);

		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
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
			Collection<JIRAIssue> orphans = issueModel.getSubtasks(null);
			if (!orphans.isEmpty()) {
				for (JIRAIssue i : orphans) {
					JIRAIssueTreeNode node = new JIRAIssueTreeNode(issueModel, i);
					getPlace(i, root).add(node);
				}
			}
		} else {
			for (JIRAIssue issue : issueModel.getIssues()) {
//				System.out.println("***************");
				long start = System.currentTimeMillis();
				getPlace(issue, root).add(new JIRAIssueTreeNode(issueModel, issue));
//				System.out.println("Executed in: " + (System.currentTimeMillis() - start));
			}
		}
		treeModel.nodeStructureChanged(root);

		// expand tree
		for (int i = 0; i < tree.getVisibleRowCount(); i++) {
			tree.expandRow(i);
		}

		selectIssueNode(tree, selectedIsse);
	}

	private void selectIssueNode(final JTree tree, final JIRAIssue selectedIssue) {
		if (selectedIssue == null) {
			tree.clearSelection();
			return;
		}

		for (int i = 0; i < tree.getRowCount(); i++) {
			TreePath path = tree.getPathForRow(i);
			Object object = path.getLastPathComponent();
			if (object instanceof JIRAIssueTreeNode) {
				JIRAIssueTreeNode node = (JIRAIssueTreeNode) object;
				if (node.getIssue().getKey().equals(selectedIssue.getKey())
						&& node.getIssue().getServerUrl().equals(selectedIssue.getServerUrl())) {
					tree.expandPath(path);
					tree.makeVisible(path);
					tree.setSelectionPath(path);
					break;
				}
			}
		}
	}

	private JIRAIssue getSelectedIssue(final JTree tree) {
		final TreePath selectionPath = tree.getSelectionPath();
		if (selectionPath != null && selectionPath.getLastPathComponent() instanceof JIRAIssueTreeNode) {
			return ((JIRAIssueTreeNode) selectionPath.getLastPathComponent()).getIssue();
		} else {
			// nothing selected
			return null;
		}
	}

	private void reCreateTree(final JTree tree, JComponent treeParent, DefaultMutableTreeNode root) {
		tree.removeAll();
		treeModel = new SortableGroupsTreeModel(root, groupBy);
		tree.setModel(treeModel);
		TreeUISetup uiSetup = new TreeUISetup(TREE_RENDERER);
		uiSetup.registerUI(tree);
		if (this.lastTree != tree) {
			this.lastTree = tree;
			tree.setShowsRootHandles(true);
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					final TreePath selectionPath = tree.getSelectionModel().getSelectionPath();
					if (selectionPath != null && selectionPath.getLastPathComponent() != null) {
						((AbstractTreeNode) selectionPath.getLastPathComponent()).onSelect();
					} else {
						issueModel.setSeletedIssue(null);
					}
				}
			});
			uiSetup.initializeUI(tree, treeParent);
			tree.setRootVisible(false);
		}
	}

	public void setGroupSubtasksUnderParent(boolean groupSubtasksUnderParent) {
		isGroupSubtasksUnderParent = groupSubtasksUnderParent;
	}

	private final class SortableGroupsTreeModel extends DefaultTreeModel {

		private final Comparator<JIRAIssueGroupTreeNode> comparator = new Comparator<JIRAIssueGroupTreeNode>() {
			public int compare(JIRAIssueGroupTreeNode lhs, JIRAIssueGroupTreeNode rhs) {
				return lhs.getComparator().compare(lhs, rhs);
			}
		};

		private Set<JIRAIssueGroupTreeNode> set = new TreeSet<JIRAIssueGroupTreeNode>(comparator);

		private boolean isFlat;

		private SortableGroupsTreeModel(TreeNode root, JiraIssueGroupBy groupBy) {
			super(root);
			isFlat = groupBy == JiraIssueGroupBy.NONE;
		}

		public DefaultMutableTreeNode getGroupNode(JIRAIssue issue, String name, Icon icon, Icon disabledIcon) {
			if (isFlat) {
				return (DefaultMutableTreeNode) getRoot();
			}
			JIRAIssueGroupTreeNode n = findGroupNode(name);
			if (n != null) {
				return n;
			}

			if (groupBy == JiraIssueGroupBy.LAST_UPDATED) {
				n = new GroupByDateTreeNode(updatedDate2Name(issue));
			} else {
				n = new JIRAIssueGroupTreeNode(issueModel, name, icon, disabledIcon);
			}

			set.add(n);
			((DefaultMutableTreeNode) getRoot()).removeAllChildren();
			for (JIRAIssueGroupTreeNode node : set) {
				((DefaultMutableTreeNode) getRoot()).add(node);
			}
			nodeStructureChanged((DefaultMutableTreeNode) getRoot());
			return n;
		}

		private JIRAIssueGroupTreeNode findGroupNode(String name) {
			for (int i = 0; i < getChildCount(getRoot()); ++i) {
				JIRAIssueGroupTreeNode node = (JIRAIssueGroupTreeNode) getChild(getRoot(), i);
				if (node.toString().equals(name)) {
					return node;
				}
			}
			return null;
		}

		@Override
		public Object getChild(Object parent, int index) {
			if (parent != getRoot() || isFlat) {
				return super.getChild(parent, index);
			}
			return new ArrayList<JIRAIssueGroupTreeNode>(set).get(index);
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			if (parent != getRoot() || isFlat) {
				return super.getIndexOfChild(parent, child);
			}
			int i = 0;
			for (JIRAIssueGroupTreeNode n : set) {
				if (n == child) {
					return i;
				}
				++i;
			}
			return -1;
		}

		@Override
		public int getChildCount(Object parent) {
			if (parent != getRoot() || isFlat) {
				return super.getChildCount(parent);
			}
			return set.size();
		}

		@Override
		public boolean isLeaf(Object node) {
			if (isFlat) {
				return super.isLeaf(node);
			}
			return !(node instanceof JIRAIssueGroupTreeNode) && super.isLeaf(node);
		}
	}

	private DefaultMutableTreeNode getPlace(JIRAIssue issue, DefaultMutableTreeNode root) {
//		long start = System.currentTimeMillis();
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

		DefaultMutableTreeNode node = treeModel.getGroupNode(issue, name, CachedIconLoader.getIcon(iconUrl),
				CachedIconLoader.getDisabledIcon(iconUrl));

//		System.out.println("getPlace in: " + (System.currentTimeMillis() - start));

		return node;
	}

	// isn't this constant defined somewhere?
	private static final int DAYS_IN_WEEK = 7;

	private UpdateGroup updatedDate2Name(JIRAIssue issue) {
		DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
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

	private String getProjectName(String key) {
		if (projectKeysToNames == null || !projectKeysToNames.containsKey(key)) {
			// bummer
			return key;
		}
		return projectKeysToNames.get(key);
	}
}
