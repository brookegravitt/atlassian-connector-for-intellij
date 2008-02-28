package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.bamboo.BambooPlan;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class PlanListItem extends JPanel {
	private JCheckBox checkBox;
	private String planName;

	private static final Icon favOnIcon = IconLoader.getIcon("/icons/fav_on.gif");
	private static final Icon favOffIcon = IconLoader.getIcon("/icons/fav_off.gif");
	private static final Icon disabledIcon = IconLoader.getIcon("/icons/icn_plan_disabled-16.gif");

	public PlanListItem(BambooPlan plan, boolean selected) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		planName = plan.getPlanKey();
		checkBox = new JCheckBox(planName, selected);		
		add(new JLabel(plan.isEnabled() ? (plan.isFavourite() ? favOnIcon : favOffIcon) : disabledIcon));
		add(checkBox);
	}

	protected JCheckBox getCheckBox() {
		return checkBox;
	}

	public boolean isSelected() {
		return checkBox.isSelected();
	}

	public void setSelected(boolean selected) {
		checkBox.setSelected(selected);
	}

	public String getPlanName() {
		return planName;
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		checkBox.setEnabled(enabled);
	}
}
