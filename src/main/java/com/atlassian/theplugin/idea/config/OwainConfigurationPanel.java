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
package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.UiTask;
import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.model.Project;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OwainConfigurationPanel extends JPanel {

	private static final int ALL_COLUMNS = 5;
	private static JComboBox defaultCrucibleServerCombo = new JComboBox();
	private static JComboBox defaultCrucibleProjectCombo = new JComboBox();
	private static JComboBox defaultFishEyeServerCombo = new JComboBox();
	private static JComboBox defaultFishEyeRepositoryCombo = new JComboBox();
	private static JTextField pathToProjectEdit = new JTextField();
	private ProjectConfiguration projectConfiguration;
	private final CrucibleServerFacade crucibleServerFacade;
	private final UiTaskExecutor uiTaskExecutor;
	private static final CrucibleServerCfgWrapper NONE = new CrucibleServerCfgWrapper(null);
	private static final CrucibleProjectWrapper NO_PROJECT = new CrucibleProjectWrapper(null);
	private static final CrucibleProjectWrapper PROJECT_FETCHING = new CrucibleProjectWrapper(null) {
		@Override
		public String toString() {
			return "Fetching...";
		}

	};
	private static final CrucibleRepoWrapper REPO_FETCHING = new CrucibleRepoWrapper(null);

	final CrucibleProjectComboBoxModel crucProjectModel = new CrucibleProjectComboBoxModel();
	private OwainConfigurationPanel.CrucibleRepoComboBoxModel crucRepoModel;

	private class CrucibleServerComboBoxModel extends AbstractListModel implements ComboBoxModel {
		private final boolean forFishEye;

		private Collection<CrucibleServerCfgWrapper> data;

		CrucibleServerComboBoxModel(final boolean forFishEye) {
			this.forFishEye = forFishEye;
		}

		private Collection<CrucibleServerCfgWrapper> getServers() {
			if (data == null) {
				data = MiscUtil.buildArrayList();
				for (ServerCfg serverCfg : projectConfiguration.getServers()) {
					if (serverCfg.getServerType() == ServerType.CRUCIBLE_SERVER && serverCfg.isEnabled()) {
						CrucibleServerCfg crucibleServerCfg = (CrucibleServerCfg) serverCfg;
						if (!forFishEye || crucibleServerCfg.isFisheyeInstance()) {
							data.add(new CrucibleServerCfgWrapper(crucibleServerCfg));
						}
					}
				}
			}
			return data;
		}

		public Object getSelectedItem() {
			for (CrucibleServerCfgWrapper server : getServers()) {
				if (server.getWrapped().getServerId().equals(projectConfiguration.getDefaultCrucibleServer())) {
					return server;
				}
			}
			return NONE;
		}

		public void setSelectedItem(final Object anItem) {
			final Object selectedItem = getSelectedItem();
			if (selectedItem != null && !selectedItem.equals(anItem) || selectedItem == null && anItem != null) {
				projectConfiguration.setDefaultCrucibleProject(null);
				if (anItem != null) {
					CrucibleServerCfgWrapper item = (CrucibleServerCfgWrapper) anItem;
					final CrucibleServerCfg wrapped = item.getWrapped();
					if (wrapped != null) {
						projectConfiguration.setDefaultCrucibleServer(wrapped.getServerId());
						projectConfiguration.setDefaultCrucibleRepo(null);
						projectConfiguration.setDefaultCrucibleProject(null);
					} else {
						clearDefaultCrucibleServer();
					}
				} else {
					clearDefaultCrucibleServer();
				}
				fireContentsChanged(this, -1, -1);
			}
		}

		private void clearDefaultCrucibleServer() {
			projectConfiguration.setDefaultCrucibleServer(null);
			projectConfiguration.setDefaultCrucibleProject(null);
			projectConfiguration.setDefaultCrucibleRepo(null);
		}

		public Object getElementAt(final int index) {
			if (index == 0) {
				return NONE;
			}
			int i = 1;
			for (CrucibleServerCfgWrapper server : getServers()) {
				if (i == index) {
					return server;
				}
				i++;
			}
			return null;
		}

		public int getSize() {
			return getServers().size() + 1;
		}

	}


	public OwainConfigurationPanel(final ProjectConfiguration projectConfiguration,
			final CrucibleServerFacade crucibleServerFacade, final UiTaskExecutor uiTaskExecutor) {
		this.projectConfiguration = projectConfiguration;
		this.crucibleServerFacade = crucibleServerFacade;
		this.uiTaskExecutor = uiTaskExecutor;
//		panel.setPreferredSize(new Dimension(300, 200));

		final FormLayout layout = new FormLayout(
				"3dlu, right:pref, 3dlu, min(150dlu;default):grow, 3dlu", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 9dlu, p, 3dlu, p, 3dlu, fill:p");	  // rows

		layout.setRowGroups(new int[][]{{11, 13}});

		PanelBuilder builder = new PanelBuilder(layout, this);
		builder.setDefaultDialogBorder();

// Obtain a reusable constraints object to place components in the grid.
		final CellConstraints cc = new CellConstraints();

		builder.addSeparator("Crucible", cc.xyw(1, 1, ALL_COLUMNS));
		builder.addLabel("Default Server", cc.xy(2, 3));
		builder.add(defaultCrucibleServerCombo, cc.xy(4, 3));
		builder.addLabel("Default Project", cc.xy(2, 5));
		builder.add(defaultCrucibleProjectCombo, cc.xy(4, 5));
		builder.addLabel("Default Repository", cc.xy(2, 7));
		builder.add(defaultFishEyeRepositoryCombo, cc.xy(4, 7));

		builder.addSeparator("FishEye", cc.xyw(1, 9, ALL_COLUMNS));
		builder.addLabel("Default Server", cc.xy(2, 11));
		builder.add(defaultFishEyeServerCombo, cc.xy(4, 11));
		builder.addLabel("Path to project", cc.xy(2, 13));
		builder.add(pathToProjectEdit, cc.xy(4, 13));

		initializeCombos();

		defaultCrucibleServerCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				crucProjectModel.refresh();
				crucRepoModel.refresh();
			}
		});


		defaultFishEyeServerCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
