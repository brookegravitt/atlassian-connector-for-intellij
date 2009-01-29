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

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.idea.crucible.comboitems.RepositoryComboBoxItem;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

enum AddMode {
	ADDREVISION,
	ADDPATCH
}

public class CrucibleHelperForm extends DialogWrapper {
	private JPanel rootComponent;
	private JComboBox reviewComboBox;
	private JTextField idField;
	private JTextField titleField;
	private JTextField authorField;
	private JTextField moderatorField;
	private JTextArea descriptionArea;
	private JTextField statusField;
	private JComboBox repositoryComboBox;

	private CrucibleServerFacade crucibleServerFacade;
	private ChangeList[] changes;
	private final Project project;
	private PermId permId;
	private String patch;
	private final CfgManager cfgManager;
	private AddMode mode;
	private CrucibleServerCfg server;

	public CrucibleHelperForm(Project project, CrucibleServerFacade crucibleServerFacade,
			ChangeList[] changes, final CfgManager cfgManager) {
		this(project, crucibleServerFacade, cfgManager);
		this.changes = changes;
		this.mode = AddMode.ADDREVISION;
		setTitle("Add revision to review... ");
		getOKAction().putValue(Action.NAME, "Add revision...");
	}

	public CrucibleHelperForm(Project project, CrucibleServerFacade crucibleServerFacade,
			String patch, final CfgManager cfgManager) {
		this(project, crucibleServerFacade, cfgManager);
		this.patch = patch;
		this.mode = AddMode.ADDPATCH;
		setTitle("Add patch");
		getOKAction().putValue(Action.NAME, "Add patch...");
	}

