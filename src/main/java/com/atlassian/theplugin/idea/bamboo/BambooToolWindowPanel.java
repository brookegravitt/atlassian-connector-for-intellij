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

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.cfg.ConfigurationListenerAdapter;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.atlassian.theplugin.configuration.BambooProjectConfiguration;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.tree.BuildTree;
import com.atlassian.theplugin.idea.bamboo.tree.BuildTreeModel;
import com.atlassian.theplugin.idea.config.ProjectCfgManager;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeRenderer;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
import com.atlassian.theplugin.util.Util;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SearchTextField;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.Collection;

/**
 * @author Wojciech Seliga
 */
public class BambooToolWindowPanel extends TwoPanePanel implements DataProvider {

	public static final String PLACE_PREFIX = BambooToolWindowPanel.class.getSimpleName();
	private static final TreeCellRenderer TREE_RENDERER = new TreeRenderer();
	private final Project project;
	private final BambooModel bambooModel;
	private final ProjectCfgManager projectCfgManager;
	private final BuildTree buildTree;
	private final BambooFilterList filterList;
	private SearchTextField searchField = new SearchTextField();
	private JComponent toolBar;
	private BambooProjectConfiguration bambooConfiguration;
	private BuildGroupBy groupBy = BuildGroupBy.NONE;

	public BambooFilterType getBambooFilterType() {
		return filterList.getBambooFilterType();
	}

