package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.idea.jira.StatusBarIssuesPane;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.SearchTextField;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * User: pmaruszak
 */
public abstract class PluginToolWindowPanel extends JPanel {
	private static final float ISSUES_PANEL_SPLIT_RATIO = 0.3f;
	protected static final float MANUAL_FILTER_PROPORTION_VISIBLE = 0.5f;
	protected static final float MANUAL_FILTER_PROPORTION_HIDDEN = 0.9f;

	private Project project;
	private CfgManager cfgManager;
	private StatusBarIssuesPane statusBarPane;
	private final Splitter splitPane = new Splitter(true, ISSUES_PANEL_SPLIT_RATIO);
	private Splitter splitLeftPane;
	private JPanel leftPanel;
	private JPanel rightPanel;
	private JScrollPane rightScrollPane;
	private SearchTextField searchField = new SearchTextField();
	private JTree rightTree;
	private JTree leftTree;
	private JScrollPane leftUpperScrollPane;
	private JScrollPane leftDownScrollPane;

	public PluginToolWindowPanel(@NotNull final Project project,
								 @NotNull final CfgManager cfgManager,
								 String leftToolbarName, String rightToolbarName) {

		this.project = project;
		this.cfgManager = cfgManager;
		setLayout(new BorderLayout());

		this.statusBarPane = new StatusBarIssuesPane("Issues panel");
		add(statusBarPane, BorderLayout.SOUTH);

		splitPane.setShowDividerControls(false);
		splitPane.setFirstComponent(createLeftContent(leftToolbarName, getActionPlaceName()));
		splitPane.setSecondComponent(createRightContent(rightToolbarName, getActionPlaceName()));
		splitPane.setHonorComponentsMinimumSize(true);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				final Dimension dimension = e.getComponent().getSize();
				final boolean doVertical = dimension.getWidth() < dimension.getHeight();
				if (doVertical != splitPane.getOrientation()) {
					splitPane.setOrientation(doVertical);
				}

			}
		});


		add(splitPane, BorderLayout.CENTER);
	}

	public void init() {
		leftUpperScrollPane.setViewportView(getLeftTree());
		rightScrollPane.setViewportView(getRightTree());
		addSearchBoxListener();
		
	}

	public void enableGetMoreIssues(boolean enable){
		statusBarPane.enableGetMoreIssues(enable);
	}

	public Project getProject() {
		return project;
	}

	public Splitter getSplitLeftPane() {
		return splitLeftPane;
	}

	public StatusBarIssuesPane getStatusBarPane() {
		return statusBarPane;
	}

	public void setStatusBarPane(StatusBarIssuesPane statusBarPane) {
		this.statusBarPane = statusBarPane;
	}

	public JComponent createLeftContent(String leftToolbarName, String leftToolbarPlace) {
		leftPanel = new JPanel(new BorderLayout());

		leftUpperScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);



		leftUpperScrollPane.setWheelScrollingEnabled(true);
		splitLeftPane = new Splitter(false, 0.5f);
		splitLeftPane.setOrientation(true);
		splitLeftPane.setShowDividerControls(true);
		splitLeftPane.setHonorComponentsMinimumSize(true);

		leftDownScrollPane =  new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		leftPanel.add(splitLeftPane, BorderLayout.CENTER);

		final JComponent toolBar = createToolBar(leftToolbarName, leftToolbarPlace);

		if (toolBar != null) {
			leftPanel.add(toolBar, BorderLayout.NORTH);
		}

		splitLeftPane.setFirstComponent(leftUpperScrollPane);

		return leftPanel;

	};


	public JScrollPane getRightScrollPane() {
		return rightScrollPane;
	}

	public void setRightScrollPane(JScrollPane rightScrollPane) {
		this.rightScrollPane = rightScrollPane;
	}

	public SearchTextField getSearchField() {
		return searchField;
	}

	private JComponent createToolBar(String toolbarName, String toolbarPalce){
		JComponent component = null;
		ActionManager actionManager = ActionManager.getInstance();
		ActionGroup toolbar = (ActionGroup) actionManager.getAction(toolbarName);
		if (toolbar != null) {
			component = actionManager.createActionToolbar(toolbarPalce, toolbar, true).getComponent();
		}
		return component;
	}





	public void setStatusMessage(final String message) {
		statusBarPane.setMessage(message);
	}

	public void setStatusMessage(final String msg, final boolean isError) {
		if (isError) {
			statusBarPane.setErrorMessage(msg);
		} else {
			statusBarPane.setMessage(msg);
		}
	}

	private JComponent createRightContent(String rightToolbarName, String rightToolbarPlace){
		rightPanel = new JPanel(new BorderLayout());

		rightScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		rightScrollPane.setWheelScrollingEnabled(true);

		rightPanel.add(rightScrollPane, BorderLayout.CENTER);

		CellConstraints cc = new CellConstraints();
		JComponent rightToolbar = createToolBar(rightToolbarName, rightToolbarPlace);

		final JPanel toolBarPanel = new JPanel(
				new FormLayout("left:pref, left:pref:grow, right:pref:grow", "pref:grow"));
		toolBarPanel.add(new JLabel("Group By "), cc.xy(1, 1));

		if (rightToolbar != null) {
			toolBarPanel.add(rightToolbar, cc.xy(1 + 1, 1));
		}
		toolBarPanel.add(searchField, cc.xy(1 + 2, 1));

		rightPanel.add(toolBarPanel, BorderLayout.NORTH);
		
		return rightPanel;
	};

	public JPanel getRightPanel() {
		return rightPanel;
	}

	public JTree getRightTree() {
		if (rightTree == null) {
			rightTree = createRightTree();
		}
		return rightTree;
	};

	public JTree getLeftTree() {
		if (leftTree == null) {
			leftTree = createLeftTree();
		}
		return leftTree;
	};

	public void expandAllRightTreeNodes() {
		for (int i = 0; i < getRightTree().getRowCount(); i++) {
			getRightTree().expandRow(i);
		}
	}

	public void collapseAllRightTreeNodes() {
		for (int i = 0; i < getRightTree().getRowCount(); i++) {
			getRightTree().collapseRow(i);
		}
	}

	public void expandAllLeftTreeNodes() {
		for (int i = 0; i < getLeftTree().getRowCount(); i++) {
			getLeftTree().expandRow(i);
		}
	}

	public void collapseAllLeftTreeNodes() {
		for (int i = 0; i < getLeftTree().getRowCount(); i++) {
			getLeftTree().collapseRow(i);
		}
	}

	public abstract void addSearchBoxListener();
	public abstract JTree createRightTree();
	public abstract JTree createLeftTree();
	public abstract void onEditButtonClickAction();
	public abstract String getActionPlaceName();
	//public abstract JComponent createCustomFiltedPanel();

}
