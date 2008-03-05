package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.bamboo.BambooPlan;
import com.atlassian.theplugin.bamboo.BambooPlanData;
import com.atlassian.theplugin.bamboo.BambooServerFacade;
import com.atlassian.theplugin.bamboo.api.BambooException;
import com.atlassian.theplugin.configuration.*;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class BambooPlansForm extends JPanel {
	private JCheckBox cbUseFavuriteBuilds;
	private JList list;
	private DefaultListModel model;
	private JButton btRefresh;

	private boolean isListModified;
	private Boolean isUseFavourite = null;
	private transient Server originalServer;
	private transient Server queryServer;
	private Map<String, List<BambooPlanItem>> serverPlans = new HashMap<String, List<BambooPlanItem>>();
	private transient final BambooServerFacade bambooServerFacade;
	private final ServerPanel serverPanel;

	public BambooPlansForm(BambooServerFacade bambooServerFacade, ServerPanel serverPanel) {
		this.serverPanel = serverPanel;
		this.bambooServerFacade = bambooServerFacade;
		this.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 5, 5), -1, -1));
		this.setBorder(BorderFactory.createTitledBorder("Build plans"));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
		this.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
				GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		cbUseFavuriteBuilds = new JCheckBox();
		cbUseFavuriteBuilds.setText("Use Favourite Builds For Server");
		cbUseFavuriteBuilds.setMnemonic('F');
		cbUseFavuriteBuilds.setDisplayedMnemonicIndex(4);
		panel1.add(cbUseFavuriteBuilds, new GridConstraints(0, 0, 1, 1,
				GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		panel1.add(spacer1, new GridConstraints(0, 1, 1, 1,
				GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		btRefresh = new JButton();
		btRefresh.setText("Refresh");
		btRefresh.setMnemonic('R');
		btRefresh.setDisplayedMnemonicIndex(0);
		panel1.add(btRefresh, new GridConstraints(0, 2, 1, 1,
				GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JScrollPane scrollPane1 = new JScrollPane();
		this.add(scrollPane1, new GridConstraints(1, 0, 1, 1,
				GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));

		model = new DefaultListModel();
		list = new JList(model);
		list.setCellRenderer(new PlanListCellRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane1.setViewportView(list);
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

		cbUseFavuriteBuilds.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabled(!cbUseFavuriteBuilds.isSelected());
			}
		});

		btRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshServerPlans();
			}
		});
	}

	private String getServerKey(Server server) {
		return Long.toString(server.getUid());
	}

	private void refreshServerPlans() {
		if (originalServer.getUseFavourite() != cbUseFavuriteBuilds.isSelected()) {
			isUseFavourite = cbUseFavuriteBuilds.isSelected();
		}
		Server server = serverPanel.getData();
		server.setSubscribedPlans(originalServer.getSubscribedPlans());
		serverPlans.remove(getServerKey(originalServer));
		retrievePlans(server);
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
		isListModified = false;
		List<BambooPlanItem> local = serverPlans.get(getServerKey(originalServer));
		for (int i = 0; i < model.getSize(); i++) {
			if (local.get(i) != null) {
				if (((BambooPlanItem) model.getElementAt(i)).isSelected()
						!= local.get(i).isSelected()) {
					isListModified = true;
					break;
				}
			}
		}
	}

	public void setData(final Server server) {
		originalServer = new ServerBean(server);
		cbUseFavuriteBuilds.setEnabled(false);
		retrievePlans(originalServer);
	}

	private void retrievePlans(final Server server) {
		queryServer = server;
		list.setEnabled(false);
		if (isUseFavourite != null) {
			cbUseFavuriteBuilds.setSelected(isUseFavourite);
			isUseFavourite = null;
		} else {
			cbUseFavuriteBuilds.setSelected(server.getUseFavourite());
		}
		model.removeAllElements();
		model.addElement("  Waiting for server plans...");

		new Thread(new Runnable() {
			public void run() {
				String key = getServerKey(server);
				if (!serverPlans.containsKey(key)) {
					Collection<BambooPlan> plans = null;
					try {
						plans = bambooServerFacade.getPlanList(server);
					} catch (ServerPasswordNotProvidedException e) {
					} catch (BambooException e) {
					}
					List<BambooPlanItem> plansForServer = new ArrayList<BambooPlanItem>();
					if (plans != null) {
						for (BambooPlan plan : plans) {
							plansForServer.add(new BambooPlanItem(plan, false));
						}
					}
					for (SubscribedPlan sPlan : server.getSubscribedPlans()) {
						boolean exists = false;
						for (BambooPlanItem bambooPlanItem : plansForServer) {
							if (bambooPlanItem.getPlan().getPlanKey().equals(sPlan.getPlanId())) {
								exists = true;
								break;
							}
						}
						if (!exists) {
							BambooPlanData p = new BambooPlanData(sPlan.getPlanId(), sPlan.getPlanId());
							p.setEnabled(false);
							p.setFavourite(false);
							plansForServer.add(new BambooPlanItem(p, true));
						}
					}
					serverPlans.put(key, plansForServer);
				}
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						updatePlanNames(server);
					}
				});
			}
		}).start();
	}

	private void updatePlanNames(Server server) {
		if (server.equals(queryServer)) {
			List<BambooPlanItem> plans = serverPlans.get(getServerKey(server));
			if (plans != null) {
				model.removeAllElements();
				for (BambooPlanItem plan : plans) {
					plan.setSelected(false);
					for (SubscribedPlan sPlan : server.getSubscribedPlans()) {
						if (sPlan.getPlanId().equals(plan.getPlan().getPlanKey())) {
							plan.setSelected(true);
							break;
						}
					}
					model.addElement(new BambooPlanItem(plan.getPlan(), plan.isSelected()));
				}
			}
			setVisible(true);
			cbUseFavuriteBuilds.setEnabled(true);
			list.setEnabled(!cbUseFavuriteBuilds.isSelected());
			isListModified = false;
		}
	}

	public Server getData() {
		Server server = new ServerBean();

		for (int i = 0; i < model.getSize(); ++i) {
			if (model.getElementAt(i) instanceof BambooPlanItem) {
				BambooPlanItem p = (BambooPlanItem) model.getElementAt(i);

				if (p.isSelected()) {
					SubscribedPlan spb = new SubscribedPlanBean();
					spb.setPlanId(p.getPlan().getPlanKey());
					((Collection <SubscribedPlan>)server.getSubscribedPlans()).add(spb);
				}
			}
		}
		server.setUseFavourite(cbUseFavuriteBuilds.isSelected());

		return server;
	}

	public boolean isModified() {
		boolean isFavModified = false;
		if (cbUseFavuriteBuilds.isSelected() != originalServer.getUseFavourite()) {
			isFavModified = true;
		}

		return isListModified || isFavModified;
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		list.setEnabled(enabled);
	}
}

