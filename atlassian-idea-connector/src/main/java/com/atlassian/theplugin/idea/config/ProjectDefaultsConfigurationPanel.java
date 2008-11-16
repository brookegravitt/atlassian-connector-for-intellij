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

public class ProjectDefaultsConfigurationPanel extends JPanel {

	private static final int ALL_COLUMNS = 5;
	private JComboBox defaultCrucibleServerCombo = new JComboBox();
	private JComboBox defaultCrucibleProjectCombo = new JComboBox();
	private JComboBox defaultCrucibleRepositoryCombo = new JComboBox();
	private JComboBox defaultFishEyeServerCombo = new JComboBox();
	private JComboBox defaultFishEyeRepositoryCombo = new JComboBox();
	private JTextField pathToProjectEdit = new JTextField();
	private ProjectConfiguration projectConfiguration;
	private final CrucibleServerFacade crucibleServerFacade;
	private final FishEyeServerFacade fishEyeServerFacade;
	private final UiTaskExecutor uiTaskExecutor;
	private static final CrucibleServerCfgWrapper CRUCIBLE_SERVER_NONE = new CrucibleServerCfgWrapper(null);
	private static final FishEyeServerWrapper FISHEYE_SERVER_NONE = new FishEyeServerWrapper(null);
	private static final CrucibleProjectWrapper CRUCIBLE_PROJECT_NONE = new CrucibleProjectWrapper(null);
	private static final GenericWrapper<String> FISHEYE_REPO_NONE = new GenericWrapper<String>(null);
	private static final GenericWrapper<String> FISHEYE_REPO_FETCHING = new GenericWrapper<String>(null) {
		@Override
		public String toString() {
			return "Fetching...";
		}
	};

	private static final CrucibleProjectWrapper CRUCIBLE_PROJECT_FETCHING = new CrucibleProjectWrapper(null) {
		@Override
		public String toString() {
			return "Fetching...";
		}

	};
	private static final CrucibleRepoWrapper CRUCIBLE_REPO_FETCHING = new CrucibleRepoWrapper(null) {
		@Override
		public String toString() {
			return "Fetching...";
		}
	};
	private static final CrucibleRepoWrapper CRUCIBLE_REPO_NONE = new CrucibleRepoWrapper(null);

	private final MyModel<CrucibleProjectWrapper, Project, CrucibleServerCfg> crucProjectModel
			= new MyModel<CrucibleProjectWrapper, Project, CrucibleServerCfg>(CRUCIBLE_PROJECT_FETCHING, CRUCIBLE_PROJECT_NONE,
			"projects", "Crucible") {

		@Override
		protected CrucibleProjectWrapper toT(final Project element) {
			return new CrucibleProjectWrapper(element);
		}

		@Override
		protected List<Project> getR(final CrucibleServerCfg serverCfg)
				throws RemoteApiException, ServerPasswordNotProvidedException {
			return crucibleServerFacade.getProjects(serverCfg);
		}

		@Override
		protected boolean isEqual(final CrucibleProjectWrapper element) {
			return element.getWrapped().getKey().equals(projectConfiguration.getDefaultCrucibleProject());
		}

		@Override
		protected void setOption(final CrucibleProjectWrapper newSelection) {
			if (newSelection == null) {
				projectConfiguration.setDefaultCrucibleProject(null);
				return;
			}
			final Project wrapped = newSelection.getWrapped();
			if (wrapped != null) {
				projectConfiguration.setDefaultCrucibleProject(wrapped.getKey());
			} else {
				projectConfiguration.setDefaultCrucibleProject(null);
			}

		}

		@Override
		protected CrucibleServerCfg getCurrentServer() {
			return getCurrentCrucibleServerCfg();
		}
	};

	private CrucibleServerCfg getCurrentCrucibleServerCfg() {
		if (projectConfiguration.getDefaultCrucibleServerId() == null) {
			return null;
		}
		return (CrucibleServerCfg) projectConfiguration.getServerCfg(projectConfiguration.getDefaultCrucibleServerId());
	}

	private final MyModel<CrucibleRepoWrapper, Repository, CrucibleServerCfg> crucRepoModel
			= new MyModel<CrucibleRepoWrapper, Repository, CrucibleServerCfg>(CRUCIBLE_REPO_FETCHING, CRUCIBLE_REPO_NONE,
			"repositories", "Crucible") {

		@Override
		protected CrucibleRepoWrapper toT(final Repository element) {
			return new CrucibleRepoWrapper(element);
		}

		@Override
		protected List<Repository> getR(final CrucibleServerCfg serverCfg) throws Exception {
			return crucibleServerFacade.getRepositories(serverCfg);
		}

		@Override
		protected boolean isEqual(final CrucibleRepoWrapper element) {
			return element.getWrapped().getName().equals(projectConfiguration.getDefaultCrucibleRepo());
		}

		@Override
		protected void setOption(final CrucibleRepoWrapper newSelection) {
			if (newSelection == null) {
				projectConfiguration.setDefaultCrucibleRepo(null);
				return;
			}
			final Repository wrapped = newSelection.getWrapped();
			if (wrapped != null) {
				projectConfiguration.setDefaultCrucibleRepo(wrapped.getName());
			} else {
				projectConfiguration.setDefaultCrucibleRepo(null);
			}
		}

		@Override
		protected CrucibleServerCfg getCurrentServer() {
			return getCurrentCrucibleServerCfg();
		}

	};


