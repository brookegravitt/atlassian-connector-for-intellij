package com.atlassian.theplugin.idea.jira;

import com.intellij.ui.HyperlinkLabel;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

/**
 * User: pmaruszak
 */
public class MessageScrollPane extends JPanel implements MessageStatusDisplay {

	private JPanel statusPanel;
	private HyperlinkLabel getMoreIssues;
	private static final Color FAIL_COLOR = new Color(255, 100, 100);
	private final Color defaultColor = this.getBackground();

	protected static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);
	private JLabel pane = new JLabel();
	private static final int PAD_Y = 8;

	public MessageScrollPane(String initialText) {
		statusPanel = new JPanel();
		statusPanel.setLayout(new FlowLayout());
		getMoreIssues = new HyperlinkLabel("Get More Issues...");
		enableGetMoreIssues(false);
		statusPanel.add(getMoreIssues);

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

		setMessage(initialText);
	}

	public void setMessage(String message) {
		pane.setText(" " + message);
		pane.setBackground(defaultColor);
		setBackground(defaultColor);
	}

	public void setMessage(String msg, boolean isError) {
		pane.setBackground(isError ? FAIL_COLOR : defaultColor);
		setBackground(isError ? FAIL_COLOR : defaultColor);
		pane.setText(" " + msg);
	}

	public void addMoreIssuesListener(HyperlinkListener listener) {
		getMoreIssues.addHyperlinkListener(listener);
	}

	public void removeMoreListener(HyperlinkListener listener) {
		getMoreIssues.removeHyperlinkListener(listener);
	}

	public void enableGetMoreIssues(boolean enable) {
		if (enable) {
			getMoreIssues.setVisible(true);
		} else {
			getMoreIssues.setVisible(false);
		}
	}
}
