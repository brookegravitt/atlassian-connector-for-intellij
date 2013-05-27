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
package com.atlassian.theplugin.idea.ui;

import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.idea.jira.IssueDetailsToolWindow;
import com.intellij.ide.BrowserUtil;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class UserLabel extends JPanel {
	private JLabel label;
	private MouseListener mouseListener;

	public UserLabel() {
		this(false);
	}

	public UserLabel(boolean fill) {
		setOpaque(false);
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;

		label = new JLabel();
		setBackground(com.intellij.util.ui.UIUtil.getTextFieldBackground());
		label.setBorder(BorderFactory.createEmptyBorder());
        label.setOpaque(false);
		label.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		add(label, gbc);
		if (fill) {
			IssueDetailsToolWindow.addFillerPanel(this, gbc, true);
		}
	}

	public void setUserName(final String serverUrl, final String userName, final String userNameId, boolean useLink) {
		String userNameFixed = userName.replace(" ", "&nbsp;");
		if (useLink) {
            Color color = IdeaVersionFacade.getInstance().getJbColor(JBColor.BLUE, JBColor.CYAN);
            String rgb = String.format("%02X", color.getRed()) + String.format("%02X", color.getGreen()) + String.format("%02X", color.getBlue());
            label.setText("<html><body><font color=\"#" + rgb + "\"><u>" + userNameFixed
                    + "</u></font></body></html>");
			if (mouseListener != null) {
				label.removeMouseListener(mouseListener);
				mouseListener = null;
			}
			addListener(serverUrl, userNameId);
		} else {
			label.setText("<html><body>" + userNameFixed + "</body></html>");
		}
	}

	public void setText(final String text) {
		if (mouseListener != null) {
			label.removeMouseListener(mouseListener);
			mouseListener = null;
		}
		label.setText(text);
	}

	private void addListener(final String serverUrl, final String userNameId) {
		mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				BrowserUtil.launchBrowser(serverUrl + "/secure/ViewProfile.jspa?name=" + userNameId);
			}

			public void mouseEntered(MouseEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			public void mouseExited(MouseEvent e) {
				setCursor(Cursor.getDefaultCursor());
			}
		};
		label.addMouseListener(mouseListener);
	}
}
