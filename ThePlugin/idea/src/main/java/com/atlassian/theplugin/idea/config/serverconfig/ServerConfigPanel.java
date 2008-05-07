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
import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.configuration.ProductServerConfiguration;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.config.ContentPanel;
import com.atlassian.theplugin.idea.config.serverconfig.model.ServerNode;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.util.Connector;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class ServerConfigPanel extends JPanel implements ContentPanel {
    private final ServerTreePanel serverTreePanel;
    private BlankPanel blankPanel = null;

    private CardLayout editPaneCardLayout;
    private JPanel editPane;
	private static final String BLANK_CARD = "Blank card";

	private static final float SPLIT_RATIO = 0.3f;
	private Map<ServerType, ServerPanel> serverPanels = new HashMap<ServerType, ServerPanel>();

	private transient PluginConfiguration localConfigCopy;
	private final transient CrucibleServerFacade crucibleServerFacade;
	private final transient BambooServerFacade bambooServerFacade;
	private final transient JIRAServerFacade jiraServerFacade;
	private static ServerConfigPanel instance;

	private ServerConfigPanel() {
		this.serverTreePanel = ServerTreePanel.getInstance();
		this.crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
		this.bambooServerFacade = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger());
		this.jiraServerFacade = JIRAServerFacadeImpl.getInstance();
		/* required due to circular dependency unhandled by pico */
		this.serverTreePanel.setServerConfigPanel(this);
		initLayout();
    }

	public static ServerConfigPanel getInstance() {
		if (instance == null){
			instance = new ServerConfigPanel();
		}
		return instance;
	}

	private void initLayout() {
		GridBagLayout gbl = new GridBagLayout();

		setLayout(gbl);

		Splitter splitter = new Splitter(false, SPLIT_RATIO);
        splitter.setShowDividerControls(true);
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
        JPanel selectPane = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		selectPane.setLayout(gbl);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.NONE;
		c.ipady = 2;
		selectPane.add(createToolbar(), c);
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 0;
		// magic - don't ask why 3 is good - looks like crap otherwise
		// moreover - 3 is a magic number according to Checkstyle and 2 is not :)
		c.insets = new Insets(2 + 1, 2, 2, 2);
		selectPane.add(serverTreePanel, c);
		return selectPane;
    }

	private JComponent createToolbar() {
         ActionManager actionManager = ActionManager.getInstance();
        ActionGroup actionGroup = (ActionGroup) actionManager.getAction("ThePlugin.ServerConfigToolBar");
        return actionManager.createActionToolbar("ThePluginConfig", actionGroup, true).getComponent();
    }

	private JComponent createEditPane() {
        editPane = new JPanel();
        editPaneCardLayout = new CardLayout();
        editPane.setLayout(editPaneCardLayout);
        for (int i = 0; i < ServerType.values().length; i++) {
            ServerType serverType = ServerType.values()[i];
            editPane.add(getServerPanel(serverType), serverType.toString());
        }
		editPane.add(getBlankPanel(), BLANK_CARD);

        return editPane;
    }

	private JComponent getBlankPanel() {
        if (blankPanel == null) {
            blankPanel = new BlankPanel();
        }
        return blankPanel;
    }

	private JComponent getServerPanel(ServerType serverType) {
        if (!serverPanels.containsKey(serverType)) {
            switch (serverType) {
                case BAMBOO_SERVER:
                    serverPanels.put(ServerType.BAMBOO_SERVER, new BambooServerConfigForm(bambooServerFacade));
                    break;
                case CRUCIBLE_SERVER:
                    serverPanels.put(ServerType.CRUCIBLE_SERVER, new GenericServerConfigForm(new Connector() {
						public void connect() throws ThePluginException {
							validate();
							try {
								crucibleServerFacade.testServerConnection(getUrl(), getUserName(), getPassword());
							} catch (RemoteApiException e) {
								throw new ThePluginException(e.getMessage(), e);
							}
						}
					}));
                    break;
                case JIRA_SERVER:
                    serverPanels.put(ServerType.JIRA_SERVER, new GenericServerConfigForm(new Connector() {
						public void connect() throws ThePluginException {
							validate();
							try {
								jiraServerFacade.testServerConnection(getUrl(), getUserName(),
										getPassword());
							} catch (RemoteApiException e) {
								throw new ThePluginException(e.getMessage(), e);
							}
						}
					}));
                    break;
                default:
                    break;
            }
        }
        return serverPanels.get(serverType).getRootComponent();
    }

	public boolean isEnabled() {
        return true;
    }

	public boolean isModified() {
        if (!getLocalPluginConfigurationCopy().equals(ConfigurationFactory.getConfiguration())) {
			return true;
        }

        for (ServerPanel entry : serverPanels.values()) {
            if (entry != null) {
                if (entry.isModified()) {
					return true;
                }
            }
        }

		return false;
    }

	public String getTitle() {
        return "Servers";
    }

	public void getData() {
        if (isModified()) {
            for (ServerType type : serverPanels.keySet()) {
				final ProductServerConfiguration conf = getLocalPluginConfigurationCopy().getProductServers(type);
				if (serverPanels.get(type).isModified()) {
                    if (conf.transientGetServer(serverPanels.get(type).getData()) != null) {
                        conf.storeServer(serverPanels.get(type).getData());
                    }
                }
                Collection<Server> s = conf.transientGetServers();
                ConfigurationFactory.getConfiguration().getProductServers(type).setServers(s);
			}

			this.serverTreePanel.setData(getLocalPluginConfigurationCopy());
       }
    }


	public void setData(PluginConfiguration config) {
		localConfigCopy = config;
		serverTreePanel.setData(getLocalPluginConfigurationCopy());
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

	public void storeServer(ServerNode serverNode) {
        Server server = serverNode.getServer();
        Server tempValue = serverPanels.get(serverNode.getServerType()).getData();
        switch (serverNode.getServerType()) {
            case BAMBOO_SERVER:
                server.transientSetSubscribedPlans(tempValue.transientGetSubscribedPlans());
				server.setUseFavourite(tempValue.getUseFavourite());
				break;
            default:
                break;
        }
        server.setName(tempValue.getName());
        server.setUserName(tempValue.getUserName());
        server.transientSetPasswordString(tempValue.transientGetPasswordString(), tempValue.getShouldPasswordBeStored());
		server.setEnabled(tempValue.getEnabled());
        server.setUrlString(tempValue.getUrlString());
    }

	public void editServer(ServerType serverType, Server server) {
        editPaneCardLayout.show(editPane, serverType.toString());
        serverPanels.get(serverType).setData(server);
    }

	public void showEmptyPanel() {
        editPaneCardLayout.show(editPane, BLANK_CARD);
    }


	static class BlankPanel extends JPanel {

        public BlankPanel() {
            initLayout();
        }

        private static final String TEXT_BEGIN = "Press the ";
        private static final String TEXT_END = " button to define a new Server configuration.";

        private void initLayout() {

            setLayout(new BorderLayout());

            DefaultStyledDocument doc = new DefaultStyledDocument();
            Style s = doc.addStyle(null, null);
            StyleConstants.setIcon(s, IconLoader.getIcon("/general/add.png"));
            Style d = doc.addStyle(null, null);
            StyleConstants.setFontFamily(d, getFont().getFamily());
            StyleConstants.setFontSize(d, getFont().getSize());
            try {
                doc.insertString(0, TEXT_BEGIN, d);
                doc.insertString(TEXT_BEGIN.length(), " ", s);
                doc.insertString(TEXT_BEGIN.length() + 1, TEXT_END, d);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            JTextPane pane = new JTextPane();
            pane.setBackground(getBackground());
            pane.setDocument(doc);
            pane.setEditable(false);
            pane.setVisible(true);

            add(pane, BorderLayout.NORTH);
        }


    }

    public PluginConfiguration getLocalPluginConfigurationCopy() {
        return localConfigCopy;
    }
}
