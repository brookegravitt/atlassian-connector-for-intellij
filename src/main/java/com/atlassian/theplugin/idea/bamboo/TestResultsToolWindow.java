package com.atlassian.theplugin.idea.bamboo;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ui.content.Content;
import com.intellij.peer.PeerFactory;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.commons.bamboo.TestDetails;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.util.*;
import java.util.List;
import java.awt.*;

public class TestResultsToolWindow {

	public interface Expandable {
		public void expand();
		public void collapse();
	}

	private static final String TOOL_WINDOW_TITLE = "Bamboo Failed Tests";
	private static final Icon LEAF_ICON = IconLoader.getIcon("/nodes/method.png");
	private static final Icon FOLDER_OPEN_ICON = IconLoader.getIcon("/nodes/TreeOpen.png");
	private static final Icon FOLDER_CLOSED_ICON = IconLoader.getIcon("/nodes/TreeClosed.png");

	private static TestResultsToolWindow instance = new TestResultsToolWindow();

	private static HashMap<String, TestDetailsPanel> panelMap = new HashMap<String, TestDetailsPanel>();

	private TestResultsToolWindow() {
	}

	public static TestResultsToolWindow getInstance() {
		return instance;
	}

	public static synchronized Expandable getExpandable(String name) {
		return panelMap.get(name);
	}

	public void showTestResults(String buildKey, String buildNumber, List<TestDetails> tests) {
		TestDetailsPanel detailsPanel;
		String contentKey = buildKey + "-" + buildNumber;

		ToolWindowManager twm = ToolWindowManager.getInstance(IdeaHelper.getCurrentProject());
		ToolWindow testDetailsToolWindow = twm.getToolWindow(TOOL_WINDOW_TITLE);
		if (testDetailsToolWindow == null) {
			testDetailsToolWindow = twm.registerToolWindow(TOOL_WINDOW_TITLE, true, ToolWindowAnchor.BOTTOM);
			testDetailsToolWindow.setIcon(IconLoader.getIcon("/icons/bamboo-blue-16.png"));
		}

		Content content = testDetailsToolWindow.getContentManager().findContent(contentKey);

		synchronized (panelMap) {
			if (content != null) {
				detailsPanel = panelMap.get(contentKey);
			} else {
				detailsPanel = new TestDetailsPanel(contentKey, tests);
				panelMap.remove(contentKey);
				panelMap.put(contentKey, detailsPanel);

				PeerFactory peerFactory = PeerFactory.getInstance();
				content = peerFactory.getContentFactory().createContent(detailsPanel, contentKey, true);
				content.setIcon(IconLoader.getIcon("/icons/bamboo-blue-16.png"));
				content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
				testDetailsToolWindow.getContentManager().addContent(content);
			}
		}
		testDetailsToolWindow.getContentManager().setSelectedContent(content);
		testDetailsToolWindow.show(null);
	}

	private class TestDetailsPanel extends JPanel implements Expandable {
		private static final float SPLIT_RATIO = 0.3f;

		private JTree tree;

		private ConsoleView console;

		private abstract class AbstractTreeNode extends DefaultMutableTreeNode {
			public AbstractTreeNode(String s) {
				super(s);
			}

			public abstract void selected();
		}

		private class NonLeafNode extends AbstractTreeNode {
			public NonLeafNode(String s) {
				super(s);
			}

			public void selected() {
				print("");
			}
		}

		private class TestErrorInfoNode extends AbstractTreeNode {
			private TestDetails details;

			public TestErrorInfoNode(TestDetails details) {
				super(details.getTestMethodName());
				this.details = details;
			}

			public String getTestErrors() {
				return details.getErrors();
			}

			public void selected() {
				print(details.getErrors());
			}
		}

