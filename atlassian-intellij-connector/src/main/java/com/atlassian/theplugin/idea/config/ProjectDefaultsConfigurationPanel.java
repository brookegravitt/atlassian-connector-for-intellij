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
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.fisheye.FishEyeServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.idea.config.serverconfig.defaultCredentials.TestDefaultCredentialsDialog;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.intellij.openapi.project.Project;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ProjectDefaultsConfigurationPanel extends JPanel {

	private static final int ALL_COLUMNS = 6;
	private JComboBox defaultCrucibleServerCombo = new JComboBox();
	private JComboBox defaultCrucibleProjectCombo = new JComboBox();
	private JComboBox defaultCrucibleRepositoryCombo = new JComboBox();
	private JComboBox defaultFishEyeServerCombo = new JComboBox();
	private JComboBox defaultFishEyeRepositoryCombo = new JComboBox();
	private JComboBox defaultJiraServerCombo = new JComboBox();
	private JTextField pathToProjectEdit = new JTextField();
	private JTextField defaultUserName = new JTextField();
	private JButton defaultCredentialsTestButton = new JButton("Test Connections");
	private JPasswordField defaultPassword = new JPasswordField();
	private final Project project;
	private ProjectConfiguration projectConfiguration;
	private final CrucibleServerFacade crucibleServerFacade;
	private final FishEyeServerFacade fishEyeServerFacade;
	private final BambooServerFacade bambooServerFacade;
	private final JIRAServerFacade jiraServerFacade;
	private final UiTaskExecutor uiTaskExecutor;
	private UserCfg defaultCredentials;
	private static final JiraServerCfgWrapper JIRA_SERVER_NONE = new JiraServerCfgWrapper(null);
	private static final CrucibleServerCfgWrapper CRUCIBLE_SERVER_NONE = new CrucibleServerCfgWrapper(null);
	private static final FishEyeServerWrapper FISHEYE_SERVER_NONE = new FishEyeServerWrapper(null);
	private static final CrucibleProjectWrapper CRUCIBLE_PROJECT_NONE = new CrucibleProjectWrapper(null);
	private static final GenericComboBoxItemWrapper<String> FISHEYE_REPO_NONE = new GenericComboBoxItemWrapper<String>(null);
	private static final GenericComboBoxItemWrapper<String> FISHEYE_REPO_FETCHING
			= new GenericComboBoxItemWrapper<String>(null) {
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

	private final MyModel<CrucibleProjectWrapper, CrucibleProject, CrucibleServerCfg> crucProjectModel
			= new MyModel<CrucibleProjectWrapper, CrucibleProject, CrucibleServerCfg>(
			CRUCIBLE_PROJECT_FETCHING, CRUCIBLE_PROJECT_NONE, "projects", "Crucible") {

		@Override
		protected CrucibleProjectWrapper toT(final CrucibleProject element) {
			return new CrucibleProjectWrapper(element);
		}

		@Override
		protected List<CrucibleProject> getR(final CrucibleServerCfg serverCfg)
				throws RemoteApiException, ServerPasswordNotProvidedException {
			return crucibleServerFacade.getProjects(new ServerData(serverCfg, defaultCredentials));
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
			final CrucibleProject wrapped = newSelection.getWrapped();
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
			return crucibleServerFacade.getRepositories(new ServerData(serverCfg, defaultCredentials));
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


	private final MyModel<GenericComboBoxItemWrapper<String>, String, FishEyeServer> fishRepositoryModel
			= new MyModel<GenericComboBoxItemWrapper<String>, String, FishEyeServer>(FISHEYE_REPO_FETCHING, FISHEYE_REPO_NONE,
			"repositories", "FishEye") {


		@Override
		protected GenericComboBoxItemWrapper<String> toT(final String element) {
			return new GenericComboBoxItemWrapper<String>(element);
		}

		@Override
		protected Collection<String> getR(final FishEyeServer serverCfg) throws Exception {
			return fishEyeServerFacade.getRepositories(new ServerData(serverCfg, defaultCredentials));
		}

		@Override
		protected boolean isEqual(final GenericComboBoxItemWrapper<String> element) {
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
		protected void setOption(final GenericComboBoxItemWrapper<String> newSelection) {
			if (newSelection != null) {
				projectConfiguration.setDefaultFishEyeRepo(newSelection.getWrapped());
			} else {
				projectConfiguration.setDefaultFishEyeRepo(null);
			}
		}
	};

	private static final String CRUCIBLE_HELP_TEXT = "<html>Default values for the Crucible review creation dialog";

	private static final String JIRA_HELP_TEXT = "<html>Default values for the Jira assigned for project";

	private static final String FISHEYE_HELP_TEXT_1 = "<html>The values below will be used for "
			+ "the construction of FishEye code pointer links, "
			+ "available in popup menus in your source code editor.";

	private static final String FISHEYE_HELP_TEXT_2 = "<html>"
			+ "Path to the root of the project in your repository. "
			+ "Typically it will be something like <b>\"trunk/\"</b> or <b>\"trunk/myproject\"</b>. "
			+ "Leave blank if your project is located at the repository root";

	public ProjectDefaultsConfigurationPanel(final Project project, final ProjectConfiguration projectConfiguration,
			final CrucibleServerFacade crucibleServerFacade, final FishEyeServerFacade fishEyeServerFacade,
			final BambooServerFacade bambooServerFacade, final JIRAServerFacade jiraServerFacade,
			final UiTaskExecutor uiTaskExecutor, @NotNull UserCfg defaultCredentials) {
		this.project = project;
		this.projectConfiguration = projectConfiguration;
		this.crucibleServerFacade = crucibleServerFacade;
		this.fishEyeServerFacade = fishEyeServerFacade;
		this.bambooServerFacade = bambooServerFacade;
		this.jiraServerFacade = jiraServerFacade;
		this.uiTaskExecutor = uiTaskExecutor;
		this.defaultCredentials = defaultCredentials;

		pathToProjectEdit.setToolTipText("Path to root directory in your repository. "
				+ "E.g. trunk/myproject. Leave it blank if your project is located at the repository root");
//		panel.setPreferredSize(new Dimension(300, 200));

		final FormLayout layout = new FormLayout(
				"10dlu, 20dlu, right:pref, 3dlu, min(150dlu;default):grow, 3dlu", //columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 20dlu, p, 3dlu, p, 3dlu, p, 3dlu, "
						+ "p, 3dlu, p, 3dlu, p, 10dlu, p, 3dlu, p, 3dlu, "
						+ "p, 20dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, fill:p"); //rows

		//CHECKSTYLE:MAGIC:OFF
		PanelBuilder builder = new PanelBuilder(layout, this);
		builder.setDefaultDialogBorder();
		final CellConstraints cc = new CellConstraints();
		builder.addSeparator("Crucible", cc.xyw(1, 1, ALL_COLUMNS));
		JLabel cruHelp = new JLabel(CRUCIBLE_HELP_TEXT);
		cruHelp.setFont(cruHelp.getFont().deriveFont(10.0f));
		// jgorycki: well, it seems like FormLayout doesn't give a shit about JLabel's maximum width. However,
		// if I set it to something sane, at least the JLabel seems to wrap its HTML contents properly, instead
		// of producing one long line
		cruHelp.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));
		builder.add(cruHelp, cc.xyw(2, 3, ALL_COLUMNS - 1));
		builder.addLabel("Default Server:", cc.xy(3, 5));
		builder.add(defaultCrucibleServerCombo, cc.xy(5, 5));
		builder.addLabel("Default Project:", cc.xy(3, 7));
		builder.add(defaultCrucibleProjectCombo, cc.xy(5, 7));
		builder.addLabel("Default Repository:", cc.xy(3, 9));
		builder.add(defaultCrucibleRepositoryCombo, cc.xy(5, 9));

		builder.addSeparator("FishEye", cc.xyw(1, 11, ALL_COLUMNS));
		JLabel fshHelp1 = new JLabel(FISHEYE_HELP_TEXT_1);
		fshHelp1.setFont(fshHelp1.getFont().deriveFont(10.0f));
		fshHelp1.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));
		builder.add(fshHelp1, cc.xyw(2, 13, ALL_COLUMNS - 1));
		builder.addLabel("Default Server:", cc.xy(3, 15));
		builder.add(defaultFishEyeServerCombo, cc.xy(5, 15));
		builder.addLabel("Default Repository:", cc.xy(3, 17));
		builder.add(defaultFishEyeRepositoryCombo, cc.xy(5, 17));
		builder.addLabel("Path to Project:", cc.xy(3, 19));
		builder.add(pathToProjectEdit, cc.xy(5, 19));
		JLabel fshHelp2 = new JLabel(FISHEYE_HELP_TEXT_2);
		fshHelp2.setFont(fshHelp2.getFont().deriveFont(10.0f));
		fshHelp2.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));
		builder.add(fshHelp2, cc.xy(5, 21));

		builder.addSeparator("JIRA", cc.xyw(1, 23, ALL_COLUMNS));
		JLabel jiraHelp = new JLabel(JIRA_HELP_TEXT);
		jiraHelp.setFont(jiraHelp.getFont().deriveFont(10.0f));
		// jgorycki: well, it seems like FormLayout doesn't give a shit about JLabel's maximum width. However,
		// if I set it to something sane, at least the JLabel seems to wrap its HTML contents properly, instead
		// of producing one long line
		jiraHelp.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));
		builder.add(jiraHelp, cc.xyw(2, 25, ALL_COLUMNS - 1));
		builder.addLabel("Default Server:", cc.xy(3, 27));
		builder.add(defaultJiraServerCombo, cc.xy(5, 27));

		builder.addSeparator("Default Credentials", cc.xyw(1, 29, ALL_COLUMNS));
		final String DEFAULT_CREDENTIALS_TEXT = "Default credentials for selected servers";
		JLabel defaultCredentialsLabel = new JLabel(DEFAULT_CREDENTIALS_TEXT);
		defaultCredentialsLabel.setFont(defaultCredentialsLabel.getFont().deriveFont(10.0f));
		builder.add(defaultCredentialsLabel, cc.xyw(2, 31, ALL_COLUMNS - 1));
		builder.addLabel("Username:", cc.xy(3, 33));
		builder.add(defaultUserName, cc.xy(5, 33));
		builder.addLabel("Password:", cc.xy(3, 35));
		builder.add(defaultPassword, cc.xy(5, 35));
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(defaultCredentialsTestButton, BorderLayout.EAST);
		defaultCredentialsTestButton.setMaximumSize(defaultCredentialsTestButton.getPreferredSize());

		builder.add(defaultCredentialsTestButton, cc.xy(5, 37, CellConstraints.RIGHT, CellConstraints.CENTER));


		initializeControls();
		registerListeners();

		//CHECKSTYLE:MAGIC:ON

	}

	private void registerListeners() {
		defaultCredentialsTestButton.addMouseListener(new MouseListener() {
			public void mouseClicked(final MouseEvent e) {
				testDefaultCredentials();
			}

			public void mousePressed(final MouseEvent e) {
			}

			public void mouseReleased(final MouseEvent e) {
			}

			public void mouseEntered(final MouseEvent e) {
			}

			public void mouseExited(final MouseEvent e) {
			}
		});
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

		defaultUserName.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(final DocumentEvent e) {
				setUserName();
			}

			public void removeUpdate(final DocumentEvent e) {
				setUserName();
			}

			public void changedUpdate(final DocumentEvent e) {
				setUserName();
			}

			private void setUserName() {
				defaultCredentials.setUserName(defaultUserName.getText());
			}
		});

		defaultPassword.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(final DocumentEvent e) {
				setPassword();
			}

			public void removeUpdate(final DocumentEvent e) {
				setPassword();
			}

			public void changedUpdate(final DocumentEvent e) {
				setPassword();
			}

			private void setPassword() {
				defaultCredentials.setPassword(String.valueOf(defaultPassword.getPassword()));
			}
		});
	}


	private void testDefaultCredentials() {
		final TestDefaultCredentialsDialog dialog = new TestDefaultCredentialsDialog(project, projectConfiguration,
				defaultCredentials);

		dialog.testConnection();

//		TestDefaultCredentials test = new TestDefaultCredentials(project, this, jiraServerFacade, crucibleServerFacade,
//				fishEyeServerFacade, bambooServerFacade);
//		Collection<TestDefaultCredentials.ServerDataExt> data = MiscUtil.buildArrayList();
//		for (ServerCfg serverCfg : projectConfiguration.getAllEnabledServersWithDefaultCredentials()) {
//			data.add(new TestDefaultCredentials.ServerDataExt(
//					new ServerData(serverCfg.getName(), serverCfg.getServerId().toString(), defaultCredentials.getUserName(),
//							defaultCredentials.getPassword(), serverCfg.getUrl()), serverCfg.getServerType()));
//		}
//		test.run(data);
	}

	private void initializeControls() {
		defaultCrucibleServerCombo.setModel(new CrucibleServerComboBoxModel());
		defaultFishEyeServerCombo.setModel(new FishEyeServerComboBoxModel());
		defaultCrucibleProjectCombo.setModel(crucProjectModel);

		defaultCrucibleRepositoryCombo.setModel(crucRepoModel);
		defaultFishEyeRepositoryCombo.setModel(fishRepositoryModel);
		pathToProjectEdit.setText(projectConfiguration.getFishEyeProjectPath());

		defaultJiraServerCombo.setModel(new JiraServerComboBoxModel());
		defaultUserName.setText(defaultCredentials.getUserName());
		defaultPassword.setText(defaultCredentials.getPassword());
	}


	public void setData(final ProjectConfiguration aProjectConfiguration) {
		this.projectConfiguration = aProjectConfiguration;
		initializeControls();
	}

	public void setDefaultCredentials(final UserCfg userCfg) {
		defaultCredentials.setUserName(userCfg.getUserName());
		defaultCredentials.setPassword(userCfg.getPassword());
		defaultUserName.setText(defaultCredentials.getUserName());
		defaultPassword.setText(defaultCredentials.getPassword());
	}


	private static class FishEyeServerWrapper extends GenericComboBoxItemWrapper<FishEyeServer> {
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


	private static class CrucibleRepoWrapper extends GenericComboBoxItemWrapper<Repository> {
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
						data.add(new CrucibleServerCfgWrapper(new ServerData(serverCfg, defaultCredentials)));
					}
				}
			}
			return data;
		}

		public Object getSelectedItem() {
			for (CrucibleServerCfgWrapper server : getServers()) {
				final ServerId defaultCrucibleServerId = projectConfiguration.getDefaultCrucibleServerId();
				if (server.getWrapped().getServerId().equals(
						defaultCrucibleServerId != null ? defaultCrucibleServerId : null)) {
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
					final ServerData wrapped = item.getWrapped();
					if (wrapped != null) {
						projectConfiguration.setDefaultCrucibleServerId((ServerIdImpl) wrapped.getServerId());
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

	private class JiraServerComboBoxModel extends AbstractListModel implements ComboBoxModel {
		private Collection<JiraServerCfgWrapper> data;

		private Collection<JiraServerCfgWrapper> getServers() {
			if (data == null) {
				data = MiscUtil.buildArrayList();
				for (ServerCfg serverCfg : projectConfiguration.getServers()) {
					if (serverCfg.getServerType() == ServerType.JIRA_SERVER && serverCfg.isEnabled()) {
						data.add(new JiraServerCfgWrapper((JiraServerCfg) serverCfg));
					}
				}
			}
			return data;
		}

		public Object getSelectedItem() {
			for (JiraServerCfgWrapper server : getServers()) {
				if (server.getWrapped().getServerId().equals(projectConfiguration.getDefaultJiraServerId())) {
					return server;
				}
			}
			return JIRA_SERVER_NONE;
		}

		public void setSelectedItem(final Object anItem) {
			final Object selectedItem = getSelectedItem();
			if (selectedItem != null && !selectedItem.equals(anItem) || selectedItem == null && anItem != null) {
				if (anItem != null) {
					JiraServerCfgWrapper item = (JiraServerCfgWrapper) anItem;
					final JiraServerCfg wrapped = item.getWrapped();
					if (wrapped != null) {
						projectConfiguration.setDefaultJiraServerId(wrapped.getServerId());
					} else {
						projectConfiguration.setDefaultJiraServerId(null);
					}
				} else {
					projectConfiguration.setDefaultJiraServerId(null);
				}
				fireContentsChanged(this, -1, -1);
			}
		}

		public Object getElementAt(final int index) {
			if (index == 0) {
				return JIRA_SERVER_NONE;
			}
			int i = 1;
			for (JiraServerCfgWrapper server : getServers()) {
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


	abstract class MyModel<T extends GenericComboBoxItemWrapper<?>, R, S extends Server>
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
				wrappers = MiscUtil.buildArrayList(fetching);
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