	public BambooToolWindowPanel(@NotNull final Project project,
			@NotNull final BambooModel bambooModel,
			@NotNull final ProjectConfigurationBean projectConfiguration,
			@NotNull final ProjectCfgManager projectCfgManager,
			@NotNull final UiTaskExecutor uiTaskExecutor) {

		this.project = project;
		this.bambooModel = bambooModel;
		this.bambooConfiguration = projectConfiguration.getBambooConfiguration();
		this.projectCfgManager = projectCfgManager;

		final ProjectId projectId = CfgUtil.getProjectId(project);
		filterList = new BambooFilterList(projectCfgManager, projectId, bambooModel);
		projectCfgManager.getCfgManager().addProjectConfigurationListener(projectId, new ConfigurationListenerAdapter() {
			@Override
			public void bambooServersChanged(final ProjectConfiguration newConfiguration) {
				filterList.update();
			}
		});

		filterList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(final ListSelectionEvent e) {
				final BambooBuildFilter filter = filterList.getSelection();
				bambooModel.setFilter(filter);
			}
		});

		bambooModel.addListener(new BambooModelListener() {
			public void filterChanged() {
				updateTree();
			}

			public void buildsChanged(@Nullable final Collection<String> additionalInfo,
					@Nullable final Collection<String> errors) {
				StringBuilder sb = new StringBuilder();
				if (additionalInfo != null) {
					for (String s : additionalInfo) {
						sb.append(s).append(Util.HTML_NEW_LINE);
					}
				}
				if (errors != null) {
					for (String s : errors) {
						sb.append(s).append(Util.HTML_NEW_LINE);
					}
				}
				setStatusMessage(sb.toString(), errors != null && errors.size() > 0);
				filterList.update();
				updateTree();
			}

			private void updateTree() {
				final Collection<BambooBuildAdapterIdea> ideas = bambooModel.getBuilds();
				buildTree.updateModel(ideas);
			}
		});

		// restore GroupBy setting
		if (bambooConfiguration != null && bambooConfiguration.getView() != null
				&& bambooConfiguration.getView().getGroupBy() != null) {
			groupBy = bambooConfiguration.getView().getGroupBy();
		}

		buildTree = new BuildTree(project, groupBy, new BuildTreeModel());
		toolBar = createToolBar();
		init();
		TreeUISetup uiSetup = new TreeUISetup(TREE_RENDERER);
		uiSetup.initializeUI(buildTree, getRightScrollPane());
		addBuildTreeListeners();
		addSearchBoxListener();
		setLeftPaneVisible(filterList.getBambooFilterType() != null);

	}



	private void addBuildTreeListeners() {
		buildTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				final BambooBuildAdapterIdea buildDetailsInfo = buildTree.getSelectedBuild();
				if (e.getKeyCode() == KeyEvent.VK_ENTER && buildDetailsInfo != null) {
					openBuild(buildDetailsInfo);
				}
			}
		});

		buildTree.addMouseListener(new PopupAwareMouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				final BambooBuildAdapterIdea buildDetailsInfo = buildTree.getSelectedBuild();
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && buildDetailsInfo != null) {
					openBuild(buildDetailsInfo);
				}
			}

			@Override
			protected void onPopup(MouseEvent e) {
				int selRow = buildTree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = buildTree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1 && selPath != null) {
					buildTree.setSelectionPath(selPath);
					final BambooBuildAdapterIdea buildDetailsInfo = buildTree.getSelectedBuild();
					if (buildDetailsInfo != null) {
						launchContextMenu(e);
					}
				}
			}
		});
	}

	private void launchContextMenu(MouseEvent e) {
//		final DefaultActionGroup actionGroup = new DefaultActionGroup();
//
//		final ActionGroup configActionGroup = (ActionGroup) ActionManager
//				.getInstance().getAction("ThePlugin.Reviews.ReviewPopupMenu");
//		actionGroup.addAll(configActionGroup);
//
//		final ActionPopupMenu popup = ActionManager.getInstance().createActionPopupMenu(getActionPlaceName(), actionGroup);
//
//		final JPopupMenu jPopupMenu = popup.getComponent();
//		jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	private void openBuild(final BambooBuildAdapterIdea buildDetailsInfo) {
		IdeaHelper.getBuildToolWindow(project).showBuild(getSelectedBuild());
	}


	protected void addSearchBoxListener() {
		searchField.addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
//				searchingReviewListModel.setSearchTerm(getSearchField().getText());
			}

			public void removeUpdate(DocumentEvent e) {
//				searchingReviewListModel.setSearchTerm(getSearchField().getText());
			}

			public void changedUpdate(DocumentEvent e) {
//				searchingReviewListModel.setSearchTerm(getSearchField().getText());
			}
		});

		searchField.addKeyboardListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchField.addCurrentTextToHistory();
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});
	}

	@Override
	public JTree getRightTree() {
		return buildTree;
	}


	public Object getData(@NonNls final String dataId) {
		return null;
	}

	public void setGroupingType(@NonNls final BuildGroupBy groupingType) {
		if (groupingType != null) {
			this.groupBy = groupingType;
			buildTree.groupBy(groupingType);
			bambooConfiguration.getView().setGroupBy(groupingType);
		}
	}

	public void setBambooFilterType(@Nullable final BambooFilterType bambooFilterType) {
//		this.bambooFilterType = bambooFilterType;
		filterList.setBambooFilterType(bambooFilterType);
		setLeftPaneVisible(filterList.getBambooFilterType() != null);
		bambooModel.setFilter(null);
		// by default there should be "ALL", which means null filter
		buildTree.updateModel(bambooModel.getBuilds());


	}

	public void refresh() {
		// I doubt if it's really necessary as refreshing anyway comes now asynchrounsly from Bamboo Status Chekcker.
		// However we could make it synchronous and show then error message if anything fails
	}


	@Override
	protected JComponent getLeftPanel() {
		return filterList;
	}

	@Override
	protected JComponent getToolBar() {
		return toolBar;
	}

	private JComponent createToolBar() {
		final JComponent leftPart = loadToolBar("ThePlugin.Bamboo.LeftToolBar");
		final JComponent middlePart = loadToolBar("ThePlugin.Bamboo.MiddleToolBar");
		final JComponent rightPart = loadToolBar("ThePlugin.Bamboo.RightToolBar");


		final JPanel toolBarPanel = new JPanel(
				new FormLayout("left:pref, left:pref, left:pref, left:pref, left:pref, right:pref:grow", "pref:grow"));
		CellConstraints cc = new CellConstraints();
		int col = 1;
		if (leftPart != null) {
			toolBarPanel.add(leftPart, cc.xy(col++, 1));
		}

		if (middlePart != null) {
			toolBarPanel.add(new JLabel("Filter By "), cc.xy(col++, 1));
			toolBarPanel.add(middlePart, cc.xy(col++, 1));
		}

		if (rightPart != null) {
			toolBarPanel.add(new JLabel("Group By "), cc.xy(col++, 1));
			toolBarPanel.add(rightPart, cc.xy(col++, 1));
		}

		toolBarPanel.add(searchField, cc.xy(col, 1));
		return toolBarPanel;
	}

	@Nullable
	private JComponent loadToolBar(final String toolbarName) {
		ActionManager actionManager = ActionManager.getInstance();
		ActionGroup toolbar = (ActionGroup) actionManager.getAction(toolbarName);
		if (toolbar != null) {
			final ActionToolbar actionToolbar =
					actionManager.createActionToolbar(PLACE_PREFIX + project.getName(), toolbar, true);
			actionToolbar.setTargetComponent(this);
			return actionToolbar.getComponent();
		}
		return null;
	}


	public BuildGroupBy getGroupBy() {
		return groupBy;
	}

	public BambooBuildAdapterIdea getSelectedBuild() {
		return buildTree.getSelectedBuild();
	}
}
