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
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.CrucibleFilteredModelProvider;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.events.ReviewCommentsDownloadadEvent;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.NodeSearchAlgorithm;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleChangeSetTitleNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import com.atlassian.theplugin.idea.ui.tree.file.FileTreeModelBuilder;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public final class ReviewItemTreePanel extends JPanel implements DataProvider {

	//	ProjectView.
	private AtlassianTreeWithToolbar reviewFilesTree = null;
	private static final int WIDTH = 150;
	private static final int HEIGHT = 250;

	public static final Logger LOGGER = PluginUtil.getLogger();

	private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
	private JLabel statusLabel;
	private CrucibleFilteredModelProvider.Filter filter;

	public ReviewItemTreePanel(final Project project, final CrucibleFilteredModelProvider.Filter filter) {
		initLayout();
		final CrucibleReviewActionListener listener = new MyReviewActionListener(project);
		IdeaHelper.getReviewActionEventBroker(project).registerListener(listener);
		this.filter = filter;
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
			reviewFilesTree.getTreeComponent().addMouseListener(new PopupAwareMouseAdapter() {

				@Override
				protected void onPopup(final MouseEvent e) {
					if (e.getComponent() instanceof AtlassianTree == false) {
						return;
					}
					AtlassianTree tree = (AtlassianTree) e.getComponent();
					TreePath path = tree.getPathForLocation(e.getX(), e.getY());
					if (path == null) {
						return;
					}
					tree.setSelectionPath(path);
					Object o = path.getLastPathComponent();
					if (!(o instanceof CrucibleFileNode)) {
						return;
					}
					ActionManager aManager = ActionManager.getInstance();
					ActionGroup menu = (ActionGroup) aManager.getAction("ThePlugin.Crucible.ReviewFileListPopupMenu");
					if (menu == null) {
						return;
					}
					aManager.createActionPopupMenu("ThePlugin.Crucible.ReviewFileListToolBar-place", menu).getComponent()
							.show(e.getComponent(), e.getX(), e.getY());

				}
			});
		}
		return reviewFilesTree;
	}

	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		getReviewItemTree().setEnabled(b);
	}


	public ProgressAnimationProvider getProgressAnimation() {
		return progressAnimation;
	}

	public void filterTreeNodes(CrucibleFilteredModelProvider.Filter aFilter) {
		this.filter = aFilter;
		((CrucibleFilteredModelProvider) reviewFilesTree.getModelProvider()).setType(aFilter);
		reviewFilesTree.triggerModelUpdated();
		reviewFilesTree.revalidate();
		reviewFilesTree.repaint();
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
						@Override
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
		public void focusOnLineCommentEvent(final ReviewData review, final CrucibleFileInfo file,
				final VersionedComment comment, final boolean openIfClosed) {
			focusOnFile(review, file);
		}

		@Override
		public void focusOnReview(final ReviewData review) {
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				public void run() {
					AtlassianTreeNode node = reviewFilesTree.getModel().locateNode(new NodeSearchAlgorithm() {
						@Override
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

		@Override
		public void showReview(final ReviewData reviewItem) {
			List<CrucibleFileInfo> files;
			try {
				List<VersionedComment> comments;
				comments = CrucibleServerFacadeImpl.getInstance().getVersionedComments(
						reviewItem.getServer(), reviewItem.getPermId());
				files = CrucibleServerFacadeImpl.getInstance().getFiles(reviewItem.getServer(), reviewItem.getPermId());
				((ReviewBean) reviewItem.getInnerReviewObject()).setFiles(files);
				for (VersionedComment comment : comments) {
					for (CrucibleFileInfo f : files) {
						if (f.getPermId().equals(comment.getReviewItemId())) {
							((CrucibleFileInfoImpl) f).addVersionedComment(comment);
						}
					}
				}
				IdeaHelper.getReviewActionEventBroker(project).trigger(
						new ReviewCommentsDownloadadEvent(this, reviewItem));
			} catch (RemoteApiException e) {
				IdeaHelper.handleRemoteApiException(project, e);
				return;
			} catch (ServerPasswordNotProvidedException e) {
				IdeaHelper.handleMissingPassword(e);
				return;
			}
			
			final List<CrucibleFileInfo> files1 = files;
			EventQueue.invokeLater(new MyRunnable(reviewItem, files1));
		}


		private String createGeneralInfoText(final ReviewData reviewItem) {
			final StringBuilder buffer = new StringBuilder();
			buffer.append("<html>");
			buffer.append("<body>");
			buffer.append(reviewItem.getAuthor().getDisplayName());
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
			List<Reviewer> reviewers;
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

		private class MyRunnable implements Runnable {
			private final ReviewData reviewItem;
			private final List<CrucibleFileInfo> files1;

			public MyRunnable(final ReviewData reviewItem, final List<CrucibleFileInfo> files1) {
				this.reviewItem = reviewItem;
				this.files1 = files1;
			}

			public void run() {

				statusLabel.setText(createGeneralInfoText(reviewItem));
				ModelProvider modelProvider = new ModelProvider() {

					@Override
					public AtlassianTreeModel getModel(final AtlassianTreeWithToolbar.State state) {
						switch (state) {
							case DIRED:
								return FileTreeModelBuilder.buildTreeModelFromCrucibleChangeSet(project, reviewItem, files1);
							case FLAT:
								return FileTreeModelBuilder.buildFlatModelFromCrucibleChangeSet(project, reviewItem, files1);
							default:
								throw new IllegalStateException("Unknown model requested");
						}
					}
				};
				CrucibleFilteredModelProvider provider = new MyCrucibleFilteredModelProvider(modelProvider, filter);
				reviewFilesTree.setModelProvider(provider);
				reviewFilesTree.setRootVisible(true);
			}
		}
	}

	private static class MyCrucibleFilteredModelProvider extends CrucibleFilteredModelProvider {
		private static final com.atlassian.theplugin.idea.ui.tree.Filter COMMENT_FILTER
				= new com.atlassian.theplugin.idea.ui.tree.Filter() {
				@Override
				public boolean isValid(final AtlassianTreeNode node) {
					if (node instanceof CrucibleFileNode) {
						CrucibleFileNode anode = (CrucibleFileNode) node;
						try {
							return anode.getFile().getNumberOfComments() > 0;
						} catch (ValueNotYetInitialized valueNotYetInitialized) {
							return false;
						}
					}
					return true;
				}
			};

		public MyCrucibleFilteredModelProvider(final ModelProvider modelProvider, final Filter filter) {
			super(modelProvider, filter);
		}

		@Override
		public com.atlassian.theplugin.idea.ui.tree.Filter getFilter(final Filter type) {
			switch (type) {
				case FILES_ALL:
					return com.atlassian.theplugin.idea.ui.tree.Filter.ALL;
				case FILES_WITH_COMMENTS_ONLY:
					return COMMENT_FILTER;
				default:
					throw new IllegalStateException("Unknows filtering requested: " + type.toString());
			}
		}
	}

//	public void valueChanged(TreeSelectionEvent e) {
//		TreePath oldPath = e.getOldLeadSelectionPath();
//		if (oldPath != null) {
//			DefaultMutableTreeNode oldNode = (DefaultMutableTreeNode) oldPath.getLastPathComponent();
//			if (oldNode != null && oldNode instanceof ServerNode) {
//				serverConfigPanel.saveData(((ServerNode) oldNode).getServerType());
//			}
//			model.nodeChanged(oldNode);
//
//		}
//
//		TreePath path = e.getNewLeadSelectionPath();
//
//		if (path != null) {
//			selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
//			if (selectedNode instanceof ServerNode) {
//				ServerCfg server = ((ServerNode) selectedNode).getServer();
//				serverConfigPanel.editServer(server);
////                else {
////					// PL-235 show blank panel if server from tree node does not exist in configuration
////					// it happens if you add server, click cancel and open config window again
////					serverConfigPanel.showEmptyPanel();
////				}
//			} else if (selectedNode instanceof ServerTypeNode) {
//				serverConfigPanel.showEmptyPanel();
//			}
//		} else {
//			serverConfigPanel.showEmptyPanel();
//		}
//	}


	@Nullable
	public Object getData(@NonNls final String dataId) {
		if (dataId.equals(Constants.CRUCIBLE_FILE_NODE)) {
			final TreePath selectionPath = reviewFilesTree.getTreeComponent().getSelectionPath();
			if (selectionPath == null) {
				return null;
			}
			Object selection = selectionPath.getLastPathComponent();
			if (selection instanceof CrucibleFileNode) {
				CrucibleFileNode crucibleFileNode = (CrucibleFileNode) selection;
				return crucibleFileNode;
			}
		}
		return null;
	}
}
