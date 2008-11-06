package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.commons.bamboo.StausIconBambooListener;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.*;

/**
 * User: pmaruszak
 */
public class MessageScrollPane extends JScrollPane {
	protected static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);

	JEditorPane pane = new JEditorPane();
	//List<MessageScrollPaneListener> listeners = new ArrayList<MessageScrollPaneListener>();

	public MessageScrollPane(String initialText) {
		JScrollPane scrollPane = new JScrollPane(pane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setWheelScrollingEnabled(true);
		pane.setEditable(false);
        pane.setContentType("text/html");
        pane.addHyperlinkListener(new GenericHyperlinkListener());
		pane.setMinimumSize(ED_PANE_MINE_SIZE);
		pane.setEditorKit(new ClasspathHTMLEditorKit());
		pane.setText(wrapBody(initialText));
	}

	private String wrapBody(String s) {
		return "<html>" + StausIconBambooListener.BODY_WITH_STYLE + s + "</body></html>";

	}

	public void setMessage(String message) {
		pane.setText(wrapBody(message));
	}

	public void setStatusMessage(String msg, boolean isError) {
		pane.setBackground(isError ? Color.RED : Color.WHITE);
		pane.setText(wrapBody("<table width=\"100%\"><tr><td colspan=\"2\">" + msg + "</td></tr></table>"));
	}
}
