package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.commons.bamboo.StausIconBambooListener;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;
import com.intellij.ui.HyperlinkLabel;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

/**
 * User: pmaruszak
 */
public class MessageScrollPane extends JPanel implements MessageStatusDisplay {

	private JScrollPane scroll;
	private JPanel statusPanel;
	private JLabel statusLabel;
	private HyperlinkLabel getMoreIssues;
	private static final Color FAIL_COLOR = new Color(255, 201, 201);

	protected static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);

	private JEditorPane pane = new JEditorPane();
	//List<MessageStatusDisplay> listeners = new ArrayList<MessageStatusDisplay>();

	public MessageScrollPane(String initialText) {
		scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		statusPanel = new JPanel();
		statusPanel.setLayout(new FlowLayout());
		statusLabel = new JLabel("No issues loaded");
		statusPanel.add(statusLabel);
		getMoreIssues = new HyperlinkLabel("Get more issues...");
		statusPanel.add(getMoreIssues);

		scroll.setWheelScrollingEnabled(true);

		pane.setEditorKit(new ClasspathHTMLEditorKit());
		pane.setEditable(false);
        pane.setContentType("text/html");
        pane.addHyperlinkListener(new GenericHyperlinkListener());
		pane.setMinimumSize(ED_PANE_MINE_SIZE);

		add(pane);
		scroll.setViewportView(pane);

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(scroll, gbc);

		gbc.gridx = 1;
		gbc.weightx = 0.0;
		add(statusPanel, gbc);

		setMessage(initialText);
	}

	private String wrapBody(String s) {
		return "<html>" + StausIconBambooListener.BODY_WITH_STYLE + s + "</body></html>";

	}

	public void setMessage(String message) {
		pane.setText(wrapBody(message));
		pane.setBackground(Color.WHITE);
	}

	public void setMessage(String msg, boolean isError) {
		pane.setBackground(isError ? FAIL_COLOR : Color.WHITE);
		pane.setText(wrapBody("<table width=\"100%\"><tr><td colspan=\"2\">" + msg + "</td></tr></table>"));
	}

	public void setStatus(String message) {
		statusLabel.setText(message);
	}

	public void addMoreListener(HyperlinkListener listener) {
		getMoreIssues.addHyperlinkListener(listener);
	}

	public void removeMoreListener(HyperlinkListener listener) {
		getMoreIssues.removeHyperlinkListener(listener);
	}
}