	private CrucibleHelperForm(Project project, CrucibleServerFacade crucibleServerFacade,
			final CfgManager cfgManager) {
		super(false);
		this.crucibleServerFacade = crucibleServerFacade;
		this.project = project;
		this.cfgManager = cfgManager;
		$$$setupUI$$$();
		init();

		reviewComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent event) {
				if (reviewComboBox.getSelectedItem() != null) {
					if (reviewComboBox.getSelectedItem() instanceof ReviewComboBoxItem) {
						ReviewComboBoxItem item = (ReviewComboBoxItem) reviewComboBox.getSelectedItem();
						if (item != null) {
							final ReviewAdapter review = item.getReview();
							server = review.getServer();
							permId = review.getPermId();
							idField.setText(review.getPermId().getId());
							statusField.setText(review.getState().value());
							titleField.setText(review.getName());
							authorField.setText(review.getAuthor().getDisplayName());
							moderatorField.setText(review.getModerator().getDisplayName());
							descriptionArea.setText(review.getDescription());

							fillServerRelatedCombos(review);

							getOKAction().setEnabled(true);
						} else {
							getOKAction().setEnabled(false);
						}
					} else {
						getOKAction().setEnabled(false);
					}
				} else {
					getOKAction().setEnabled(false);
				}
			}
		});

		fillReviewCombos();
	}

	private void fillServerRelatedCombos(final ReviewAdapter review) {
		repositoryComboBox.removeAllItems();
		getOKAction().setEnabled(false);


		new Thread(new Runnable() {
			public void run() {
				final List<Repository> repositories = new ArrayList<Repository>();

				try {
					List<Repository> repos = crucibleServerFacade.getRepositories(review.getServer());
					repositories.addAll(repos);
				} catch (final Exception e) {
					if (CrucibleHelperForm.this.getRootComponent().isShowing()) {
						ApplicationManager.getApplication().invokeAndWait(new Runnable() {
							public void run() {
								DialogWithDetails.showExceptionDialog(project, "Cannot retrieve data from Crucible server",
										e, "Error");
							}
						}, ModalityState.stateForComponent(CrucibleHelperForm.this.getRootComponent()));
					}
				}
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						updateServerRelatedCombos(review, repositories);
					}
				});
			}
		}, "atlassian-idea-plugin crucible patch upload combos refresh").start();
	}

	private void updateServerRelatedCombos(final ReviewAdapter reviewAdapter, final List<Repository> repositories) {

		final ReviewComboBoxItem selectedItem = (ReviewComboBoxItem) reviewComboBox.getSelectedItem();
		if (selectedItem == null || selectedItem.getReview().equals(reviewAdapter) == false) {
			return;
		}

		// we are doing here once more, as it's executed by a separate thread and meantime
		// the combos could have been populated by another thread
		repositoryComboBox.removeAllItems();

		if (this.mode == AddMode.ADDPATCH) {
			repositoryComboBox.addItem(""); // repo is not required for instance for patch review
		}
		if (!repositories.isEmpty()) {
			for (Repository repo : repositories) {
				repositoryComboBox.addItem(new RepositoryComboBoxItem(repo));
			}

			if (this.mode == AddMode.ADDREVISION) {
				ProjectConfiguration prjCfg = cfgManager.getProjectConfiguration(CfgUtil.getProjectId(project));
				// setting default repo if such is defined
				if (prjCfg != null) {
					final String defaultRepo = prjCfg.getDefaultCrucibleRepo();
					if (defaultRepo != null) {
						for (int i = 0; i < repositoryComboBox.getItemCount(); ++i) {
							if (repositoryComboBox.getItemAt(i) instanceof RepositoryComboBoxItem) {
								if (((RepositoryComboBoxItem) repositoryComboBox.getItemAt(i)).getRepository().getName()
										.equals(defaultRepo)) {
									repositoryComboBox.setSelectedIndex(i);
									break;
								}
							}
						}
					}
				}
			}
		}
		getOKAction().setEnabled(true);
	}

	@Override
	public JComponent getPreferredFocusedComponent() {
		return this.reviewComboBox;
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		rootComponent = new JPanel();
		rootComponent.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.setBackground(UIManager.getColor("Button.background"));
		rootComponent.setEnabled(false);
		rootComponent.setMinimumSize(new Dimension(600, 300));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 2, new Insets(1, 1, 1, 1), -1, -1));
		rootComponent.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		reviewComboBox = new JComboBox();
		panel1.add(reviewComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("Review");
		panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		rootComponent.add(spacer1,
				new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
						GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(7, 2, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Id");
		panel2.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setText("Title");
		panel2.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label4 = new JLabel();
		label4.setText("Author");
		panel2.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label5 = new JLabel();
		label5.setText("Moderator");
		panel2.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label6 = new JLabel();
		label6.setText("Statement of Objectives");
		panel2.add(label6, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		idField = new JTextField();
		idField.setBackground(UIManager.getColor("Button.background"));
		idField.setEnabled(false);
		panel2.add(idField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
				false));
		titleField = new JTextField();
		titleField.setBackground(UIManager.getColor("Button.background"));
		titleField.setEnabled(false);
		panel2.add(titleField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
				false));
		authorField = new JTextField();
		authorField.setBackground(UIManager.getColor("Button.background"));
		authorField.setEnabled(false);
		panel2.add(authorField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
				false));
		moderatorField = new JTextField();
		moderatorField.setBackground(UIManager.getColor("Button.background"));
		moderatorField.setEnabled(false);
		panel2.add(moderatorField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
				false));
		descriptionArea = new JTextArea();
		descriptionArea.setBackground(UIManager.getColor("Button.background"));
		descriptionArea.setEnabled(false);
		panel2.add(descriptionArea, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null,
				0, false));
		statusField = new JTextField();
		statusField.setBackground(UIManager.getColor("Button.background"));
		statusField.setEnabled(false);
		panel2.add(statusField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
				false));
		final JLabel label7 = new JLabel();
		label7.setText("State");
		panel2.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		repositoryComboBox = new JComboBox();
		panel2.add(repositoryComboBox,
				new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
						GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label8 = new JLabel();
		label8.setText("Repository");
		panel2.add(label8, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootComponent;
	}

	private static final class ReviewComboBoxItem {
		private final ReviewAdapter review;

		private ReviewComboBoxItem(ReviewAdapter review) {
			this.review = review;
		}

		@Override
		public String toString() {
			return review.getPermId().getId();
		}

		public ReviewAdapter getReview() {
			return review;
		}
	}


	private void addToReviewAdapterList(final List<ReviewAdapter> target, final Collection<Review> source,
			final CrucibleServerCfg aServer) {

		for (Review review : source) {
			target.add(new ReviewAdapter(review, aServer));
		}
	}

	private void fillReviewCombos() {
		reviewComboBox.removeAllItems();
		getOKAction().setEnabled(false);

		new Thread(new Runnable() {
			public void run() {
				List<ReviewAdapter> drafts = MiscUtil.buildArrayList();
				List<ReviewAdapter> outForReview = MiscUtil.buildArrayList();

				Collection<CrucibleServerCfg> servers = cfgManager.getAllEnabledCrucibleServers(CfgUtil.getProjectId(project));
				for (CrucibleServerCfg server : servers) {
					try {
						addToReviewAdapterList(drafts,
								crucibleServerFacade.getReviewsForFilter(server, PredefinedFilter.Drafts), server);
						addToReviewAdapterList(outForReview,
								crucibleServerFacade.getReviewsForFilter(server, PredefinedFilter.OutForReview), server);
					}
					catch (RemoteApiException e) {
						// nothing can be done here
					}
					catch (ServerPasswordNotProvidedException e) {
						// nothing can be done here
					}
				}
				final List<ReviewAdapter> reviews = MiscUtil.buildArrayList(drafts.size() + outForReview.size());
				reviews.addAll(drafts);
				reviews.addAll(outForReview);


				EventQueue.invokeLater(new Runnable() {
					public void run() {
						updateReviewCombo(reviews);
					}
				});
			}
		}, "atlassian-idea-plugin crucible patch upload combos refresh").start();
	}

	private void updateReviewCombo(List<ReviewAdapter> reviews) {
		reviewComboBox.addItem("");
		if (!reviews.isEmpty()) {
			for (ReviewAdapter review : reviews) {
				reviewComboBox.addItem(new ReviewComboBoxItem(review));
			}
		}
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
		final String repoName;
		if (repositoryComboBox.getSelectedItem() instanceof RepositoryComboBoxItem) {
			repoName = ((RepositoryComboBoxItem) repositoryComboBox.getSelectedItem()).getRepository().getName();
		} else {
			repoName = "";
		}

		Task.Backgroundable changesTask = new Task.Backgroundable(project,
				"Adding " + (mode == AddMode.ADDREVISION ? "changeset" : "patch") + " to review...", false) {
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				try {
					switch (mode) {
						case ADDREVISION:
							final List<String> revisions = new ArrayList<String>();
							for (ChangeList change : changes) {
								for (Change change1 : change.getChanges()) {
									revisions.add(change1.getAfterRevision().getRevisionNumber().asString());
								}
							}
							crucibleServerFacade.addRevisionsToReview(server, permId, repoName, revisions);
							break;
						case ADDPATCH:
							crucibleServerFacade.addPatchToReview(server, permId, repoName, patch);
							break;
					}
				} catch (final Throwable e) {
					ApplicationManager.getApplication().invokeAndWait(new Runnable() {
						public void run() {
							DialogWithDetails
									.showExceptionDialog(project, "Cannot retrieve data from Crucible server",
											e, "Error");
						}
					}, ModalityState.stateForComponent(CrucibleHelperForm.this.getRootComponent()));
				}
			}
		};
		ProgressManager.getInstance().run(changesTask);
		super.doOKAction();
	}

	private void createUIComponents() {
	}
}