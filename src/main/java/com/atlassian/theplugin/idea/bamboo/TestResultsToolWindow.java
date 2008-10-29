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

package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.commons.bamboo.TestDetails;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.util.ColorToHtml;
import com.intellij.execution.*;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.junit.JUnitConfiguration;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.peer.PeerFactory;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.content.Content;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TestResultsToolWindow {
	private final Project project;

	public interface TestTree extends Expandable {
		boolean PASSED_TESTS_VISIBLE_DEFAULT = false;
		
        void setPassedTestsVisible(boolean visible);
		boolean isPassedTestsVisible();
		boolean createTestConfiguration(RunConfiguration configuration);
	}

	private static final String TOOL_WINDOW_TITLE = "Bamboo Failed Tests";
	private static final Icon TEST_PASSED_ICON = IconLoader.getIcon("/runConfigurations/testPassed.png");
	private static final Icon TEST_FAILED_ICON = IconLoader.getIcon("/runConfigurations/testFailed.png");

	private HashMap<String, TestDetailsPanel> panelMap = new HashMap<String, TestDetailsPanel>();

	public TestResultsToolWindow(Project project) {
		this.project = project;
	}


	public TestTree getTestTree(String name) {
		return panelMap.get(name);
	}

	public void showTestResults(String buildKey, String buildNumber,
                                List<TestDetails> failedTests, List<TestDetails> succeededTests) {
		TestDetailsPanel detailsPanel;
		String contentKey = buildKey + "-" + buildNumber;

		ToolWindowManager twm = ToolWindowManager.getInstance(project);
		ToolWindow testDetailsToolWindow = twm.getToolWindow(TOOL_WINDOW_TITLE);
		if (testDetailsToolWindow == null) {
			testDetailsToolWindow = twm.registerToolWindow(TOOL_WINDOW_TITLE, true, ToolWindowAnchor.BOTTOM);
			testDetailsToolWindow.setIcon(Constants.BAMBOO_TRACE_ICON);
		}

		Content content = testDetailsToolWindow.getContentManager().findContent(contentKey);

		if (content == null) {
			detailsPanel = new TestDetailsPanel(contentKey, failedTests, succeededTests);
			panelMap.remove(contentKey);
			panelMap.put(contentKey, detailsPanel);

			PeerFactory peerFactory = PeerFactory.getInstance();
			content = peerFactory.getContentFactory().createContent(detailsPanel, contentKey, true);
			content.setIcon(Constants.BAMBOO_TRACE_ICON);
			content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
			testDetailsToolWindow.getContentManager().addContent(content);
		}

		testDetailsToolWindow.getContentManager().setSelectedContent(content);
		testDetailsToolWindow.show(null);
	}

	public void runFailedTests(AnActionEvent ev, boolean debug) {
		RunManagerImpl runManager = (RunManagerImpl) RunManager.getInstance(project);
		ConfigurationFactory factory = runManager.getFactory("JUnit", null);
		RunnerAndConfigurationSettings settings = runManager.createRunConfiguration("test from bamboo", factory);

		TestTree tree = getTestTree(ev.getPlace());
		if (tree == null) {
			return;
		}
		if (!tree.createTestConfiguration(settings.getConfiguration())) {
			return;
		}

        IdeaVersionFacade.getInstance().runTests(settings, ev, debug);
	}

	public boolean canRunFailedTests(AnActionEvent ev) {
		TestTree tree = getTestTree(ev.getPlace());
		if (tree == null) {
			return false;
		}
		return tree.createTestConfiguration(null);
	}

	private abstract class AbstractTreeNode extends DefaultMutableTreeNode {
        public AbstractTreeNode(String s) {
            super(s);
        }

        public abstract void selected();
        public abstract boolean isFailed();
        public String getTestStats() {
            return "";
        }

        public abstract void navigate();

		public abstract boolean createTestConfiguration(RunConfiguration configuration);
    }

    private class TestDetailsPanel extends JPanel implements TestTree {
		private static final float SPLIT_RATIO = 0.3f;

		private JTree tree;
        private JScrollPane scroll;

        private List<TestDetails> succeededTests;
        private List<TestDetails> failedTests;

        private ConsoleView console;
		private boolean passedTestsVisible;

		private abstract class NonLeafNode extends AbstractTreeNode {
			protected int totalTests;
			protected int failedTests;

            public NonLeafNode(String s, int totalTests, int failedTests) {
				super(s);
				this.totalTests = totalTests;
				this.failedTests = failedTests;
            }

			@Override
			public void selected() {
				print("");
			}

            @Override
			public boolean isFailed() {
                return failedTests > 0;
            }

			@Override
			public String getTestStats() {
				return " (" + failedTests + " out of " + totalTests + " failed)";
			}

			public void addFailedTest() {
				++failedTests;
			}

			public void addTest() {
				++totalTests;
			}

			public int getFailedTests() {
				return failedTests;
			}

			public int getTotalTests() {
				return totalTests;
			}
		}

		private class PackageNode extends NonLeafNode {
			public PackageNode(String s, int totalTests, int failedTests) {
				super(s, totalTests, failedTests);
			}

			@Override
			public void navigate() {
				// no-op for packages
			}

			public boolean createTestConfiguration(RunConfiguration configuration) {
				// bummer, JUnit does not support testing the whole package
				return false;
			}
		}

		private class ClassNode extends NonLeafNode {
			private String className;

			public ClassNode(String fqcn, int totalTests, int failedTests) {
				super(fqcn.substring(fqcn.lastIndexOf('.') + 1), totalTests, failedTests);
				className = fqcn;
			}

			@Override
			public void navigate() {
                PsiClass cls = IdeaVersionFacade.getInstance().findClass(className, project);
				if (cls != null) {
					cls.navigate(true);
				}
			}

			public boolean createTestConfiguration(RunConfiguration configuration) {
                PsiClass cls = IdeaVersionFacade.getInstance().findClass(className, project);
				if (cls == null) {
					return false;
				}
				if (configuration != null) {
					IdeaVersionFacade.getInstance().setTestClass(configuration, cls);
				}
				return true;
			}
		}

		private abstract class TestNode extends AbstractTreeNode {
			protected TestDetails details;

			public TestNode(TestDetails details) {
				super(details.getTestMethodName());
				this.details = details;
			}

			private PsiMethod getMethod() {
                PsiClass cls = IdeaVersionFacade.getInstance().findClass(details.getTestClassName(), project);
				if (cls == null) {
					return null;
				}
				PsiMethod[] methods = cls.findMethodsByName(details.getTestMethodName(), false);
				if (methods.length == 0 || methods[0] == null) {
					return null;
				}

				return methods[0];
			}

			@Override
			public void navigate() {
				PsiMethod m = getMethod();
				if (m != null) {
					m.navigate(true);
				}
			}

			public boolean createTestConfiguration(RunConfiguration configuration) {
				PsiMethod m = getMethod();

				if (m != null) {
					if (configuration != null) {
						IdeaVersionFacade.getInstance().setTestMethod(configuration,
								PsiLocation.fromPsiElement(project, m));
					}
					return true;
				}
				return false;
			}
		}

		private class TestErrorInfoNode extends TestNode {
			public TestErrorInfoNode(TestDetails details) {
				super(details);
			}

			@Override
			public void selected() {
				print(details.getErrors());
			}

            @Override
			public boolean isFailed() {
                return true;
            }
		}

        private class TestSuccessInfoNode extends TestNode {
            public TestSuccessInfoNode(TestDetails details) {
                super(details);
            }

            @Override
			public void selected() {
                print("Test successful");
            }

            @Override
			public boolean isFailed() {
                return false;
            }
		}

		private JTree createTestTree() {
			NonLeafNode root = new PackageNode("All Tests",
					failedTests.size() + succeededTests.size(), failedTests.size());

            Map<String, NonLeafNode> packages = new LinkedHashMap<String, NonLeafNode>();
			for (TestDetails d : succeededTests) {
				String fqcn = d.getTestClassName();
				String pkg = fqcn.substring(0, fqcn.lastIndexOf('.'));
				NonLeafNode n = packages.get(pkg);
				if (n == null) {
					packages.put(pkg, new PackageNode(pkg, 1, 0));
				} else {
					n.addTest();
				}
            }
            for (TestDetails d : failedTests) {
                String fqcn = d.getTestClassName();
                String pkg = fqcn.substring(0, fqcn.lastIndexOf('.'));
				NonLeafNode n = packages.get(pkg);
				if (n == null) {
	                packages.put(pkg, new PackageNode(pkg, 1, 1));
				} else {
					n.addTest();
					n.addFailedTest();
				}
			}

            for (Map.Entry<String, NonLeafNode> p : packages.entrySet()) {
				NonLeafNode n = p.getValue();
				if (n.isFailed() || passedTestsVisible) {
					root.add(n);
				}
			}

			Map<String, NonLeafNode> classes = new LinkedHashMap<String, NonLeafNode>();
			for (TestDetails d : succeededTests) {
				String fqcn = d.getTestClassName();
				NonLeafNode n = classes.get(fqcn);
				if (n == null) {
					classes.put(fqcn, new ClassNode(fqcn, 1, 0));
				} else {
					n.addTest();
				}
			}
            for (TestDetails d : failedTests) {
                String fqcn = d.getTestClassName();
				NonLeafNode n = classes.get(fqcn);
				if (n == null) {
					classes.put(fqcn, new ClassNode(fqcn, 1, 1));
				} else {
					n.addTest();
					n.addFailedTest();
				}
			}

            for (Map.Entry<String, NonLeafNode> c : classes.entrySet()) {
				String fqcn = c.getKey();
				String pkg = fqcn.substring(0, fqcn.lastIndexOf('.'));
				NonLeafNode n = c.getValue(); packages.get(pkg);
				if (n.isFailed() || passedTestsVisible) {
					packages.get(pkg).add(n);
				}
			}

            if (passedTestsVisible) {
                for (TestDetails d : succeededTests) {
                    String fqcn = d.getTestClassName();
                    AbstractTreeNode node = classes.get(fqcn);
                    node.add(new TestSuccessInfoNode(d));
                }
            }
            for (TestDetails d : failedTests) {
                String fqcn = d.getTestClassName();
                AbstractTreeNode node = classes.get(fqcn);
                node.add(new TestErrorInfoNode(d));
            }

            DefaultTreeModel tm = new DefaultTreeModel(root);
			JTree testTree = new JTree(tm);
			testTree.setRootVisible(true);
			testTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			testTree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					AbstractTreeNode errInfoNode = (AbstractTreeNode) e.getPath().getLastPathComponent();
					errInfoNode.selected();
				}
			});
			testTree.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() >= 2) {
						TreePath path = tree.getPathForLocation(e.getX(), e.getY());
						AbstractTreeNode node = (AbstractTreeNode) path.getLastPathComponent();
						node.navigate();
					}
				}
			});

			DefaultTreeCellRenderer renderer = new MyDefaultTreeCellRenderer();
            testTree.setCellRenderer(renderer);

			return testTree;
		}

		public TestDetailsPanel(String name, final List<TestDetails> failedTests,
                                final List<TestDetails> succeededTests) {
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

				passedTestsVisible = PASSED_TESTS_VISIBLE_DEFAULT;

				tree = createTestTree();
				expand();
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
				TextConsoleBuilder builder = factory.createBuilder(project);
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
			console.print(txt, ConsoleViewContentType.ERROR_OUTPUT);
		}

		public void expand() {
			for (int row = 1; row < tree.getRowCount(); ++row) {
				tree.expandRow(row);
			}
		}

		public void collapse() {
			for (int row = tree.getRowCount() - 1; row > 0; --row) {
				tree.collapseRow(row);
			}
		}

        public void setPassedTestsVisible(boolean visible) {
			passedTestsVisible = visible;
			tree = createTestTree();
			expand();
			scroll.setViewportView(tree);
        }

		public boolean isPassedTestsVisible() {
			return passedTestsVisible;
		}

	    public boolean createTestConfiguration(RunConfiguration configuration) {
		    TreePath p = tree.getSelectionPath();
		    return p != null && ((AbstractTreeNode) p.getLastPathComponent()).createTestConfiguration(configuration);
	    }
    }

    private static class MyDefaultTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                boolean expanded, boolean leaf, int row, boolean hasFocus) {

            Component c = super.getTreeCellRendererComponent(
                    tree, value, selected, expanded, leaf, row, hasFocus);

            // this sort of is not right, as it assumes that getTreeCellRendererComponent() of the
            // DefaultTreeCellRenderer will always return _this_ (JLabel). If the implementation changes
            // someday, we are screwed :)
            try {
                AbstractTreeNode node = (AbstractTreeNode) value;
                if (node.isFailed()) {
                    setIcon(TEST_FAILED_ICON);
                } else {
                    setIcon(TEST_PASSED_ICON);
                }
                Color statsColor = selected
                        ? UIUtil.getTreeSelectionForeground() : UIUtil.getInactiveTextColor();
                StringBuilder txt = new StringBuilder();
                txt.append("<html><body>");
                txt.append(getText());
                txt.append(" <font color=");
                txt.append(ColorToHtml.getHtmlFromColor(statsColor));
                txt.append("><i>");
                txt.append(node.getTestStats());
                txt.append("</i></font>");
                txt.append("</body></html>");
                setText(txt.toString());
            } catch (ClassCastException e) {
                // should not happen, making compiler happy
                setIcon(null);
            }

            return c;
        }
    }

}
