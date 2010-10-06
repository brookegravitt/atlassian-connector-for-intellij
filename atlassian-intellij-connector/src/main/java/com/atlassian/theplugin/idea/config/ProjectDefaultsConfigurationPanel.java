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

import com.atlassian.connector.intellij.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.jira.JiraServerFacade;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.idea.config.serverconfig.defaultCredentials.TestDefaultCredentialsDialog;
import com.intellij.openapi.project.Project;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;

public class ProjectDefaultsConfigurationPanel extends JPanel {

	private static final int ALL_COLUMNS = 6;
	private JComboBox defaultJiraServerCombo = new JComboBox();
	private JTextField defaultUsername = new JTextField();
	private JButton defaultCredentialsTestButton = new JButton("Test Connections");
	private JPasswordField defaultPassword = new JPasswordField();
	private final Project project;
	private ProjectConfiguration projectConfiguration;
	private final BambooServerFacade bambooServerFacade;
	private final JiraServerFacade jiraServerFacade;
	private final UiTaskExecutor uiTaskExecutor;
	private UserCfg defaultCredentials;
	private static final JiraServerCfgWrapper JIRA_SERVER_NONE = new JiraServerCfgWrapper(null);



	private static final String JIRA_HELP_TEXT = "<html>Default values for the Jira assigned for project";

	public ProjectDefaultsConfigurationPanel(final Project project, final ProjectConfiguration projectConfiguration,
			final BambooServerFacade bambooServerFacade, final JiraServerFacade jiraServerFacade,
			final UiTaskExecutor uiTaskExecutor, @NotNull UserCfg defaultCredentials) {
		this.project = project;
		this.projectConfiguration = projectConfiguration;
		this.bambooServerFacade = bambooServerFacade;
		this.jiraServerFacade = jiraServerFacade;
		this.uiTaskExecutor = uiTaskExecutor;
		this.defaultCredentials = defaultCredentials;


		final FormLayout layout = new FormLayout(
				"10dlu, 20dlu, right:pref, 3dlu, min(150dlu;default):grow, 3dlu", //columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 20dlu, p, 3dlu, p, 3dlu, p, 3dlu, "
						+ "p, 3dlu, p, 3dlu, p, 10dlu, p, 3dlu, p, 3dlu, "
						+ "p, 20dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, fill:p"); //rows

		//CHECKSTYLE:MAGIC:OFF
		PanelBuilder builder = new PanelBuilder(layout, this);
		builder.setDefaultDialogBorder();
		final CellConstraints cc = new CellConstraints();

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
		builder.add(defaultUsername, cc.xy(5, 33));
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


		defaultUsername.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(final DocumentEvent e) {
				setUsername();
			}

			public void removeUpdate(final DocumentEvent e) {
				setUsername();
			}

			public void changedUpdate(final DocumentEvent e) {
				setUsername();
			}

			private void setUsername() {
				defaultCredentials.setUsername(defaultUsername.getText());
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

		defaultJiraServerCombo.setModel(new JiraServerComboBoxModel());
		defaultUsername.setText(defaultCredentials.getUsername());
		defaultPassword.setText(defaultCredentials.getPassword());
	}


	public void setData(final ProjectConfiguration aProjectConfiguration) {
		this.projectConfiguration = aProjectConfiguration;
		initializeControls();
	}

	public void setDefaultCredentials(final UserCfg userCfg) {
		defaultCredentials.setUsername(userCfg.getUsername());
		defaultCredentials.setPassword(userCfg.getPassword());
		defaultUsername.setText(defaultCredentials.getUsername());
		defaultPassword.setText(defaultCredentials.getPassword());
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
            //ServerData defaultJira = IdeaHelper.getProjectCfgManager(project).getDefaultJiraServer();
            final JiraServerCfg defaultJira = projectConfiguration.getDefaultJiraServer();
            final ServerId defaultJiraServerId = defaultJira != null ? defaultJira.getServerId() : null;

			for (JiraServerCfgWrapper server : getServers()) {
				if (server.getWrapped().getServerId().equals(defaultJiraServerId)) {
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
}
