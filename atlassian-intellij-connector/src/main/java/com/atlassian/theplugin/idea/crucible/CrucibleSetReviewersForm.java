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

package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.crucible.model.UpdateReason;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static java.lang.System.arraycopy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CrucibleSetReviewersForm extends DialogWrapper {
	private JPanel rootComponent;
	private JList reviewersList;
	private DefaultListModel model;

	private Project project;
	private CrucibleServerFacade crucibleServerFacade;
	private ReviewAdapter reviewData;
	private List<Reviewer> actualReviewers;


	public CrucibleSetReviewersForm(Project project, CrucibleServerFacade crucibleServerFacade, ReviewAdapter reviewData) {
		super(false);
		this.project = project;
		this.crucibleServerFacade = crucibleServerFacade;
		this.reviewData = reviewData;
		$$$setupUI$$$();
		init();
		getOKAction().putValue(Action.NAME, "Set reviewers...");

		reviewersList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int index = reviewersList.locationToIndex(e.getPoint());
				setCheckboxState(index);
			}
		});

		reviewersList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					int index = reviewersList.getSelectedIndex();
					setCheckboxState(index);
				}
			}
		});

		fillServerRelatedCombos(reviewData.getServerData());
		pack();
	}

	private void setCheckboxState(int index) {
		if (index != -1) {
			UserListItem pi = (UserListItem) reviewersList.getModel().getElementAt(index);
			pi.setSelected(!pi.isSelected());
			setViewState(index, pi.isSelected());
			repaint();
		}
	}

	private void setViewState(int index, boolean newState) {
		int[] oldIdx = reviewersList.getSelectedIndices();
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
		reviewersList.setSelectedIndices(newIdx);
	}

	private void fillServerRelatedCombos(final ServerData server) {
		model.removeAllElements();
		getOKAction().setEnabled(false);

		new Thread(new Runnable() {
			public void run() {
				List<User> users = new ArrayList<User>();
				List<Reviewer> reviewers = new ArrayList<Reviewer>();

				try {
					users = crucibleServerFacade.getUsers(server);
					reviewers = crucibleServerFacade.getReviewers(server, reviewData.getPermId());
				} catch (RemoteApiException e) {
					// nothing can be done here
				} catch (ServerPasswordNotProvidedException e) {
					// nothing can be done here
				}
				final List<User> finalUsers = users;
				final List<Reviewer> finalReviewers = reviewers;
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						updateServerRelatedData(server, finalUsers, finalReviewers);
					}
				});
			}
		}, "atlassian-idea-plugin crucible patch upload combos refresh").start();
	}

	private void updateServerRelatedData(ServerData server, List<User> users, List<Reviewer> reviewers) {
		actualReviewers = reviewers;
		if (!users.isEmpty()) {
			for (User user : users) {
				if (!user.getUserName().equals(server.getUserName())
						&& !user.getUserName().equals(reviewData.getAuthor().getUserName())
                        && !user.getUserName().equals(reviewData.getModerator().getUserName())) {
					boolean rev = false;
					for (Reviewer reviewer : reviewers) {
						if (reviewer.getUserName().equals(user.getUserName())) {
							rev = true;
						}
					}
					model.addElement(new UserListItem(user, rev));
				}
			}
		}
		getOKAction().setEnabled(true);
	}


	@Override
	public JComponent getPreferredFocusedComponent() {
		return reviewersList;
	}

	public JComponent getRootComponent() {
		return rootComponent;
	}

	@Override
	@Nullable
	protected JComponent createCenterPanel() {
		return getRootComponent();
	}

	@Override
	protected void doOKAction() {

		Set<String> reviewersForAdd = new HashSet<String>();
		Set<String> reviewersForRemove = new HashSet<String>();

		for (int i = 0; i < model.getSize(); ++i) {
			UserListItem item = (UserListItem) model.get(i);
			String username = item.getUser().getUserName();
			boolean found = false;
			if (item.isSelected()) {
				for (Reviewer actualReviewer : actualReviewers) {
					if (username.equals(actualReviewer.getUserName())) {
						found = true;
						break;
					}
				}
				if (!found) {
					reviewersForAdd.add(username);
				}
			} else {
				for (Reviewer actualReviewer : actualReviewers) {
					if (username.equals(actualReviewer.getUserName())) {
						found = true;
						break;
					}
				}
				if (found) {
					reviewersForRemove.add(username);
				}
			}
		}
		try {
			if (!reviewersForAdd.isEmpty()) {
				crucibleServerFacade.addReviewers(reviewData.getServerData(), reviewData.getPermId(), reviewersForAdd);
			}
			if (!reviewersForRemove.isEmpty()) {
				for (String reviewer : reviewersForRemove) {
					crucibleServerFacade.removeReviewer(reviewData.getServerData(), reviewData.getPermId(), reviewer);
				}
			}
            // this sucks a bit, because efreshing one review should not equire refreshing all reviews.
            // Posted PL-1506 to fix this
            if (!(reviewersForAdd.isEmpty() && reviewersForRemove.isEmpty())) {
                final ReviewsToolWindowPanel panel = IdeaHelper.getReviewsToolWindowPanel(project);
                if (panel != null) {
                    panel.refresh(UpdateReason.REFRESH);
                }
            }
		} catch (RemoteApiException e) {
			DialogWithDetails.showExceptionDialog(project,
					e.getMessage() + "Error creating review: " + reviewData.getServerData().getUrl(), e);
//			Messages.showErrorDialog(project, e.getMessage() + "Error creating review: " + reviewData.getServer().getUrl(), "");
		} catch (ServerPasswordNotProvidedException e) {
			Messages.showErrorDialog(project, e.getMessage() + "Error creating review: "
					+ reviewData.getServerData().getUrl(), "");
		}


		super.doOKAction();

	}


	private void createUIComponents() {
		model = new DefaultListModel();
		reviewersList = new JList(model);
		reviewersList.setCellRenderer(new UserListCellRenderer());
		reviewersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		reviewersList.setVisibleRowCount(15);
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
		rootComponent.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.setMinimumSize(new Dimension(0, 0));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JScrollPane scrollPane1 = new JScrollPane();
		panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1,
				null, null, null, 0, false));
		scrollPane1.setViewportView(reviewersList);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootComponent;
	}
}
  
