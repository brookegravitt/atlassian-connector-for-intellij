package com.atlassian.theplugin.idea.crucible.tree;
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

import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.NodeSearchAlgorithm;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleChangeSetTitleNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import com.atlassian.theplugin.idea.ui.tree.file.FileTreeModelBuilder;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class ReviewItemTreePanel extends JPanel {


	//	ProjectView.
	private AtlassianTreeWithToolbar reviewFilesTree = null;
	private CrucibleReviewActionListener listener;
	private static final int WIDTH = 150;
	private static final int HEIGHT = 250;

	public static final Logger LOGGER = PluginUtil.getLogger();

	private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
	private JLabel statusLabel;

    public ReviewItemTreePanel(final Project project) {
        initLayout();
        listener = new MyReviewActionListener(project);
        IdeaHelper.getReviewActionEventBroker().registerListener(listener);
    }

	private void initLayout() {
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		add(getReviewItemTree(), BorderLayout.CENTER);
		statusLabel = new JLabel();
		statusLabel.setBackground(UIUtil.getTreeTextBackground());
		add(statusLabel, BorderLayout.SOUTH);
	}

	public JPanel getReviewItemTree() {
		if (reviewFilesTree == null) {
			reviewFilesTree = new AtlassianTreeWithToolbar("ThePlugin.Crucible.ReviewFileListToolBar");
			reviewFilesTree.setRootVisible(false);
		}
		return reviewFilesTree;
	}

	public void setEnabled(boolean b) {
		super.setEnabled(b);
		getReviewItemTree().setEnabled(b);
	}


	public ProgressAnimationProvider getProgressAnimation() {
		return progressAnimation;
	}

	private final class MyReviewActionListener extends CrucibleReviewActionListener {
		private Project project;

		private MyReviewActionListener(Project project) {
			this.project = project;
		}

		@Override
		public void focusOnFile(final ReviewData review, final CrucibleFileInfo file) {
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				public void run() {
					AtlassianTreeNode node = reviewFilesTree.getModel().locateNode(new NodeSearchAlgorithm() {
						public boolean check(final AtlassianTreeNode node) {
							if (node instanceof CrucibleFileNode) {
								CrucibleFileNode anode = (CrucibleFileNode) node;
								if (anode.getReview().equals(review) && anode.getFile().equals(file)) {
									return true;
								}
							}
							return false;
						}
					});
					reviewFilesTree.focusOnNode(node);
				}
			});

		}

		@Override
		public void focusOnVersionedCommentEvent(final ReviewData review, final CrucibleFileInfo file,
				final VersionedComment comment) {
			focusOnFile(review, file);
		}

		@Override
		public void focusOnLineCommentEvent(final ReviewData review, final CrucibleFileInfo file, final VersionedComment comment) {
			focusOnFile(review, file);
		}

		@Override
		public void focusOnReview(final ReviewData review) {
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				public void run() {
					AtlassianTreeNode node = reviewFilesTree.getModel().locateNode(new NodeSearchAlgorithm() {
						public boolean check(final AtlassianTreeNode node) {
							if (node instanceof CrucibleChangeSetTitleNode) {
								CrucibleChangeSetTitleNode anode = (CrucibleChangeSetTitleNode) node;
								if (anode.getReview().equals(review)) {
									return true;
								}
							}
							return false;
						}
					});
					reviewFilesTree.focusOnNode(node);
				}
			});
		}


		public void showReview(final ReviewData reviewItem) {
			List<CrucibleFileInfo> files;
			try {
				files = reviewItem.getFiles();
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				try {
					files = CrucibleServerFacadeImpl.getInstance().getFiles(reviewItem.getServer(), reviewItem.getPermId());
				} catch (RemoteApiException e) {
					IdeaHelper.handleRemoteApiException(project, e);
					return;
				} catch (ServerPasswordNotProvidedException e) {
					IdeaHelper.handleMissingPassword(e);
					return;
				}
			}
			final List<CrucibleFileInfo> files1 = files;
			EventQueue.invokeLater(new Runnable() {
				public void run() {

					statusLabel.setText(createGeneralInfoText(reviewItem));
					reviewFilesTree.setModelProvider(new ModelProvider() {
						private AtlassianTreeModel diredModel =
								FileTreeModelBuilder.buildTreeModelFromCrucibleChangeSet(reviewItem, files1);
						private AtlassianTreeModel flatModel =
								FileTreeModelBuilder.buildFlatModelFromCrucibleChangeSet(reviewItem, files1);

						public AtlassianTreeModel getModel(final AtlassianTreeWithToolbar.STATE state) {
							switch (state) {
								case DIRED:
									return diredModel;
								case FLAT:
									return flatModel;
								default:
									throw new IllegalStateException("Unknown model requested");
							}
						}
					});
					reviewFilesTree.setRootVisible(true);
				}
			});
		}

		private String createGeneralInfoText(final ReviewData reviewItem) {
			final StringBuilder buffer = new StringBuilder();
			buffer.append("<html>");
			buffer.append("<body>");
			buffer.append(reviewItem.getCreator().getDisplayName());
			buffer.append(" ");
			buffer.append("<font size=-1 color=");
			buffer.append(CrucibleConstants.CRUCIBLE_AUTH_COLOR);
			buffer.append(">AUTH</font>");
			buffer.append(" ");
			if (!reviewItem.getCreator().equals(reviewItem.getModerator())) {
				buffer.append(reviewItem.getModerator().getDisplayName());
			}
			buffer.append(" ");
			buffer.append("<font size=-1 color=");
			buffer.append(CrucibleConstants.CRUCIBLE_MOD_COLOR);
			buffer.append(">MOD</font>");
			int i = 0;
			List<Reviewer> reviewers = null;
			try {
				reviewers = reviewItem.getReviewers();
				if (reviewers != null) {
					buffer.append("<br>");
					for (Reviewer reviewer : reviewers) {
						if (i > 0) {
							buffer.append(", ");
						}
						buffer.append(reviewer.getDisplayName());
						i++;
					}
				}
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				//ignore
			}
			buffer.append("</body>");
			buffer.append("</html>");

			return buffer.toString();
		}

	}
}
