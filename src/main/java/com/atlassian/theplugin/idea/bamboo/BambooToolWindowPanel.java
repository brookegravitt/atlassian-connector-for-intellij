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
import com.atlassian.theplugin.commons.bamboo.BambooStatusListener;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.bamboo.BuildDetailsInfo;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.PluginToolWindowPanel;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeRenderer;
import com.atlassian.theplugin.idea.bamboo.tree.BuildTree;
import com.atlassian.theplugin.idea.bamboo.tree.BuildTreeModel;
import com.atlassian.theplugin.idea.config.GenericComboBoxItemWrapper;
import com.atlassian.theplugin.idea.config.ProjectCfgManager;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeCellRenderer;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;

/**
 * @author Wojciech Seliga
 */
public class BambooToolWindowPanel extends PluginToolWindowPanel implements DataProvider {

	public static final String PLACE_PREFIX = BambooToolWindowPanel.class.getSimpleName();
	private static final TreeCellRenderer TREE_RENDERER = new TreeRenderer();
	private final BambooModel bambooModel;
	private final ProjectCfgManager projectCfgManager;
	private final BuildTree buildTree;

	public BambooFilterType getBambooFilterType() {
		return bambooFilterType;
	}

	private BambooFilterType bambooFilterType;

	public BambooToolWindowPanel(@NotNull final Project project,
			@NotNull final BambooModel bambooModel,
			@NotNull final ProjectConfigurationBean projectConfiguration,
			@NotNull final ProjectCfgManager projectCfgManager,
			@NotNull final CrucibleReviewListModel reviewListModel,
			@NotNull final UiTaskExecutor uiTaskExecutor) {
		super(project, "ThePlugin.Bamboo.LeftToolBar", "ThePlugin.Bamboo.RightToolBar");
		this.bambooModel = bambooModel;
		bambooModel.addListener(new BambooModelListener() {
			public void filterChanged() {
				updateTree();
			}

			public void buildsChanged() {
				updateTree();
			}

			private void updateTree() {
				final Collection<BambooBuildAdapterIdea> ideas = bambooModel.getBuilds();
				buildTree.updateModel(ideas);
			}
		});
		this.projectCfgManager = projectCfgManager;
		this.bambooFilterType = BambooFilterType.STATE;
		buildTree = new BuildTree(new BuildTreeModel());
		init();
		TreeUISetup uiSetup = new TreeUISetup(TREE_RENDERER);
		uiSetup.initializeUI(buildTree, getRightScrollPane());
		addBuildTreeListeners();
	}

//	@Override
//	public void init() {
//		super.init();
////		getSplitLeftPane().setSecondComponent(null);
////		getSplitLeftPane().setProportion(MANUAL_FILTER_PROPORTION_HIDDEN);
//	}

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


	public BambooStatusListener getStatusCheckerListener() {
		if (buildTree == null) {
			createRightTree();
		}
		return null; //buildTree;
	}

