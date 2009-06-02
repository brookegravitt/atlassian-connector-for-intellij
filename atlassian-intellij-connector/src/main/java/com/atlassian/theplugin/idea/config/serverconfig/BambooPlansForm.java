/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.cfg.SubscribedPlan;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static java.lang.System.arraycopy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class BambooPlansForm extends JPanel {

	private static final int MIN_TIMEZONE_DIFF = -24;
	private static final int MAX_TIMEZONE_DIFF = 24;

	private JPanel statusPanel;
	private JPanel toolbarPanel;
	private JCheckBox cbUseFavouriteBuilds;
	private JButton btRefresh;
	private JList list;
	private JPanel rootComponent;
	private JEditorPane statusPane;
	private JScrollPane scrollList;
	private JPanel listPanel;
	private JPanel plansPanel;
	private JSpinner spinnerTimeZoneDifference;
	private SpinnerModel timezoneOffsetSpinnerModel = new SpinnerNumberModel(0, MIN_TIMEZONE_DIFF, MAX_TIMEZONE_DIFF, 1);
	private JPanel timezonePanel;
	private JLabel bambooVersionNumberInfo;
	private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();

	private DefaultListModel model;

	private boolean isListModified;
	private Boolean isUseFavourite;
	private transient BambooServerCfg bambooServerCfg;
	private static final int NUM_SERVERS = 10;
	private Map<ServerId, List<BambooPlanItem>> serverPlans = MiscUtil.buildConcurrentHashMap(NUM_SERVERS);
	private final transient BambooServerFacade bambooServerFacade;
	private final BambooServerConfigForm serverPanel;
	private UserCfg defaultCredentials;

	public BambooPlansForm(BambooServerFacade bambooServerFacade, BambooServerCfg bambooServerCfg,
			final BambooServerConfigForm bambooServerConfigForm, @NotNull UserCfg defaultCredentials) {
		this.bambooServerFacade = bambooServerFacade;
		this.bambooServerCfg = bambooServerCfg;
		this.serverPanel = bambooServerConfigForm;
		this.defaultCredentials = defaultCredentials;

		$$$setupUI$$$();

		final GridConstraints constraint = new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
				GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false);

		progressAnimation.configure(listPanel, scrollList, constraint);

		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int index = list.locationToIndex(e.getPoint());
				setCheckboxState(index);
			}
		});

		list.addKeyListener(new KeyAdapter() {
			@Override
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

		spinnerTimeZoneDifference.setModel(timezoneOffsetSpinnerModel);
		bambooVersionNumberInfo.setEnabled(false);
		bambooVersionNumberInfo.setEnabled(true);
	}

	private void refreshServerPlans() {
		serverPlans.remove(bambooServerCfg.getServerId());
		serverPanel.saveData();
		bambooServerCfg.setIsBamboo2(bambooServerFacade.isBamboo2(ServerData.create(bambooServerCfg, defaultCredentials)));
		retrievePlans(bambooServerCfg);
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
		List<BambooPlanItem> local = serverPlans.get(bambooServerCfg.getServerId());
		if (local != null) {
			for (int i = 0; i < model.getSize(); i++) {
				if (local.get(i) != null) {
					if (((BambooPlanItem) model.getElementAt(i)).isSelected() != local.get(i).isSelected()) {
						isListModified = true;
						break;
					}
				}
			}
		} else {
			isListModified = !model.isEmpty();
		}
	}

	/**
	 * helper field used to avoid race conditions between thread polling for isModified,
	 * which calls {@link #saveData()}
	 */
	private BambooServerCfg currentlyPopulatedServer;

	private synchronized BambooServerCfg getCurrentlyPopulatedServer() {
		return currentlyPopulatedServer;
	}

	private synchronized void setCurrentlyPopulatedServer(BambooServerCfg bambooServerCfg) {
		this.currentlyPopulatedServer = bambooServerCfg;
	}


	public void setData(final BambooServerCfg serverCfg) {
		bambooServerCfg = serverCfg;

		if (bambooServerCfg != null) {
			int offs = bambooServerCfg.getTimezoneOffset();
			offs = Math.min(MAX_TIMEZONE_DIFF, Math.max(MIN_TIMEZONE_DIFF, offs));
			timezoneOffsetSpinnerModel.setValue(offs);
			if (bambooServerCfg.getUrl().length() > 0) {
				bambooServerCfg.setIsBamboo2(bambooServerFacade.isBamboo2(
						ServerData.create(bambooServerCfg, defaultCredentials)));
				retrievePlans(bambooServerCfg);

			} else {
				model.removeAllElements();
			}
		}
	}

	private void retrievePlans(final BambooServerCfg queryServer) {
		list.setEnabled(false);
		if (isUseFavourite != null) {
			cbUseFavouriteBuilds.setSelected(isUseFavourite);
			isUseFavourite = null;
		} else {
			cbUseFavouriteBuilds.setSelected(queryServer.isUseFavourites());
		}
		statusPane.setText("Waiting for server plans...");

		new Thread(new Runnable() {
			public void run() {
				progressAnimation.startProgressAnimation();
				StringBuilder msg = new StringBuilder();
				try {
					ServerId key = queryServer.getServerId();
					if (!serverPlans.containsKey(key)) {
						Collection<BambooPlan> plans;
						try {
							plans = bambooServerFacade.getPlanList(ServerData.create(queryServer, defaultCredentials));
						} catch (ServerPasswordNotProvidedException e) {
							msg.append("Unable to connect: password for server not provided\n");
							return;
						} catch (RemoteApiException e) {
							msg.append("Unable to connect: ");
							msg.append(e.getMessage());
							msg.append("\n");
							return;
						}
						List<BambooPlanItem> plansForServer = new ArrayList<BambooPlanItem>();
						if (plans != null) {
							for (BambooPlan plan : plans) {
								plansForServer.add(new BambooPlanItem(plan, false));
							}
							msg.append("Build plans updated from server\n");
						}
						for (SubscribedPlan sPlan : queryServer.getSubscribedPlans()) {
							boolean exists = false;
							for (BambooPlanItem bambooPlanItem : plansForServer) {
								if (bambooPlanItem.getPlan().getPlanKey().equals(sPlan.getKey())) {
									exists = true;
									break;
								}
							}
							if (!exists) {
								BambooPlan p = new BambooPlan(sPlan.getKey(), sPlan.getKey(), false, false);
								plansForServer.add(new BambooPlanItem(p, true));
							}
						}
						msg.append("Build plans updated based on stored configuration");
						serverPlans.put(key, plansForServer);

					} else {
						msg.append("Build plans updated based on cached values");
					}
				} finally {
					setBambooVersionNumberInfo(queryServer);
					progressAnimation.stopProgressAnimation();
					final String message = msg.toString();
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							updatePlanNames(queryServer, message);
						}
					});
				}

			}
		}, "atlassian-idea-plugin bamboo panel retrieve plans").start();
	}

	private void setBambooVersionNumberInfo(BambooServerCfg serverCfg) {
		if (serverCfg.isBamboo2()) {
			bambooVersionNumberInfo.setEnabled(false);
			bambooVersionNumberInfo.setVisible(false);
			bambooVersionNumberInfo.setText("");
		} else {
			bambooVersionNumberInfo.setEnabled(true);
			bambooVersionNumberInfo.setVisible(true);
			bambooVersionNumberInfo
					.setText("Server version number is < 2.0. Some plugin features are disabled (i.e. re-run failed tests)");
		}
	}

	/**
	 * Synchronizes Bamboo plan view with given data.
	 * Must be run in EDT.
	 * This method is not thread-safe, but I leave it without synchronization,
	 * as it's effective run only in single-threaded way (EDT)
	 *
	 * @param server  Bamboo sever for which to update planes
	 * @param message additional message which was built during fetching of data
	 */
	private void updatePlanNames(BambooServerCfg server, String message) {
		if (server.equals(bambooServerCfg)) {
			setCurrentlyPopulatedServer(null);
			model.removeAllElements();
			List<BambooPlanItem> plans = serverPlans.get(server.getServerId());
			if (plans != null) {
				for (BambooPlanItem plan : plans) {
					plan.setSelected(false);
					for (SubscribedPlan sPlan : server.getSubscribedPlans()) {
						if (sPlan.getKey().equals(plan.getPlan().getPlanKey())) {
							plan.setSelected(true);
							break;
						}
					}
					model.addElement(new BambooPlanItem(plan.getPlan(), plan.isSelected()));
				}
			} else {
				// for those servers for which we cannot fetch metadata, we just show current plans
				List<BambooPlanItem> modelPlans = MiscUtil.buildArrayList();
				for (SubscribedPlan plan : server.getSubscribedPlans()) {
					final BambooPlanItem bambooPlanItem = new BambooPlanItem(new BambooPlan("Unknown", plan.getKey()), true);
					model.addElement(bambooPlanItem);
					modelPlans.add(bambooPlanItem);
				}
				serverPlans.put(server.getServerId(), modelPlans);
			}
			statusPane.setText(message);
			statusPane.setCaretPosition(0);
			setVisible(true);
			//	cbUseFavouriteBuilds.setEnabled(true);
			list.setEnabled(!cbUseFavouriteBuilds.isSelected());
			isListModified = false;
			setCurrentlyPopulatedServer(server);
		}
	}

	/**
	 * This method could theoretically have race conditions with updatePlanNames names above,
	 * as they move play with model. However both methods are run only in single thread (EWT), so race conditions
	 * are not possible.
	 */
	public void saveData() {
		// additional check for getCurrentlyPopulatedServer() is used
		// to avoid fetching data from list model when it's really not yet representing
		// our current server
		if (bambooServerCfg == null || !bambooServerCfg.equals(getCurrentlyPopulatedServer())) {
			return;
		}
		// move data only when we have fetched the data - otherwise we could overwrite user data due to e.g. network problems
		if (serverPlans.containsKey(bambooServerCfg.getServerId()) == true) {
			bambooServerCfg.clearSubscribedPlans();
			for (int i = 0; i < model.getSize(); ++i) {
				if (model.getElementAt(i) instanceof BambooPlanItem) {
					BambooPlanItem p = (BambooPlanItem) model.getElementAt(i);

					if (p.isSelected()) {
						SubscribedPlan spb = new SubscribedPlan(p.getPlan().getPlanKey());
						bambooServerCfg.getSubscribedPlans().add(spb);
					}
				}
			}
		}
		bambooServerCfg.setUseFavourites(cbUseFavouriteBuilds.isSelected());
		bambooServerCfg.setTimezoneOffset(((SpinnerNumberModel) spinnerTimeZoneDifference.getModel()).getNumber().intValue());
	}

	public boolean isModified() {
		boolean isFavModified = false;
		if (bambooServerCfg != null) {
			if (cbUseFavouriteBuilds.isSelected() != bambooServerCfg.isUseFavourites()) {
				isFavModified = true;
			}
		} else {
			return false;
		}

		return isListModified || isFavModified;
	}

	@Override
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
		plansPanel = new JPanel();
		plansPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		rootComponent.add(plansPanel, gbc);
		plansPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Build Plans"));
		listPanel = new JPanel();
		listPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		listPanel.setBackground(new Color(-1));
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 12, 0, 12);
		plansPanel.add(listPanel, gbc);
		scrollList = new JScrollPane();
		scrollList.setBackground(new Color(-1));
		listPanel.add(scrollList, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		scrollList.setViewportView(list);
		toolbarPanel = new JPanel();
		toolbarPanel.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 10, 12, 8);
		plansPanel.add(toolbarPanel, gbc);
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
		plansPanel.add(statusPanel, gbc);
		final JScrollPane scrollPane1 = new JScrollPane();
		scrollPane1.setEnabled(true);
		scrollPane1.setHorizontalScrollBarPolicy(31);
		scrollPane1.setVerticalScrollBarPolicy(20);
		statusPanel.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				1, 1, new Dimension(-1, 40), null, new Dimension(-1, 40), 0, false));
		statusPane = new JEditorPane();
		statusPane.setEditable(false);
		scrollPane1.setViewportView(statusPane);
		timezonePanel = new JPanel();
		timezonePanel.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		rootComponent.add(timezonePanel, gbc);
		spinnerTimeZoneDifference = new JSpinner();
		spinnerTimeZoneDifference.setMinimumSize(new Dimension(60, 28));
		spinnerTimeZoneDifference.setPreferredSize(new Dimension(60, 28));
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 3;
		gbc.anchor = GridBagConstraints.WEST;
		timezonePanel.add(spinnerTimeZoneDifference, gbc);
		final JLabel label1 = new JLabel();
		label1.setText("Time Zone Difference:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridheight = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 12, 0, 12);
		timezonePanel.add(label1, gbc);
		final JLabel label2 = new JLabel();
		label2.setFont(new Font(label2.getFont().getName(), label2.getFont().getStyle(), 10));
		label2.setHorizontalAlignment(0);
		label2.setHorizontalTextPosition(0);
		label2.setText("This computer has a time difference of x hours");
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 12, 0, 0);
		timezonePanel.add(label2, gbc);
		final JLabel label3 = new JLabel();
		label3.setFont(new Font(label3.getFont().getName(), label3.getFont().getStyle(), 10));
		label3.setHorizontalAlignment(0);
		label3.setHorizontalTextPosition(0);
		label3.setText("from the Bamboo server. Positive number denotes");
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 12, 0, 0);
		timezonePanel.add(label3, gbc);
		final JLabel label4 = new JLabel();
		label4.setFont(new Font(label4.getFont().getName(), label4.getFont().getStyle(), 10));
		label4.setText("hours ahead, negative number denotes hours behind.");
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 3;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 12, 0, 0);
		timezonePanel.add(label4, gbc);
		bambooVersionNumberInfo = new JLabel();
		bambooVersionNumberInfo.setEnabled(false);
		bambooVersionNumberInfo.setFocusTraversalPolicyProvider(true);
		bambooVersionNumberInfo.setFocusable(false);
		bambooVersionNumberInfo.setFont(
				new Font(bambooVersionNumberInfo.getFont().getName(), bambooVersionNumberInfo.getFont().getStyle(), 10));
		bambooVersionNumberInfo.setForeground(new Color(-3407872));
		bambooVersionNumberInfo.setText("");
		bambooVersionNumberInfo.setVisible(true);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(4, 12, 4, 0);
		timezonePanel.add(bambooVersionNumberInfo, gbc);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootComponent;
	}
}
