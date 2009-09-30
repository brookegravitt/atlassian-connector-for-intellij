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

package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.connector.intellij.bamboo.BambooServerFacade;
import com.atlassian.connector.intellij.bamboo.IntelliJBambooServerFacade;
import com.atlassian.connector.intellij.crucible.CrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.connector.intellij.fisheye.FishEyeServerFacade;
import com.atlassian.connector.intellij.fisheye.IntelliJFishEyeServerFacade;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.jira.IntelliJJiraServerFacade;
import com.atlassian.theplugin.commons.jira.JiraServerFacade;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.config.serverconfig.action.AddServerAction;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;

public class ServerConfigPanel extends JPanel implements DataProvider {
	private final ServerTreePanel serverTreePanel;
	private BlankPanel blankPanel;

	private CardLayout editPaneCardLayout;
	private JPanel editPane;
	private static final String BLANK_CARD = "Blank card";

	private static final float SPLIT_RATIO = 0.39f;

	private Collection<ServerCfg> serverCfgs;
	private final BambooServerConfigForm bambooServerConfigForm;
	private final JiraServerConfigForm jiraServerConfigForm;
	private final CrucibleServerConfigForm crucibleServerConfigForm;
	private final GenericServerConfigForm fisheyeServerConfigFrom;
	private final UserCfg defaultUser;
	private boolean isDefaultCredentialsAsked = false;

	public ServerConfigPanel(final Project project, final UserCfg defaultUser,
			ProjectConfiguration projectConfiguration,
			final ServerData selectedServer, final boolean isDefaultCredentialsAsked) {
		this.defaultUser = defaultUser;
		this.serverCfgs = projectConfiguration != null ? projectConfiguration.getServers() : new ArrayList<ServerCfg>();
		this.serverTreePanel = new ServerTreePanel(project, defaultUser);
		final CrucibleServerFacade crucibleServerFacade = IntelliJCrucibleServerFacade.getInstance();
		final BambooServerFacade bambooServerFacade = IntelliJBambooServerFacade.getInstance(PluginUtil.getLogger());
		final JiraServerFacade jiraServerFacade = IntelliJJiraServerFacade.getInstance();
		this.isDefaultCredentialsAsked = isDefaultCredentialsAsked;
		final FishEyeServerFacade fishEyeServerFacade = IntelliJFishEyeServerFacade.getInstance();
		/* required due to circular dependency unhandled by pico */
		this.serverTreePanel.setServerConfigPanel(this);
		jiraServerConfigForm = new JiraServerConfigForm(project, defaultUser, jiraServerFacade);
		crucibleServerConfigForm = new CrucibleServerConfigForm(project, defaultUser, crucibleServerFacade,
				fishEyeServerFacade);
		bambooServerConfigForm = new BambooServerConfigForm(project, defaultUser, bambooServerFacade);
		fisheyeServerConfigFrom = new GenericServerConfigForm(project, defaultUser, new ProductConnector(fishEyeServerFacade));
		initLayout();

		serverTreePanel.setData(serverCfgs);
		// This line selects server currently selected in the main panel
		serverTreePanel.setSelectedServer(selectedServer);
	}


	public boolean isDefaultCredentialsAsked() {
		return isDefaultCredentialsAsked;
	}

	public UserCfg getDefaultUser() {
		return defaultUser;
	}

	public void setData(Collection<ServerCfg> aServerCfgs) {
		serverCfgs = aServerCfgs;
		serverTreePanel.setData(serverCfgs);
	}


	private void initLayout() {
		GridBagLayout gbl = new GridBagLayout();

		setLayout(gbl);

		Splitter splitter = new Splitter(false, SPLIT_RATIO);
		splitter.setShowDividerControls(false);
		splitter.setFirstComponent(createSelectPane());
		splitter.setSecondComponent(createEditPane());
		splitter.setHonorComponentsMinimumSize(true);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(Constants.DIALOG_MARGIN,
				Constants.DIALOG_MARGIN,
				Constants.DIALOG_MARGIN,
				Constants.DIALOG_MARGIN);
		add(splitter, c);
	}

	private JComponent createSelectPane() {

		final JPanel selectPane = new JPanel(new BorderLayout());
		final JPanel toolBarPanel = new JPanel(new BorderLayout());
		toolBarPanel.add(createToolbar(), BorderLayout.NORTH);
		selectPane.add(toolBarPanel, BorderLayout.NORTH);
		selectPane.add(serverTreePanel, BorderLayout.CENTER);
		selectPane.setMinimumSize(new Dimension(ServerTreePanel.WIDTH, ServerTreePanel.HEIGHT));
		return selectPane;
	}

	protected JComponent createToolbar() {
		ActionManager actionManager = ActionManager.getInstance();
		ActionGroup actionGroup = (ActionGroup) actionManager.getAction("ThePlugin.ServerConfigToolBar");
		return actionManager.createActionToolbar("ThePluginConfig", actionGroup, true).getComponent();
	}

