package com.atlassian.theplugin.idea.config.serverconfig;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.project.Project;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.ServerType;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * User: kalamon
 * Date: Jul 8, 2009
 * Time: 3:03:34 PM
 */
public class JiraStudioConfigDialog extends DialogWrapper {
    private ConfigPanel rootPanel;
    private JTextField serverName;
    private JTextField serverUrl;
    private JTextField userName;
    private JPasswordField password;
    private JCheckBox rememberPassword;
    private JButton testConnection;
    private JCheckBox useDefaultCredentials;
    private DocumentListener documentListener;
    private ServerTreePanel serverTree;

    protected JiraStudioConfigDialog(Project project, ServerTreePanel serverTree) {
        super(project, false);

        this.serverTree = serverTree;

        setTitle("Create JIRA Studio Server");

        documentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent documentEvent) {
                updateButtons();
            }

            public void removeUpdate(DocumentEvent documentEvent) {
                updateButtons();
            }

            public void changedUpdate(DocumentEvent documentEvent) {
                updateButtons();
            }
        };

        serverName = new JTextField();
        serverName.getDocument().addDocumentListener(documentListener);
        serverUrl = new JTextField();
        serverUrl.getDocument().addDocumentListener(documentListener);
        serverUrl.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                serverUrl.getDocument().removeDocumentListener(documentListener);
                String url = GenericServerConfigForm.adjustUrl(serverUrl.getText());
                serverUrl.setText(url);
                serverUrl.getDocument().addDocumentListener(documentListener);
            }
        });
        userName = new JTextField();
        password = new JPasswordField();
        rememberPassword = new JCheckBox("Remember Password");
        testConnection = new JButton("Test Connection");
        testConnection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                testServerConnections();
            }
        });
        useDefaultCredentials = new JCheckBox("Use Default Credentials");

        rootPanel = new ConfigPanel();

        updateButtons();

        init();
    }

    private void testServerConnections() {
        //To change body of created methods use File | Settings | File Templates.
    }

    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    @Override
    protected Action[] createActions() {
        return new Action[] {
                getOKAction(),
                getCancelAction()
        };
    }

    @Override
    protected void doOKAction() {
        serverUrl.getDocument().removeDocumentListener(documentListener);
        GenericServerConfigForm.adjustUrl(serverUrl.getText());
        generateAllStudioServers();
        super.doOKAction();
    }

    private void generateAllStudioServers() {
        ServerIdImpl idJira = new ServerIdImpl();
        ServerIdImpl idCrucible = new ServerIdImpl();

        String name = serverName.getText().trim();
        if (name.length() < 1) {
            return;
        }
        JiraServerCfg jira = new JiraServerCfg(true, name, idJira);
        CrucibleServerCfg cru = new CrucibleServerCfg(true, name, idCrucible);

        jira.setUrl(serverUrl.getText());
        cru.setUrl(serverUrl.getText() + "/source");

        String user = userName.getText();
        if (user.length() > 0) {
            jira.setUsername(user);
            cru.setUsername(user);
        }
        jira.setPassword(new String(password.getPassword()));
        cru.setPassword(new String(password.getPassword()));
        jira.setPasswordStored(rememberPassword.isSelected());
        cru.setPasswordStored(rememberPassword.isSelected());
        jira.setUseDefaultCredentials(useDefaultCredentials.isSelected());
        cru.setUseDefaultCredentials(useDefaultCredentials.isSelected());
        cru.setFisheyeInstance(true);

        serverTree.addNewServerCfg(ServerType.JIRA_SERVER, jira);
        serverTree.addNewServerCfg(ServerType.CRUCIBLE_SERVER, cru);
    }

    private class ConfigPanel extends JPanel {

        private ConfigPanel() {
            setLayout(new FormLayout("3dlu, right:pref, 3dlu, fill:pref:grow, right:pref, 3dlu",
                    "3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu"));
            CellConstraints cc = new CellConstraints();

            //CHECKSTYLE:MAGIC:OFF
            add(new JLabel("Server Name:"), cc.xy(2, 2));
            add(serverName, cc.xyw(4, 2, 2));
            add(new JLabel("Server URL:"), cc.xy(2, 4));
            add(serverUrl, cc.xyw(4, 4, 2));
            add(new JLabel("Username:"), cc.xy(2, 6));
            add(userName, cc.xyw(4, 6, 2));
            add(new JLabel("Password:"), cc.xy(2, 8));
            add(password, cc.xyw(4, 8, 2));
            add(rememberPassword, cc.xy(4, 10));
            add(testConnection, cc.xy(5, 10));
            add(useDefaultCredentials, cc.xy(4, 12));
            //CHECKSTYLE:MAGIC:ON
        }
    }

    private void updateButtons() {
        boolean enabled =
                serverName.getText().length() > 0
                && serverUrl.getText().length() > 0;
        setOKActionEnabled(enabled);
        testConnection.setEnabled(enabled);
    }
}
