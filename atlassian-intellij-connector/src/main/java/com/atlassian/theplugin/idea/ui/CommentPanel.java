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

import com.atlassian.connector.commons.jira.beans.JIRAComment;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.jira.IssueDetailsToolWindow;
import com.atlassian.theplugin.idea.jira.StackTraceDetector;
import com.atlassian.theplugin.idea.util.Html2text;
import com.intellij.ide.BrowserUtil;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;

public class CommentPanel extends JPanel {

	private ShowHideButton btnShowHide;

	private static final int COMMENT_GAP = 6;

	public CommentPanel(int cmtNumber, final JIRAComment comment, final ServerData server, JTabbedPane tabs,
			IssueDetailsToolWindow.IssuePanel ip) {
		setOpaque(true);
		setBackground(Color.WHITE);

		int upperMargin = cmtNumber == 1 ? 0 : COMMENT_GAP;

		setLayout(new GridBagLayout());
		GridBagConstraints gbc;

		JEditorPane commentBody = new JEditorPane();
		btnShowHide = new ShowHideButton(commentBody, this);
		HeaderListener headerListener = new HeaderListener();

		gbc = new GridBagConstraints();
		gbc.gridx++;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(upperMargin, 0, 0, 0);
		add(btnShowHide, gbc);

		gbc.gridx++;
		gbc.insets = new Insets(upperMargin, Constants.DIALOG_MARGIN / 2, 0, 0);
		UserLabel ul = new UserLabel();
		ul.setUserName(server != null ? server.getUrl() : "", comment.getAuthorFullName(),
				comment.getAuthor(), false);
		ul.setFont(ul.getFont().deriveFont(Font.BOLD));
		add(ul, gbc);

		final JLabel hyphen = new WhiteLabel();
		hyphen.setText("-");
		gbc.gridx++;
		gbc.insets = new Insets(upperMargin, Constants.DIALOG_MARGIN / 2, 0, Constants.DIALOG_MARGIN / 2);
		add(hyphen, gbc);

		final JLabel creationDate = new WhiteLabel();
		creationDate.setForeground(Color.GRAY);
		creationDate.setFont(creationDate.getFont().deriveFont(Font.ITALIC));

//		DateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.US);
//		DateFormat dfo = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

//        DateTimeFormatter dft = DateTimeFormat.forPattern("EEE MMM d HH:mm:ss Z yyyy").withLocale(Locale.US);
        DateTimeFormatter dfto = DateTimeFormat.shortDateTime().withLocale(Locale.US);
		String t;
//		try {
            t = dfto.print(new DateTime(comment.getCreationDate()));
//                    t = dfo.format(df.parse(comment.getCreationDate().getTime().toString()));
//		} catch (java.text.ParseException e) {
//			t = "Invalid date: " + comment.getCreationDate().getTime().toString();
//		}

		creationDate.setText(t);
		gbc.gridx++;
		gbc.insets = new Insets(upperMargin, 0, 0, 0);
		add(creationDate, gbc);


		String dehtmlizedBody = Html2text.translate(comment.getBody());
		if (StackTraceDetector.containsStackTrace(dehtmlizedBody)) {
			int stackTraceCounter = ip.incrementStackTraceCounter();
			tabs.add("Comment Stack Trace #" + (++stackTraceCounter), new StackTracePanel(dehtmlizedBody, ip.getProject()));

			gbc.gridx++;
			gbc.insets = new Insets(upperMargin, Constants.DIALOG_MARGIN / 2, 0, 0);
			JLabel traceNumber = new WhiteLabel();
			traceNumber.setText("Stack Trace #" + stackTraceCounter);
			traceNumber.setForeground(Color.RED);

			add(traceNumber, gbc);
		}

		// filler
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		JPanel filler = new JPanel();
		filler.setBackground(Color.WHITE);
		filler.setOpaque(true);
		gbc.insets = new Insets(upperMargin, 0, 0, 0);
		add(filler, gbc);

		int gridwidth = gbc.gridx + 1;

		commentBody.setEditable(false);
		commentBody.setOpaque(true);
		commentBody.setBackground(Color.WHITE);
		commentBody.setMargin(new Insets(0, 2 * Constants.DIALOG_MARGIN, 0, 0));
		commentBody.setContentType("text/html");
		commentBody.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		// JEditorPane does not do XHTML :(
		String bodyFixed = comment.getBody().replace("/>", ">");
		commentBody.setText("<html><head></head><body>" + bodyFixed + "</body></html>");
		commentBody.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					BrowserUtil.launchBrowser(e.getURL().toString());
				}
			}
		});
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = gridwidth;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		add(commentBody, gbc);

		addMouseListener(headerListener);
	}

	private class HeaderListener extends MouseAdapter {
				public void mouseClicked(MouseEvent e) {
					btnShowHide.click();
				}
			}

			public ShowHideButton getShowHideButton() {
				return btnShowHide;
			}
		}

