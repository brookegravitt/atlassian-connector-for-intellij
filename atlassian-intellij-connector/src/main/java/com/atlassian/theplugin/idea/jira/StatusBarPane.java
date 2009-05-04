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

import com.atlassian.theplugin.idea.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jacek Jaroczynski
 */
public class StatusBarPane extends JPanel implements StatusBar {
	private static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);
	private static final int PAD_Y = 8;
	protected final Color defaultColor = this.getBackground();

	protected JLabel textPanel = new JLabel();
	protected JPanel additionalPanel;

	public StatusBarPane(String initialText) {

		additionalPanel = new JPanel();
		additionalPanel.setLayout(new FlowLayout());

		textPanel.setMinimumSize(ED_PANE_MINE_SIZE);
		textPanel.setOpaque(false);

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.ipady = PAD_Y;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(textPanel, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		add(additionalPanel, gbc);

		setMessage(initialText, false);
	}


	public void addComponent(JComponent component) {
		additionalPanel.add(component);
	}


	public void setMessage(String message, boolean rightAlign) {
		textPanel.setHorizontalAlignment(rightAlign ? SwingConstants.RIGHT : SwingConstants.LEFT);
		textPanel.setText(" " + message);
		textPanel.setBackground(defaultColor);
		setBackground(defaultColor);
		additionalPanel.setBackground(defaultColor);
	}

	public void setErrorMessage(String msg) {
		additionalPanel.setBackground(Constants.FAIL_COLOR);
		textPanel.setHorizontalAlignment(SwingConstants.LEFT);
		textPanel.setBackground(Constants.FAIL_COLOR);
		setBackground(Constants.FAIL_COLOR);
		textPanel.setText(" " + msg);
	}
}
