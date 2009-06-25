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

import com.atlassian.theplugin.ConnectionWrapper;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.UrlUtil;
import com.atlassian.theplugin.idea.TestConnectionListener;
import com.atlassian.theplugin.idea.TestConnectionProcessor;
import com.atlassian.theplugin.util.Connector;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.DocumentAdapter;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Plugin configuration form.
 */
public class GenericServerConfigForm implements TestConnectionProcessor {
	private JPanel rootComponent;
	private JTextField serverName;
	private JTextField serverUrl;
	private JTextField username;
	private JPasswordField password;
	private JButton testConnection;
	private JCheckBox chkPasswordRemember;
	private JCheckBox cbEnabled;
	private JCheckBox useDefault;
	private DocumentListener listener;

	private transient ServerCfg serverCfg;
	private Project project;
	private DocumentAdapter urlDocumentListener;

	synchronized ServerCfg getServerCfg() {
		return serverCfg;
	}

	public GenericServerConfigForm(final Project project, final UserCfg defaultUser, final Connector tester) {
		this.project = project;
		$$$setupUI$$$();
		testConnection
				.addActionListener(new TestConnectionListener(project, tester, new TestConnectionListener.ServerDataProvider() {
					public ServerData getServer() {
						synchronized (GenericServerConfigForm.this) {
							saveData();
							final String username = serverCfg.isUseDefaultCredentials() ? defaultUser.getUserName()
									: serverCfg.getUserName();
							final String password = serverCfg.isUseDefaultCredentials() ? defaultUser.getPassword()
									: serverCfg.getPassword();
							return new ServerData(serverCfg.getName(), serverCfg.getServerId(), username, password,
									serverCfg.getUrl());
						}
					}
				}, this));
		serverUrl.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				adjustUrl();
			}
		});

		listener = new DocumentListener() {

			public void insertUpdate(DocumentEvent e) {
				setServerState();
			}

			public void removeUpdate(DocumentEvent e) {
				setServerState();
			}

			public void changedUpdate(DocumentEvent e) {
				setServerState();
			}
		};

		useDefault.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				enableDisableUserPassword();
			}
		});

		cbEnabled.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent changeEvent) {

			}
		});

		urlDocumentListener = new DocumentAdapter() {

			protected void textChanged(DocumentEvent documentEvent) {
				enableDisableSever(false);
			}
		};

		enableDisableSever(true);
		enableDisableUserPassword();
	}

	private synchronized void enableDisableSever(boolean quiet) {
		if (serverUrl.getText().length() > 0) {
			cbEnabled.setEnabled(true);
		} else {
			if (cbEnabled.isEnabled() && cbEnabled.isSelected() && !quiet) {
				emptyURLMessage();
				serverCfg.setEnabled(false);
			}
			cbEnabled.setSelected(false);
			cbEnabled.setEnabled(false);

		}
	}

	private void enableDisableUserPassword() {
		if (useDefault.isSelected()) {
			username.setEnabled(false);
			password.setEnabled(false);
			chkPasswordRemember.setEnabled(false);
		} else {
			username.setEnabled(true);
			password.setEnabled(true);
			chkPasswordRemember.setEnabled(true);
		}
	}

	public void finalizeData() {
		adjustUrl();
	}

	private void setServerState() {
		// user name and password can be empty (for anonymous connections), do not check for them
		boolean enabled = serverName.getText().length() > 0 && serverUrl.getText().length() > 0;
		cbEnabled.setSelected(enabled);
	}

	private void adjustUrl() {
		serverUrl.getDocument().removeDocumentListener(urlDocumentListener);
		String url = serverUrl.getText();
		url = adjustUrl(url);
		serverUrl.setText(url);
		serverUrl.getDocument().addDocumentListener(urlDocumentListener);
	}

	public static String adjustUrl(String url) {
		url = UrlUtil.addHttpPrefix(url);
		url = UrlUtil.removeUrlTrailingSlashes(url);
		return url;
	}

	public synchronized void setData(ServerCfg server) {
		serverUrl.getDocument().removeDocumentListener(urlDocumentListener);

		username.getDocument().removeDocumentListener(listener);
		password.getDocument().removeDocumentListener(listener);

		serverCfg = server;


		serverName.setText(server.getName());
		serverUrl.setText(server.getUrl());
		username.setText(server.getUserName());
		chkPasswordRemember.setSelected(server.isPasswordStored());
		password.setText(server.getPassword());
		cbEnabled.setSelected(server.isEnabled());
		useDefault.setSelected(server.isUseDefaultCredentials());

		username.getDocument().addDocumentListener(listener);
		password.getDocument().addDocumentListener(listener);
		enableDisableUserPassword();
		enableDisableSever(true);
		serverUrl.getDocument().addDocumentListener(urlDocumentListener);
	}

	public synchronized void saveData() {
		if (serverCfg == null) {
			return;
		}

		serverCfg.setName(serverName.getText());
		serverCfg.setUrl(serverUrl.getText());
		serverCfg.setUsername(username.getText());
		serverCfg.setPassword(String.valueOf(password.getPassword()));
		serverCfg.setPasswordStored(chkPasswordRemember.isSelected());
		serverCfg.setEnabled(cbEnabled.isSelected());
		serverCfg.setUseDefaultCredentials(useDefault.isSelected());
		if (serverCfg.getUrl().length() > 0) {
			cbEnabled.setEnabled(true);
		} else {
			cbEnabled.setEnabled(false);
		}
	}

	public JComponent getRootComponent() {
		return rootComponent;
	}


	public String getServerUrl() {
		return serverUrl.getText();
	}

	public String getUserName() {
		return username.getText();
	}

	public String getPassword() {
		return String.valueOf(password.getPassword());
	}

	public void onSuccess() {
	}

	public void onError(final String errorMessage) {
	}

	public void setConnectionResult(ConnectionWrapper.ConnectionState result) {
		if (result == ConnectionWrapper.ConnectionState.SUCCEEDED) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setServerState();
				}
			});
		}
	}


	public void emptyURLMessage() {

		ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
			public void run() {

				final ModalityState modalityState = ModalityState.stateForComponent(rootComponent);
				ApplicationManager.getApplication().invokeAndWait(new Runnable() {
					public void run() {
						Messages.showInfoMessage(project,
								"<html>Server <b>" + serverCfg.getName() + "</b> will be disabled</b>",
								"Empty server URL");
					}

				}, modalityState);
			}
		});
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		rootComponent = new JPanel();
		rootComponent.setLayout(new GridBagLayout());
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(7, 3, new Insets(0, 0, 0, 0), -1, -1));
		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		rootComponent.add(panel1, gbc);
		serverName = new JTextField();
		serverName.setText("");
		panel1.add(serverName, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, new Dimension(150, -1), null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setHorizontalAlignment(4);
		label1.setHorizontalTextPosition(4);
		label1.setText("Server Name:");
		label1.setDisplayedMnemonic('S');
		label1.setDisplayedMnemonicIndex(0);
		panel1.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1),
				new Dimension(92, 16), null, 0, false));
		serverUrl = new JTextField();
		panel1.add(serverUrl, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, new Dimension(150, -1), null, 0, false));
		username = new JTextField();
		panel1.add(username, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, new Dimension(150, -1), null, 0, false));
		password = new JPasswordField();
		panel1.add(password, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, new Dimension(150, -1), null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setHorizontalAlignment(4);
		label2.setText("Server URL:");
		label2.setDisplayedMnemonic('U');
		label2.setDisplayedMnemonicIndex(7);
		panel1.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1),
				new Dimension(92, 16), null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setHorizontalAlignment(4);
		label3.setText("Username:");
		label3.setDisplayedMnemonic('N');
		label3.setDisplayedMnemonicIndex(4);
		panel1.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(92, 16), null, 0,
				false));
		final JLabel label4 = new JLabel();
		label4.setHorizontalAlignment(4);
		label4.setText("Password:");
		label4.setDisplayedMnemonic('P');
		label4.setDisplayedMnemonicIndex(0);
		panel1.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(92, 16), null, 0,
				false));
		testConnection = new JButton();
		testConnection.setText("Test Connection");
		testConnection.setMnemonic('T');
		testConnection.setDisplayedMnemonicIndex(0);
		panel1.add(testConnection, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, null, null, 0, false));
		chkPasswordRemember = new JCheckBox();
		chkPasswordRemember.setSelected(true);
		chkPasswordRemember.setText("Remember Password");
		chkPasswordRemember.setMnemonic('R');
		chkPasswordRemember.setDisplayedMnemonicIndex(0);
		panel1.add(chkPasswordRemember, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, null, null, 0, false));
		cbEnabled = new JCheckBox();
		cbEnabled.setEnabled(false);
		cbEnabled.setHorizontalTextPosition(11);
		cbEnabled.setText("Server Enabled");
		cbEnabled.setMnemonic('E');
		cbEnabled.setDisplayedMnemonicIndex(7);
		panel1.add(cbEnabled, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, new Dimension(115, 25), null, 0, false));
		useDefault = new JCheckBox();
		useDefault.setText("Use Default Credentials");
		panel1.add(useDefault, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, null, null, 0, false));
		final JPanel spacer1 = new JPanel();
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.VERTICAL;
		rootComponent.add(spacer1, gbc);
		label1.setLabelFor(serverName);
		label2.setLabelFor(serverUrl);
		label3.setLabelFor(username);
		label4.setLabelFor(password);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootComponent;
	}
}