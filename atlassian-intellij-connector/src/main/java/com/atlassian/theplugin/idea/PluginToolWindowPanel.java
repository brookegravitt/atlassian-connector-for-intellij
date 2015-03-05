package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.idea.jira.StatusBarIssuesPane;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.SearchTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;

/**
 * User: pmaruszak
 */
public abstract class PluginToolWindowPanel extends JPanel {
	public static final float PANEL_SPLIT_RATIO = 0.3f;
	protected static final float MANUAL_FILTER_PROPORTION_VISIBLE = 0.8f;
	protected static final float MANUAL_FILTER_PROPORTION_HIDDEN = 0.9f;

	protected Project project;
	private StatusBarIssuesPane statusBarPane;
	private final Splitter splitPane = new Splitter(true, PANEL_SPLIT_RATIO);
	private Splitter splitLeftPane;
	private JPanel rightPanel;
	private JScrollPane rightScrollPane;
	private SearchTextField searchField = new SearchTextField();
	private JTree rightTree;
	private JTree leftTree;
	private JScrollPane leftUpperScrollPane;
	private String rightToolbarName;
	private String leftToolbarName;

	public PluginToolWindowPanel(@NotNull final Project project, String leftToolbarName, String rightToolbarName) {

		this.project = project;
		setLayout(new BorderLayout());
		this.leftToolbarName = leftToolbarName;
		this.rightToolbarName = rightToolbarName;

		this.statusBarPane = new StatusBarIssuesPane("");
		add(statusBarPane, BorderLayout.SOUTH);
		splitPane.setShowDividerControls(false);
		splitPane.setHonorComponentsMinimumSize(false);

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

	public void init(int margin) {
		splitPane.setFirstComponent(createLeftContent());
        splitPane.setSecondComponent(createRightContent());
		leftUpperScrollPane.setViewportView(getLeftTree());
		leftUpperScrollPane.setViewportBorder(BorderFactory.createEmptyBorder(0, margin, 0, 0));
		rightScrollPane.setViewportView(getRightTree());
		addSearchBoxListener();
	}

	public void enableGetMoreIssues(boolean enable) {
		statusBarPane.enableGetMoreIssues(enable);
	}

	@NotNull
	public Project getProject() {
		return project;
	}

	public Splitter getSplitLeftPane() {
		return splitLeftPane;
	}

	public StatusBarIssuesPane getStatusBarPane() {
		return statusBarPane;
	}

//	public void setStatusBarPane(StatusBarIssuesPane statusBarPane) {
//		this.statusBarPane = statusBarPane;
//	}

	public JComponent createLeftContent() {
		JPanel leftPanel = new JPanel(new BorderLayout());

		leftUpperScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		leftUpperScrollPane.setWheelScrollingEnabled(true);
		splitLeftPane = new Splitter(false, MANUAL_FILTER_PROPORTION_HIDDEN);
		splitLeftPane.setOrientation(true);
		splitLeftPane.setShowDividerControls(true);
		splitLeftPane.setHonorComponentsMinimumSize(true);

//		JScrollPane leftDownScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
//				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		leftPanel.add(splitLeftPane, BorderLayout.CENTER);

		final JComponent toolBar = createToolBar(leftToolbarName, getActionPlaceName());

		if (toolBar != null) {
			leftPanel.add(toolBar, BorderLayout.NORTH);
		}

		splitLeftPane.setFirstComponent(leftUpperScrollPane);

		return leftPanel;

	}

	public JScrollPane getRightScrollPane() {
		return rightScrollPane;
	}

	public void setRightScrollPane(JScrollPane rightScrollPane) {
		this.rightScrollPane = rightScrollPane;
	}

	public SearchTextField getSearchField() {
		return searchField;
	}

	private JComponent createToolBar(String toolbarName, String toolbarPalce) {
		JComponent component = null;
		ActionManager actionManager = ActionManager.getInstance();
		ActionGroup toolbar = (ActionGroup) actionManager.getAction(toolbarName);
		if (toolbar != null) {
			final ActionToolbar actionToolbar = actionManager.createActionToolbar(toolbarPalce, toolbar, true);
			actionToolbar.setTargetComponent(this);
			component = actionToolbar.getComponent();
		}
		return component;
	}

	public void setStatusErrorMessage(final String error, final Throwable exception) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				statusBarPane.setErrorMessage(error, exception);
			}
		});
	}


	public void setStatusErrorMessages(final String error, final Collection<Throwable> exceptions) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				statusBarPane.setErrorMessages(error, exceptions);
			}
		});
	}

	public void setStatusErrorMessage(final String error) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				statusBarPane.setErrorMessage(error);
			}
		});
	}

	public void setStatusInfoMessage(final String message, boolean overrideError) {
		setStatusInfoMessage(message, false, overrideError);
	}

	public void setStatusInfoMessage(final String msg, final boolean rightAlign, final boolean overrideError) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				statusBarPane.setInfoMessage(msg, rightAlign, overrideError);
			}
		});
	}

	private JComponent createRightContent() {
		rightPanel = new JPanel(new BorderLayout());

		rightScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		rightScrollPane.setWheelScrollingEnabled(true);

		rightPanel.add(rightScrollPane, BorderLayout.CENTER);

//		CellConstraints cc = new CellConstraints();
		JComponent rightToolbar = createToolBar(rightToolbarName, getActionPlaceName());

//		final JPanel toolBarPanel = new JPanel(
//				new FormLayout("left:pref, left:pref:grow, right:pref:grow", "pref:grow"));
//		toolBarPanel.add(new JLabel("Group By "), cc.xy(1, 1));
//
//		if (rightToolbar != null) {
//			toolBarPanel.add(rightToolbar, cc.xy(1 + 1, 1));
//		}
//		toolBarPanel.add(searchField, cc.xy(1 + 2, 1));

		final JPanel toolBarPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
//		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
//		gbc.fill = GridBagConstraints.NONE;
//		JLabel groupByLabel = new JLabel("Group By ");
//		groupByLabel.setMinimumSize(new Dimension(0, getPreferredSize().height));
//		toolBarPanel.add(groupByLabel, gbc);

//		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		if (rightToolbar != null) {
			toolBarPanel.add(rightToolbar, gbc);
		}
		gbc.gridx++;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_END;

		searchField.setMinimumSize(searchField.getPreferredSize());
		searchField.setMaximumSize(searchField.getPreferredSize());
		toolBarPanel.add(searchField, gbc);

		rightPanel.add(toolBarPanel, BorderLayout.NORTH);

		return rightPanel;
	}

	public JPanel getRightPanel() {
		return rightPanel;
	}

	public JTree getRightTree() {
        JTree rTree = rightTree;

		if (rightTree == null) {
			rTree = createRightTree();
            rightTree = rTree;
		}
		return rTree;
	}

	public JTree getLeftTree() {
        JTree lTree  = leftTree;
        
		if (leftTree == null) {
			lTree = createLeftTree();
            leftTree = lTree;
		}
		return lTree;
	}

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

	protected abstract void addSearchBoxListener();

	protected abstract JTree createRightTree();

	public abstract JTree createLeftTree();

	public abstract String getActionPlaceName();
	//public abstract JComponent createCustomFiltedPanel();

}
