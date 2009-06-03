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
import com.atlassian.theplugin.commons.cfg.ConfigurationListenerAdapter;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.atlassian.theplugin.configuration.BambooWorkspaceConfiguration;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.tree.BuildTree;
import com.atlassian.theplugin.idea.bamboo.tree.BuildTreeModel;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.SearchTextField;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
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
	private final Project project;
	private final BuildListModelImpl bambooModel;
	private final BuildTree buildTree;
	private final BambooFilterList filterList;
	private SearchTextField searchField = new SearchTextField();
	private JComponent toolBar;
	private BambooWorkspaceConfiguration bambooConfiguration;
	private BuildGroupBy groupBy = BuildGroupBy.NONE;
	private SearchBuildListModel searchBuildModel;

	public BambooFilterType getBambooFilterType() {
		return filterList.getBambooFilterType();
	}

	public BambooToolWindowPanel(@NotNull final Project project,
			@NotNull final BuildListModelImpl bambooModel,
			@NotNull final WorkspaceConfigurationBean projectConfiguration,
			@NotNull final ProjectCfgManagerImpl projectCfgManager) {

		this.project = project;
		this.bambooModel = bambooModel;
		this.bambooConfiguration = projectConfiguration.getBambooConfiguration();

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
				if (!e.getValueIsAdjusting()) {
					final BambooBuildFilter filter = filterList.getSelection();
					bambooModel.setFilter(filter);
				}
			}
		});

		bambooModel.addListener(new BuildListModelListener() {
			public void modelChanged() {
			}

			public void generalProblemsHappened(@Nullable Collection<Exception> generalExceptions) {
				if (generalExceptions != null && generalExceptions.size() > 0) {
					Exception e = generalExceptions.iterator().next();
					setErrorMessage(e.getMessage(), e);
				}
			}

			public void buildsChanged(@Nullable final Collection<String> additionalInfo,
					@Nullable final Collection<Pair<String, Throwable>> errors) {

				// we do not support multiple messages in status bar yet (waiting for inbox to be implemented)
				if (errors != null && !errors.isEmpty()) {

					// get last error message
					Pair<String, Throwable> error = (Pair<String, Throwable>) errors.toArray()[errors.size() - 1];

					setErrorMessage(error.getFirst(), error.getSecond());
				} else if (additionalInfo != null && !additionalInfo.isEmpty()) {
					setStatusMessage(additionalInfo.toArray(new String[1])[additionalInfo.size() - 1]);
				}
				filterList.update();
			}
		});

		searchBuildModel = new SearchBuildListModel(bambooModel);
		buildTree = new BuildTree(groupBy, new BuildTreeModel(searchBuildModel), getRightScrollPane());
		toolBar = createToolBar();
		init();
		addBuildTreeListeners();
		addSearchBoxListener();

		// restore GroupBy and FilterBy setting
		if (bambooConfiguration != null && bambooConfiguration.getView() != null) {
			if (bambooConfiguration.getView().getGroupBy() != null) {
				groupBy = bambooConfiguration.getView().getGroupBy();
                setGroupingType(groupBy);
			}
			if (bambooConfiguration.getView().getFilterType() != null) {
				setBambooFilterType(bambooConfiguration.getView().getFilterType());
			}
		}

		setLeftPaneVisible(filterList.getBambooFilterType() != null);
	}

	private void addBuildTreeListeners() {
		buildTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
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
		final DefaultActionGroup actionGroup = new DefaultActionGroup();
		final ActionGroup configActionGroup = (ActionGroup) ActionManager
				.getInstance().getAction("ThePlugin.Bamboo.BuildPopupMenuNew");
		actionGroup.addAll(configActionGroup);

		final ActionPopupMenu popup = ActionManager.getInstance().createActionPopupMenu(getActionPlaceName(), actionGroup);

		final JPopupMenu jPopupMenu = popup.getComponent();
		jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	private String getActionPlaceName() {
		return PLACE_PREFIX + project.getName();
	}

	private void openBuild(final BambooBuildAdapterIdea buildDetailsInfo) {
		if (buildDetailsInfo != null && buildDetailsInfo.isBamboo2()
				&& buildDetailsInfo.areActionsAllowed()) {
			IdeaHelper.getBuildToolWindow(project).showBuild(buildDetailsInfo);
		}
	}

	protected void addSearchBoxListener() {
		searchField.addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				searchBuildModel.setSearchTerm(getSearchField().getText());
			}

			public void removeUpdate(DocumentEvent e) {
				searchBuildModel.setSearchTerm(getSearchField().getText());
			}

			public void changedUpdate(DocumentEvent e) {
				searchBuildModel.setSearchTerm(getSearchField().getText());
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

	private SearchTextField getSearchField() {
		return searchField;
	}

	@Override
	public JTree getRightTree() {
		return buildTree;
	}

	public Object getData(@NonNls final String dataId) {
		if (dataId.equals(Constants.SERVER)) {
			// return server of selected build
			if (buildTree.getSelectedBuild() != null) {
				return buildTree.getSelectedBuild().getServer();
			}

			final BambooBuildFilter filter = filterList.getSelection();

			// return server of selected filter in case of server filtering
			if (getBambooFilterType() == BambooFilterType.SERVER
					&& filter != null && filter instanceof BambooCompositeOrFilter) {

				BambooCompositeOrFilter filterImpl = (BambooCompositeOrFilter) filter;

				Collection<BambooBuildFilter> filters = filterImpl.getFilters();
				for (BambooBuildFilter buildFilter : filters) {
					if (buildFilter instanceof BambooFilterList.BambooServerFilter) {
						BambooFilterList.BambooServerFilter serverFilter =
								(BambooFilterList.BambooServerFilter) buildFilter;

						return serverFilter.getBambooServerCfg();
					}
				}
			}
		}

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
		filterList.setBambooFilterType(bambooFilterType);
		setLeftPaneVisible(filterList.getBambooFilterType() != null);
		bambooModel.setFilter(null);
		// by default there should be "ALL", which means null filter
//		buildTree.updateModel(bambooModel.getBuilds());

		bambooConfiguration.getView().setFilterType(bambooFilterType);
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
		final JPanel toolBarPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		toolBarPanel.add(loadToolBar("ThePlugin.Bamboo.ToolBar"), gbc);
		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.FIRST_LINE_END;
		searchField.setMinimumSize(searchField.getPreferredSize());
		toolBarPanel.add(searchField, gbc);

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