	private JComponent createEditPane() {
		editPane = new JPanel();
		editPaneCardLayout = new CardLayout();
		editPane.setLayout(editPaneCardLayout);
		editPane.add(bambooServerConfigForm.getRootComponent(), "Bamboo Servers");
		editPane.add(jiraServerConfigForm.getRootComponent(), "JIRA Servers");
		editPane.add(crucibleServerConfigForm.getRootComponent(), "Crucible Servers");
		editPane.add(fisheyeServerConfigFrom.getRootComponent(), "FishEye Servers");
		editPane.add(getBlankPanel(), BLANK_CARD);

		return editPane;
	}

	private JComponent getBlankPanel() {
		if (blankPanel == null) {
			blankPanel = new BlankPanel();
		}
		return blankPanel;
	}


	@Override
	public boolean isEnabled() {
		return true;
	}

	public String getTitle() {
		return "Servers";
	}

	public void addServer(ServerType serverType) {
		serverTreePanel.addServer(serverType);
	}

	public void removeServer() {
		serverTreePanel.removeServer();
	}

	public void copyServer() {
		serverTreePanel.copyServer();
	}


	public void saveData(ServerType serverType) {
		switch (serverType) {
			case BAMBOO_SERVER:
				bambooServerConfigForm.saveData();
				break;
			case CRUCIBLE_SERVER:
				crucibleServerConfigForm.saveData();
				break;
			case JIRA_SERVER:
				jiraServerConfigForm.saveData();
				break;
			case FISHEYE_SERVER:
				fisheyeServerConfigFrom.saveData();
				break;
            case JIRA_STUDIO_SERVER:
                break;
			default:
				throw new AssertionError("switch not implemented for [" + serverType + "]");
		}
	}

	public void saveData() {
		for (ServerType serverType : ServerType.values()) {
			saveData(serverType);
		}

	}


	public void editServer(ServerCfg serverCfg) {
		ServerType serverType = serverCfg.getServerType();
		editPaneCardLayout.show(editPane, serverType.toString());
		switch (serverType) {
			case BAMBOO_SERVER:
				BambooServerCfg bambooServerCfg = (BambooServerCfg) serverCfg;
				bambooServerConfigForm.saveData();
				bambooServerConfigForm.setData(bambooServerCfg);
				break;
			case CRUCIBLE_SERVER:
				CrucibleServerCfg crucibleServerCfg = (CrucibleServerCfg) serverCfg;
				crucibleServerConfigForm.saveData();
				crucibleServerConfigForm.setData(crucibleServerCfg);
				break;
			case JIRA_SERVER:
				jiraServerConfigForm.saveData();
				jiraServerConfigForm.setData((JiraServerCfg) serverCfg);
				break;
			case FISHEYE_SERVER:
				fisheyeServerConfigFrom.saveData();
				fisheyeServerConfigFrom.setData(serverCfg);
				break;
			default:
				throw new AssertionError("switch not implemented for [" + serverType + "]");
		}
	}

	public void showEmptyPanel() {
		editPaneCardLayout.show(editPane, BLANK_CARD);
	}

	public void finalizeData() {
		bambooServerConfigForm.finalizeData();
		crucibleServerConfigForm.finalizeData();
		jiraServerConfigForm.finalizeData();
		fisheyeServerConfigFrom.finalizeData();
	}

	public ServerCfg getSelectedServer() {
		return serverTreePanel.getSelectedServer();
	}


	private class BlankPanel extends JPanel {

		public BlankPanel() {
			initLayout();
		}

		private static final String TEXT_BEGIN = "Press the ";
		private static final String TEXT_END = " button to define a new Server configuration.";

		private void initLayout() {

			setLayout(new BorderLayout());

			JPanel instructionsPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.NONE;
			gbc.weightx = 0.0;
			instructionsPanel.setOpaque(false);
			instructionsPanel.add(new JLabel(TEXT_BEGIN), gbc);
			gbc.gridx++;
			JLabel addServerLabel = new JLabel(IconLoader.getIcon("/general/add.png"));
			addServerLabel.addMouseListener(new MouseAdapter() {
				private Cursor oldCursor;

				@Override
				public void mouseEntered(MouseEvent mouseEvent) {
					oldCursor = getCursor();
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent) {
					if (oldCursor != null) {
						setCursor(oldCursor);
						oldCursor = null;
					}
				}

				@Override
				public void mouseClicked(MouseEvent mouseEvent) {
					runAddServerAction(mouseEvent);
				}
			});
			instructionsPanel.add(addServerLabel, gbc);
			gbc.gridx++;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			instructionsPanel.add(new JLabel(TEXT_END), gbc);
			add(instructionsPanel, BorderLayout.NORTH);
		}

		private void runAddServerAction(MouseEvent mouseEvent) {
			ServerType type = serverTreePanel.getSelectedServerType();
			if (type != null) {
				addServer(type);
			} else {
				AddServerAction.showAddServerPopup(mouseEvent);
			}
		}


	}

	@Nullable
	public Object getData(@NonNls final String dataId) {
		if (dataId.equals(Constants.SERVER_CONFIG_PANEL)) {
			return this;
		} else if (dataId.equals(Constants.SERVERS)) {
			return serverCfgs;
		} else {
			return serverTreePanel.getData(dataId);
		}
	}

}

