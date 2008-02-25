package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.crucible.CrucibleServerFactory;
import com.atlassian.theplugin.crucible.api.CrucibleException;
import com.atlassian.theplugin.exception.ThePluginException;
import com.atlassian.theplugin.idea.config.AbstractContentPanel;
import com.atlassian.theplugin.idea.config.ConfigPanel;
import com.atlassian.theplugin.idea.config.serverconfig.model.ServerNode;
import com.atlassian.theplugin.jira.JIRAServerFactory;
import com.atlassian.theplugin.jira.api.JIRALoginException;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.VerticalFlowLayout;
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

public class ServerConfigPanel extends AbstractContentPanel {
    private ServerTreePanel treePanel = null;
    private BlankPanel blankPanel = null;

    private CardLayout editPaneCardLayout;
    private JPanel editPane;
	private static final String BLANK_CARD = "Blank card";
	private static final String BAMBOO_GENERAL_CARD = "BAmboo General Card";

	private static final float SPLIT_RATIO = 0.3f;
	private Map<ServerType, AbstractServerPanel> serverPanels;
	private static ServerConfigPanel instance;
	private BambooGeneralPanel bambooGeneralPanel;

	private ServerConfigPanel() {
        serverPanels = new HashMap<ServerType, AbstractServerPanel>();
        initLayout();
    }

    private void initLayout() {
        setLayout(new BorderLayout());

        Splitter splitter = new Splitter(false, SPLIT_RATIO);
        splitter.setShowDividerControls(true);
        splitter.setFirstComponent(createSelectPane());
        splitter.setSecondComponent(createEditPane());
        splitter.setHonorComponentsMinimumSize(true);

        add(splitter, BorderLayout.CENTER);
    }

    private JComponent createSelectPane() {
        JPanel selectPane = new JPanel();
        selectPane.setLayout(new VerticalFlowLayout(true, true));
        selectPane.add(createToolbar());
        selectPane.add(getTreePanel());
        return selectPane;
    }

    private JComponent getTreePanel() {
        if (treePanel == null) {
            treePanel = new ServerTreePanel();
        }
        return treePanel;
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
		editPane.add(getBambooGeneralPanel(), BAMBOO_GENERAL_CARD);
		editPane.add(getBlankPanel(), BLANK_CARD);

        return editPane;
    }

	private JComponent getBlankPanel() {
        if (blankPanel == null) {
            blankPanel = new BlankPanel();
        }
        return blankPanel;
    }

	public JComponent getBambooGeneralPanel() {
		if (bambooGeneralPanel == null) {
			bambooGeneralPanel = new BambooGeneralPanel();
		}

		return bambooGeneralPanel;
	}

	private JComponent getServerPanel(ServerType serverType) {
        if (!serverPanels.containsKey(serverType)) {
            switch (serverType) {
                case BAMBOO_SERVER:
                    serverPanels.put(ServerType.BAMBOO_SERVER, new BambooServerConfigForm());
                    break;
                case CRUCIBLE_SERVER:
                    serverPanels.put(ServerType.CRUCIBLE_SERVER, new GenericServerConfigForm(new ConnectionTester() {
                        public void testConnection(String username, String password, String server)
								throws ThePluginException {
                            try {
                                CrucibleServerFactory.getCrucibleServerFacade()
                                        .testServerConnection(server, username, password);
                            } catch (CrucibleException e) {
                                throw new ThePluginException(e.getMessage());
                            }
                        }
                    }));
                    break;
                case JIRA_SERVER:
                    serverPanels.put(ServerType.JIRA_SERVER, new GenericServerConfigForm(new ConnectionTester() {
                        public void testConnection(String username, String password, String server)
								throws ThePluginException {
                            try {
                                JIRAServerFactory.getJIRAServerFacade().testServerConnection(server, username, password);
                            } catch (JIRALoginException e) {
                                throw new ThePluginException(e.getMessage());
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
        if (!getPluginConfiguration().equals(ConfigurationFactory.getConfiguration())) {
            return true;
        }

        for (AbstractServerPanel entry : serverPanels.values()) {
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
                if (serverPanels.get(type).isModified()) {
                    if (getPluginConfiguration().getProductServers(type).getServer(serverPanels.get(type).getData()) != null) {
                        getPluginConfiguration().getProductServers(type).storeServer(serverPanels.get(type).getData());
                    }
                }
                Collection<Server> s = getPluginConfiguration().getProductServers(type).getServers();
                ConfigurationFactory.getConfiguration().getProductServers(type).setServers(s);
            }

            this.treePanel.setData(getPluginConfiguration());
        }
    }

	public void setData() {
        this.treePanel.setData(ConfigPanel.getInstance().getPluginConfiguration());
    }


	public void addServer(ServerType serverType) {
        treePanel.addServer(serverType);
    }

	public void removeServer() {
        treePanel.removeServer();
    }

	public void copyServer() {
        treePanel.copyServer();
    }

	public void storeServer(ServerNode serverNode) {
        ServerBean server = serverNode.getServer();
        ServerBean tempValue = serverPanels.get(serverNode.getServerType()).getData();
        switch (serverNode.getServerType()) {
            case BAMBOO_SERVER:
                server.setSubscribedPlansData(tempValue.getSubscribedPlansData());
                break;
            default:
                break;
        }
        server.setName(tempValue.getName());
        server.setUserName(tempValue.getUserName());
        server.setPasswordString(tempValue.getPasswordString(), tempValue.getShouldPasswordBeStored());

        server.setUrlString(tempValue.getUrlString());
    }

	public void editServer(ServerType serverType, ServerBean server) {
        editPaneCardLayout.show(editPane, serverType.toString());
        serverPanels.get(serverType).setData(server);
    }

	public void showEmptyPanel() {
        editPaneCardLayout.show(editPane, BLANK_CARD);
    }


	public void showBambooGeneralPanel() {
		editPaneCardLayout.show(editPane, BAMBOO_GENERAL_CARD);
	}

	public static ServerConfigPanel getInstance() {
		if (instance == null) {
			instance = new ServerConfigPanel();
		}

		return instance;
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

    public PluginConfiguration getPluginConfiguration() {
        return ConfigPanel.getInstance().getPluginConfiguration();
    }
}
