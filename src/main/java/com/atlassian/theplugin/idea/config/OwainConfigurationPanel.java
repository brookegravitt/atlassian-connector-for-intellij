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
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.model.Project;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.fisheye.FishEyeServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OwainConfigurationPanel extends JPanel {

	private static final int ALL_COLUMNS = 5;
	private JComboBox defaultCrucibleServerCombo = new JComboBox();
	private JComboBox defaultCrucibleProjectCombo = new JComboBox();
	private JComboBox defaultFishEyeServerCombo = new JComboBox();
	private JComboBox defaultCrucibleRepositoryCombo = new JComboBox();
	private JComboBox defaultFishEyeRepositoryCombo = new JComboBox();
	private JTextField pathToProjectEdit = new JTextField();
	//private JTextField defaultFishEyeRepoEdit = new JTextField();
	private ProjectConfiguration projectConfiguration;
	private final CrucibleServerFacade crucibleServerFacade;
	private final FishEyeServerFacade fishEyeServerFacade;
	private final UiTaskExecutor uiTaskExecutor;
	private static final CrucibleServerCfgWrapper NONE_CRUCIBLE = new CrucibleServerCfgWrapper(null);	
	private static final FishEyeServerCfgWrapper NONE_FISHEYE = new FishEyeServerCfgWrapper(null);
	private static final CrucibleProjectWrapper NO_PROJECT = new CrucibleProjectWrapper(null);
	private static final String NO_REPOSITORY = "None";
	private static final String REPOSITORY_FETCHING = "Fetching...";
	private static final CrucibleProjectWrapper PROJECT_CRUCIBLE_FETCHING = new CrucibleProjectWrapper(null) {
		@Override
		public String toString() {
			return "Fetching...";
		}

	};
//	private static final FishEyeProjectWrapper PROJECT_FISHEYE_FETCHING = new FishEyeProjectWrapper(null) {
//		@Override
//		public String toString() {
//			return "Fetching...";
//		}
//
//	};
	
	private static final CrucibleRepoWrapper REPO_FETCHING = new CrucibleRepoWrapper(null);

	private final CrucibleProjectComboBoxModel crucProjectModel = new CrucibleProjectComboBoxModel();
	private final CrucibleRepoComboBoxModel crucRepoModel = new CrucibleRepoComboBoxModel();
	private final FishEyeRepositoryComboBoxModel fishRepositoryModel = new FishEyeRepositoryComboBoxModel();


	private class CrucibleServerComboBoxModel extends AbstractListModel implements ComboBoxModel {
		private Collection<CrucibleServerCfgWrapper> data;

		private Collection<CrucibleServerCfgWrapper> getServers() {
			if (data == null) {
				data = MiscUtil.buildArrayList();
				for (ServerCfg serverCfg : projectConfiguration.getServers()) {
					if (serverCfg.getServerType() == ServerType.CRUCIBLE_SERVER && serverCfg.isEnabled()) {
						CrucibleServerCfg crucibleServerCfg = (CrucibleServerCfg) serverCfg;
						if (crucibleServerCfg.isFisheyeInstance()) {
							data.add(new CrucibleServerCfgWrapper(crucibleServerCfg));
						}
					}
				}
			}
			return data;
		}

		public Object getSelectedItem() {
			for (CrucibleServerCfgWrapper server : getServers()) {
				if (server.getWrapped().getServerId().equals(projectConfiguration.getDefaultCrucibleServerId())) {
					return server;
				}
			}
			return NONE_CRUCIBLE;
		}

		public void setSelectedItem(final Object anItem) {
			final Object selectedItem = getSelectedItem();
			if (selectedItem != null && !selectedItem.equals(anItem) || selectedItem == null && anItem != null) {
				projectConfiguration.setDefaultCrucibleProject(null);
				if (anItem != null) {
					CrucibleServerCfgWrapper item = (CrucibleServerCfgWrapper) anItem;
					final CrucibleServerCfg wrapped = item.getWrapped();
					if (wrapped != null) {
						projectConfiguration.setDefaultCrucibleServerId(wrapped.getServerId());
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
			projectConfiguration.setDefaultCrucibleServerId(null);
			projectConfiguration.setDefaultCrucibleProject(null);
			projectConfiguration.setDefaultCrucibleRepo(null);
		}

		public Object getElementAt(final int index) {
			if (index == 0) {
				return NONE_CRUCIBLE;
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
			final CrucibleServerFacade crucibleServerFacade, final FishEyeServerFacade fishEyeServerFacade,
			final UiTaskExecutor uiTaskExecutor) {
		this.projectConfiguration = projectConfiguration;
		this.crucibleServerFacade = crucibleServerFacade;
		this.fishEyeServerFacade = fishEyeServerFacade;
		this.uiTaskExecutor = uiTaskExecutor;

		pathToProjectEdit.setToolTipText("Path to root directory in your repo. E.g. trunk/myproject.");
//		panel.setPreferredSize(new Dimension(300, 200));

		final FormLayout layout = new FormLayout(
				"3dlu, right:pref, 3dlu, min(150dlu;default):grow, 3dlu", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 9dlu, p, 3dlu, p, 3dlu, fill:p, 3dlu, fill:p");	  // rows

		//CHECKSTYLE:MAGIC:OFF
		layout.setRowGroups(new int[][]{{11, 13, 15}});

		PanelBuilder builder = new PanelBuilder(layout, this);
		builder.setDefaultDialogBorder();

		final CellConstraints cc = new CellConstraints();

		builder.addSeparator("Crucible", cc.xyw(1, 1, ALL_COLUMNS));
		builder.addLabel("Default Server", cc.xy(2, 3));
		builder.add(defaultCrucibleServerCombo, cc.xy(4, 3));
		builder.addLabel("Default Project", cc.xy(2, 5));
		builder.add(defaultCrucibleProjectCombo, cc.xy(4, 5));
		builder.addLabel("Default Repository", cc.xy(2, 7));
		builder.add(defaultCrucibleRepositoryCombo, cc.xy(4, 7));

		builder.addSeparator("FishEye", cc.xyw(1, 9, ALL_COLUMNS));
		builder.addLabel("Default Server", cc.xy(2, 11));
		builder.add(defaultFishEyeServerCombo, cc.xy(4, 11));
		builder.addLabel("Default Repository", cc.xy(2, 13));
		builder.add(defaultFishEyeRepositoryCombo, cc.xy(4, 13));
		builder.addLabel("Path to project", cc.xy(2, 15));
		builder.add(pathToProjectEdit, cc.xy(4, 15));
		//CHECKSTYLE:MAGIC:ON

		initializeControls();

		defaultCrucibleServerCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				crucProjectModel.refresh();
				crucRepoModel.refresh();
			}
		});


		defaultFishEyeServerCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				fishRepositoryModel.refresh();
			}
		});

//		defaultFishEyeRepoEdit.getDocument().addDocumentListener(new DocumentListener() {
//			public void changedUpdate(final DocumentEvent e) {
//				projectConfiguration.setDefaultFishEyeRepo(defaultFishEyeRepoEdit.setgetText());
//			}
//
//			public void insertUpdate(final DocumentEvent e) {
//				projectConfiguration.setDefaultFishEyeRepo(defaultFishEyeRepoEdit.getText());
//			}
//
//			public void removeUpdate(final DocumentEvent e) {
//				projectConfiguration.setDefaultFishEyeRepo(defaultFishEyeRepoEdit.getText());
//			}
//		});

		pathToProjectEdit.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(final DocumentEvent e) {
				projectConfiguration.setFishEyeProjectPath(pathToProjectEdit.getText());
			}

			public void insertUpdate(final DocumentEvent e) {
				projectConfiguration.setFishEyeProjectPath(pathToProjectEdit.getText());
			}

			public void removeUpdate(final DocumentEvent e) {
				projectConfiguration.setFishEyeProjectPath(pathToProjectEdit.getText());
			}
		});

	}

	private void initializeControls() {
		defaultCrucibleServerCombo.setModel(new CrucibleServerComboBoxModel());
		defaultFishEyeServerCombo.setModel(new FishEyeServerComboBoxModel());
		defaultCrucibleProjectCombo.setModel(crucProjectModel);

		defaultCrucibleRepositoryCombo.setModel(crucRepoModel);
		defaultFishEyeRepositoryCombo.setModel(fishRepositoryModel);
		//defaultFishEyeRepoEdit.setText(projectConfiguration.getDefaultFishEyeRepo());
		pathToProjectEdit.setText(projectConfiguration.getFishEyeProjectPath());
	}


	public void setData(final ProjectConfiguration aProjectConfiguration) {
		this.projectConfiguration = aProjectConfiguration;
		initializeControls();
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

	private static class FishEyeServerCfgWrapper extends GenericWrapper<FishEyeServerCfg> {
		public FishEyeServerCfgWrapper(final FishEyeServerCfg fishEyeProject) {
			super(fishEyeProject);
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

//	private static class FishEyeProjectWrapper extends GenericWrapper<String> {
//		public FishEyeProjectWrapper(final String fishEyeProject) {
//			super(fishEyeProject);
//		}
//
//		@Override
//		public String toString() {
//			if (wrapped.length() > 0) {
//				return wrapped;
//			}
//			return "None";
//		}
//	}

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

	private class FishEyeRepositoryComboBoxModel extends AbstractListModel implements ComboBoxModel {
		private Map<ServerId, Collection<String>> data;
		private static final int INITIAL_CAPACITY = 10;

		private Collection<String> getRepositories(final FishEyeServerCfg fishEyeServerCfg) {
			if (data == null) {
				data = MiscUtil.buildConcurrentHashMap(INITIAL_CAPACITY);
			}

			Collection<String> projects = data.get(fishEyeServerCfg.getServerId());
			if (projects == null) {
				projects = MiscUtil.buildArrayList("None");
				data.put(fishEyeServerCfg.getServerId(), projects);

				uiTaskExecutor.execute(new UiTask() {

					private String lastAction;
					public void run() throws RemoteApiException, ServerPasswordNotProvidedException {
						lastAction = "retrieving available projects from FishEye server";

						final Collection<String> repositories = fishEyeServerFacade.getRepositories(fishEyeServerCfg);
						data.put(fishEyeServerCfg.getServerId(), repositories);
					}

					public void onSuccess() {
						lastAction = "populating project combobox";
						refresh();
					}

					public void onError() {
						projectConfiguration.setDefaultFishEyeServerId(null);
						data.remove(fishEyeServerCfg.getServerId());
						refresh();
					}

					public String getLastAction() {
						return lastAction;
					}

					public Component getComponent() {
						return OwainConfigurationPanel.this;
					}
				});
			}

			return projects;
		}

		public int getSize() {
				final FishEyeServerCfg currentFishEyeServerCfg = getCurrentFishEyeServerCfg();
			if (currentFishEyeServerCfg != null) {
				return getRepositories(currentFishEyeServerCfg).size() + 1;
			} else {
				return 1;
			}
		}

		public Object getElementAt(final int index) {
			if (index == 0) {
				return NO_REPOSITORY;
			}
			int i = 1;
			for (String repository : getRepositories(getCurrentFishEyeServerCfg())) {
				if (i == index) {
					return repository;
				}
				i++;
			}
			return null;
		}

		public void setSelectedItem(final Object anItem) {
			final Object selectedItem = getSelectedItem();
			if (selectedItem != null && !selectedItem.equals(anItem) || selectedItem == null && anItem != null) {
				if (anItem != null) {
					String item = (String) anItem;

					if (item.length() != 0) {
						projectConfiguration.setDefaultFishEyeRepo(item);
					} else {
						projectConfiguration.setDefaultFishEyeRepo(null);
					}
				} else {
					projectConfiguration.setDefaultFishEyeRepo(null);
				}
				fireContentsChanged(this, -1, -1);
			}
		}

		public Object getSelectedItem() {
			final FishEyeServerCfg currentFishEyeServerCfg = getCurrentFishEyeServerCfg();
			if (currentFishEyeServerCfg == null) {
				return NO_REPOSITORY;
			}
			for (String repository : getRepositories(currentFishEyeServerCfg)) {
				if (repository.length() == 0) {
					return REPOSITORY_FETCHING;
				}
				if (repository.equals(projectConfiguration.getDefaultFishEyeRepo())) {
					return repository;
				}
			}
			return NO_REPOSITORY;
		}

		public void refresh() {
			fireContentsChanged(this, -1, -1);
		}

		private FishEyeServerCfg getCurrentFishEyeServerCfg() {
			if (projectConfiguration.getDefaultFishEyeServerId() == null) {
				return null;
			}
			return (FishEyeServerCfg) projectConfiguration.getServerCfg(projectConfiguration.getDefaultFishEyeServerId());
		}
	}
	private class CrucibleProjectComboBoxModel extends AbstractListModel implements ComboBoxModel {
		private Map<ServerId, Collection<CrucibleProjectWrapper>> data;
		private static final int INITIAL_CAPACITY = 10;

		private Collection<CrucibleProjectWrapper> getProjects(final CrucibleServerCfg crucibleServerCfg) {
			if (data == null) {
				data = MiscUtil.buildConcurrentHashMap(INITIAL_CAPACITY);
			}

			Collection<CrucibleProjectWrapper> projectsWrappers = data.get(crucibleServerCfg.getServerId());
			if (projectsWrappers == null) {
				projectsWrappers = MiscUtil.buildArrayList(PROJECT_CRUCIBLE_FETCHING);
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
						projectConfiguration.setDefaultCrucibleServerId(null);
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
					return PROJECT_CRUCIBLE_FETCHING;
				}
				if (project.getWrapped().getKey().equals(projectConfiguration.getDefaultCrucibleProject())) {
					return project;
				}
			}
			return NO_PROJECT;
		}

		private CrucibleServerCfg getCurrentCrucibleServerCfg() {
			if (projectConfiguration.getDefaultCrucibleServerId() == null) {
				return null;
			}
			return (CrucibleServerCfg) projectConfiguration.getServerCfg(projectConfiguration.getDefaultCrucibleServerId());
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
		private static final int INITIAL_CAPACITY = 10;

		private Collection<CrucibleRepoWrapper> getRepositories(final CrucibleServerCfg crucibleServerCfg) {
			if (data == null) {
				data = MiscUtil.buildConcurrentHashMap(INITIAL_CAPACITY);
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
						projectConfiguration.setDefaultCrucibleServerId(null);
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
					return PROJECT_CRUCIBLE_FETCHING;
				}
				if (project.getWrapped().getName().equals(projectConfiguration.getDefaultCrucibleRepo())) {
					return project;
				}
			}
			return NO_PROJECT;
		}

		private CrucibleServerCfg getCurrentCrucibleServerCfg() {
			if (projectConfiguration.getDefaultCrucibleServerId() == null) {
				return null;
			}
			return (CrucibleServerCfg) projectConfiguration.getServerCfg(projectConfiguration.getDefaultCrucibleServerId());
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


	private class FishEyeServerComboBoxModel extends AbstractListModel implements ComboBoxModel {
		private Collection<FishEyeServerCfgWrapper> data;

		FishEyeServerComboBoxModel() {
		}

		private Collection<FishEyeServerCfgWrapper> getServers() {
			if (data == null) {
				data = MiscUtil.buildArrayList();
				for (ServerCfg serverCfg : projectConfiguration.getServers()) {
					if (serverCfg.getServerType() == ServerType.FISHEYE_SERVER && serverCfg.isEnabled()) {
						FishEyeServerCfg fishEyeServerCfg = (FishEyeServerCfg) serverCfg;
							data.add(new FishEyeServerCfgWrapper(fishEyeServerCfg));

					}
				}
			}
			return data;
		}

		public Object getSelectedItem() {
			for (FishEyeServerCfgWrapper server : getServers()) {
				if (server.getWrapped().getServerId().equals(projectConfiguration.getDefaultFishEyeServerId())) {
					return server;
				}
			}
			return NONE_FISHEYE;
		}

		public void setSelectedItem(final Object anItem) {
			final Object selectedItem = getSelectedItem();
			if (selectedItem != null && !selectedItem.equals(anItem) || selectedItem == null && anItem != null) {
				if (anItem != null) {
					FishEyeServerCfgWrapper item = (FishEyeServerCfgWrapper) anItem;
					final FishEyeServerCfg wrapped = item.getWrapped();
					if (wrapped != null) {
						projectConfiguration.setDefaultFishEyeServerId(wrapped.getServerId());
						projectConfiguration.setDefaultFishEyeRepo(null);
					} else {
						projectConfiguration.setDefaultFishEyeServerId(null);
						projectConfiguration.setDefaultFishEyeRepo(null);
					}
				} else {
					projectConfiguration.setDefaultFishEyeServerId(null);
					projectConfiguration.setDefaultFishEyeRepo(null);
				}
				fireContentsChanged(this, -1, -1);
			}
		}

		public Object getElementAt(final int index) {
			if (index == 0) {
				return NONE_FISHEYE;
			}
			int i = 1;
			for (FishEyeServerCfgWrapper server : getServers()) {
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

}
