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

import com.atlassian.connector.intellij.crucible.CrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.PathAndRevision;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.SvnRepository;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.crucible.model.ReviewKeyComparator;
import com.atlassian.theplugin.crucible.model.UpdateReason;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.idea.crucible.comboitems.RepositoryComboBoxItem;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.ui.MultipleChangeListBrowser;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static javax.swing.Action.NAME;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

enum AddMode {
	ADDREVISION,
	ADDITEMS
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
	private JPanel customComponentPanel;
	private JLabel repositoryLabel;

	private CrucibleServerFacade crucibleServerFacade;
	private ChangeList[] changes;
	private final Project project;
	private PermId permId;
	private final ProjectCfgManagerImpl projectCfgManager;
	private AddMode mode;
	private ServerData server;
	private Collection<Change> localChanges;
    private MultipleChangeListBrowser changesBrowser;

	private RepositoryComboBoxItem NON_REPO;
    private int changesetAddTimeout = -1;

    public CrucibleHelperForm(Project project, CrucibleServerFacade crucibleServerFacade,
			ChangeList[] changes, final ProjectCfgManagerImpl projectCfgManager) {
		this(project, crucibleServerFacade, projectCfgManager);
		this.changes = changes;
		this.mode = AddMode.ADDREVISION;
		setTitle("Add revision to review... ");
		getOKAction().putValue(NAME, "Add revision...");
	}

	public CrucibleHelperForm(Project project, CrucibleServerFacade crucibleServerFacade,
			Collection<Change> changes, final ProjectCfgManagerImpl projectCfgManager) {

		this(project, crucibleServerFacade, projectCfgManager);
		localChanges = changes;
        this.mode = AddMode.ADDITEMS;
		this.repositoryLabel.setVisible(false);
		this.repositoryComboBox.setVisible(false);
		ChangeListManager changeListManager = ChangeListManager.getInstance(project);
		changesBrowser = IdeaVersionFacade.getInstance().getChangesListBrowser(project, changeListManager, changes);
		setCustomComponent(changesBrowser);
		setTitle("Add files");
		getOKAction().putValue(NAME, "Add files...");
	}

    public void setChangesetAddTimeout(int changesetAddTimeout) {
        this.changesetAddTimeout = changesetAddTimeout;
    }

