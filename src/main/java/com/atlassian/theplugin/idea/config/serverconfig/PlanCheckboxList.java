package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.bamboo.BambooPlan;

import com.atlassian.theplugin.bamboo.BambooServerFactory;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.SubscribedPlan;
import com.atlassian.theplugin.configuration.SubscribedPlanBean;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PlanCheckboxList extends JList {
	private static final int VISIBLE_ROW_COUNT = 4;
	protected static final Border NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
	private Object[] cbArray;
	private Object[] cbInitialArray;
	private boolean isModified;

	public PlanCheckboxList() {
//		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		setCellRenderer(new CheckBoxCellRenderer());
		setVisibleRowCount(VISIBLE_ROW_COUNT);
//		setAutoscrolls(true);

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
		if (index != -1 && isEnabled()) {
			PlanListItem pi = (PlanListItem) getModel().getElementAt(index);
			pi.setSelected(!pi.isSelected());
			repaint();

			setModifiedState();
		}
	}

	private void setModifiedState() {
		for (int i = 0; i < cbArray.length; ++i) {
			if (((PlanListItem) cbArray[i]).isSelected()
					!= ((PlanListItem) cbInitialArray[i]).isSelected()) {
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

			pi.setEnabled(isEnabled());
			pi.getCheckBox().setFont(getFont());
			pi.getCheckBox().setFocusPainted(false);

			pi.getCheckBox().setBorderPainted(true);
			pi.getCheckBox().setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : NO_FOCUS_BORDER);

			return pi;
		}
	}

	public void setBuilds(final Server server) {
		final SwingWorker<Collection<BambooPlan>, Object> worker = new SwingWorker<Collection<BambooPlan>, Object>() {
			protected Collection<BambooPlan> doInBackground() throws Exception {
				return BambooServerFactory.getBambooServerFacade().getPlanList(server);
			}

			protected void done() {
				try {
					updatePlanNames(server, get());
				} catch (InterruptedException e) {
					// do nothing
				} catch (ExecutionException e) {
					// do nothing
				}
			}
		};
		worker.execute();
	}

	private void updatePlanNames(Server server, Collection<BambooPlan> plans) {
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
		} else {
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

				if (p.isSelected()) {
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

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (cbArray != null) {
			for (Object item : cbArray) {
				((PlanListItem) item).setEnabled(enabled);
			}
		}
	}
}

