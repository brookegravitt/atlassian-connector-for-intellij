package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.bamboo.BambooPlan;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class PlanListItem extends JPanel {
	private JCheckBox checkBox;
	private String planName;

	private static Icon favOnIcon;
	private static Icon favOffIcon;


	static {
		favOnIcon = IconLoader.getIcon("/icons/fav_on.gif");
		favOffIcon = IconLoader.getIcon("/icons/fav_off.gif");
	}

	public PlanListItem(BambooPlan plan, boolean selected) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		planName = plan.getPlanKey();
		checkBox = new JCheckBox(planName, selected);		
		add(new JLabel(plan.isFavourite() ? favOnIcon : favOffIcon));
		add(checkBox);
	}

	public JCheckBox getCheckBox() {
		return checkBox;
	}

	public String getPlanName() {
		return planName;
	}
}
