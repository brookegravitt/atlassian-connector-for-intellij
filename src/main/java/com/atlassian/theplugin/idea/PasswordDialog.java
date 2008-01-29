package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BambooServerFactory;
import com.atlassian.theplugin.bamboo.api.BambooLoginException;
import com.atlassian.theplugin.configuration.Server;
import com.intellij.openapi.ui.Messages;
import static com.intellij.openapi.ui.Messages.showMessageDialog;

import javax.swing.*;
import java.awt.event.*;

public class PasswordDialog extends JDialog {

    private JPanel passwordPanel;
    private JCheckBox chkRememberPassword;
    private JPasswordField password;
    private JButton testConnectionButton;
    private JLabel lblCommand;
	private JTextField username;
	private transient Server server;

    public PasswordDialog(final Server server) {
        this.server = server;
        setContentPane(passwordPanel);
        setModal(true);
// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        lblCommand.setText("Please provide password to connect \"" + this.server.getName() + "\" server");
// call onCancel() on ESCAPE
        passwordPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        testConnectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    BambooServerFactory.getBambooServerFacade().testServerConnection(
                            server.getUrlString(),
                            server.getUsername(),
                            getPasswordString());
                    showMessageDialog("Connected successfully", "Connection OK", Messages.getInformationIcon());
                } catch (BambooLoginException e1) {
                    showMessageDialog(e1.getMessage(), "Connection Error" + server.getUrlString()
                            + "-" + getPasswordString(), Messages.getErrorIcon());
                }
            }
        });
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    public JPanel getPasswordPanel() {
        return passwordPanel;
    }

    public String getPasswordString() {
        return String.valueOf(password.getPassword());
    }

    public Boolean getShouldPasswordBeStored() {
        return chkRememberPassword.isSelected();
    }

	public void setUserName(String username) {
		this.username.setText(username);
	}

	public String getUserName() {
		return this.username.getText();
	}

	private void createUIComponents() {
		// TODO: place custom component creation code here 
	}
}
