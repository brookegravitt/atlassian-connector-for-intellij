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

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.intellij.bamboo.BambooBuildAdapter;
import com.atlassian.connector.intellij.bamboo.BambooStatusChecker;
import com.atlassian.connector.intellij.bamboo.IntelliJBambooServerFacade;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.bamboo.BambooServerData;
import com.atlassian.theplugin.commons.cfg.ConfigurationListenerAdapter;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.BambooWorkspaceConfiguration;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.idea.bamboo.tree.BuildTree;
import com.atlassian.theplugin.idea.bamboo.tree.BuildTreeModel;
import com.atlassian.theplugin.idea.config.ProjectConfigurationComponent;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
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
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Wojciech Seliga
 */
public class BambooToolWindowPanel extends ThreePanePanel implements DataProvider {

	public static final String PLACE_PREFIX = BambooToolWindowPanel.class.getSimpleName();
	private static final String COMPLETED_BUILDS = "Completed Builds";
	private final Project project;
	private final BuildListModelImpl bambooModel;
	private final ProjectCfgManager projectCfgManager;
	private final BuildTree buildTree;
	private final BambooFilterList filterList;
	private final SearchTextField searchField = new SearchTextField();
	private final JComponent leftToolBar;
	private final JComponent rightToolBar;
	private final BambooWorkspaceConfiguration bambooConfiguration;
	private BuildGroupBy groupBy = BuildGroupBy.PLAN_AND_BRANCH;
	private final SearchBuildListModel searchBuildModel;
	private final BuildHistoryPanel buildHistoryPanel;
	private JLabel planHistoryListLabel;

	public BambooFilterType getBambooFilterType() {
		return filterList.getBambooFilterType();
	}

