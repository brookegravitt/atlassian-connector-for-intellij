package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.bamboo.BambooServerFacade;
import com.atlassian.theplugin.configuration.*;
import com.atlassian.theplugin.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.crucible.api.CrucibleException;
import com.atlassian.theplugin.exception.ThePluginException;
import com.atlassian.theplugin.idea.config.ContentPanel;
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

public final class ServerConfigPanel extends JPanel implements ContentPanel {
    private final ServerTreePanel serverTreePanel;
    private BlankPanel blankPanel = null;

    private CardLayout editPaneCardLayout;
    private JPanel editPane;
	private static final String BLANK_CARD = "Blank card";

	private static final float SPLIT_RATIO = 0.3f;
	private Map<ServerType, ServerPanel> serverPanels = new HashMap<ServerType, ServerPanel>();

	private transient PluginConfiguration localConfigCopy;
	private final CrucibleServerFacade crucibleServerFacade;
	private final BambooServerFacade bambooServerFacade;

	public ServerConfigPanel(ServerTreePanel serverTreePanel,
							 CrucibleServerFacade crucibleServerFacade,
							 BambooServerFacade bambooServerFacade) {
		this.serverTreePanel = serverTreePanel;
		this.crucibleServerFacade = crucibleServerFacade;
		this.bambooServerFacade = bambooServerFacade;
		/* required due to circular dependency unhandled by pico */
		this.serverTreePanel.setServerConfigPanel(this);
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
        selectPane.add(serverTreePanel);
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
                    serverPanels.put(ServerType.CRUCIBLE_SERVER, new GenericServerConfigForm(new ConnectionTester() {
                        public void testConnection(String username, String password, String server)
								throws ThePluginException {
                            try {
                                crucibleServerFacade.testServerConnection(server, username, password);
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
                    if (conf.getServer(serverPanels.get(type).getData()) != null) {
                        conf.storeServer(serverPanels.get(type).getData());
                    }
                }
                Collection<Server> s = conf.getServers();
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
                server.setSubscribedPlans(tempValue.getSubscribedPlans());
                break;
            default:
                break;
        }
        server.setName(tempValue.getName());
        server.setUserName(tempValue.getUserName());
        server.setPasswordString(tempValue.getPasswordString(), tempValue.getShouldPasswordBeStored());
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
