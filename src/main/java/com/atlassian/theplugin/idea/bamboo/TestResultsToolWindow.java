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

	public interface TestTree {
		public void expand();
		public void collapse();
        public void toggleAll();
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

	public static synchronized TestTree getTestTree(String name) {
		return panelMap.get(name);
	}

	public void showTestResults(String buildKey, String buildNumber,
                                List<TestDetails> failedTests, List<TestDetails> succeededTests) {
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
				detailsPanel = new TestDetailsPanel(contentKey, failedTests, succeededTests);
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

	private class TestDetailsPanel extends JPanel implements TestTree {
		private static final float SPLIT_RATIO = 0.3f;

		private JTree tree;
        JScrollPane scroll;
        private boolean showAllTests;

        private List<TestDetails> succeededTests;
        private List<TestDetails> failedTests;

        private ConsoleView console;

		private abstract class AbstractTreeNode extends DefaultMutableTreeNode {
            public AbstractTreeNode(String s) {
				super(s);
			}

			public abstract void selected();
            public abstract boolean isFailed();
        }

		private class NonLeafNode extends AbstractTreeNode {
            private boolean failed;

            public NonLeafNode(String s, boolean failed) {
				super(s);
                this.failed = failed;
            }

			public void selected() {
				print("");
			}

            public boolean isFailed() {
                return failed;
            }
        }

		private class TestErrorInfoNode extends AbstractTreeNode {
			private TestDetails details;

			public TestErrorInfoNode(TestDetails details) {
				super(details.getTestMethodName());
				this.details = details;
			}

			public void selected() {
				print(details.getErrors());
			}

            public boolean isFailed() {
                return true;
            }
		}

        private class TestSuccessInfoNode extends AbstractTreeNode {
            private TestDetails details;

            public TestSuccessInfoNode(TestDetails details) {
                super(details.getTestMethodName());
                this.details = details;
            }

            public void selected() {
                print(details.getTestResult().toString());
            }

            public boolean isFailed() {
                return false;
            }
        }

        private JTree createTestTree() {
			DefaultMutableTreeNode root = new NonLeafNode("root", false);

            //
            // warning: somewhat tricky algorithm below:
            // package and class tree nodes are first populated as leaf nodes. Because of the fact
            // that we iterate over failed tests after succeeded tests, packages and classes are
            // automagically determined to be failing if at least one of their test methods fails
            // after determining whether a node should be failing or not, it is replaced with a non-leaf node
            //
            HashMap<String, AbstractTreeNode> packages = new HashMap<String, AbstractTreeNode>();
            if (showAllTests) {
                for (TestDetails d : succeededTests) {
                    String fqcn = d.getTestClassName();
                    String pkg = fqcn.substring(0, fqcn.lastIndexOf('.'));
                    packages.put(pkg, new TestSuccessInfoNode(d));
                }
            }
            for (TestDetails d : failedTests) {
                String fqcn = d.getTestClassName();
                String pkg = fqcn.substring(0, fqcn.lastIndexOf('.'));
                packages.put(pkg, new TestErrorInfoNode(d));
            }

            for (Map.Entry<String, AbstractTreeNode> p : packages.entrySet()) {
                AbstractTreeNode pkgNode = new NonLeafNode(p.getKey(), p.getValue().isFailed());
                p.setValue(pkgNode);
				root.add(pkgNode);
			}

			HashMap<String, AbstractTreeNode> classes = new HashMap<String, AbstractTreeNode>();
            if (showAllTests) {
                for (TestDetails d : succeededTests) {
                    String fqcn = d.getTestClassName();
                    classes.put(fqcn, new TestSuccessInfoNode(d));
                }
            }
            for (TestDetails d : failedTests) {
                String fqcn = d.getTestClassName();
                classes.put(fqcn, new TestErrorInfoNode(d));
            }

            for (Map.Entry<String, AbstractTreeNode> c : classes.entrySet()) {
				String fqcn = c.getKey();
				String pkg = fqcn.substring(0, fqcn.lastIndexOf('.'));
				String cls = fqcn.substring(fqcn.lastIndexOf('.') + 1);
				AbstractTreeNode clsNode = new NonLeafNode(cls, c.getValue().isFailed());
				packages.get(pkg).add(clsNode);
				c.setValue(clsNode);
			}

            if (showAllTests) {
                for (TestDetails d : succeededTests) {
                    String fqcn = d.getTestClassName();
                    DefaultMutableTreeNode node = classes.get(fqcn);
                    node.add(new TestSuccessInfoNode(d));
                }
            }
            for (TestDetails d : failedTests) {
                String fqcn = d.getTestClassName();
                DefaultMutableTreeNode node = classes.get(fqcn);
                node.add(new TestErrorInfoNode(d));
            }

            //
            // end of tricky algorithm
            //

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

            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
                public Component getTreeCellRendererComponent(JTree tree,
                                   Object value,
                                   boolean selected,
                                   boolean expanded,
                                   boolean leaf,
                                   int row,
                                   boolean hasFocus) {
                    Component c = super.getTreeCellRendererComponent(
                            tree, value, selected, expanded, leaf, row, hasFocus);
                    try {
                        if (((AbstractTreeNode) value).isFailed()) {
                            setIcon(LEAF_ICON);
                        } else {
                            setIcon(FOLDER_CLOSED_ICON);
                        }
                    } catch (ClassCastException e) {
                        // should not happen, making compiler happy
                        setIcon(FOLDER_OPEN_ICON);
                    }
                    return c;
                }
            };
            renderer.setOpenIcon(FOLDER_OPEN_ICON);
            renderer.setClosedIcon(FOLDER_CLOSED_ICON);
            renderer.setLeafIcon(LEAF_ICON);
            tree.setCellRenderer(renderer);

			expand();

			return tree;
		}

		public TestDetailsPanel(String name, final List<TestDetails> failedTests,
                                final List<TestDetails> succeededTests) {
			super();
            this.failedTests = failedTests;
            this.succeededTests = succeededTests;

            if (failedTests.size() > 0) {

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

                showAllTests = true;
                JTree tree = createTestTree();
				scroll = new JScrollPane(tree);

				gbc1.gridy = 1;
				gbc1.weighty = 1.0;
				gbc1.fill = GridBagConstraints.BOTH;

                treePanel.add(scroll, gbc1);

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
				add(new JLabel("No failed failedTests in build " + name));
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

        public void toggleAll() {
            showAllTests = !showAllTests;
            tree = createTestTree();
            scroll.setViewportView(tree);
        }
    }
}
