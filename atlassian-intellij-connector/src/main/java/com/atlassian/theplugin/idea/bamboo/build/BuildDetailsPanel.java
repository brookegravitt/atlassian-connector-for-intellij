package com.atlassian.theplugin.idea.bamboo.build;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.atlassian.theplugin.idea.ui.BoldLabel;
import com.atlassian.theplugin.commons.util.DateUtil;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.Calendar;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 1:33:07 PM
 */
public class BuildDetailsPanel extends JPanel implements ActionListener {

	private final BambooBuildAdapterIdea build;

	private JLabel relativeBuildTime = new JLabel();

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

		Border b = BorderFactory.createTitledBorder("Details");
		setBorder(b);
		Insets i = b.getBorderInsets(this);
		setMinimumSize(new Dimension(0, i.top + i.bottom));
	}

	public JPanel createBody() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());

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
		gbc1.insets = new Insets(0, Constants.DIALOG_MARGIN, Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
		gbc1.anchor = GridBagConstraints.FIRST_LINE_END;
		gbc2.anchor = GridBagConstraints.FIRST_LINE_START;


		p.add(new BoldLabel("State"), gbc1);
		p.add(new JLabel(build.getState().getName(), build.getState().getIcon(), SwingConstants.LEFT), gbc2);
		gbc1.gridy++;
		gbc2.gridy++;
		p.add(new BoldLabel("Last Build"), gbc1);
		p.add(new JLabel(build.getBuildNumber()), gbc2);
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
		p.add(new BoldLabel("Build Reason"), gbc1);
		p.add(new JLabel(build.getBuildReason()), gbc2);
		gbc1.gridy++;
		gbc2.gridy++;
		p.add(new BoldLabel("Tests"), gbc1);
		p.add(new JLabel(build.getTestsPassedSummary() + " failed"), gbc2);

		return p;
	}

	public void actionPerformed(ActionEvent e) {
		refreshBuildRelativeTime();
	}

	private void refreshBuildRelativeTime() {
		relativeBuildTime.setText(getRelativeBuildTime());
	}
	
	private String getRelativeBuildTime() {
		Date d = build.getBuildTime();
		if (d != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			c.add(Calendar.HOUR_OF_DAY, build.getServer().getTimezoneOffset());
			return DateUtil.getRelativePastDate(new Date(), c.getTime());
		}
		return "Unknown";
	}
}
