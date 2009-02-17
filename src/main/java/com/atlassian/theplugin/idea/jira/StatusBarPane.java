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
package com.atlassian.theplugin.idea.jira;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jacek Jaroczynski
 */
public class StatusBarPane extends JPanel implements StatusBar {
	protected static final Color FAIL_COLOR = new Color(255, 100, 100);
	private static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);
	private static final int PAD_Y = 8;
	protected final Color defaultColor = this.getBackground();

	protected JLabel pane = new JLabel();
	protected JPanel statusPanel;

	public StatusBarPane(String initialText) {

		statusPanel = new JPanel();
		statusPanel.setLayout(new FlowLayout());

		pane.setMinimumSize(ED_PANE_MINE_SIZE);
		pane.setOpaque(false);

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.ipady = PAD_Y;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(pane, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		add(statusPanel, gbc);

		setMessage(initialText, false);
	}


	public void addComponent(JComponent component) {
		statusPanel.add(component);
	}


	public void setMessage(String message, boolean rightAlign) {
		pane.setHorizontalAlignment(rightAlign ? SwingConstants.RIGHT : SwingConstants.LEFT);
		pane.setText(" " + message);
		pane.setBackground(defaultColor);
		setBackground(defaultColor);
		statusPanel.setBackground(defaultColor);
	}

	public void setErrorMessage(String msg) {
		statusPanel.setBackground(FAIL_COLOR);
		pane.setHorizontalAlignment(SwingConstants.LEFT);
		pane.setBackground(FAIL_COLOR);
		setBackground(FAIL_COLOR);
		pane.setText(" " + msg);
	}
}
