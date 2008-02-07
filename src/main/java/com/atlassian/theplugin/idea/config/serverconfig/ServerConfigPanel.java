package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.configuration.BambooConfigurationBean;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.idea.config.AbstractContentPanel;
import com.atlassian.theplugin.idea.config.ConfigPanel;
import com.atlassian.theplugin.idea.config.FooterPanel;
import com.atlassian.theplugin.idea.config.HeaderPanel;
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

public class ServerConfigPanel extends AbstractContentPanel {
	private ServerTreePanel treePanel = null;
	private BlankPanel blankPanel = null;

	private HeaderPanel headerPanel = null;
	private FooterPanel footerPanel = null;

	private BambooServerConfigForm bambooEditForm = null;
	private CardLayout editPaneCardLayout;
	private JPanel editPane;
	private static final String BAMBOO_SERVER_EDIT_CARD = "Bamboo server card";
	private static final String BLANK_CARD = "Blank card";

	public ServerConfigPanel() {
		initLayout();
	}

	private void initLayout() {
		setLayout(new BorderLayout());

		Splitter splitter = new Splitter(false, 0.3f);
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
		editPane.add(getBambooServerPanel(), BAMBOO_SERVER_EDIT_CARD);
		editPane.add(getBlankPanel(), BLANK_CARD);

		return editPane;
	}

	private JComponent getBlankPanel() {
		if (blankPanel == null) {
			blankPanel = new BlankPanel();
		}
		return blankPanel;
	}

	private JComponent getBambooServerPanel() {
		if (bambooEditForm == null) {
			bambooEditForm = new BambooServerConfigForm();
		}
		return bambooEditForm.getRootComponent();
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isModified() {
		if (!getPluginConfiguration().equals(ConfigurationFactory.getConfiguration())) {
			return true;
		}
		if (bambooEditForm != null) {
			return bambooEditForm.isModified();
		} else {
			return false;
		}
	}

	public String getTitle() {
		return "Servers";
	}

	public void getData() {
		if (isModified()) {
			if (((BambooConfigurationBean) getPluginConfiguration().getBambooConfiguration()).getServer(bambooEditForm.getData()) != null) {
				getPluginConfiguration().getBambooConfiguration().storeServer(bambooEditForm.getData());
			}
			ConfigurationFactory.getConfiguration().getBambooConfiguration().setServers(getPluginConfiguration().getBambooConfiguration().getServers());

			this.treePanel.setData(getPluginConfiguration());
		}
	}

	public void setData(PluginConfiguration pluginConfiguration) {
		this.treePanel.setData(pluginConfiguration);
	}

	public void addBambooServer() {
		treePanel.addBambooServer();
	}

	public void addCrucibleServer() {
		treePanel.addCrucibleServer();
	}

	public void removeServer() {
		treePanel.removeServer();
	}


	public void copyServer() {
		treePanel.copyServer();
	}

	public void storeBambooServer(ServerBean server) {

		ServerBean tempValue = bambooEditForm.getData();

		server.setName(tempValue.getName());
		server.setUsername(tempValue.getUsername());
		server.setPasswordString(tempValue.getPasswordString(), tempValue.getShouldPasswordBeStored());
		server.setUrlString(tempValue.getUrlString());
		server.setSubscribedPlansData(tempValue.getSubscribedPlansData());
	}

	public void editBambooServer(ServerBean server) {
		editPaneCardLayout.show(editPane, BAMBOO_SERVER_EDIT_CARD);
		bambooEditForm.setData(server);
	}

	public void showEmptyPanel() {
		editPaneCardLayout.show(editPane, BLANK_CARD);
	}

	static class BlankPanel extends JPanel {

		public BlankPanel() {
			initLayout();
		}

		private void initLayout() {

			setLayout(new BorderLayout());

			DefaultStyledDocument doc = new DefaultStyledDocument();
			Style s = doc.addStyle(null, null);
			StyleConstants.setIcon(s, IconLoader.getIcon("/general/add.png"));
			Style d = doc.addStyle(null, null);
			StyleConstants.setFontFamily(d, getFont().getFamily());
			StyleConstants.setFontSize(d, getFont().getSize());
			try {
				doc.insertString(0, "Press the ", d);
				doc.insertString(10, " ", s);
				doc.insertString(11, " button to define a new Server configuration.", d);
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
