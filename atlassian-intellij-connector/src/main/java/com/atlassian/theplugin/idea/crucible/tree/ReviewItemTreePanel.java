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

import com.atlassian.connector.intellij.crucible.CrucibleReviewListener;
import com.atlassian.connector.intellij.crucible.CrucibleReviewListenerAdapter;
import com.atlassian.connector.intellij.crucible.CrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.cfg.ConfigurationListenerAdapter;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.idea.action.crucible.comment.NextDiffAction;
import com.atlassian.theplugin.idea.action.crucible.comment.PrevDiffAction;
import com.atlassian.theplugin.idea.crucible.CrucibleFilteredModelProvider;
import com.atlassian.theplugin.idea.crucible.editor.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.ui.ReviewCommentRenderer;
import com.atlassian.theplugin.idea.crucible.ui.ReviewDetailsTreeMouseListener;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.CommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import com.atlassian.theplugin.idea.ui.tree.file.FileNode;
import com.atlassian.theplugin.idea.ui.tree.file.FileTreeModelBuilder;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public final class ReviewItemTreePanel extends JPanel implements DataProvider {

	//	ProjectView.
	private AtlassianTreeWithToolbar reviewFilesAndCommentsTree;

	private static final int WIDTH = 150;

	private static final int HEIGHT = 250;

	public static final Logger LOGGER = PluginUtil.getLogger();

	private final ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();

	private CrucibleFilteredModelProvider.Filter filter;

	public static final String MENU_PLACE = "menu review files";

	private Project project;

	private ReviewAdapter crucibleReview;
	private final LocalConfigurationListener configurationListener = new LocalConfigurationListener();
	private final CrucibleReviewListener reviewListener = new LocalReviewListener();
	private final CrucibleReviewListModel crucibleReviewListModel;
	private final ThePluginProjectComponent pluginProjectComponent;
	private TreeUISetup treeUISetup;
	private static final String THE_PLUGIN_CRUCIBLE_REVIEW_FILE_LIST_TOOL_BAR = "ThePlugin.Crucible.ReviewFileListToolBar";

	public synchronized ReviewAdapter getCrucibleReview() {
		return crucibleReview;
	}

	public synchronized void setCrucibleReview(ReviewAdapter crucibleReview) {
		this.crucibleReview = crucibleReview;
	}

	public void switchFilter() {
		filterTreeNodes(filter.getNextState());
	}

	public ReviewItemTreePanel(final Project project, final CrucibleFilteredModelProvider.Filter filter,
			@NotNull final ThePluginProjectComponent pluginProjectComponent) {
		this.pluginProjectComponent = pluginProjectComponent;
		initLayout();
		this.filter = filter;
		this.crucibleReviewListModel = IdeaHelper.getProjectComponent(project, CrucibleReviewListModel.class);
	}

	private void initLayout() {
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		add(getReviewItemTree(), BorderLayout.CENTER);
	}

	public JPanel getReviewItemTree() {
		if (reviewFilesAndCommentsTree == null) {
			final ReviewCommentRenderer renderer = new ReviewCommentRenderer();
			treeUISetup = new TreeUISetup(renderer);
			reviewFilesAndCommentsTree = new AtlassianTreeWithToolbar(THE_PLUGIN_CRUCIBLE_REVIEW_FILE_LIST_TOOL_BAR,
					treeUISetup, new AtlassianTree.ViewStateListener() {
				public void setViewState(AtlassianTreeWithToolbar.ViewState state) {
					setCommentsState(state);
				}
			});

			// disable collapsing tree on double click - use 3 clicks to collapse/expand - see PL-1513
			reviewFilesAndCommentsTree.getTreeComponent().setToggleClickCount(2 + 1);

			final ActionGroup group = (ActionGroup) ActionManager.getInstance()
					.getAction(THE_PLUGIN_CRUCIBLE_REVIEW_FILE_LIST_TOOL_BAR);
			final AnAction globalShowNextAction = ActionManager.getInstance().getAction("VcsShowNextChangeMarker");
			final AnAction globalShowPrevAction = ActionManager.getInstance().getAction("VcsShowPrevChangeMarker");

			AnAction[] actions = group.getChildren(null);
			for (AnAction a : actions) {
				if (a instanceof NextDiffAction) {
					a.copyShortcutFrom(globalShowNextAction);
					a.registerCustomShortcutSet(a.getShortcutSet(), reviewFilesAndCommentsTree);
				}
				if (a instanceof PrevDiffAction) {
					a.copyShortcutFrom(globalShowPrevAction);
					a.registerCustomShortcutSet(a.getShortcutSet(), reviewFilesAndCommentsTree);
				}
			}

			new ReviewDetailsTreeMouseListener(reviewFilesAndCommentsTree.getTreeComponent(), renderer, treeUISetup);
			reviewFilesAndCommentsTree.setRootVisible(false);
            reviewFilesAndCommentsTree.getTreeComponent().getSelectionModel().addTreeSelectionListener(
                    new TreeSelectionListener() {
                        public void valueChanged(TreeSelectionEvent event) {
                            TreePath path = event.getNewLeadSelectionPath();
                            if (path == null) {
                                return;
                            }
                            Object o = path.getLastPathComponent();
                            if (o instanceof CommentTreeNode) {
                                CommentTreeNode node = (CommentTreeNode) o;
                                Comment.ReadState state = node.getComment().getReadState();
                                if (state != Comment.ReadState.UNREAD) {
                                    return;
                                }
                                markCommentRead(node);
                            }
                        }
                    }
            );
			reviewFilesAndCommentsTree.getTreeComponent().addMouseListener(new PopupAwareMouseAdapter() {

				@Override
				protected void onPopup(final MouseEvent e) {
					if (!(e.getComponent() instanceof AtlassianTree)) {
						return;
					}
					AtlassianTree tree = (AtlassianTree) e.getComponent();
					TreePath path = tree.getPathForLocation(e.getX(), e.getY());
					if (path == null) {
						return;
					}
					tree.setSelectionPath(path);
					Object o = path.getLastPathComponent();
					if (!(o instanceof FileNode)) {
						return;
					}
					ActionManager aManager = ActionManager.getInstance();
					ActionGroup menu = (ActionGroup) aManager.getAction("ThePlugin.Crucible.ReviewFileListPopupMenuExt");
					if (menu == null) {
						return;
					}
					aManager.createActionPopupMenu(MENU_PLACE, menu).getComponent()
							.show(e.getComponent(), e.getX(), e.getY());

				}
			});
		}
		return reviewFilesAndCommentsTree;
	}

    private void markCommentRead(final CommentTreeNode node) {
        Task.Backgroundable task = new Task.Backgroundable(project, "Marking comment as read", true) {
            private Throwable error = null;
            public void run(@NotNull ProgressIndicator progressIndicator) {
                CrucibleServerFacade f = IntelliJCrucibleServerFacade.getInstance();

                try {
                    f.markCommentRead(node.getReview().getServerData(),
                            node.getReview().getPermId(), node.getComment().getPermId());
                } catch (RemoteApiException e) {
                    error = e;
                } catch (ServerPasswordNotProvidedException e) {
                    error = e;
                }
            }

            @Override
            public void onSuccess() {
                if (error != null) {
                    DialogWithDetails.showExceptionDialog(
                            getAtlassianTreeWithToolbar(), "Marking comment as read failed", error);
                } else {
                    ((CommentBean) node.getComment()).setReadState(Comment.ReadState.READ);
                    ((DefaultTreeModel) reviewFilesAndCommentsTree.getTreeComponent().getModel()).nodeChanged(node);
                }
            }
        };
        ProgressManager.getInstance().run(task);
    }

    private void setCommentsState(AtlassianTreeWithToolbar.ViewState state) {
		// ok, this shit should be banned by international conventions,
		// as it is both cruel and harmful. Gaaah, my eyes hurt. Whoever
		// invented Swing, should be taken out and shot - and then really
		// really hurt
		final JTree tree = reviewFilesAndCommentsTree.getTreeComponent();
		for (int i = 0; i < tree.getRowCount(); i++) {
			Object o = tree.getPathForRow(i).getLastPathComponent();
			if (o instanceof CommentTreeNode) {
				CommentTreeNode ctn = (CommentTreeNode) o;
				ctn.setExpanded(state == AtlassianTreeWithToolbar.ViewState.EXPANDED);
			}
		}
		treeUISetup.forceTreePrefSizeRecalculation(tree);
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
		((CrucibleFilteredModelProvider) reviewFilesAndCommentsTree.getModelProvider()).setType(aFilter);
		reviewFilesAndCommentsTree.triggerModelUpdated();
		reviewFilesAndCommentsTree.revalidate();
		reviewFilesAndCommentsTree.repaint();
	}

	public AtlassianTreeWithToolbar getAtlassianTreeWithToolbar() {
		return reviewFilesAndCommentsTree;
	}

//	public void configurationCredentialsUpdated(final ServerId serverId) {
//		if (getCrucibleReview().getJiraServerData().getServerId().equals(serverId)) {
//			reviewFilesAndCommentsTree.clear();
//			stopListeningForCredentialChanges();
//		}
//	}

	public void startListeningForCredentialChanges(final Project aProject, final ReviewAdapter aCrucibleReview) {
		setCrucibleReview(aCrucibleReview);
		this.project = aProject;
		pluginProjectComponent.getCfgManager().addProjectConfigurationListener(configurationListener);
//				addConfigurationCredentialsListener(CfgUtil.getProjectId(project), this);
	}

	public void stopListeningForCredentialChanges() {
		pluginProjectComponent.getCfgManager().removeProjectConfigurationListener(configurationListener);
//				removeConfigurationCredentialsListener(CfgUtil.getProjectId(project), configurationListener);
	}

	/**
	 * Blocking method. Should be called in the background thread.
	 * If refreshDetails is true or review has no details (files and comments) data is retrieved from server.
	 *
	 * @param reviewItem	 review to open
	 * @param refreshDetails force to refresh review data
	 */
	public void showReview(ReviewAdapter reviewItem, boolean refreshDetails) {

		setCrucibleReview(reviewItem);

		boolean hasNoDetails = false;

		try {
			reviewItem.getGeneralComments();
			reviewItem.getFiles();
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			hasNoDetails = true;
		}

		if (hasNoDetails || refreshDetails) {
			try {
				IntelliJCrucibleServerFacade.getInstance().fillDetailsForReview(reviewItem);
			} catch (RemoteApiException e) {
				IdeaHelper.handleRemoteApiException(project, e);
				return;
			} catch (ServerPasswordNotProvidedException e) {
				IdeaHelper.handleMissingPassword(e);
				return;
			}
		}
		EventQueue.invokeLater(new MyRunnable(reviewItem));
	}


	private void refreshView(final ReviewAdapter review) {
		if (crucibleReviewListModel != null && review != null) {
			if (crucibleReviewListModel.getOpenInIdeReviews().contains(review)) {
				this.crucibleReview = review;
			}
			EventQueue.invokeLater(new MyRunnable(review));
		}
	}

	public CrucibleReviewListener getReviewListener() {
		return reviewListener;
	}

	public void selectVersionedComment(final CrucibleFileInfo file, final Comment comment) {
		final JTree tree = reviewFilesAndCommentsTree.getTreeComponent();
		for (int i = 0; i < tree.getRowCount(); i++) {
			Object o = tree.getPathForRow(i).getLastPathComponent();
			if (o instanceof VersionedCommentTreeNode) {
				VersionedCommentTreeNode node = (VersionedCommentTreeNode) o;
				if (node.getFile().getPermId().equals(file.getPermId())
						&& node.getComment().getPermId().equals(comment.getPermId())) {
					tree.setSelectionRow(i);
					tree.scrollRowToVisible(i);
					break;
				}
			}
		}
	}

	public void selectGeneralComment(final Comment comment) {
		final JTree tree = reviewFilesAndCommentsTree.getTreeComponent();
		for (int i = 0; i < tree.getRowCount(); i++) {
			Object o = tree.getPathForRow(i).getLastPathComponent();
			if (o instanceof GeneralCommentTreeNode) {
				GeneralCommentTreeNode node = (GeneralCommentTreeNode) o;
				if (node.getComment().getPermId().equals(comment.getPermId())) {
					tree.setSelectionRow(i);
					tree.scrollRowToVisible(i);
					break;
				}
			}
		}
	}

	public void selectFile(final CrucibleFileInfo file) {
		final JTree tree = reviewFilesAndCommentsTree.getTreeComponent();
		for (int i = 0; i < tree.getRowCount(); i++) {
			Object o = tree.getPathForRow(i).getLastPathComponent();
			if (o instanceof CrucibleFileNode) {
				CrucibleFileNode node = (CrucibleFileNode) o;
				if (node.getFile().getPermId().equals(file.getPermId())) {
					tree.setSelectionRow(i);
					tree.scrollRowToVisible(i);
					break;
				}
			}
		}
	}

    private class MyRunnable implements Runnable {

		private final ReviewAdapter review;

		public MyRunnable(final ReviewAdapter review) {
			this.review = review;
		}

		public void run() {

			ModelProvider modelProvider = new ModelProvider() {

				@Override
				public AtlassianTreeModel getModel(final AtlassianTreeWithToolbar.State state) {
					switch (state) {
						case DIRED:
							return FileTreeModelBuilder.buildTreeModelFromCrucibleChangeSet(
									project, review);
						case FLAT:
							return FileTreeModelBuilder.buildFlatModelFromCrucibleChangeSet(
									project, review);
						default:
							throw new IllegalStateException("Unknown model requested");
					}
				}
			};
			CrucibleFilteredModelProvider provider = new MyCrucibleFilteredModelProvider(modelProvider, filter);
			reviewFilesAndCommentsTree.setModelProvider(provider);
			reviewFilesAndCommentsTree.setRootVisible(true);
			reviewFilesAndCommentsTree.expandAll();

			// commenting out due to PL-1273
//			reviewFilesAndCommentsTree.requestFocus();

			CommentHighlighter.updateCommentsInEditors(project, review);
		}

	}


	private static class MyCrucibleFilteredModelProvider extends CrucibleFilteredModelProvider {
		private static final com.atlassian.theplugin.idea.ui.tree.Filter COMMENT_FILTER
				= new com.atlassian.theplugin.idea.ui.tree.Filter() {
			@Override
			public boolean isValid(final AtlassianTreeNode node) {
				if (node instanceof CrucibleFileNode) {
					CrucibleFileNode anode = (CrucibleFileNode) node;
					return (anode.getFile().getNumberOfComments() > 0) || (node.getChildCount() > 0);
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

	@Nullable
	public Object getData(@NonNls final String dataId) {
		if (dataId.equals(Constants.CRUCIBLE_FILE_NODE) || dataId.equals(Constants.CRUCIBLE_VERSIONED_COMMENT_NODE)) {
			final TreePath selectionPath = reviewFilesAndCommentsTree.getTreeComponent().getSelectionPath();
			if (selectionPath == null) {
				return null;
			}
			Object selection = selectionPath.getLastPathComponent();
			if (dataId.equals(Constants.CRUCIBLE_FILE_NODE)
					&& selection instanceof CrucibleFileNode) {
				return selection;
			} else if (dataId.equals(Constants.CRUCIBLE_VERSIONED_COMMENT_NODE)
					&& selection instanceof VersionedCommentTreeNode) {
				return selection;
			}
		}
		return null;

	}

	private class LocalConfigurationListener extends ConfigurationListenerAdapter {
		@Override
		public void serverConnectionDataChanged(ServerId serverId) {
			if (getCrucibleReview().getServerData().getServerId().equals(serverId)) {
				reviewFilesAndCommentsTree.clear();
				stopListeningForCredentialChanges();
			}
		}
	}


	private class LocalReviewListener extends CrucibleReviewListenerAdapter {
		@Override
		public void createdOrEditedGeneralComment(final ReviewAdapter review, final Comment comment) {
			refreshView(review);
		}

		@Override
		public void createdOrEditedGeneralCommentReply(final ReviewAdapter review, final Comment parentComment,
				final Comment comment) {
			refreshView(review);
		}

		@Override
		public void createdOrEditedVersionedComment(final ReviewAdapter review, final PermId filePermId,
				final VersionedComment comment) {
			refreshView(review);
		}

		@Override
		public void createdOrEditedVersionedCommentReply(final ReviewAdapter review, final PermId filePermId,
				final VersionedComment parentComment,
				final VersionedComment comment) {
			refreshView(review);
		}

		@Override
		public void removedComment(final ReviewAdapter review, final Comment comment) {
			refreshView(review);
		}

		@Override
		public void publishedGeneralComment(final ReviewAdapter review, final Comment comment) {
			refreshView(review);
		}

		@Override
		public void publishedVersionedComment(final ReviewAdapter review, final PermId permId,
				final VersionedComment comment) {
			refreshView(review);
		}

		@Override
		public void reviewChanged(final ReviewAdapter review,
				final List<CrucibleNotification> notifications) {
			refreshView(review);
		}

        public void commentReadStateChanged(final ReviewAdapter review, final Comment comment) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    AtlassianTree tree = reviewFilesAndCommentsTree.getTreeComponent();
                    DefaultMutableTreeNode start =
                            (DefaultMutableTreeNode) tree.getModel().getRoot();

                    AtlassianTreeNode n = (AtlassianTreeNode) start.getNextNode();
                    while (n != null) {
                        if (n instanceof CommentTreeNode) {
                            CommentTreeNode ctn = (CommentTreeNode) n;
                            if (ctn.getComment().getPermId().equals(comment.getPermId())) {
                                ((CommentBean) ctn.getComment()).setReadState(comment.getReadState());
                                ((DefaultTreeModel) tree.getModel()).nodeChanged(ctn);
                                TreeNode[] path = n.getPath();
                                for (TreeNode treeNode : path) {
                                    ((DefaultTreeModel) tree.getModel()).nodeChanged(treeNode);
                                }
                            }
                        }
                        n = (AtlassianTreeNode) n.getNextNode();
                    }
                    CommentHighlighter.updateCommentsInEditors(project, review);
                }
            });
        }
    }

}