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
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.fisheye.FishEyeServerFacadeImpl;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
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
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.Collection;

public class ServerConfigPanel extends JPanel implements DataProvider {
    private final ServerTreePanel serverTreePanel;
    private BlankPanel blankPanel;

	private CardLayout editPaneCardLayout;
    private JPanel editPane;
	private static final String BLANK_CARD = "Blank card";

	private static final float SPLIT_RATIO = 0.3f;

	private Collection<ServerCfg> serverCfgs;
    private final BambooServerConfigForm bambooServerConfigForm;
    private final GenericServerConfigForm jiraServerConfigForm;
    private final CrucibleServerConfigForm crucibleServerConfigForm;
	private final GenericServerConfigForm fisheyeServerConfigFrom;

	public ServerConfigPanel(Project project, Collection<ServerCfg> serverCfgs) {		
		this.serverCfgs = serverCfgs;
        this.serverTreePanel = new ServerTreePanel();
		final CrucibleServerFacade crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
		final BambooServerFacade bambooServerFacade = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger());
		final JIRAServerFacade jiraServerFacade = JIRAServerFacadeImpl.getInstance();
		final FishEyeServerFacadeImpl fishEyeServerFacade = FishEyeServerFacadeImpl.getInstance();
		/* required due to circular dependency unhandled by pico */
		this.serverTreePanel.setServerConfigPanel(this);
		jiraServerConfigForm = new GenericServerConfigForm(project, new ProductConnector(jiraServerFacade));
		crucibleServerConfigForm = new CrucibleServerConfigForm(project, crucibleServerFacade);
		bambooServerConfigForm = new BambooServerConfigForm(project, bambooServerFacade);
		fisheyeServerConfigFrom = new GenericServerConfigForm(project, new ProductConnector(fishEyeServerFacade));
		initLayout();

        serverTreePanel.setData(serverCfgs);
        
    }


    public void setData(Collection<ServerCfg> aServerCfgs) {
        serverCfgs = aServerCfgs;
        serverTreePanel.setData(serverCfgs);
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

		final JPanel selectPane = new JPanel(new BorderLayout());
		final JPanel toolBarPanel = new JPanel(new BorderLayout());
		toolBarPanel.add(createToolbar(), BorderLayout.NORTH);
		selectPane.add(toolBarPanel, BorderLayout.NORTH);
		selectPane.add(serverTreePanel, BorderLayout.CENTER);
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
                PluginUtil.getLogger().error(e);
            }
            JTextPane pane = new JTextPane();
            pane.setBackground(getBackground());
            pane.setDocument(doc);
            pane.setEditable(false);
            pane.setVisible(true);

            add(pane, BorderLayout.NORTH);
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