	public BambooToolWindowPanel(@NotNull final Project project,
			@NotNull final BuildListModelImpl bambooModel,
			@NotNull final WorkspaceConfigurationBean projectConfiguration,
			@NotNull final ProjectCfgManager projectCfgManager) {

		this.project = project;
		this.bambooModel = bambooModel;
		this.projectCfgManager = projectCfgManager;
		this.bambooConfiguration = projectConfiguration.getBambooConfiguration();

		filterList = new BambooFilterList(projectCfgManager, bambooModel);
		projectCfgManager.addProjectConfigurationListener(new ConfigurationListenerAdapter() {
			@Override
			public void bambooServersChanged(final ProjectConfiguration newConfiguration) {
				filterList.update();
                buildTree.groupBy(groupBy);
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
		buildTree = new BuildTree(groupBy, new BuildTreeModel(projectCfgManager, searchBuildModel), getRightScrollPane());
		leftToolBar = createLeftToolBar();
		rightToolBar = createRightToolBar();
		buildHistoryPanel = new BuildHistoryPanel(project);
        //buildTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

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
        final BuildTreeEventHandler buildTreeEventHandler = new BuildTreeEventHandler(this, buildTree, buildHistoryPanel);
    }

	public void launchContextMenu(MouseEvent e) {
		final DefaultActionGroup actionGroup = new DefaultActionGroup();
		final ActionGroup configActionGroup = (ActionGroup) ActionManager
				.getInstance().getAction("ThePlugin.Bamboo.BuildPopupMenuNew");
		actionGroup.addAll(configActionGroup);

		final ActionPopupMenu popup = ActionManager.getInstance().createActionPopupMenu(getActionPlaceName(), actionGroup);

		final JPopupMenu jPopupMenu = popup.getComponent();
		jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
	}


	public void launchContextMenuGorGroup(MouseEvent e) {
		final DefaultActionGroup actionGroup = new DefaultActionGroup();
		final ActionGroup configActionGroup = (ActionGroup) ActionManager
				.getInstance().getAction("ThePlugin.Bamboo.BuildPopupMenuNewGroup");
		actionGroup.addAll(configActionGroup);

		final ActionPopupMenu popup = ActionManager.getInstance().createActionPopupMenu(getActionPlaceName(), actionGroup);

		final JPopupMenu jPopupMenu = popup.getComponent();
		jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
	}
	private String getActionPlaceName() {
		return PLACE_PREFIX + project.getName();
	}

	public void openBuild(final BambooBuildAdapter buildDetailsInfo) {
		if (buildDetailsInfo != null && buildDetailsInfo.isBamboo2()
				&& buildDetailsInfo.areActionsAllowed(true)) {
			IdeaHelper.getBuildToolWindow(project).showBuild(buildDetailsInfo);
		}
	}

	/**
	 * Open build details window, selects 'Tests' tab and run SINGLE specified test.
	 * It does not run all tests in the class nor all tests in the package.
	 */
	private void openBuildAndRunTest(final BambooBuildAdapter buildDetailsInfo,
			@NotNull final String testPackage, @NotNull final String testClass, @NotNull final String testMethod) {

		IdeaHelper.getBuildToolWindow(project).showBuildAndRunTest(buildDetailsInfo, testPackage, testClass, testMethod);
	}

	public void openBuild(@NotNull final String buildKey, final int buildNumber, @NotNull final String serverUrl) {
		final Collection<ServerData> servers = new ArrayList<ServerData>(projectCfgManager.getAllBambooServerss());

		ServerData server = projectCfgManager.findServer(serverUrl, servers);

		if (server != null && server instanceof BambooServerData) {
			openBuild(buildKey, buildNumber, (BambooServerData) server);
		} else {
			ProjectConfigurationComponent.fireDirectClickedServerPopup(project, serverUrl, ServerType.BAMBOO_SERVER,
					new Runnable() {
						public void run() {
							openBuild(buildKey, buildNumber, serverUrl);
						}
					});
		}
	}

	private void openBuild(final String buildKey, final int buildNumber, final BambooServerData server) {
		BambooBuildAdapter build = getBuildFromModel(buildKey, buildNumber, server);

		if (build != null) {
			openBuild(build);
		} else {
			new FetchingBuildTask(server, buildKey, buildNumber, new BuildLoadedHandler() {
				public void buildLoaded(final BambooBuildAdapter build) {
					openBuild(build);
				}
			}).queue();
		}
	}

	public void openBuildAndRunTest(@NotNull final String buildKey, final int buildNumber, @NotNull final String serverUrl,
			@NotNull final String testPackage, @NotNull final String testClass, @NotNull final String testName) {

		final Collection<ServerData> servers = new ArrayList<ServerData>(projectCfgManager.getAllBambooServerss());

		ServerData server = projectCfgManager.findServer(serverUrl, servers);

		if (server != null && server instanceof BambooServerData) {
			openBuildAndRunTest(buildKey, buildNumber, (BambooServerData) server, testPackage, testClass, testName);
		} else {
			ProjectConfigurationComponent.fireDirectClickedServerPopup(project, serverUrl, ServerType.BAMBOO_SERVER,
					new Runnable() {
						public void run() {
							openBuildAndRunTest(buildKey, buildNumber, serverUrl, testPackage, testClass, testName);
						}
					});
		}
	}

	private void openBuildAndRunTest(@NotNull final String buildKey, final int buildNumber,
			@NotNull final BambooServerData server,
			@NotNull final String testPackage, @NotNull final String testClass, @NotNull final String testMethod) {

		BambooBuildAdapter build = getBuildFromModel(buildKey, buildNumber, server);

		if (build != null) {
			openBuildAndRunTest(build, testPackage, testClass, testMethod);
		} else {
			new FetchingBuildTask(server, buildKey, buildNumber, new BuildLoadedHandler() {
				public void buildLoaded(final BambooBuildAdapter build) {
					openBuildAndRunTest(build, testPackage, testClass, testMethod);
				}
			}).queue();
		}
	}

	private BambooBuildAdapter getBuildFromModel(final String buildKey, final int buildNumber,
			final BambooServerData server) {
        BambooBuildAdapter build = null;
        for (BambooBuildAdapter b : bambooModel.getAllBuilds()) {
            try {
                if (b.getBuild().getPlanKey().equals(buildKey)
                        && b.getNumber() == buildNumber
                        && b.getServer().getServerId().equals(server.getServerId())) {
                    build = b;
                    break;
                }
            } catch (UnsupportedOperationException e) {
               //unsupported operation can be ignored ==> build number == null
            }
        }
		return build;
	}

	public void refresh() {
		final ThePluginProjectComponent currentProject = IdeaHelper.getCurrentProjectComponent(project);
		if (currentProject == null) {
			return;
		}

		final BambooStatusChecker checker = currentProject.getBambooStatusChecker();

		if (checker.canSchedule()) {
			Task.Backgroundable refresh = new Task.Backgroundable(project, "Refreshing Bamboo Panel", false) {
				public void run(@NotNull final ProgressIndicator indicator) {
					checker.newTimerTask().run();
				}
			};

			ProgressManager.getInstance().run(refresh);
		}
	}

	public BambooBuildAdapter getSelectedHistoryBuild() {
		return buildHistoryPanel.getSelectedBuild();
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

	@Override
	protected JComponent getRightMostPanel() {
		return buildHistoryPanel;
	}

	@Override
	protected JComponent getRightMostToolBar() {
		return rightToolBar;
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

	public Collection<BambooServerData> getServers() {
		return projectCfgManager.getAllEnabledBambooServerss();
	}

	public void setGroupingType(@NonNls final BuildGroupBy groupingType) {
		if (groupingType != null) {
			this.groupBy = groupingType;
			buildTree.groupBy(groupingType);
			bambooConfiguration.getView().setGroupBy(groupingType);
		}
	}

    public List<BambooBuildAdapter> getSelectedBuilds() {
        return buildTree.getSelectedBuilds();
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
	protected JComponent getLeftToolBar() {
		return leftToolBar;
	}

	private JComponent createLeftToolBar() {
		final JPanel toolBarPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		toolBarPanel.add(loadToolBar("ThePlugin.Bamboo.LeftToolBar"), gbc);
		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.FIRST_LINE_END;
		searchField.setMinimumSize(searchField.getPreferredSize());
		toolBarPanel.add(searchField, gbc);

		return toolBarPanel;
	}

	private JComponent createRightToolBar() {
		final JPanel toolBarPanel = new JPanel(new FormLayout("pref, fill:pref:grow, 3dlu", "pref, 3dlu, pref, 3dlu"));
		CellConstraints cc = new CellConstraints();

		toolBarPanel.add(loadToolBar("ThePlugin.Bamboo.RightToolBar"), cc.xyw(1, 1, 2));
		planHistoryListLabel = new JLabel(COMPLETED_BUILDS);
		toolBarPanel.add(planHistoryListLabel, cc.xy(1, 3));
		toolBarPanel.add(new JSeparator(SwingConstants.HORIZONTAL), cc.xy(2, 3));
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

    @Nullable
	public BambooBuildAdapter getSelectedBuild() {
		return buildTree.getSelectedBuild();
        }

	private class FetchingBuildTask extends Task.Modal {
		private final BambooServerData server;
		private final String buildKey;
		private final int buildNumber;
		private final BuildLoadedHandler handler;

		private BambooBuildAdapter build;
		private Throwable exception;

		public FetchingBuildTask(final BambooServerData server, final String buildKey, final int buildNumber,
				BuildLoadedHandler handler) {
			super(project, "Fetching build " + buildKey + "-" + buildNumber, false);
			this.server = server;
			this.buildKey = buildKey;
			this.buildNumber = buildNumber;
			this.handler = handler;
		}

		public void run(@NotNull ProgressIndicator progressIndicator) {
			progressIndicator.setIndeterminate(true);
			try {
				build =
						IntelliJBambooServerFacade.getInstance(PluginUtil.getLogger()).getBuildForPlanAndNumber(server,
								buildKey, buildNumber, server.getTimezoneOffset());
			} catch (RemoteApiException e) {
				exception = e;
			} catch (ServerPasswordNotProvidedException e) {
				exception = e;
			}
		}

		@Override
		public void onSuccess() {
			if (getProject().isDisposed()) {
				return;
			}
			if (exception != null) {
				DialogWithDetails.showExceptionDialog(project, "Cannot fetch build "
						+ buildKey + "-" + buildNumber + " from server " + server.getName(), exception);
				return;
			}
			if (build != null) {
				handler.buildLoaded(build);
			} else {
				Messages.showInfoMessage(project, "Build " + buildKey + "-" + buildNumber + " not found.",
						PluginUtil.PRODUCT_NAME);
			}
		}
	}

	private interface BuildLoadedHandler {
		void buildLoaded(final BambooBuildAdapter build);
	}
}