	private final MyModel<GenericWrapper<String>, String, FishEyeServer> fishRepositoryModel
			= new MyModel<GenericWrapper<String>, String, FishEyeServer>(FISHEYE_REPO_FETCHING, FISHEYE_REPO_NONE,
			"repositories", "FishEye") {


		@Override
		protected GenericWrapper<String> toT(final String element) {
			return new GenericWrapper<String>(element);
		}

		@Override
		protected Collection<String> getR(final FishEyeServer serverCfg) throws Exception {
			return fishEyeServerFacade.getRepositories(serverCfg);
		}

		@Override
		protected boolean isEqual(final GenericWrapper<String> element) {
			return element.getWrapped().equals(projectConfiguration.getDefaultFishEyeRepo());
		}

		@Override
		protected FishEyeServer getCurrentServer() {
			if (projectConfiguration.getDefaultFishEyeServerId() == null) {
				return null;
			}
			return projectConfiguration.getServerCfg(projectConfiguration.getDefaultFishEyeServerId()).asFishEyeServer();
		}

		@Override
		protected void setOption(final GenericWrapper<String> newSelection) {
			if (newSelection != null) {
				projectConfiguration.setDefaultFishEyeRepo(newSelection.getWrapped());
			} else {
				projectConfiguration.setDefaultFishEyeRepo(null);
			}
		}
	};


	public ProjectDefaultsConfigurationPanel(final ProjectConfiguration projectConfiguration,
			final CrucibleServerFacade crucibleServerFacade, final FishEyeServerFacade fishEyeServerFacade,
			final UiTaskExecutor uiTaskExecutor) {
		this.projectConfiguration = projectConfiguration;
		this.crucibleServerFacade = crucibleServerFacade;
		this.fishEyeServerFacade = fishEyeServerFacade;
		this.uiTaskExecutor = uiTaskExecutor;

		pathToProjectEdit.setToolTipText("Path to root directory in your repo. "
				+ "E.g. trunk/myproject. Leave it blank if your project is located at the repository root");
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
		builder.addLabel("Path to Project", cc.xy(2, 15));
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
		pathToProjectEdit.setText(projectConfiguration.getFishEyeProjectPath());
	}


	public void setData(final ProjectConfiguration aProjectConfiguration) {
		this.projectConfiguration = aProjectConfiguration;
		initializeControls();
	}


	private static class GenericWrapper<T> {
		protected final T wrapped;