		private JTree createTestTree(final List<TestDetails> tests) {
			DefaultMutableTreeNode root = new NonLeafNode("root");

			HashMap<String, DefaultMutableTreeNode> packages = new HashMap<String, DefaultMutableTreeNode>();
			for (TestDetails d : tests) {
				String fqcn = d.getTestClassName();
				String pkg = fqcn.substring(0, fqcn.lastIndexOf('.'));
				packages.put(pkg, null);
			}

			for (Map.Entry<String, DefaultMutableTreeNode> p : packages.entrySet()) {
				DefaultMutableTreeNode pkgNode = new NonLeafNode(p.getKey());
				p.setValue(pkgNode);
				root.add(pkgNode);
			}

			HashMap<String, DefaultMutableTreeNode> classes = new HashMap<String, DefaultMutableTreeNode>();
			for (TestDetails d : tests) {
				String fqcn = d.getTestClassName();
				classes.put(fqcn, null);
			}

			for (Map.Entry<String, DefaultMutableTreeNode> c : classes.entrySet()) {
				String fqcn = c.getKey();
				String pkg = fqcn.substring(0, fqcn.lastIndexOf('.'));
				String cls = fqcn.substring(fqcn.lastIndexOf('.') + 1);
				DefaultMutableTreeNode clsNode = new NonLeafNode(cls);
				packages.get(pkg).add(clsNode);
				c.setValue(clsNode);
			}

			for (TestDetails d : tests) {
				String fqcn = d.getTestClassName();
				DefaultMutableTreeNode node = classes.get(fqcn);
				node.add(new TestErrorInfoNode(d));
			}

			DefaultTreeModel tm = new DefaultTreeModel(root);
			tree = new JTree(tm);
			tree.setRootVisible(false);
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					AbstractTreeNode errInfoNode = (AbstractTreeNode) e.getPath().getLastPathComponent();
					errInfoNode.selected();
				}
			});

			if (LEAF_ICON != null) {
				DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
				renderer.setOpenIcon(FOLDER_OPEN_ICON);
				renderer.setClosedIcon(FOLDER_CLOSED_ICON);
				renderer.setLeafIcon(LEAF_ICON);
				tree.setCellRenderer(renderer);
			}

			expand();

			return tree;
		}

		public TestDetailsPanel(String name, final List<TestDetails> tests) {
			super();
			if (tests.size() > 0) {

				Splitter split = new Splitter(false, SPLIT_RATIO);
				split.setShowDividerControls(true);

				setLayout(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.fill = GridBagConstraints.BOTH;

				JPanel treePanel = new JPanel();
				treePanel.setLayout(new GridBagLayout());
				GridBagConstraints gbc1 = new GridBagConstraints();

				ActionManager manager = ActionManager.getInstance();
				ActionGroup group = (ActionGroup) manager.getAction("ThePlugin.Bamboo.TestResultsToolBar");
				ActionToolbar toolbar = manager.createActionToolbar(name, group, true);
				JComponent comp = toolbar.getComponent();

				gbc1.gridx = 0;
				gbc1.gridy = 0;
				gbc1.weightx = 1.0;
				gbc1.weighty = 0.0;
				gbc1.fill = GridBagConstraints.HORIZONTAL;
				
				treePanel.add(comp, gbc1);

				JTree tree = createTestTree(tests);
				JScrollPane sp = new JScrollPane(tree);

				gbc1.gridy = 1;
				gbc1.weighty = 1.0;
				gbc1.fill = GridBagConstraints.BOTH;

				treePanel.add(sp, gbc1);

				split.setFirstComponent(treePanel);

				JPanel consolePanel = new JPanel();
				consolePanel.setLayout(new GridBagLayout());

				gbc1.gridy = 0;
				gbc1.weighty = 0.0;
				gbc1.fill = GridBagConstraints.NONE;
				gbc1.anchor = GridBagConstraints.LINE_START;

				JLabel label = new JLabel("Test Stack Trace");

				Dimension d = label.getPreferredSize();
				d.height = toolbar.getMaxButtonHeight();
				label.setMinimumSize(d);
				label.setPreferredSize(d); 
				consolePanel.add(label, gbc1);

				TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
				TextConsoleBuilder builder = factory.createBuilder(IdeaHelper.getCurrentProject());
				console = builder.getConsole();

				gbc1.gridy = 1;
				gbc1.weighty = 1.0;
				gbc1.fill = GridBagConstraints.BOTH;

				consolePanel.add(console.getComponent(), gbc1);

				split.setSecondComponent(consolePanel);
				
				add(split, gbc);
			} else {
				add(new JLabel("No failed tests in build " + name));
			}
		}

		public void print(String txt) {
			console.clear();
			console.print(txt, ConsoleViewContentType.NORMAL_OUTPUT);
		}

		public void expand() {
			for (int row = 0; row < tree.getRowCount(); ++row) {
				tree.expandRow(row);
			}
		}

		public void collapse() {
			for (int row = tree.getRowCount() - 1; row >= 0; --row) {
				tree.collapseRow(row);
			}
		}
	}
}
