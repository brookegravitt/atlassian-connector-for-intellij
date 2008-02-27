package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.bamboo.BambooPlan;
import com.atlassian.theplugin.bamboo.BambooServerFactory;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.configuration.SubscribedPlan;
import com.atlassian.theplugin.configuration.SubscribedPlanBean;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlanCheckboxList extends JList {
	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
	private Object[] cbArray;
	private Object[] cbInitialArray;
	private boolean isModified;

	public PlanCheckboxList() {
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		setCellRenderer(new CheckBoxCellRenderer());

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				setCheckboxState(index);
			}
		});

		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					int index = getSelectedIndex();
					setCheckboxState(index);
				}
			}
		});

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	private void setCheckboxState(int index) {
		if (index != -1) {
			PlanListItem pi = (PlanListItem) getModel().getElementAt(index);
			pi.getCheckBox().setSelected(!pi.getCheckBox().isSelected());
			repaint();

			setModifiedState();
		}
	}

	private void setModifiedState() {
		for (int i = 0; i < cbArray.length; ++i) {
			if (((PlanListItem) cbArray[i]).getCheckBox().isSelected()
					!= ((PlanListItem) cbInitialArray[i]).getCheckBox().isSelected()) {
				isModified = true;
				break;
			}
		}
	}

	protected class CheckBoxCellRenderer implements ListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index,
													  boolean isSelected, boolean cellHasFocus) {

			PlanListItem pi = (PlanListItem) value;
			pi.setBackground(isSelected ? getSelectionBackground() : getBackground());
			pi.setForeground(isSelected ? getSelectionForeground() : getForeground());
			pi.getCheckBox().setBackground(isSelected ? getSelectionBackground() : getBackground());
			pi.getCheckBox().setForeground(isSelected ? getSelectionForeground() : getForeground());

			pi.getCheckBox().setEnabled(isEnabled());
			pi.getCheckBox().setFont(getFont());
			pi.getCheckBox().setFocusPainted(false);

			pi.getCheckBox().setBorderPainted(true);
			pi.getCheckBox().setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);

			return pi;
		}
	}

	public void setBuilds(Server server) {
		try {
			Collection<BambooPlan> plans = BambooServerFactory.getBambooServerFacade().getPlanList(server);
			if (plans != null) {
				cbArray = new Object[plans.size()];
				cbInitialArray = new Object[plans.size()];
				int i = 0;
				for (BambooPlan plan : plans) {
					boolean enabled = false;
					for (SubscribedPlan sPlan : server.getSubscribedPlans()) {
						if (sPlan.getPlanId().equals(plan.getPlanKey())) {
							enabled = true;
							break;
						}
					}
					cbArray[i] = new PlanListItem(plan, enabled);
					cbInitialArray[i++] = new PlanListItem(plan, enabled);
				}
			}
		} catch (ServerPasswordNotProvidedException ex) {
			cbArray = new Object[0];
		}
		setListData(cbArray);
		setVisible(true);
		isModified = false;
	}

	public java.util.List<SubscribedPlanBean> getSubscribedPlans() {
		List<SubscribedPlanBean> plans = new ArrayList<SubscribedPlanBean>();

		if (cbArray != null) {
			for (int i = 0; i < cbArray.length; ++i) {
				PlanListItem p = (PlanListItem) cbArray[i];

				if (p.getCheckBox().isSelected()) {
					SubscribedPlanBean spb = new SubscribedPlanBean();
					spb.setPlanId(p.getPlanName());
					plans.add(spb);
				}
			}
		}
		return plans;
	}

	public boolean isModified() {
		return isModified;
	}	
}