		public GenericWrapper(final T wrapped) {
			this.wrapped = wrapped;
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

	private static class FishEyeServerWrapper extends GenericWrapper<FishEyeServer> {
		public FishEyeServerWrapper(final FishEyeServer fishEyeProject) {
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

	private class CrucibleServerComboBoxModel extends AbstractListModel implements ComboBoxModel {
		private Collection<CrucibleServerCfgWrapper> data;

		private Collection<CrucibleServerCfgWrapper> getServers() {
			if (data == null) {
				data = MiscUtil.buildArrayList();
				for (ServerCfg serverCfg : projectConfiguration.getServers()) {
					if (serverCfg.getServerType() == ServerType.CRUCIBLE_SERVER && serverCfg.isEnabled()) {
						data.add(new CrucibleServerCfgWrapper((CrucibleServerCfg) serverCfg));
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
			return CRUCIBLE_SERVER_NONE;
		}

		public void setSelectedItem(final Object anItem) {
			final Object selectedItem = getSelectedItem();
			if (selectedItem != null && !selectedItem.equals(anItem) || selectedItem == null && anItem != null) {
				if (anItem != null) {
					CrucibleServerCfgWrapper item = (CrucibleServerCfgWrapper) anItem;
					final CrucibleServerCfg wrapped = item.getWrapped();
					if (wrapped != null) {
						projectConfiguration.setDefaultCrucibleServerId(wrapped.getServerId());
						projectConfiguration.setDefaultCrucibleRepo(null);
						projectConfiguration.setDefaultCrucibleProject(null);
					} else {
						projectConfiguration.setDefaultCrucibleServerId(null);
					}
				} else {
					projectConfiguration.setDefaultCrucibleServerId(null);
				}
				fireContentsChanged(this, -1, -1);
			}
		}

		public Object getElementAt(final int index) {
			if (index == 0) {
				return CRUCIBLE_SERVER_NONE;
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


	private class FishEyeServerComboBoxModel extends AbstractListModel implements ComboBoxModel {
		private Collection<FishEyeServerWrapper> data;

		private Collection<FishEyeServerWrapper> getServers() {
			if (data == null) {
				data = MiscUtil.buildArrayList();
				for (ServerCfg serverCfg : projectConfiguration.getServers()) {
					final FishEyeServer fishEye = serverCfg.asFishEyeServer();
					if (fishEye != null && fishEye.isEnabled()) {
						data.add(new FishEyeServerWrapper(fishEye));
					}
				}
			}
			return data;
		}

		public Object getSelectedItem() {
			for (FishEyeServerWrapper server : getServers()) {
				if (server.getWrapped().getServerId().equals(projectConfiguration.getDefaultFishEyeServerId())) {
					return server;
				}
			}
			return FISHEYE_SERVER_NONE;
		}

		public void setSelectedItem(final Object anItem) {
			final Object selectedItem = getSelectedItem();
			if (selectedItem != null && !selectedItem.equals(anItem) || selectedItem == null && anItem != null) {
				if (anItem != null) {
					FishEyeServerWrapper item = (FishEyeServerWrapper) anItem;
					final FishEyeServer wrapped = item.getWrapped();
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
				return FISHEYE_SERVER_NONE;
			}
			int i = 1;
			for (FishEyeServerWrapper server : getServers()) {
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



	abstract class MyModel<T extends GenericWrapper<?>, R, S extends Server>
			extends AbstractListModel implements ComboBoxModel {
		private Map<ServerId, Collection<T>> data;
		private static final int INITIAL_CAPACITY = 10;
		private final T fetching;
		private final T none;
		private final String elementsType;
		private final String serverType;

		public MyModel(final T fetching, final T none, final String elementsType, final String serverType) {
			this.fetching = fetching;
			this.none = none;
			this.elementsType = elementsType;
			this.serverType = serverType;
		}

		protected abstract T toT(R element);
		protected abstract Collection<R> getR(S serverCfg) throws Exception;
		protected abstract boolean isEqual(T element);
		protected abstract void setOption(final T newSelection);

		private Collection<T> getElements(final S server) {
			if (data == null) {
				data = MiscUtil.buildConcurrentHashMap(INITIAL_CAPACITY);
			}

			Collection<T> wrappers = data.get(server.getServerId());
			if (wrappers == null) {
				wrappers = MiscUtil.<T>buildArrayList(fetching);
				data.put(server.getServerId(), wrappers);

				uiTaskExecutor.execute(new UiTask() {

					private String lastAction;
					public void run() throws Exception {
						lastAction = "retrieving available " + elementsType + " from " + serverType
								+ " server " + server.getName();
						final Collection<T> elements = MiscUtil.buildArrayList();
						elements.add(none);
						final Collection<R> remoteElems = getR(server);
						for (R remoteElem : remoteElems) {
							final T wrapper = toT(remoteElem);
							elements.add(wrapper);
						}

						data.put(server.getServerId(), elements);
					}

					public void onSuccess() {
						lastAction = "populating " + elementsType + " combobox";
						refresh();
					}

					public void onError() {
						final Collection<T> elements = MiscUtil.buildArrayList(none);
						data.put(server.getServerId(), elements);
						setOption(null);
						refresh();
					}

					public Component getComponent() {
						return ProjectDefaultsConfigurationPanel.this;
					}

					public String getLastAction() {
						return lastAction;
					}
				});
			}

			return wrappers;
		}

		public T getSelectedItem() {
			final S currentServer = getCurrentServer();
			if (currentServer == null) {
				return none;
			}
			for (T element : getElements(currentServer)) {
				if (element == fetching) {
					return fetching;
				}
				if (element.getWrapped() != null
						&& isEqual(element)) {
					return element;
				}

			}
			return none;
		}

		protected abstract S getCurrentServer();


		public void setSelectedItem(final Object anItem) {
			final Object selectedItem = getSelectedItem();
			if (selectedItem != null && !selectedItem.equals(anItem) || selectedItem == null && anItem != null) {
				if (anItem != null) {
					@SuppressWarnings("unchecked")
					final T item = (T) anItem;
					setOption(item);
				} else {
					setOption(null);
				}
				fireContentsChanged(this, -1, -1);
			}
		}

		public void refresh() {
			fireContentsChanged(this, -1, -1);
		}

		public T getElementAt(final int index) {
			int i = 0;
			final S cfg = getCurrentServer();
			if (cfg == null) {
				return none;
			}
			for (T element : getElements(cfg)) {
				if (i == index) {
					return element;
				}
				i++;
			}
			return null;
		}

		public int getSize() {
			final S currentServer = getCurrentServer();
			if (currentServer != null) {
				return getElements(currentServer).size();
			} else {
				return 1;
			}

		}
	}
}
