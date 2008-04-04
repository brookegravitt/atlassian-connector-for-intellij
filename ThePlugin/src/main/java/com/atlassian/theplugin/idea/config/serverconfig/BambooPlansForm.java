package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.bamboo.BambooPlan;
import com.atlassian.theplugin.bamboo.BambooPlanData;
import com.atlassian.theplugin.bamboo.BambooServerFacade;
import com.atlassian.theplugin.configuration.*;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.remoteapi.RemoteApiException;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static java.lang.System.arraycopy;
import java.util.*;
import java.util.List;


public class BambooPlansForm extends JPanel {
	private JPanel statusPanel;
	private JPanel toolbarPanel;
	private JCheckBox cbUseFavouriteBuilds;
	private JButton btRefresh;
	private JList list;
	private JPanel rootComponent;
	private JEditorPane statusPane;
	private JScrollPane scrollList;
	private JPanel listPanel;
	private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();

	private DefaultListModel model;

	private boolean isListModified;
	private Boolean isUseFavourite = null;
	private transient Server originalServer;
	private transient Server queryServer;
	private Map<String, List<BambooPlanItem>> serverPlans = new HashMap<String, List<BambooPlanItem>>();
	private transient final BambooServerFacade bambooServerFacade;
	private final ServerPanel serverPanel;

	public BambooPlansForm(BambooServerFacade bambooServerFacade, ServerPanel serverPanel) {
		this.bambooServerFacade = bambooServerFacade;
		this.serverPanel = serverPanel;

		$$$setupUI$$$();

		GridConstraints constraint = new GridConstraints(0, 0, 1, 1,
				GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false);

		progressAnimation.configure(listPanel, scrollList, constraint);

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

		cbUseFavouriteBuilds.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabled(!cbUseFavouriteBuilds.isSelected());
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
		if (originalServer.getUseFavourite() != cbUseFavouriteBuilds.isSelected()) {
			isUseFavourite = cbUseFavouriteBuilds.isSelected();
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
			setViewState(index, pi.isSelected());
			repaint();

			setModifiedState();
		}
	}

	private void setViewState(int index, boolean newState) {
		int[] oldIdx = list.getSelectedIndices();
		int[] newIdx;
		if (newState) {
			newIdx = new int[oldIdx.length + 1];
			arraycopy(newIdx, 0, oldIdx, 0, oldIdx.length);
			newIdx[newIdx.length - 1] = index;
		} else {
			newIdx = new int[Math.max(0, oldIdx.length - 1)];
			int i = 0;
			for (int id : oldIdx) {
				if (id == index) {
					continue;
				}
				newIdx[i++] = id;
			}
		}
		list.setSelectedIndices(newIdx);
	}

	private void setModifiedState() {
		isListModified = false;
		List<BambooPlanItem> local = serverPlans.get(getServerKey(originalServer));
		if (local != null) {
			for (int i = 0; i < model.getSize(); i++) {
				if (local.get(i) != null) {
					if (((BambooPlanItem) model.getElementAt(i)).isSelected()
							!= local.get(i).isSelected()) {
						isListModified = true;
						break;
					}
				}
			}
		} else {
			isListModified = !model.isEmpty();
		}
	}

	public void setData(final Server server) {
		originalServer = new ServerBean(server);
		cbUseFavouriteBuilds.setEnabled(false);
		if (!"".equals(originalServer.getUrlString())) {
			retrievePlans(originalServer);
		} else {
			model.removeAllElements();
		}
	}

	private void retrievePlans(final Server server) {
		queryServer = server;
		list.setEnabled(false);
		if (isUseFavourite != null) {
			cbUseFavouriteBuilds.setSelected(isUseFavourite);
			isUseFavourite = null;
		} else {
			cbUseFavouriteBuilds.setSelected(server.getUseFavourite());
		}
		model.removeAllElements();
		statusPane.setText("Waiting for server plans...");

		new Thread(new Runnable() {
			public void run() {
				progressAnimation.startProgressAnimation();
				StringBuffer msg = new StringBuffer();
				String key = getServerKey(server);
				if (!serverPlans.containsKey(key)) {
					Collection<BambooPlan> plans = null;
					try {
						plans = bambooServerFacade.getPlanList(server);
					} catch (ServerPasswordNotProvidedException e) {
						msg.append("Unable to connect: password for server not provided\n");
					} catch (RemoteApiException e) {
						msg.append("Unable to connect: ");
						msg.append(e.getMessage());
						msg.append("\n");
					}
					List<BambooPlanItem> plansForServer = new ArrayList<BambooPlanItem>();
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

				progressAnimation.stopProgressAnimation();

			}
		}, "atlassian-idea-plugin bamboo panel retrieve plans").start();
	}

	private void updatePlanNames(Server server, String message) {
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
			statusPane.setText(message);
			statusPane.setCaretPosition(0);
			setVisible(true);
			cbUseFavouriteBuilds.setEnabled(true);
			list.setEnabled(!cbUseFavouriteBuilds.isSelected());
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
		server.setUseFavourite(cbUseFavouriteBuilds.isSelected());

		return server;
	}

	public boolean isModified() {
		boolean isFavModified = false;
		if (originalServer != null) {
			if (cbUseFavouriteBuilds.isSelected() != originalServer.getUseFavourite()) {
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
		rootComponent.setLayout(new GridBagLayout());
		rootComponent.setBorder(BorderFactory.createTitledBorder("Build Plans"));
		toolbarPanel = new JPanel();
		toolbarPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 10, 12, 8);
		rootComponent.add(toolbarPanel, gbc);
		cbUseFavouriteBuilds = new JCheckBox();
		cbUseFavouriteBuilds.setText("Use Favourite Builds For Server");
		cbUseFavouriteBuilds.setMnemonic('F');
		cbUseFavouriteBuilds.setDisplayedMnemonicIndex(4);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		toolbarPanel.add(cbUseFavouriteBuilds, gbc);
		btRefresh = new JButton();
		btRefresh.setText("Refresh");
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		toolbarPanel.add(btRefresh, gbc);
		statusPanel = new JPanel();
		statusPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 12, 12, 12);
		rootComponent.add(statusPanel, gbc);
		final JScrollPane scrollPane1 = new JScrollPane();
		scrollPane1.setEnabled(true);
		scrollPane1.setHorizontalScrollBarPolicy(31);
		scrollPane1.setVerticalScrollBarPolicy(20);
		statusPanel.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, new Dimension(-1, 40), null, new Dimension(-1, 40), 0, false));
		statusPane = new JEditorPane();
		statusPane.setEditable(false);
		scrollPane1.setViewportView(statusPane);
		listPanel = new JPanel();
		listPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		listPanel.setBackground(new Color(-1));
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 12, 0, 12);
		rootComponent.add(listPanel, gbc);
		scrollList = new JScrollPane();
		scrollList.setBackground(new Color(-1));
		listPanel.add(scrollList, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		scrollList.setViewportView(list);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootComponent;
	}
}
