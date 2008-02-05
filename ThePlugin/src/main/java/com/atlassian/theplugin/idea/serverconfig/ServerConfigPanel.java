package com.atlassian.theplugin.idea.serverconfig;

import com.atlassian.theplugin.configuration.*;
import com.atlassian.theplugin.idea.GridBagLayoutConstraints;
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
import java.util.Observable;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-26
 * Time: 12:23:08
 * To change this template use File | Settings | File Templates.
 */
public class ServerConfigPanel extends JPanel {
	private static ServerConfigPanel instance;

	private PluginConfiguration pluginConfiguration = null;

	private ServerTreePanel treePanel = null;
	private BlankPanel blankPanel = null;
	private FooterPanel footerPanel = null;

	private BambooServerConfigForm bambooEditForm = null;

	private ServerConfigPanel() {
		initLayout();
	}

	public static ServerConfigPanel getInstance() {
		if (instance == null) {
			instance = new ServerConfigPanel();
		}
		return instance;
	}

	private void initLayout() {
		setLayout(new BorderLayout());
		Splitter splitter = new Splitter(false, 0.3f);
		splitter.setShowDividerControls(true);
		splitter.setFirstComponent(createSelectPane());
		splitter.setSecondComponent(createEditPane());
		splitter.setHonorComponentsMinimumSize(true);

		add(splitter, BorderLayout.CENTER);
		add(getFooterPanel(), BorderLayout.SOUTH);
	}

	public void update(Observable o, Object arg) {
		blankPanel.setVisible(true);
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
		JPanel editPane = new JPanel();
		editPane.setLayout(new VerticalFlowLayout());
		editPane.add(getBambooServerPanel());
		editPane.add(getBlankPanel());

		blankPanel.setVisible(true);
		bambooEditForm.setVisible(false);

		return editPane;
	}

	private JPanel getFooterPanel() {
		if (footerPanel == null) {
			footerPanel = new FooterPanel();
		}
		return footerPanel;
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

	public boolean isModified() {
		if (!this.pluginConfiguration.equals(ConfigurationFactory.getConfiguration())) {
			return true;
		}
		if (bambooEditForm != null) {
			return bambooEditForm.isModified();
		}
		return false;
	}

	public void getData() {
		if (isModified()) {
			if (((BambooConfigurationBean) pluginConfiguration.getBambooConfiguration()).getServer(bambooEditForm.getData()) != null) {
				pluginConfiguration.getBambooConfiguration().storeServer(bambooEditForm.getData());
			}
			ConfigurationFactory.getConfiguration().getBambooConfiguration().setServers(pluginConfiguration.getBambooConfiguration().getServers());

			this.treePanel.setData(pluginConfiguration);
		}
	}

	public void setData() {
		clonePluginConfiguration();
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

		ServerBean tempValue = (ServerBean) bambooEditForm.getData();

		server.setName(tempValue.getName());
		server.setUsername(tempValue.getUsername());
		try {
			server.setPasswordString(tempValue.getPasswordString(), tempValue.getShouldPasswordBeStored());
		} catch (ServerPasswordNotProvidedException e) {
			// ignore here
		}
		server.setUrlString(tempValue.getUrlString());
		server.setSubscribedPlansData(tempValue.getSubscribedPlansData());
	}

	public void editBambooServer(ServerBean server) {
		blankPanel.setVisible(false);
		bambooEditForm.setVisible(true);
		bambooEditForm.setData(server);
	}

	public void showEmptyPanel() {
		blankPanel.setVisible(true);
		bambooEditForm.setVisible(false);
	}

	static class BlankPanel extends JPanel {

		public BlankPanel() {
			initLayout();
		}

		private void initLayout() {

			setLayout(new GridBagLayout());

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

			add(pane, new GridBagLayoutConstraints(1, 1).setAnchor(GridBagLayoutConstraints.NORTHWEST).setFill(GridBagLayoutConstraints.BOTH).setWeight(0.01, 0.0));
			add(new JPanel(), new GridBagLayoutConstraints(1, 2).setFill(GridBagLayoutConstraints.BOTH).setWeight(0.0, 1.0));
		}


	}

	public PluginConfiguration getPluginConfiguration() {
		return pluginConfiguration;
	}

	public void setPluginConfiguration(PluginConfiguration pluginConfiguration) {
		this.pluginConfiguration = pluginConfiguration;
	}

	synchronized private void clonePluginConfiguration() {
		try {
			this.pluginConfiguration = (PluginConfiguration) ((PluginConfigurationBean) ConfigurationFactory.getConfiguration()).clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

}
