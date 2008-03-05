package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.bamboo.BambooPlan;
import com.atlassian.theplugin.bamboo.BambooServerFactory;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.configuration.SubscribedPlan;
import com.atlassian.theplugin.configuration.SubscribedPlanBean;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlanCheckboxList extends JPanel {
	private static final int VISIBLE_ROW_COUNT = 4;

	private boolean isModified;
	private boolean enabledState;

	private JList list;
	private DefaultListModel model;

	private List<BambooPlanItem> localPlans = new ArrayList<BambooPlanItem>();

	public PlanCheckboxList() {
		model = new DefaultListModel();
		list = new JList(model);
		list.setCellRenderer(new PlanListCellRenderer());
		list.setVisibleRowCount(VISIBLE_ROW_COUNT);

		list.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int index = list.locationToIndex(e.getPoint());
				setCheckboxState(index);
			}
		});

		list.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					int index = list.getSelectedIndex();
					setCheckboxState(index);
				}
			}
		});

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setLayout(new BorderLayout());
		this.add(list, BorderLayout.CENTER);
	}

	private void setCheckboxState(int index) {
		if (index != -1 && isEnabled()) {
			BambooPlanItem pi = (BambooPlanItem) list.getModel().getElementAt(index);
			pi.setSelected(!pi.isSelected());
			repaint();

			setModifiedState();
		}
	}

	private void setModifiedState() {
		isModified = false;
		for (int i = 0; i < model.getSize(); i++) {
			if (((BambooPlanItem) model.getElementAt(i)).isSelected() != localPlans.get(i).isSelected()) {
				isModified = true;
				break;
			}
		}
	}

	public void setBuilds(final Server server) {
		doEnable(false);
		model.removeAllElements();
		localPlans.clear();
		new Thread(new Runnable() {
			public void run() {
				Collection<BambooPlan> plans;
				try {
					plans = BambooServerFactory.getBambooServerFacade().getPlanList(server);
				} catch (ServerPasswordNotProvidedException e) {
					plans = new ArrayList<BambooPlan>();
				}
				final Collection<BambooPlan> finalPlans = plans;
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						updatePlanNames(server, finalPlans);
						doEnable(enabledState);
					}
				});
			}
		}).start();
	}

	private void updatePlanNames(Server server, Collection<BambooPlan> plans) {
		if (plans != null) {
			int i = 0;
			for (BambooPlan plan : plans) {
				boolean selected = false;
				for (SubscribedPlan sPlan : server.getSubscribedPlans()) {
					if (sPlan.getPlanId().equals(plan.getPlanKey())) {
						selected = true;
						break;
					}
				}
				model.addElement(new BambooPlanItem(plan, selected));
				localPlans.add(new BambooPlanItem(plan, selected));
			}
		}
		setVisible(true);
		isModified = false;
	}

	public java.util.List<SubscribedPlanBean> getSubscribedPlans() {
		List<SubscribedPlanBean> plans = new ArrayList<SubscribedPlanBean>();

		for (int i = 0; i < model.getSize(); ++i) {
			BambooPlanItem p = (BambooPlanItem) model.getElementAt(i);

			if (p.isSelected()) {
				SubscribedPlanBean spb = new SubscribedPlanBean();
				spb.setPlanId(p.getPlan().getPlanKey());
				plans.add(spb);
			}
		}
		return plans;
	}

	public boolean isModified() {
		return isModified;
	}

	private void doEnable(boolean enabled) {
		super.setEnabled(enabled);
		list.setEnabled(enabled);
	}

	public void setEnabled(boolean enabled) {
		enabledState = enabled;
		doEnable(enabled);
	}
}

