package com.atlassian.theplugin.idea.bamboo.build;

import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.util.Html2text;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.atlassian.theplugin.idea.bamboo.tree.BuildTreeNode;
import com.atlassian.theplugin.idea.ui.BoldLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 1:33:07 PM
 */
public class BuildDetailsPanel extends JPanel implements ActionListener {

	private final BambooBuildAdapterIdea build;

	private JLabel relativeBuildTime = new JLabel();
	private static final int MAX_NR_OF_COMMITTERS_TO_LIST_IN_A_DETAILED_WAY = 3;

	public BuildDetailsPanel(BambooBuildAdapterIdea build) {
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
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		p.setBackground(Color.WHITE);

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


		p.add(new BoldLabel("State"), gbc1);
		p.add(new JLabel(build.getAdjustedStatus().getName(), build.getIcon(), SwingConstants.LEFT), gbc2);
		gbc1.gridy++;
		gbc2.gridy++;
		gbc1.insets = new Insets(0, Constants.DIALOG_MARGIN, Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
		gbc2.insets = new Insets(0, Constants.DIALOG_MARGIN, Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
		p.add(new BoldLabel("Last Build"), gbc1);
		p.add(new JLabel(build.getBuildNumberAsString()), gbc2);
		gbc1.gridy++;
		gbc2.gridy++;
		p.add(new BoldLabel("When"), gbc1);
		p.add(relativeBuildTime, gbc2);
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
		p.add(new BoldLabel("Build Reason"), gbc1);
		p.add(new JLabel(Html2text.translate(reason.toString()).trim()), gbc2);
		gbc1.gridy++;
		gbc2.gridy++;
		p.add(new BoldLabel("Tests"), gbc1);
		p.add(new JLabel(build.getTestsPassedSummary() + " failed"), gbc2);

		gbc1.gridy++;
		gbc1.gridwidth = 2;
		gbc1.weighty = 1.0;
		gbc1.fill = GridBagConstraints.VERTICAL;
		JPanel filler = new JPanel();
		filler.setOpaque(true);
		filler.setBackground(Color.WHITE);
		p.add(filler, gbc1);

		return p;
	}

	public void actionPerformed(ActionEvent e) {
		refreshBuildRelativeTime();
	}

	private void refreshBuildRelativeTime() {
		relativeBuildTime.setText(DateUtil.getRelativeBuildTime(build.getCompletionDate()));
	}
}