//				crucRepoModel.refresh();
			}
		});


	}

	private void initializeCombos() {
		defaultCrucibleServerCombo.setModel(new CrucibleServerComboBoxModel(false));
		defaultFishEyeServerCombo.setModel(new CrucibleServerComboBoxModel(true));
		defaultCrucibleProjectCombo.setModel(crucProjectModel);
		crucRepoModel = new CrucibleRepoComboBoxModel();
		defaultFishEyeRepositoryCombo.setModel(crucRepoModel);
	}


	public void setData(final ProjectConfiguration projectConfiguration) {
		this.projectConfiguration = projectConfiguration;
		initializeCombos();
	}


	private static class GenericWrapper<T> {
		protected final T wrapped;

		public GenericWrapper(final T crucibleProject) {
			this.wrapped = crucibleProject;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof GenericWrapper)) {
				return false;
			}

			final GenericWrapper<?> that = (GenericWrapper<?>) o;

			if (wrapped != null ? !wrapped.equals(that.wrapped) : that.wrapped != null) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return (wrapped != null ? wrapped.hashCode() : 0);
		}

		@Override
		public String toString() {
			if (wrapped != null) {
				return wrapped.toString();
			} else {
				return "None";
			}
		}

		public T getWrapped() {
			return wrapped;
		}
	}



	private static class CrucibleServerCfgWrapper extends GenericWrapper<CrucibleServerCfg> {
		public CrucibleServerCfgWrapper(final CrucibleServerCfg crucibleProject) {
			super(crucibleProject);
		}

		@Override
		public String toString() {
			if (wrapped != null) {
				return wrapped.getName();
			}
			return "None";
		}
	}


	private static class CrucibleProjectWrapper extends GenericWrapper<Project> {
		public CrucibleProjectWrapper(final Project crucibleProject) {
			super(crucibleProject);
		}

		@Override
		public String toString() {
			if (wrapped != null) {
				return wrapped.getName();
			}
			return "None";
		}
	}


	private static class CrucibleRepoWrapper extends GenericWrapper<Repository> {
		public CrucibleRepoWrapper(final Repository repository) {
			super(repository);
		}

		@Override
		public String toString() {
			if (wrapped != null) {
				return wrapped.getName();
			}
			return super.toString();
		}
	}


	private class CrucibleProjectComboBoxModel extends AbstractListModel implements ComboBoxModel {
		private Map<ServerId, Collection<CrucibleProjectWrapper>> data;

		private Collection<CrucibleProjectWrapper> getProjects(final CrucibleServerCfg crucibleServerCfg) {
			if (data == null) {
				data = MiscUtil.buildConcurrentHashMap(10);
			}

			Collection<CrucibleProjectWrapper> projectsWrappers = data.get(crucibleServerCfg.getServerId());
			if (projectsWrappers == null) {
				projectsWrappers = MiscUtil.buildArrayList(PROJECT_FETCHING);
				data.put(crucibleServerCfg.getServerId(), projectsWrappers);

				uiTaskExecutor.execute(new UiTask() {

					private String lastAction;
					public void run() throws RemoteApiException, ServerPasswordNotProvidedException {
						lastAction = "retrieving available projects from Crucible server";
						final Collection<CrucibleProjectWrapper> projectsWrappers = MiscUtil.buildArrayList();
						final List<Project> projects = crucibleServerFacade.getProjects(crucibleServerCfg);
						for (Project project : projects) {
							final CrucibleProjectWrapper projectWrapper = new CrucibleProjectWrapper(project);
							projectsWrappers.add(projectWrapper);
						}

						data.put(crucibleServerCfg.getServerId(), projectsWrappers);
					}

					public void onSuccess() {
//						defaultCrucibleProjectCombo.setEnabled(true);
						lastAction = "populating project combobox";
						refresh();
					}

					public void onError() {
						projectConfiguration.setDefaultCrucibleServer(null);
						data.remove(crucibleServerCfg.getServerId());
						refresh();
					}

					public Component getComponent() {
						return OwainConfigurationPanel.this;
					}

					public String getLastAction() {
						return lastAction;
					}
				});
			}

			return projectsWrappers;
		}

		public Object getSelectedItem() {
			final CrucibleServerCfg currentCrucibleServerCfg = getCurrentCrucibleServerCfg();
			if (currentCrucibleServerCfg == null) {
				return NO_PROJECT;
			}
			for (CrucibleProjectWrapper project : getProjects(currentCrucibleServerCfg)) {
				if (project.getWrapped() == null) {
					return PROJECT_FETCHING;
				}
				if (project.getWrapped().getKey().equals(projectConfiguration.getDefaultCrucibleProject())) {
					return project;
				}
			}
			return NO_PROJECT;
		}

		private CrucibleServerCfg getCurrentCrucibleServerCfg() {
			if (projectConfiguration.getDefaultCrucibleServer() == null) {
				return null;
			}
			return (CrucibleServerCfg) projectConfiguration.getServerCfg(projectConfiguration.getDefaultCrucibleServer());
		}

		public void setSelectedItem(final Object anItem) {
			final Object selectedItem = getSelectedItem();
			if (selectedItem != null && !selectedItem.equals(anItem) || selectedItem == null && anItem != null) {
				if (anItem != null) {
					CrucibleProjectWrapper item = (CrucibleProjectWrapper) anItem;
					final Project wrapped = item.getWrapped();
					if (wrapped != null) {
						projectConfiguration.setDefaultCrucibleProject(wrapped.getKey());
					} else {
						projectConfiguration.setDefaultCrucibleProject(null);
					}
				} else {
					projectConfiguration.setDefaultCrucibleProject(null);
				}
				fireContentsChanged(this, -1, -1);
			}
		}

		public void refresh() {
			fireContentsChanged(this, -1, -1);
		}

		public Object getElementAt(final int index) {
			if (index == 0) {
				return NO_PROJECT;
			}
			int i = 1;
			for (CrucibleProjectWrapper projectWrapper : getProjects(getCurrentCrucibleServerCfg())) {
				if (i == index) {
					return projectWrapper;
				}
				i++;
			}
			return null;
		}

		public int getSize() {
			final CrucibleServerCfg currentCrucibleServerCfg = getCurrentCrucibleServerCfg();
			if (currentCrucibleServerCfg != null) {
				return getProjects(currentCrucibleServerCfg).size() + 1;
			} else {
				return 1;
			}

		}

	}



	private class CrucibleRepoComboBoxModel extends AbstractListModel implements ComboBoxModel {
		private Map<ServerId, Collection<CrucibleRepoWrapper>> data;

		private Collection<CrucibleRepoWrapper> getRepositories(final CrucibleServerCfg crucibleServerCfg) {
			if (data == null) {
				data = MiscUtil.buildConcurrentHashMap(10);
			}

			Collection<CrucibleRepoWrapper> repoWrappers = data.get(crucibleServerCfg.getServerId());
			if (repoWrappers == null) {
				repoWrappers = MiscUtil.buildArrayList(REPO_FETCHING);
				data.put(crucibleServerCfg.getServerId(), repoWrappers);

				uiTaskExecutor.execute(new UiTask() {

					private String lastAction;
					public void run() throws RemoteApiException, ServerPasswordNotProvidedException {
						lastAction = "retrieving available repositories from Crucible server " + crucibleServerCfg.getName();
						final Collection<CrucibleRepoWrapper> repoWrappers = MiscUtil.buildArrayList();
						final List<Repository> projects = crucibleServerFacade.getRepositories(crucibleServerCfg);
						for (Repository project : projects) {
							final CrucibleRepoWrapper projectWrapper = new CrucibleRepoWrapper(project);
							repoWrappers.add(projectWrapper);
						}

						data.put(crucibleServerCfg.getServerId(), repoWrappers);
					}

					public void onSuccess() {
						lastAction = "populating repository combobox";
						refresh();
					}

					public void onError() {
						data.remove(crucibleServerCfg.getServerId());
						projectConfiguration.setDefaultCrucibleServer(null);
						refresh();
					}

					public Component getComponent() {
						return OwainConfigurationPanel.this;
					}

					public String getLastAction() {
						return lastAction;
					}
				});
			}

			return repoWrappers;
		}

		public Object getSelectedItem() {
			final CrucibleServerCfg currentCrucibleServerCfg = getCurrentCrucibleServerCfg();
			if (currentCrucibleServerCfg == null) {
				return NO_PROJECT;
			}
			for (CrucibleRepoWrapper project : getRepositories(currentCrucibleServerCfg)) {
				if (project.getWrapped() == null) {
					return PROJECT_FETCHING;
				}
				if (project.getWrapped().getName().equals(projectConfiguration.getDefaultCrucibleRepo())) {
					return project;
				}
			}
			return NO_PROJECT;
		}

		private CrucibleServerCfg getCurrentCrucibleServerCfg() {
			if (projectConfiguration.getDefaultCrucibleServer() == null) {
				return null;
			}
			return (CrucibleServerCfg) projectConfiguration.getServerCfg(projectConfiguration.getDefaultCrucibleServer());
		}

		public void setSelectedItem(final Object anItem) {
			final Object selectedItem = getSelectedItem();
			if (selectedItem != null && !selectedItem.equals(anItem) || selectedItem == null && anItem != null) {
				if (anItem != null) {
					CrucibleRepoWrapper item = (CrucibleRepoWrapper) anItem;
					final Repository wrapped = item.getWrapped();
					if (wrapped != null) {
						projectConfiguration.setDefaultCrucibleRepo(wrapped.getName());
					} else {
						projectConfiguration.setDefaultCrucibleRepo(null);
					}
				} else {
					projectConfiguration.setDefaultCrucibleRepo(null);
				}
				fireContentsChanged(this, -1, -1);
			}
		}

		public void refresh() {
			fireContentsChanged(this, -1, -1);
		}

		public Object getElementAt(final int index) {
			if (index == 0) {
				return NO_PROJECT;
			}
			int i = 1;
			for (CrucibleRepoWrapper projectWrapper : getRepositories(getCurrentCrucibleServerCfg())) {
				if (i == index) {
					return projectWrapper;
				}
				i++;
			}
			return null;
		}

		public int getSize() {
			final CrucibleServerCfg currentCrucibleServerCfg = getCurrentCrucibleServerCfg();
			if (currentCrucibleServerCfg != null) {
				return getRepositories(currentCrucibleServerCfg).size() + 1;
			} else {
				return 1;
			}

		}

	}


}
