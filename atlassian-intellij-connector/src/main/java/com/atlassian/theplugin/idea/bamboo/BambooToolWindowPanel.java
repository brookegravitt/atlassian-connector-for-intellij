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
import com.atlassian.theplugin.commons.bamboo.BuildDetailsInfo;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.bamboo.tree.BuildTree;
import com.atlassian.theplugin.idea.bamboo.tree.BuildTreeModel;
import com.atlassian.theplugin.idea.config.ProjectCfgManager;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeRenderer;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
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
	private final BambooFilterPanel filterList;

	public BambooFilterType getBambooFilterType() {
		return filterList.getBambooFilterType();
	}

	public BambooToolWindowPanel(@NotNull final Project project,
			@NotNull final BambooModel bambooModel,
			@NotNull final ProjectConfigurationBean projectConfiguration,
			@NotNull final ProjectCfgManager projectCfgManager,
			@NotNull final UiTaskExecutor uiTaskExecutor) {
		super("ThePlugin.Bamboo.LeftToolBar", "ThePlugin.Bamboo.RightToolBar", PLACE_PREFIX + project.getName());
		this.project = project;
		this.bambooModel = bambooModel;
		filterList = new BambooFilterPanel(projectCfgManager, CfgUtil.getProjectId(project), bambooModel);
//		filterList = new JList(createListModel(BambooFilterType.STATE));

		filterList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(final ListSelectionEvent e) {

//				final BambooBuildFilter filter = createBuildFilter(bambooFilterType, filterList.getSelectedValues());
				final BambooBuildFilter filter = filterList.getSelection();
				bambooModel.setFilter(filter);
			}
		});

		bambooModel.addListener(new BambooModelListener() {
			public void filterChanged() {
				updateTree();
			}

			public void buildsChanged() {
//				filterList.setModel(updateFilterModel(bambooModel.getAllBuilds()));
				filterList.update();
				updateTree();
			}

			private void updateTree() {
				final Collection<BambooBuildAdapterIdea> ideas = bambooModel.getBuilds();
				buildTree.updateModel(ideas);
			}
		});
		this.projectCfgManager = projectCfgManager;
		buildTree = new BuildTree(new BuildTreeModel());
		init();
		TreeUISetup uiSetup = new TreeUISetup(TREE_RENDERER);
		uiSetup.initializeUI(buildTree, getRightScrollPane());
		addBuildTreeListeners();
	}



	private void addBuildTreeListeners() {
		buildTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				final BuildDetailsInfo buildDetailsInfo = buildTree.getSelectedBuild();
				if (e.getKeyCode() == KeyEvent.VK_ENTER && buildDetailsInfo != null) {
					openBuild(buildDetailsInfo);
				}
			}
		});

		buildTree.addMouseListener(new PopupAwareMouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				final BuildDetailsInfo buildDetailsInfo = buildTree.getSelectedBuild();
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
					final BuildDetailsInfo buildDetailsInfo = buildTree.getSelectedBuild();
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
	
	private void openBuild(final BuildDetailsInfo buildDetailsInfo) {

	}


	@Override
	protected void addSearchBoxListener() {
		getSearchField().addDocumentListener(new DocumentListener() {
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

		getSearchField().addKeyboardListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					getSearchField().addCurrentTextToHistory();
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});
	}

	@Override
	public JTree createRightTree() {
		return buildTree;
	}


	public Object getData(@NonNls final String dataId) {
		return null;
	}

	public void setGroupingType(final BuildGroupBy groupingType) {
	}

	public void setBambooFilterType(@Nullable final BambooFilterType bambooFilterType) {
//		this.bambooFilterType = bambooFilterType;
		filterList.setBambooFilterType(bambooFilterType);
		bambooModel.setFilter(null);
		// by default there should be "ALL", which means null filter
		buildTree.updateModel(bambooModel.getBuilds());


	}

/*
	private DefaultListModel updateFilterModel(@NotNull final Collection<BambooBuildAdapterIdea> buildStatuses) {
		final DefaultListModel listModel = new DefaultListModel();
		if (bambooFilterType == null) {
			return listModel;
		}
		switch (bambooFilterType) {
			case PROJECT:
				break;
			case SERVER: {
				Map<BambooServerCfg, Integer> hitMap = new LinkedHashMap<BambooServerCfg, Integer>();
				final Collection<BambooServerCfg> bambooServers = projectCfgManager.getCfgManager()
						.getAllEnabledBambooServers(CfgUtil.getProjectId(project));

				for (BambooServerCfg bambooServer : bambooServers) {
					hitMap.put(bambooServer, 0);
				}
				for (BambooBuildAdapterIdea buildStatus : buildStatuses) {
					final Integer integer = hitMap.get(buildStatus.getServer());
					hitMap.put(buildStatus.getServer(), integer != null ? integer + 1 : 1);
				}

				for (Map.Entry<BambooServerCfg, Integer> entry : hitMap.entrySet()) {
					listModel.addElement(new BamboServerCfgWrapper(entry.getKey(), entry.getValue()));

				}
			}
				break;
			case STATE: {

				Map<BuildStatus, Integer> hitMap = new LinkedHashMap<BuildStatus, Integer>();
				for (BuildStatus buildStatus : BuildStatus.values()) {
					hitMap.put(buildStatus, 0);
				}
				for (BambooBuildAdapterIdea buildAdapterIdea : buildStatuses) {
					final Integer integer = hitMap.get(buildAdapterIdea.getStatus());
					hitMap.put(buildAdapterIdea.getStatus(), integer != null ? integer + 1 : 1);
				}

				for (Map.Entry<BuildStatus, Integer> entry : hitMap.entrySet()) {
					listModel.addElement(new BuildStatusWrapper(entry.getKey(), entry.getValue()));

				}
			}
		}
		return listModel;
	}
*/


	public void refresh() {
		// I doubt if it's really necessary as refreshing anyway comes now asynchrounsly from Bamboo Status Chekcker.
		// However we could make it synchronous and show then error message if anything fails
	}



/*

	private ListModel createListModel(BambooFilterType filterType) {
		final DefaultListModel listModel = new DefaultListModel();
		switch (filterType) {
			case PROJECT:
				Set<BambooProjectBean> projects = new TreeSet<BambooProjectBean>();
				for (BambooBuildAdapterIdea build : bambooModel.getAllBuilds()) {
					projects.add(new BambooProjectBean(build.getProjectName()));
				}
				for (BambooProjectBean bambooProject : projects) {
					listModel.addElement(bambooProject);
				}
				break;
			case SERVER:
				final Collection<BambooServerCfg> bambooServers = projectCfgManager.getCfgManager()
						.getAllEnabledBambooServers(CfgUtil.getProjectId(project));
				for (BambooServerCfg bambooServer : bambooServers) {
					listModel.addElement(new BamboServerCfgWrapper(bambooServer, 0));
				}
				break;
			case STATE:
				for (BuildStatus buildStatus : BuildStatus.values()) {
					listModel.addElement(new BuildStatusWrapper(buildStatus, 0));
				}
				break;
			default:
				break;
		}
		return listModel;
	}
*/

	@Override
	protected JComponent getLeftPanel() {
		return filterList;
	}

}
