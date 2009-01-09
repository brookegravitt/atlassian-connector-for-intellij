package com.atlassian.theplugin.idea.bamboo.build;

import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.bamboo.TestDetails;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.atlassian.theplugin.util.ColorToHtml;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jgorycki
 * Date: Jan 8, 2009
 * Time: 3:17:45 PM
 */
public class TestDetailsPanel extends JPanel implements ActionListener {
	private static final float SPLIT_RATIO = 0.3f;

	private JTree tree;
	private JScrollPane scroll;

	private List<TestDetails> succeededTests;
	private final Project project;
	private final BambooBuildAdapterIdea build;
	private final String contentKey;
	private List<TestDetails> failedTests;

	private ConsoleView console;
	private boolean passedTestsVisible;

	private static final Icon TEST_PASSED_ICON = IconLoader.getIcon("/runConfigurations/testPassed.png");
	private static final Icon TEST_FAILED_ICON = IconLoader.getIcon("/runConfigurations/testFailed.png");

	private static final boolean PASSED_TESTS_VISIBLE_DEFAULT = false;

	public TestDetailsPanel(Project project, final BambooBuildAdapterIdea build, String contentKey) {
		this.project = project;
		this.build = build;
		this.contentKey = contentKey;
		setLayout(new GridBagLayout());

		final BambooServerFacade bambooFacade = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger());

		Task.Backgroundable stackTraceTask = new Task.Backgroundable(project, "Retrieving Tests Results", false) {
			@Override
			public void run(final ProgressIndicator indicator) {
				try {
					BuildDetails details = bambooFacade.getBuildDetails(
							build.getServer(), build.getBuildKey(), build.getBuildNumber());
					failedTests = details.getFailedTestDetails();
					succeededTests = details.getSuccessfulTestDetails();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							fillContent();
						}
					});
				} catch (ServerPasswordNotProvidedException e) {
					showError(e);
				} catch (RemoteApiException e) {
					showError(e);
				}
			}
		};

		ProgressManager.getInstance().run(stackTraceTask);
	}

	private void showError(final Exception e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				add(new JLabel("Failed to retrieve test results: " + e.getMessage()));
			}
		});
	}

	private void fillContent() {
		if (failedTests.size() > 0 || succeededTests.size() > 0) {

			Splitter split = new Splitter(false, SPLIT_RATIO);
			split.setShowDividerControls(true);

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
			ActionToolbar toolbar = manager.createActionToolbar(contentKey, group, true);
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
			add(new JLabel("No tests in build " + build.getBuildKey() + "-" + build.getBuildNumber()));
		}
	}

	public void actionPerformed(ActionEvent e) {
		// ignore
	}

	private void print(String txt) {
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

	private boolean createTestConfiguration(RunConfiguration configuration) {
		TreePath p = tree.getSelectionPath();
		return p != null && ((AbstractTreeNode) p.getLastPathComponent()).createTestConfiguration(configuration);
	}

	public void runFailedTests(AnActionEvent ev, boolean debug) {
		RunManagerImpl runManager = (RunManagerImpl) RunManager.getInstance(project);
		ConfigurationFactory factory = runManager.getFactory("JUnit", null);
		RunnerAndConfigurationSettings settings = runManager.createRunConfiguration("test from bamboo", factory);

		if (!createTestConfiguration(settings.getConfiguration())) {
			return;
		}

		IdeaVersionFacade.getInstance().runTests(settings, ev, debug);
	}

	public boolean canRunFailedTests() {
		return createTestConfiguration(null);
	}

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

		@Override
		public boolean createTestConfiguration(RunConfiguration configuration) {
			if (configuration != null) {
				JUnitConfiguration conf = (JUnitConfiguration) configuration;
				conf.getPersistentData().TEST_OBJECT = JUnitConfiguration.TEST_PACKAGE;
				conf.getPersistentData().PACKAGE_NAME = toString();
			}
			return true;
		}
	}

	private class AllTestsNode extends PackageNode {
		public AllTestsNode(final int totalTests, final int failedTests) {
			super("All Tests", totalTests, failedTests);
		}

		@Override
		public boolean createTestConfiguration(RunConfiguration configuration) {
			if (configuration != null) {
				JUnitConfiguration conf = (JUnitConfiguration) configuration;
				conf.getPersistentData().TEST_OBJECT = JUnitConfiguration.TEST_PACKAGE;
				conf.getPersistentData().PACKAGE_NAME = "";
			}
			return true;
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

		@Override
		public boolean createTestConfiguration(RunConfiguration configuration) {
			PsiClass cls = IdeaVersionFacade.getInstance().findClass(className, project);
			if (cls == null) {
				return false;
			}
			if (configuration != null) {
				JUnitConfiguration conf = (JUnitConfiguration) configuration;
				conf.beClassConfiguration(cls);
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

		@Override
		public boolean createTestConfiguration(RunConfiguration configuration) {
			PsiMethod m = getMethod();

			if (m != null) {
				if (configuration != null) {
					JUnitConfiguration conf = (JUnitConfiguration) configuration;
					conf.beMethodConfiguration(PsiLocation.fromPsiElement(project, m));
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

	private String getPackageFromClassName(String fqcn) {
		int pkgidx = fqcn.lastIndexOf('.');
		return pkgidx > -1 ? fqcn.substring(0, pkgidx) : "<default>";
	}

	private JTree createTestTree() {
		NonLeafNode root = new AllTestsNode(
				failedTests.size() + succeededTests.size(), failedTests.size());

		Map<String, NonLeafNode> packages = new LinkedHashMap<String, NonLeafNode>();
		for (TestDetails d : succeededTests) {
			String fqcn = d.getTestClassName();
			String pkg = getPackageFromClassName(fqcn);
			NonLeafNode n = packages.get(pkg);
			if (n == null) {
				packages.put(pkg, new PackageNode(pkg, 1, 0));
			} else {
				n.addTest();
			}
		}
		for (TestDetails d : failedTests) {
			String fqcn = d.getTestClassName();
			String pkg = getPackageFromClassName(fqcn);
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
			String pkg = getPackageFromClassName(fqcn);
			NonLeafNode n = c.getValue();
			packages.get(pkg);
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
}
