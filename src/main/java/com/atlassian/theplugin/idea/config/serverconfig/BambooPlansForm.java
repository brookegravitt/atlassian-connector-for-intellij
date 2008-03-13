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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class BambooPlansForm extends JComponent {
	private JPanel statusPanel;
	private JPanel toolbarPanel;
	private JCheckBox cbUseFavuriteBuilds;
	private JButton btRefresh;
	private JList list;
	private JPanel rootComponent;
	private JEditorPane statusPane;

	private DefaultListModel model;

	private boolean isListModified;
	private Boolean isUseFavourite = null;
	private transient Server originalServer;
	private transient Server queryServer;
	private Map<String, java.util.List<BambooPlanItem>> serverPlans = new HashMap<String, java.util.List<BambooPlanItem>>();
	private transient final BambooServerFacade bambooServerFacade;
	private final ServerPanel serverPanel;

	public BambooPlansForm(BambooServerFacade bambooServerFacade, ServerPanel serverPanel) {
		this.bambooServerFacade = bambooServerFacade;
		this.serverPanel = serverPanel;

		$$$setupUI$$$();

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
		java.util.List<BambooPlanItem> local = serverPlans.get(getServerKey(originalServer));
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
		statusPane.setText("Waiting for server plans...");

		new Thread(new Runnable() {
			public void run() {
				StringBuffer msg = new StringBuffer();
				String key = getServerKey(server);
				if (!serverPlans.containsKey(key)) {
					Collection<BambooPlan> plans = null;
					try {
						plans = bambooServerFacade.getPlanList(server);
					} catch (ServerPasswordNotProvidedException e) {
						msg.append("Unable to connect: password for server not provided\n");
					} catch (BambooException e) {
						msg.append("Unable to connect: ");
						msg.append(e.getMessage());
						msg.append("\n");
					}
					java.util.List<BambooPlanItem> plansForServer = new ArrayList<BambooPlanItem>();
					if (plans != null) {
						for (BambooPlan plan : plans) {
							plansForServer.add(new BambooPlanItem(plan, false));
						}
						msg.append("Build plans updated from server\n");
					}
					if (!server.getSubscribedPlans().isEmpty()) {
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
						msg.append("Build plans updated based on stored configuration");
					}
					serverPlans.put(key, plansForServer);
				} else {
					msg.append("Build plans updated based on cached values");
				}
				final String message = msg.toString();
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						updatePlanNames(server, message);
					}
				});
			}
		}).start();
	}

	private void updatePlanNames(Server server, String message) {
		if (server.equals(queryServer)) {
			java.util.List<BambooPlanItem> plans = serverPlans.get(getServerKey(server));
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
			statusPane.setText(message);
			statusPane.setCaretPosition(0);
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
					server.getSubscribedPlans().add(spb);
				}
			}
		}
		server.setUseFavourite(cbUseFavuriteBuilds.isSelected());

		return server;
	}

	public boolean isModified() {
		boolean isFavModified = false;
		if (originalServer != null) {
			if (cbUseFavuriteBuilds.isSelected() != originalServer.getUseFavourite()) {
				isFavModified = true;
			}
		} else {
			return false;
		}

		return isListModified || isFavModified;
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		list.setEnabled(enabled);
	}

	private void createUIComponents() {
		model = new DefaultListModel();
		list = new JList(model);
		list.setCellRenderer(new PlanListCellRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		createUIComponents();
		rootComponent = new JPanel();
		rootComponent.setLayout(new BorderLayout(0, 0));
		rootComponent.setBorder(BorderFactory.createTitledBorder("Build Plans"));
		toolbarPanel = new JPanel();
		toolbarPanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.add(toolbarPanel, BorderLayout.NORTH);
		cbUseFavuriteBuilds = new JCheckBox();
		cbUseFavuriteBuilds.setText("Use Favourite Builds For Server");
		cbUseFavuriteBuilds.setMnemonic('F');
		cbUseFavuriteBuilds.setDisplayedMnemonicIndex(4);
		toolbarPanel.add(cbUseFavuriteBuilds, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		toolbarPanel.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		btRefresh = new JButton();
		btRefresh.setText("Refresh");
		toolbarPanel.add(btRefresh, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JScrollPane scrollPane1 = new JScrollPane();
		rootComponent.add(scrollPane1, BorderLayout.CENTER);
		scrollPane1.setViewportView(list);
		statusPanel = new JPanel();
		statusPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.add(statusPanel, BorderLayout.SOUTH);
		final JScrollPane scrollPane2 = new JScrollPane();
		scrollPane2.setEnabled(true);
		scrollPane2.setHorizontalScrollBarPolicy(31);
		scrollPane2.setVerticalScrollBarPolicy(20);
		statusPanel.add(scrollPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, new Dimension(-1, 40), null, new Dimension(-1, 40), 0, false));
		statusPane = new JEditorPane();
		statusPane.setEditable(false);
		scrollPane2.setViewportView(statusPane);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootComponent;
	}
}
