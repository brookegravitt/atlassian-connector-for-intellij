package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.idea.jira.CachedIconLoader;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.JIRAIssueListModel;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class JIRAIssueTreeNode extends JIRAAbstractTreeNode {
	private final JIRAIssueListModel model;
	private final JIRAIssue issue;

	public JIRAIssueTreeNode(JIRAIssueListModel model, JIRAIssue issue) {
		this.model = model;
		this.issue = issue;
	}


	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
		int x = 0;
		//typeIcon/issueKey/issueSummary/issueState/stateIcon/priorityIcon
		JPanel p = new JPanel(new FormLayout("left:pref, left:pref:grow, "
				+ "left:pref, left:pref, left:pref, 70dlu, 10dlu", "pref:grow"));
		CellConstraints cc = new CellConstraints();
		Color bgColor = selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground();
		Color fgColor = selected ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground();

		fgColor = c.isEnabled() ? fgColor : UIUtil.getInactiveTextColor();

		SimpleTextAttributes textAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, fgColor);


		Icon typeIcon = c.isEnabled() ? CachedIconLoader.getIcon(issue.getTypeIconUrl())
				: CachedIconLoader.getDisabledIcon(issue.getTypeIconUrl());

		JLabel icon = new JLabel(typeIcon, SwingConstants.LEADING);
		icon.setBackground(UIUtil.getTreeTextBackground());
//		p.add(icon, cc.xy(++x, 1));

		cc.xy(++x, 1);
		SimpleColoredComponent key = new SimpleColoredComponent();
		key.append(issue.getKey(), textAttributes);
		p.add(key, cc);

		cc.xy(++x, 1);
		SimpleColoredComponent summary = new SimpleColoredComponent();
		summary.append(issue.getSummary(), textAttributes);
		p.add(summary, cc);

		cc.xy(++x, 1);
		Icon statusIcon = c.isEnabled() ? CachedIconLoader.getIcon(issue.getStatusTypeUrl())
				: CachedIconLoader.getDisabledIcon(issue.getStatusTypeUrl());

		SimpleColoredComponent state = new SimpleColoredComponent();
		state.append(issue.getStatus(), textAttributes);
		p.add(state, cc);

		cc.xy(++x, 1);
		SimpleColoredComponent stateIcon = new SimpleColoredComponent();
		stateIcon.setIcon(statusIcon);
		p.add(stateIcon, cc);

		Icon prioIcon = c.isEnabled() ? CachedIconLoader.getIcon(issue.getPriorityIconUrl())
				: CachedIconLoader.getDisabledIcon(issue.getPriorityIconUrl());

		if (prioIcon != null) {
			cc.xy(++x, 1);
			SimpleColoredComponent priorityIcon = new SimpleColoredComponent();
			priorityIcon.setIcon(prioIcon);
			p.add(priorityIcon, cc);
        }

		cc.xy(++x, 1);
		SimpleColoredComponent updated = new SimpleColoredComponent();
		DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		DateFormat ds = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		String t;
		
		try {
			t = ds.format(df.parse(issue.getUpdated()));
		} catch (java.text.ParseException e) {
			t = "Invalid";
		}

		updated.append(t, textAttributes);
		p.add(updated, cc);

		JLabel padding = new JLabel("  ");
		cc.xy(++x, 1);
		padding.setBackground(fgColor);
		padding.setForeground(bgColor);
        p.add(padding, cc);

		p.setBackground(bgColor);
		JPanel panel = new JPanel(new FormLayout("pref, pref:grow", "pref"));
		panel.setBackground(UIUtil.getTreeTextBackground());
		panel.add(icon, cc.xy(1,1));
		panel.add(p, cc.xy(2,1));

		//panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		//((FlowLayout)panel.getLayout()).setAlignment(FlowLayout.TRAILING);

		return panel;
	}

	public void onSelect() {
		model.setSeletedIssue(issue);	
	}

	public String toString() {
		return issue.getKey() + " " + issue.getSummary();
	}
}
