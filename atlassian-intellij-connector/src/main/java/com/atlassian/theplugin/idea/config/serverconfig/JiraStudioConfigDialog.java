package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.theplugin.ConnectionWrapper;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.TestConnectionProcessor;
import com.atlassian.theplugin.idea.TestConnectionTask;
import com.atlassian.theplugin.idea.config.serverconfig.util.ServerNameUtil;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.idea.util.IdeaUiMultiTaskExecutor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: kalamon
 * Date: Jul 8, 2009
 * Time: 3:03:34 PM
 */
public class JiraStudioConfigDialog extends DialogWrapper {
    private final ConfigPanel rootPanel;
    private final JTextField serverName;
    private final JTextField serverUrl;
    private final JTextField userName;
    private final JPasswordField password;
    private final JCheckBox rememberPassword;
    private final JButton testConnection;
    private final JCheckBox useDefaultCredentials;
    private final DocumentListener documentListener;
    private final Project project;
    private final ServerTreePanel serverTree;
    private final UserCfg defaultUser;
    private static final String JIRA_STUDIO_SUFFIX = " (JIRA Studio)";

    protected JiraStudioConfigDialog(Project project, ServerTreePanel serverTree,
                                     UserCfg defaultUser, Collection<ServerCfg> servers) {
        super(project, false);
        this.project = project;

        this.serverTree = serverTree;
        this.defaultUser = defaultUser;

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

        serverName = new JTextField(ServerNameUtil.suggestNewName(servers));
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
        useDefaultCredentials.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                boolean enabled = !useDefaultCredentials.isSelected();
                userName.setEnabled(enabled);
                password.setEnabled(enabled);
                rememberPassword.setEnabled(enabled);
            }
        });

        rootPanel = new ConfigPanel();

        updateButtons();

        init();
    }

    private final Map<String, Throwable> connectionErrors = new HashMap<String, Throwable>();

    private void testServerConnections() {

        TestConnectionProcessor processor = new TestConnectionProcessor() {
            // kalamon: ok, I know, this counter is lame as hell. But so what,
            // it is "the simplest thing that could ever work". And I challenge
            // you to invent something more clever that does not result in
            // overcomplication of the code
            private int counter = 1;

            public void setConnectionResult(ConnectionWrapper.ConnectionState result) {
            }

            public void onSuccess() {
                if (counter-- > 0) {
                    testCrucibleConnection(this);
                } else {
                    showResultDialog();
                }
            }

            public void onError(String errorMessage, Throwable exception, String helpUrl) {
                connectionErrors.put(errorMessage, exception);
                if (counter-- > 0) {
                    testCrucibleConnection(this);
                } else {
                    showResultDialog();
                }
            }
        };

        connectionErrors.clear();
        testJiraConnection(processor);
    }

    private void showResultDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (connectionErrors.size() > 0) {
                    List<IdeaUiMultiTaskExecutor.ErrorObject> errors = new ArrayList<IdeaUiMultiTaskExecutor.ErrorObject>();
                    for (String error : connectionErrors.keySet()) {
                        errors.add(new IdeaUiMultiTaskExecutor.ErrorObject(error, connectionErrors.get(error)));
                    }
                    DialogWithDetails.showExceptionDialog(rootPanel, errors);
                } else {
                    showMessageDialog(project, "Connected successfully", "Connection OK", Messages.getInformationIcon());
                }
            }
        });
    }

    private void testJiraConnection(final TestConnectionProcessor processor) {
        final Task.Modal testConnectionTask = new TestConnectionTask(project,
                new ProductConnector(IntelliJCrucibleServerFacade.getInstance()),
                new ServerData(generateJiraServerCfg(), defaultUser),
                processor, "Testing JIRA Connection", true, false, false);
        testConnectionTask.setCancelText("Stop");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ProgressManager.getInstance().run(testConnectionTask);
            }
        });
    }

    private void testCrucibleConnection(final TestConnectionProcessor processor) {
        final Task.Modal testConnectionTask = new TestConnectionTask(project,
                new ProductConnector(IntelliJCrucibleServerFacade.getInstance()),
                new ServerData(generateCrucibleServerCfg(), defaultUser),
                processor, "Testing Crucible Connection", true, false, false);
        testConnectionTask.setCancelText("Stop");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ProgressManager.getInstance().run(testConnectionTask);
            }
        });
    }

    @Override
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

    private @NotNull JiraServerCfg generateJiraServerCfg() {
        ServerIdImpl idJira = new ServerIdImpl();

        String name = serverName.getText().trim() + JIRA_STUDIO_SUFFIX;
        JiraServerCfg jira = new JiraServerCfg(true, name, idJira, true);

        jira.setUrl(serverUrl.getText());

        String user = userName.getText();
        if (user.length() > 0) {
            jira.setUsername(user);
        }
        jira.setPassword(new String(password.getPassword()));
        jira.setPasswordStored(rememberPassword.isSelected());
        jira.setUseDefaultCredentials(useDefaultCredentials.isSelected());

        return jira;
    }

    private @NotNull CrucibleServerCfg generateCrucibleServerCfg() {
        ServerIdImpl idCrucible = new ServerIdImpl();

        String name = serverName.getText().trim() + JIRA_STUDIO_SUFFIX;
        CrucibleServerCfg cru = new CrucibleServerCfg(true, name, idCrucible);

        cru.setUrl(serverUrl.getText() + "/source");

        String user = userName.getText();
        if (user.length() > 0) {
            cru.setUsername(user);
        }
        cru.setPassword(new String(password.getPassword()));
        cru.setPasswordStored(rememberPassword.isSelected());
        cru.setUseDefaultCredentials(useDefaultCredentials.isSelected());
        cru.setFisheyeInstance(true);

        return cru;
    }

    private void generateAllStudioServers() {
        serverTree.addNewServerCfg(ServerType.JIRA_SERVER, generateJiraServerCfg());
        serverTree.addNewServerCfg(ServerType.CRUCIBLE_SERVER, generateCrucibleServerCfg());
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
                serverName.getText().trim().length() > 0
                && serverUrl.getText().trim().length() > 0;
        setOKActionEnabled(enabled);
        testConnection.setEnabled(enabled);
    }
}