	@Override
	public JTree createLeftTree() {
		final JTree tree = new JTree(toTreeModel(createListModel(BambooFilterType.STATE)));
		tree.setRootVisible(false);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(final TreeSelectionEvent e) {
				final BambooBuildFilter filter = createBuildFilter(bambooFilterType, tree.getSelectionPaths());
				bambooModel.setFilter(filter);
			}
		});
		return tree;
	}

	@Override
	public String getActionPlaceName() {
		return PLACE_PREFIX + this.getProject().getName();
	}

	public Object getData(@NonNls final String dataId) {
		return null;
	}

	public void setGroupingType(final BambooGroupingType groupingType) {
	}

	public void setBambooFilterType(@Nullable final BambooFilterType bambooFilterType) {
		this.bambooFilterType = bambooFilterType;
//		getLeftTree().setVisible(bambooFilterType != null);
		if (bambooFilterType == null) {
			getLeftTree().setModel(null);
		} else {
			final ListModel listModel = createListModel(bambooFilterType);

			DefaultMutableTreeNode root = new DefaultMutableTreeNode();
			for (int i = 0; i < listModel.getSize(); i++) {
				root.add(new DefaultMutableTreeNode(listModel.getElementAt(i)));
			}
			getLeftTree().setModel(new DefaultTreeModel(root));
		}
		// by default there should be "ALL", which means null filter
		bambooModel.setFilter(null);
		buildTree.updateModel(bambooModel.getBuilds());

//		DefaultMutableTreeNode root2 = new DefaultMutableTreeNode();
//		for (BambooBuildAdapterIdea build : bambooModel.getBuilds()) {
//			root2.add(new DefaultMutableTreeNode(build));
//		}
//		getRightTree().setModel(new DefaultTreeModel(root2));

	}

	@Nullable
	private BambooBuildFilter createBuildFilter(@Nullable final BambooFilterType filterType,
			@Nullable final TreePath[] selectedPaths) {
		if (filterType == null) {
			return null;
		}
		switch (filterType) {
			case PROJECT:
				Collection<String> projectKeys = MiscUtil.buildArrayList();
				if (selectedPaths != null) {
					for (TreePath selectedPath : selectedPaths) {
						if (selectedPath.getLastPathComponent() instanceof DefaultMutableTreeNode) {
							DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) selectedPath
									.getLastPathComponent();
							if (defaultMutableTreeNode.getUserObject() instanceof BambooProjectBean) {
								final BambooProjectBean projectBean
										= (BambooProjectBean) defaultMutableTreeNode.getUserObject();
								projectKeys.add(projectBean.name);
							}

						}
					}
				}
				return bambooModel.createProjectFilter(projectKeys);
			case SERVER:
				Collection<ServerCfg> serverCfgs = MiscUtil.buildArrayList();
				if (selectedPaths != null) {
					for (TreePath selectedPath : selectedPaths) {
						if (selectedPath.getLastPathComponent() instanceof DefaultMutableTreeNode) {
							DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) selectedPath
									.getLastPathComponent();
							if (defaultMutableTreeNode.getUserObject() instanceof BamboServerCfgWrapper) {
								final BamboServerCfgWrapper bamboServerCfgWrapper
										= (BamboServerCfgWrapper) defaultMutableTreeNode.getUserObject();
								serverCfgs.add(bamboServerCfgWrapper.getWrapped());
							}
						}
					}
				}
				return bambooModel.createServerFilter(serverCfgs);
			case STATE:
				Collection<BuildStatus> buildStatuses = MiscUtil.buildArrayList();
				if (selectedPaths != null) {
					for (TreePath selectedPath : selectedPaths) {
						if (selectedPath.getLastPathComponent() instanceof DefaultMutableTreeNode) {
							DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) selectedPath
									.getLastPathComponent();
							if (defaultMutableTreeNode.getUserObject() instanceof BuildStatusWrapper) {
								final BuildStatusWrapper buildStatusWrapper = (BuildStatusWrapper) defaultMutableTreeNode
										.getUserObject();
								buildStatuses.add(buildStatusWrapper.getWrapped());
							}
						}
					}
				}
				return bambooModel.createStateFilter(buildStatuses);
			default:
				throw new UnsupportedOperationException("Method not implemented for " + filterType);
		}
	}


	private static class BamboServerCfgWrapper extends GenericComboBoxItemWrapper<BambooServerCfg> {
		public BamboServerCfgWrapper(final BambooServerCfg wrapped) {
			super(wrapped);
		}

		@Override
		public String toString() {
			if (wrapped != null) {
				return wrapped.getName();
			}
			return "None";
		}
	}


	private static class BuildStatusWrapper extends GenericComboBoxItemWrapper<BuildStatus> {

		public BuildStatusWrapper(final BuildStatus wrapped) {
			super(wrapped);
		}

		@Override
		public String toString() {
			if (wrapped == null) {
				return "None";
			}
			switch (wrapped) {
				case BUILD_DISABLED:
					return "Build Disabled";
				case BUILD_FAILED:
					return "Build Failed";
				case BUILD_SUCCEED:
					return "Build Succeeded";
				case UNKNOWN:
					return "Unknown";
				default:
					return "???";
			}
		}
	}

	private static class BambooProjectBean implements Comparable<BambooProjectBean> {
		@NotNull
		private final String name;

		public BambooProjectBean(@NotNull final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			if (name.length() == 0) {
				return "[No Project Data]";
			}
			return name;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			final BambooProjectBean that = (BambooProjectBean) o;

			if (!name.equals(that.name)) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}

		public int compareTo(final BambooProjectBean o) {
			return name.compareTo(o.name);
		}
	}


	private TreeModel toTreeModel(ListModel listModel) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		for (int i = 0; i < listModel.getSize(); i++) {
			root.add(new DefaultMutableTreeNode(listModel.getElementAt(i)));
		}
		return new DefaultTreeModel(root);
	}

	private ListModel createListModel(BambooFilterType filterType) {
		final DefaultListModel listModel = new DefaultListModel();
		switch (filterType) {
			case PROJECT:
				Set<BambooProjectBean> projects = new TreeSet<BambooProjectBean>();
				for (BambooBuildAdapterIdea build : bambooModel.getAllBuilds()) {
					projects.add(new BambooProjectBean(build.getProjectName()));
				}
				for (BambooProjectBean project : projects) {
					listModel.addElement(project);
				}
				break;
			case SERVER:
				final Collection<BambooServerCfg> bambooServers = projectCfgManager.getCfgManager()
						.getAllEnabledBambooServers(CfgUtil.getProjectId(getProject()));
				for (BambooServerCfg bambooServer : bambooServers) {
					listModel.addElement(new BamboServerCfgWrapper(bambooServer));
				}
				break;
			case STATE:
				for (BuildStatus buildStatus : BuildStatus.values()) {
					listModel.addElement(new BuildStatusWrapper(buildStatus));
				}
				break;
			default:
				break;
		}
		return listModel;
	}

}
