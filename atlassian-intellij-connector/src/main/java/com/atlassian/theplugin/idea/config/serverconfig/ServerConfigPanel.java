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

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.fisheye.FishEyeServerFacadeImpl;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.config.ProjectConfigurationPanel;
import com.atlassian.theplugin.idea.config.serverconfig.action.AddServerAction;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
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
	private final GenericServerConfigForm jiraServerConfigForm;
	private final CrucibleServerConfigForm crucibleServerConfigForm;
	private final GenericServerConfigForm fisheyeServerConfigFrom;
	private final ProjectConfigurationPanel projectConfigurationPanel;
	private final UserCfg defaultUser;
	private boolean isDefaultCredentialsAsked = false;

	public ServerConfigPanel(final ProjectConfigurationPanel projectConfigurationPanel,
			Project project, final UserCfg defaultUser,
			ProjectConfiguration projectConfiguration,
			final ServerCfg selectedServer, final boolean isDefaultCredentialsAsked) {
		this.projectConfigurationPanel = projectConfigurationPanel;
		this.defaultUser = defaultUser;
		this.serverCfgs = projectConfiguration != null ? projectConfiguration.getServers() : new ArrayList<ServerCfg>();
		this.serverTreePanel = new ServerTreePanel();
		final CrucibleServerFacade crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
		final BambooServerFacade bambooServerFacade = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger());
		final JIRAServerFacade jiraServerFacade = JIRAServerFacadeImpl.getInstance();
		this.isDefaultCredentialsAsked = isDefaultCredentialsAsked;
		final FishEyeServerFacadeImpl fishEyeServerFacade = FishEyeServerFacadeImpl.getInstance();
		/* required due to circular dependency unhandled by pico */
		this.serverTreePanel.setServerConfigPanel(this);
		jiraServerConfigForm = new GenericServerConfigForm(project, defaultUser, new ProductConnector(jiraServerFacade));
		crucibleServerConfigForm = new CrucibleServerConfigForm(project, defaultUser, crucibleServerFacade,
				fishEyeServerFacade);
		bambooServerConfigForm = new BambooServerConfigForm(project, defaultUser, bambooServerFacade);
		fisheyeServerConfigFrom = new GenericServerConfigForm(project, defaultUser, new ProductConnector(fishEyeServerFacade));
		initLayout();

		serverTreePanel.setData(serverCfgs);
        //this line duplicates selction setData does it
		//serverTreePanel.setSelectedServer(selectedServer);
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
				jiraServerConfigForm.setData(serverCfg);
				break;
			case FISHEYE_SERVER:
				fisheyeServerConfigFrom.saveData();
				fisheyeServerConfigFrom.setData(serverCfg);
				break;
			default:
				throw new AssertionError("switch not implemented for [" + serverType + "]");
		}


		askForDefaultCredentials(serverCfg);

	}


	private void askForDefaultCredentials(final ServerCfg serverCfg) {

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				if (projectConfigurationPanel.isShowing()) {
					if (projectConfigurationPanel != null && !isDefaultCredentialsAsked
							&& (defaultUser == null
                            || (defaultUser.getPassword().equals("") && defaultUser.getPassword().equals(""))
									&& serverCfg.getUsername().length() > 0)) {
						int answer = Messages.showYesNoDialog(projectConfigurationPanel,
								"<html>Do yo want to set server <b>" + serverCfg.getName()
                                + "</b> <i>username</i> and <i>password</i>"
								+ " as default credentials for Atlassian IntelliJ Connector?</html>", "Set as default",
								Messages.getQuestionIcon());
						isDefaultCredentialsAsked = true;
						if (answer == DialogWrapper.OK_EXIT_CODE) {
							projectConfigurationPanel.setDefaultCredentials(
									new UserCfg(serverCfg.getUsername(), serverCfg.getPassword(), true));

						}
					}
				}
			}
		});

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

