package com.atlassian.theplugin.idea.bamboo.build;

import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.commons.bamboo.BuildIssue;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.connector.intellij.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.idea.bamboo.tree.BuildTreeNode;
import com.atlassian.theplugin.idea.ui.BoldLabel;
import com.atlassian.theplugin.idea.util.Html2text;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import org.jetbrains.annotations.Nullable;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 1:33:07 PM
 */
public class BuildDetailsPanel extends JPanel implements ActionListener {

    private Project project;
    private final BambooBuildAdapter build;

	private JLabel relativeBuildTime = new JLabel();
	private static final int MAX_NR_OF_COMMITTERS_TO_LIST_IN_A_DETAILED_WAY = 3;
    private JPanel issuesPanel;
    private JPanel body;

    public BuildDetailsPanel(Project project, BambooBuildAdapter build) {
        this.project = project;
        this.build = build;
		setLayout(new GridBagLayout());

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;

		refreshBuildRelativeTime();

		JScrollPane scroll = new JScrollPane(createBody());
		scroll.setBorder(BorderFactory.createEmptyBorder());
		add(scroll, gbc);
	}

	public JPanel createBody() {
        body = new JPanel();
		body.setLayout(new GridBagLayout());
		body.setBackground(Color.WHITE);

		GridBagConstraints gbc1 = new GridBagConstraints();
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc2.gridx = 1;
		gbc1.gridy = 0;
		gbc2.gridy = 0;
		gbc1.weighty = 0.0;
		gbc2.weighty = 0.0;
		gbc1.weightx = 0.0;
		gbc2.weightx = 1.0;
		gbc1.fill = GridBagConstraints.NONE;
		gbc2.fill = GridBagConstraints.HORIZONTAL;
		gbc1.insets = new Insets(Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN,
				Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
		gbc2.insets = new Insets(Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN,
				Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
		gbc1.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc2.anchor = GridBagConstraints.FIRST_LINE_START;


		body.add(new BoldLabel("State"), gbc1);
		body.add(new JLabel(build.getAdjustedStatus().getName(), build.getIcon(), SwingConstants.LEFT), gbc2);
		gbc1.gridy++;
		gbc2.gridy++;
		gbc1.insets = new Insets(0, Constants.DIALOG_MARGIN, Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
		gbc2.insets = new Insets(0, Constants.DIALOG_MARGIN, Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
		body.add(new BoldLabel("Last Build"), gbc1);
		body.add(new JLabel(build.getBuildNumberAsString()), gbc2);
		gbc1.gridy++;
		gbc2.gridy++;
		body.add(new BoldLabel("When"), gbc1);
		body.add(relativeBuildTime, gbc2);
		// we don't have any way to get build duration yet
//		gbc1.gridy++;
//		gbc2.gridy++;
//		p.add(new BoldLabel("Build Duration"), gbc1);
//		p.add(HGW???, gbc2);
		gbc1.gridy++;
		gbc2.gridy++;

		StringBuilder reason = new StringBuilder(build.getReason());
		Collection<String> committers = build.getCommiters();
		// bleeeee, ugly ugly
		if (committers.size() > 0 && reason.toString().equals(BuildTreeNode.CODE_HAS_CHANGED)) {
			reason = new StringBuilder("Code was changed by ");
			if (committers.size() > MAX_NR_OF_COMMITTERS_TO_LIST_IN_A_DETAILED_WAY) {
				reason.append(committers.size()).append(" people");
			} else {
				int i = 0;
				for (String committer : committers) {
					reason.append(committer);
					if (++i < committers.size()) {
						reason.append(", ");
					}
				}
			}
		}
		body.add(new BoldLabel("Build Reason"), gbc1);
		body.add(new JLabel(Html2text.translate(reason.toString()).trim()), gbc2);
		gbc1.gridy++;
		gbc2.gridy++;
		body.add(new BoldLabel("Tests"), gbc1);
		body.add(new JLabel(build.getTestsPassedSummary() + " failed"), gbc2);

        gbc1.gridy++;
        gbc2.gridy++;

        issuesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        issuesPanel.setOpaque(false);
        JLabel label = new JLabel("Retrieving issues related to this build...");
        issuesPanel.add(label);
        body.add(new BoldLabel("Issues"), gbc1);
        body.add(issuesPanel, gbc2);

		gbc1.gridy++;
		gbc1.gridwidth = 2;
		gbc1.weighty = 1.0;
		gbc1.fill = GridBagConstraints.VERTICAL;
		JPanel filler = new JPanel();
		filler.setOpaque(true);
		filler.setBackground(Color.WHITE);
		body.add(filler, gbc1);

		return body;
	}

	public void actionPerformed(ActionEvent e) {
		refreshBuildRelativeTime();
	}

	private void refreshBuildRelativeTime() {
		relativeBuildTime.setText(DateUtil.getRelativePastDate(build.getCompletionDate()));
	}

    public void setIssues(@Nullable Collection<BuildIssue> issues) {
        issuesPanel.removeAll();
        issuesPanel.validate();
        body.validate();
        if (issues == null) {
            issuesPanel.add(new JLabel("No issue information available"));
        } else {
            int i = 0;
            if (issues.size() == 0) {
                issuesPanel.add(new JLabel("No issues associated with this build"));
            } else {
                JiraServerData server = null;
                String selectedJiraServerUrl = null;
                final IssueListToolWindowPanel issueListToolWindowPanel = IdeaHelper.getIssueListToolWindowPanel(project);
                if (issueListToolWindowPanel != null) {
                    server = issueListToolWindowPanel.getSelectedServer();
                }
                if (server != null) {
                    selectedJiraServerUrl = server.getUrl();
                }
                for (final BuildIssue issue : issues) {
                    HyperlinkLabel label = new HyperlinkLabel(issue.getIssueKey());
                    label.setOpaque(false);
                    if (selectedJiraServerUrl == null || !selectedJiraServerUrl.equals(issue.getServerUrl())) {
                        label.setToolTipText(issue.getIssueUrl() + " - click to open in external browser");
                    } else {
                        label.setToolTipText("Click to open in IDEA");
                    }
                    final String jiraServerUrlFinal = selectedJiraServerUrl;
                    final JiraServerData serverFinal = server;
                    label.addHyperlinkListener(new HyperlinkListener() {
                        public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
                            if (jiraServerUrlFinal == null || !jiraServerUrlFinal.equals(issue.getServerUrl())) {
                                BrowserUtil.launchBrowser(issue.getIssueUrl());
                            } else {
                                issueListToolWindowPanel.openIssue(issue.getIssueKey(), serverFinal, false);
                            }
                        }
                    });
                    ++i;
                    issuesPanel.add(label);
                    if (i < issues.size()) {
                        issuesPanel.add(new JLabel(", "));
                    }
                }
            }
        }
        issuesPanel.validate();
        body.validate();
    }
}