    private CrucibleHelperForm(Project project, CrucibleServerFacade crucibleServerFacade,
			final ProjectCfgManagerImpl projectCfgManager) {

		super(false);
		this.crucibleServerFacade = crucibleServerFacade;
		this.project = project;
		this.projectCfgManager = projectCfgManager;

		NON_REPO = new RepositoryComboBoxItem(new Repository("", "unknown", false));

		$$$setupUI$$$();
		init();

		reviewComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent event) {
				if (reviewComboBox.getSelectedItem() != null) {
					if (reviewComboBox.getSelectedItem() instanceof ReviewComboBoxItem) {
						ReviewComboBoxItem item = (ReviewComboBoxItem) reviewComboBox.getSelectedItem();
						if (item != null) {
							final ReviewAdapter review = item.getReview();
							server = review.getServerData();
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

		repositoryComboBox.setEnabled(false);
		fillReviewCombos();
	}

    private void fillServerRelatedCombos(final ReviewAdapter review) {
		repositoryComboBox.removeAllItems();
		repositoryComboBox.setEnabled(false);
		getOKAction().setEnabled(false);


		new Thread(new Runnable() {
			public void run() {
				final List<Repository> repositories = new ArrayList<Repository>();

				try {
					List<Repository> repos = crucibleServerFacade.getRepositories(review.getServerData());
					repositories.addAll(repos);
				} catch (final Exception e) {
					if (CrucibleHelperForm.this.getRootComponent().isShowing()) {
						ApplicationManager.getApplication().invokeAndWait(new Runnable() {
							public void run() {
								DialogWithDetails.showExceptionDialog(project, "Cannot retrieve data from Crucible server", e);
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
		if (selectedItem == null || !selectedItem.getReview().equals(reviewAdapter)) {
			return;
		}

		// we are doing here once more, as it's executed by a separate thread and meantime
		// the combos could have been populated by another thread
		repositoryComboBox.removeAllItems();

		if (this.mode == AddMode.ADDITEMS) {
			repositoryComboBox.addItem(NON_REPO); // repo is not required for instance for patch review
		}
		if (!repositories.isEmpty()) {
			for (Repository repo : repositories) {
				repositoryComboBox.addItem(new RepositoryComboBoxItem(repo));
			}

			if (this.mode == AddMode.ADDREVISION) {
				// setting default repo if such is defined
				if (projectCfgManager != null) {
					final String defaultRepo = projectCfgManager.getDefaultCrucibleRepo();
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
		repositoryComboBox.setEnabled(true);
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
		rootComponent.setLayout(new FormLayout("fill:d:grow",
				"center:max(d;4px):noGrow,top:3dlu:noGrow,center:d:grow,top:3dlu:noGrow,center:max(d;4px):noGrow"));
		rootComponent.setBackground(UIManager.getColor("Button.background"));
		rootComponent.setEnabled(false);
		rootComponent.setMinimumSize(new Dimension(600, 300));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new FormLayout("left:90px:noGrow,left:4dlu:noGrow,fill:300px:grow",
				"top:d:noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:d:grow"));
		CellConstraints cc = new CellConstraints();
		rootComponent.add(panel1, cc.xywh(1, 1, 1, 3));
		reviewComboBox = new JComboBox();
		panel1.add(reviewComboBox, cc.xy(3, 1));
		statusField = new JTextField();
		statusField.setBackground(UIManager.getColor("Button.background"));
		statusField.setEnabled(false);
		panel1.add(statusField, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
		idField = new JTextField();
		idField.setBackground(UIManager.getColor("Button.background"));
		idField.setEnabled(false);
		panel1.add(idField, cc.xy(3, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
		titleField = new JTextField();
		titleField.setBackground(UIManager.getColor("Button.background"));
		titleField.setEnabled(false);
		panel1.add(titleField, cc.xy(3, 7, CellConstraints.FILL, CellConstraints.DEFAULT));
		authorField = new JTextField();
		authorField.setBackground(UIManager.getColor("Button.background"));
		authorField.setEnabled(false);
		panel1.add(authorField, cc.xy(3, 9, CellConstraints.FILL, CellConstraints.DEFAULT));
		moderatorField = new JTextField();
		moderatorField.setBackground(UIManager.getColor("Button.background"));
		moderatorField.setEnabled(false);
		panel1.add(moderatorField, cc.xy(3, 11, CellConstraints.FILL, CellConstraints.DEFAULT));
		final JLabel label1 = new JLabel();
		label1.setText("State:");
		panel1.add(label1, cc.xy(1, 3));
		final JLabel label2 = new JLabel();
		label2.setText("Id:");
		panel1.add(label2, cc.xy(1, 5));
		final JLabel label3 = new JLabel();
		label3.setText("Title:");
		panel1.add(label3, cc.xy(1, 7));
		final JLabel label4 = new JLabel();
		label4.setText("Author:");
		panel1.add(label4, cc.xy(1, 9));
		final JLabel label5 = new JLabel();
		label5.setText("Moderator:");
		panel1.add(label5, cc.xy(1, 11));
		repositoryComboBox = new JComboBox();
		panel1.add(repositoryComboBox, cc.xy(3, 13));
		repositoryLabel = new JLabel();
		repositoryLabel.setText("Repository:");
		panel1.add(repositoryLabel, cc.xy(1, 13));
		final JScrollPane scrollPane1 = new JScrollPane();
		panel1.add(scrollPane1, cc.xy(3, 15, CellConstraints.FILL, CellConstraints.FILL));
		descriptionArea = new JTextArea();
		descriptionArea.setBackground(UIManager.getColor("Button.background"));
		descriptionArea.setEditable(false);
		descriptionArea.setEnabled(false);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		scrollPane1.setViewportView(descriptionArea);
		final JLabel label6 = new JLabel();
		label6.setText("<html>Statement <br>of Objectives:</html>");
		panel1.add(label6, cc.xy(1, 15, CellConstraints.DEFAULT, CellConstraints.TOP));
		final JLabel label7 = new JLabel();
		label7.setText("Review:");
		panel1.add(label7, cc.xy(1, 1, CellConstraints.DEFAULT, CellConstraints.CENTER));
		customComponentPanel = new JPanel();
		customComponentPanel.setLayout(new BorderLayout(0, 0));
		rootComponent.add(customComponentPanel, cc.xy(1, 5));
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
			return review.getPermId().getId() + " - " + review.getName();
		}

		public ReviewAdapter getReview() {
			return review;
		}
	}


	private void fillReviewCombos() {
		reviewComboBox.removeAllItems();
		getOKAction().setEnabled(false);

		new Thread(new Runnable() {
			public void run() {
				List<ReviewAdapter> drafts = MiscUtil.buildArrayList();
				List<ReviewAdapter> outForReview = MiscUtil.buildArrayList();
				List<ReviewAdapter> toSummarize = MiscUtil.buildArrayList();

				Collection<ServerData> servers = projectCfgManager.getAllEnabledCrucibleServerss();
				for (ServerData serverData : servers) {
					try {
						drafts.addAll(crucibleServerFacade.getReviewsForFilter(serverData, PredefinedFilter.Drafts));
						outForReview.addAll(
								crucibleServerFacade.getReviewsForFilter(serverData, PredefinedFilter.OutForReview));
						toSummarize.addAll(crucibleServerFacade.getReviewsForFilter(serverData, PredefinedFilter.ToSummarize));
					} catch (RemoteApiException e) {
						// nothing can be done here
					} catch (ServerPasswordNotProvidedException e) {
						// nothing can be done here
					}
				}
				final List<ReviewAdapter> reviews = MiscUtil
						.buildArrayList(drafts.size() + outForReview.size() + toSummarize.size());
				reviews.addAll(drafts);
				reviews.addAll(outForReview);
				reviews.addAll(toSummarize);

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
			Set<ReviewAdapterComparable> sorted = new TreeSet<ReviewAdapterComparable>();
			for (ReviewAdapter review : reviews) {
				sorted.add(new ReviewAdapterComparable(review));
			}

			for (ReviewAdapter review : sorted) {
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

	protected void setCustomComponent(JComponent component) {
		customComponentPanel.removeAll();
		if (component != null) {
			customComponentPanel.add(component);
			customComponentPanel.validate();
		}
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
                "Adding " + (mode == AddMode.ADDREVISION ? "change set" : "patch") + " to review...", true) {
            @Override
            public void run(@NotNull final ProgressIndicator indicator) {
                try {
                    switch (mode) {
                        case ADDREVISION:
                            SvnRepository svnRepo = crucibleServerFacade.getRepository(server, repoName);
                            if (svnRepo == null) {
                                throw new IllegalArgumentException("Selected repository is not an SVN repository");
                            }

                            final Set<String> revisions = new HashSet<String>();
                            final List<PathAndRevision> pathsAndRevisions = new ArrayList<PathAndRevision>();
                            for (ChangeList changeList : changes) {
                                ContentRevision contentRevision = null;

                                List<String> revList = new ArrayList<String>();
                                for (Change change : changeList.getChanges()) {
                                    revList.add(change.getBeforeRevision().getRevisionNumber().asString());
                                    revList.add(change.getAfterRevision().getRevisionNumber().asString());

                                    if (change.getType() == Change.Type.DELETED) {
                                        contentRevision = change.getBeforeRevision();
                                    } else {
                                        contentRevision = change.getAfterRevision();
                                    }
                                    break;
                                }
                                if (contentRevision != null) {
                                    String path = getPathFromRevision(contentRevision);
                                    if (path != null) {
//                                    if (contentRevision instanceof SvnRepositoryContentRevision) {
//                                        String path = ((SvnRepositoryContentRevision) contentRevision).getPath();
                                        String svnRepoPath = svnRepo.getPath();
                                        if (!path.startsWith("/" + svnRepoPath + "/")) {
                                            throw new IllegalArgumentException(
                                                    "Selected files do not seem to be\n"
                                                    + "present in the selected Crucible repository");
                                        }
                                        path = path.substring(svnRepoPath.length() + 2);

                                        pathsAndRevisions.add(new PathAndRevision(path, revList));
                                    } else {
                                        revisions.add(contentRevision.getRevisionNumber().asString());
                                    }
                                }
                            }

                            ReviewAdapter newReview = null;
                            Date startDate = new Date();
                            boolean isCancelled = false;
                            do {
                                try {
                                    indicator.setText("Adding change set to review...");

                                    if (pathsAndRevisions.size() > 0) {
                                        newReview = crucibleServerFacade.addFileVersionsToReview(
                                                server, permId, repoName, pathsAndRevisions);
                                    } else if (revisions.size() > 0) {
                                        newReview = crucibleServerFacade.addRevisionsToReview(
                                                server, permId, repoName, new ArrayList<String>(revisions));
                                    }
                                } catch (Exception e) {
                                    if (isUnknownChangeSetException(e, repoName)) {
                                        try {
                                            Date now = new Date();
                                            if (changesetAddTimeout > 0
                                                    && now.getTime() - startDate.getTime() >
                                                    changesetAddTimeout * CrucibleReviewCreateForm.MILLISECONDS_IN_MINUTE) {
                                                SwingUtilities.invokeLater(new Runnable() {
                                                    public void run() {
                                                        Messages.showErrorDialog(project,
                                                                "Adding change set to review " + permId
                                                                        + " on server " + server.getName()
                                                                        + "\ntimed out after "
                                                                        + changesetAddTimeout + " minutes",
                                                                "Adding Change Set to Review Timeout");
                                                    }
                                                });
                                                break;
                                            }
                                            indicator.setText("Waiting for Crucible to update to newest change set...");
                                            for (int i = 0; i < 10; ++i) {
                                                if (indicator.isCanceled()) {
                                                    isCancelled = true;
                                                    break;
                                                }
                                                Thread.sleep(1000);
                                            }
                                        } catch (InterruptedException ex) {
                                            // eeeem, now what?
                                            LoggerImpl.getInstance().error(ex);
                                        }
                                    }
                                }
                            } while (newReview == null || isCancelled);

                            final ReviewAdapter newReviewFinal = newReview;
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    final ReviewListToolWindowPanel panel = IdeaHelper.getReviewListToolWindowPanel(project);
                                    if (panel != null && newReviewFinal != null) {
                                        panel.refresh(UpdateReason.REFRESH);
                                        panel.openReview(newReviewFinal, true);
                                    }
                                }
                            });
                            break;
                        case ADDITEMS:
                            Collection<UploadItem> uploadItems = CrucibleHelper
                                    .getUploadItemsFromChanges(project, localChanges);
                            crucibleServerFacade.addItemsToReview(server, permId, uploadItems);
                            break;
                    }
                } catch (final Throwable e) {
                    ApplicationManager.getApplication().invokeAndWait(new Runnable() {
                        public void run() {
                            DialogWithDetails.showExceptionDialog(project,
                                    "Cannot add revision to review. Check selected repository.", e);
                        }
                    }, ModalityState.stateForComponent(CrucibleHelperForm.this.getRootComponent()));
                }
            }

            /**
             * This method exists because I am a dumbass for deploying wrong svn4idea jar to maven repo
             * thus breaking bamboo builds :(
             *
             * @param contentRevision - revision to get path of
             * @return file path
             */
            private String getPathFromRevision(ContentRevision contentRevision) {
                try {
                    Method method = contentRevision.getClass().getMethod("getPath");
                    return method.invoke(contentRevision).toString();
                } catch (Exception e) {
                    LoggerImpl.getInstance().error(e);
                    return null;
                }
            }
        };
        ProgressManager.getInstance().run(changesTask);
        
        super.doOKAction();
    }

    private boolean isUnknownChangeSetException(Throwable e, String repoName) {
        return e != null
                && e.getMessage() != null
                && e.getMessage().contains("does not exist in source " + repoName);
    }

	private void createUIComponents() {
	}
}

class ReviewAdapterComparable extends ReviewAdapter implements Comparable {
	ReviewAdapterComparable(final ReviewAdapter review) {
		super(review);
	}

	public int compareTo(final Object o) {
		if (!(o instanceof ReviewAdapterComparable)) {
			return 0;
		}
		ReviewKeyComparator c = new ReviewKeyComparator();
		return c.compare(this, (ReviewAdapter) o);
	}
}