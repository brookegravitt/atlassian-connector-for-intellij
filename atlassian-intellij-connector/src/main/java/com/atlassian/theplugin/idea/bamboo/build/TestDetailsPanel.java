package com.atlassian.theplugin.idea.bamboo.build;

import com.atlassian.connector.intellij.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.commons.bamboo.BambooJob;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.bamboo.TestDetails;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.util.ColorToHtml;
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
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	private final BambooBuildAdapter build;
	private final String contentKey;
	private List<TestDetails> failedTests;

	private ConsoleView console;
	private boolean passedTestsVisible;

	private static final Icon TEST_PASSED_ICON = IconLoader.getIcon("/runConfigurations/testPassed.png");
	private static final Icon TEST_FAILED_ICON = IconLoader.getIcon("/runConfigurations/testFailed.png");

	private static final boolean PASSED_TESTS_VISIBLE_DEFAULT = false;
	private BuildDetails buildDetails = null;

	public TestDetailsPanel(Project project, final BambooBuildAdapter build, String contentKey) {
		this.project = project;
		this.build = build;
		this.contentKey = contentKey;
		setLayout(new GridBagLayout());
	}

	public void showError(final Exception e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				add(new JLabel("Failed to retrieve test results: " + e.getMessage()));
			}
		});
	}

	public void fillContent(final BuildDetails buildDetails) {
		this.buildDetails = buildDetails;

		failedTests = buildDetails.getFailedTestDetails();
		succeededTests = buildDetails.getSuccessfulTestDetails();

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
			add(new JLabel("No tests in build " + build.getPlanKey() + "-" + build.getBuildNumberAsString()));
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

	public void runSelectedTests(DataContext dataContext, boolean debug) {
		RunManagerImpl runManager = (RunManagerImpl) RunManager.getInstance(project);
		ConfigurationFactory factory = runManager.getFactory("JUnit", null);
		RunnerAndConfigurationSettings settings = runManager.createRunConfiguration("test from bamboo", factory);

		if (!createTestConfiguration(settings.getConfiguration())) {
			return;
		}

		IdeaVersionFacade.getInstance().runTests(settings, dataContext, debug);
	}

	public void runTests(DataContext dataContext, boolean debug,
			@NotNull final String testPackage, @NotNull final String testClass, @NotNull final String testName) {
		RunManagerImpl runManager = (RunManagerImpl) RunManager.getInstance(project);
		ConfigurationFactory factory = runManager.getFactory("JUnit", null);
		RunnerAndConfigurationSettings settings = runManager.createRunConfiguration("test from bamboo", factory);

		if (!createTestConfiguration(settings.getConfiguration(), testPackage, testClass, testName)) {
			return;
		}

		IdeaVersionFacade.getInstance().runTests(settings, dataContext, debug);
	}

	private boolean createTestConfiguration(final RunConfiguration configuration,
			@NotNull final String testPackage, @NotNull final String testClass, @NotNull final String testMethod) {

		for (int i = 0; i < tree.getRowCount(); ++i) {
			final TreePath path = tree.getPathForRow(i);
			Object node = path.getLastPathComponent();
			if (node instanceof TestNode) {
				TestNode testNode = (TestNode) node;
				if (testNode.getTestDetails().getTestMethodName().equals(testMethod)
						&& testNode.getTestDetails().getTestClassName().equals(testPackage + "." + testClass)) {
					tree.setSelectionPath(path);
					tree.scrollPathToVisible(path);
					return testNode.createTestConfiguration(configuration);
				}
			}
		}

		return false;
	}

	public boolean canRunTests() {
		return createTestConfiguration(null);
	}

	private static JUnitConfiguration getJunitConfiguration(RunConfiguration config) {
		try {
			return (JUnitConfiguration) config;
		} catch (ClassCastException e) {
			LoggerImpl.getInstance().warn("Unexpected RunConfiguration instance", e);
		}
		return null;
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
			final String msg;
			if (failedTests == 0) {
				msg = " " + totalTests + " tests passed";
			} else {
				msg = " " + failedTests + "/" + totalTests + " tests failed";
			}
			return msg;
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

		public void addFailedTest(int tests) {
			this.failedTests += tests;

		}

		public void addTest(int tests) {
			this.totalTests += tests;
		}
	}

	private class JobNode extends NonLeafNode {
		private JobNode(String s, int totalTests, int failedTests) {
			super(s, totalTests, failedTests);
		}

		@Override
		public boolean navigate(boolean testOnly) {
			return false;
		}

		@Override
		public boolean createTestConfiguration(RunConfiguration configuration) {
			return false;
		}
	}

	private class PackageNode extends NonLeafNode {
		public PackageNode(String s, int totalTests, int failedTests) {
			super(s, totalTests, failedTests);
		}

		@Override
		public boolean navigate(boolean testOnly) {
			return false;
		}

		@Override
		public boolean createTestConfiguration(RunConfiguration configuration) {
			if (configuration != null) {
				JUnitConfiguration conf = getJunitConfiguration(configuration);
				if (conf != null) {
					conf.getPersistentData().TEST_OBJECT = JUnitConfiguration.TEST_PACKAGE;
					conf.getPersistentData().PACKAGE_NAME = toString();
				} else {
					return false;
				}
			}
			return true;
		}
	}

	private class AllTestsNode extends PackageNode {
		public AllTestsNode(final int totalTests, final int failedTests) {
			super("All", totalTests, failedTests);
		}

		@Override
		public boolean createTestConfiguration(RunConfiguration configuration) {
			if (configuration != null) {
				JUnitConfiguration conf = getJunitConfiguration(configuration);
				if (conf != null) {
					conf.getPersistentData().TEST_OBJECT = JUnitConfiguration.TEST_PACKAGE;
					conf.getPersistentData().PACKAGE_NAME = "";
				} else {
					return false;
				}
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
		public boolean navigate(boolean testOnly) {
			PsiClass cls = IdeaVersionFacade.getInstance().findClass(className, project);
			if (cls != null) {
				if (!testOnly) {
					cls.navigate(true);
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean createTestConfiguration(RunConfiguration configuration) {
			PsiClass cls = IdeaVersionFacade.getInstance().findClass(className, project);
			if (cls == null) {
				return false;
			}
			if (configuration != null) {
				JUnitConfiguration conf = getJunitConfiguration(configuration);
				if (conf != null) {
					conf.beClassConfiguration(cls);
				} else {
					return false;
				}
			}
			return true;
		}
	}

	private abstract class TestNode extends AbstractTreeNode {
		protected TestDetails testDetails;

		public TestNode(TestDetails testDetails) {
			super(testDetails.getTestMethodName());
			this.testDetails = testDetails;
		}

		public TestDetails getTestDetails() {
			return testDetails;
		}

		private PsiMethod getMethod() {
			PsiClass cls = IdeaVersionFacade.getInstance().findClass(testDetails.getTestClassName(), project);
			if (cls == null) {
				return null;
			}
			PsiMethod[] methods = cls.findMethodsByName(testDetails.getTestMethodName(), false);
			if (methods.length == 0 || methods[0] == null) {
				return null;
			}

			return methods[0];
		}

		@Override
		public boolean navigate(boolean testOnly) {
			PsiMethod m = getMethod();
			if (m != null) {
				if (!testOnly) {
					m.navigate(true);
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean createTestConfiguration(RunConfiguration configuration) {
			PsiMethod m = getMethod();

			if (m != null) {
				if (configuration != null) {
					JUnitConfiguration conf = getJunitConfiguration(configuration);
					if (conf != null) {
						conf.beMethodConfiguration(PsiLocation.fromPsiElement(project, m));
					} else {
						return false;
					}
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
			print(testDetails.getErrors());
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


	private Map<String, PackageNode> getPackagesNodes(List<TestDetails> failedTests, List<TestDetails> successfulTests) {
		Map<String, PackageNode> packages = new LinkedHashMap<String, PackageNode>();
		Map<String, NonLeafNode> classes = new LinkedHashMap<String, NonLeafNode>();

		//add packages and count SUCCESSFUL tests
		for (TestDetails test : successfulTests) {
			String fqcn = test.getTestClassName();
			String pkg = getPackageFromClassName(fqcn);
			NonLeafNode n = packages.get(pkg);
			if (n == null) {
				packages.put(pkg, new PackageNode(pkg, 1, 0));
			} else {
				n.addTest();
			}
		}
		//add packages and count FAILED tests
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

		//add class to package NODES
		for (Map.Entry<String, NonLeafNode> c : classes.entrySet()) {
			String fqcn = c.getKey();
			String pkg = getPackageFromClassName(fqcn);
			NonLeafNode n = c.getValue();
			packages.get(pkg);
			if (n.isFailed() || passedTestsVisible) {
				packages.get(pkg).add(n);
			}
		}
		//add Ssuccessful test to class name
		if (passedTestsVisible) {
			for (TestDetails d : succeededTests) {
				String fqcn = d.getTestClassName();
				AbstractTreeNode node = classes.get(fqcn);
				node.add(new TestSuccessInfoNode(d));
			}
		}

		//add failed test to class name
		for (TestDetails d : failedTests) {
			String fqcn = d.getTestClassName();
			AbstractTreeNode node = classes.get(fqcn);
			node.add(new TestErrorInfoNode(d));
		}

		return packages;

	}

	private JTree createTestTree() {
		NonLeafNode root;

		if (buildDetails.getJobs() != null) {
			root = new AllTestsNode(0, 0);

			for (BambooJob job : buildDetails.getJobs()) {

				JobNode jobNode = new JobNode(job.getShortName().equals("") ? job.getName() : job.getShortName(),
						job.getFailedTests().size() + job.getSuccessfulTests().size(),
						job.getFailedTests().size());

				root.addFailedTest(job.getFailedTests().size());
				root.addTest(job.getSuccessfulTests().size());

				if (!passedTestsVisible && job.getFailedTests().size() == 0) {
					continue;
				}
				root.add(jobNode);
				for (Map.Entry<String, PackageNode> e : getPackagesNodes(job.getFailedTests(), job.getSuccessfulTests()).entrySet()) {
					PackageNode n = e.getValue();
					if (n.isFailed() || passedTestsVisible) {
						//if no jobs then add to root !!!!!
						jobNode.add(n);
					}
				}
			}
		} else {//no jobs old Bamboo version

			List<TestDetails> failedTests = buildDetails.getFailedTestDetails();
			List<TestDetails> successfulTests = buildDetails.getSuccessfulTestDetails();

					root = new AllTestsNode(
					failedTests.size() + successfulTests.size(), failedTests.size());
			for (Map.Entry<String, PackageNode> e : getPackagesNodes(failedTests, successfulTests).entrySet()) {
				PackageNode n = e.getValue();
				if (n.isFailed() || passedTestsVisible) {
					//if no jobs then add to root !!!!!
					root.add(n);
				}
			}

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
		testTree.addMouseListener(new PopupAwareMouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					TreePath path = tree.getPathForLocation(e.getX(), e.getY());
					if (path != null) {
						AbstractTreeNode node = (AbstractTreeNode) path.getLastPathComponent();
						jumpToSource(node);
					}
				}
			}

			@Override
			protected void onPopup(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1 && selPath != null) {
					tree.setSelectionPath(selPath);
					launchPopup(e);
				}
			}
		});

		DefaultTreeCellRenderer renderer = new MyDefaultTreeCellRenderer();
		testTree.setCellRenderer(renderer);

		return testTree;
	}

	public boolean canJumpToSource() {
		TreePath p = tree.getSelectionPath();
		if (p != null) {
			AbstractTreeNode node = (AbstractTreeNode) p.getLastPathComponent();
			return node != null && node.navigate(true);
		}
		return false;
	}

	public void jumpToSource() {
		TreePath p = tree.getSelectionPath();
		if (p != null) {
			jumpToSource((AbstractTreeNode) p.getLastPathComponent());
		}
	}

	private void jumpToSource(AbstractTreeNode node) {
		if (node != null) {
			node.navigate(false);
		}
	}

	private void launchPopup(MouseEvent e) {
		final DefaultActionGroup actionGroup = new DefaultActionGroup();
		final ActionGroup configActionGroup = (ActionGroup) ActionManager
				.getInstance().getAction("ThePlugin.Bamboo.TestResultsPopupMenu");
		actionGroup.addAll(configActionGroup);

		final ActionPopupMenu popup = ActionManager.getInstance().createActionPopupMenu(contentKey, actionGroup);

		final JPopupMenu jPopupMenu = popup.getComponent();
		jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
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
				if (statsColor == null) {
					LoggerImpl.getInstance().warn("Cannot determine system color for tree selection. Using black.");
					statsColor = Color.BLACK;
				}
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

		public abstract boolean navigate(boolean testOnly);

		public abstract boolean createTestConfiguration(RunConfiguration configuration);
	}
}
