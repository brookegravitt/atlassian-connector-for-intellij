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

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.ConfigurationListenerAdapter;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.CrucibleReviewListener;
import com.atlassian.theplugin.commons.crucible.CrucibleReviewListenerAdapter;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.*;
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
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.CommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import com.atlassian.theplugin.idea.ui.tree.file.FileNode;
import com.atlassian.theplugin.idea.ui.tree.file.FileTreeModelBuilder;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;

public final class ReviewItemTreePanel extends JPanel implements DataProvider {

	//	ProjectView.
	private AtlassianTreeWithToolbar reviewFilesAndCommentsTree;

	private static final int WIDTH = 150;

	private static final int HEIGHT = 250;

	public static final Logger LOGGER = PluginUtil.getLogger();

	private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();

	private CrucibleFilteredModelProvider.Filter filter;

	public static final String MENU_PLACE = "menu review files";

	private Project project;

	private ReviewAdapter crucibleReview;
	private final LocalConfigurationListener configurationListener = new LocalConfigurationListener();
	private final CrucibleReviewListener reviewListener = new LocalReviewListener();
	private final CrucibleReviewListModel crucibleReviewListModel;
	private final CfgManager cfgManager;
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

	public ReviewItemTreePanel(@NotNull CfgManager cfgManager, final Project project,
			final CrucibleFilteredModelProvider.Filter filter,
			@NotNull final ThePluginProjectComponent pluginProjectComponent) {
		this.cfgManager = cfgManager;
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
//		if (getCrucibleReview().getServer().getServerId().equals(serverId)) {
//			reviewFilesAndCommentsTree.clear();
//			stopListeningForCredentialChanges();
//		}
//	}

	public void startListeningForCredentialChanges(final Project aProject, final ReviewAdapter aCrucibleReview) {
		setCrucibleReview(aCrucibleReview);
		this.project = aProject;
		pluginProjectComponent.getCfgManager().addProjectConfigurationListener(
				CfgUtil.getProjectId(project), configurationListener);
//				addConfigurationCredentialsListener(CfgUtil.getProjectId(project), this);
	}                                                     	

	public void stopListeningForCredentialChanges() {
		pluginProjectComponent.getCfgManager().removeProjectConfigurationListener(
				CfgUtil.getProjectId(project), configurationListener);
//				removeConfigurationCredentialsListener(CfgUtil.getProjectId(project), configurationListener);
	}

	public void showReview(ReviewAdapter reviewItem) {

		setCrucibleReview(reviewItem);

		Set<CrucibleFileInfo> files;
		try {
			List<VersionedComment> comments;
			comments = CrucibleServerFacadeImpl.getInstance().getVersionedComments(
					reviewItem.getServerData(), reviewItem.getPermId());

			reviewItem.setGeneralComments(CrucibleServerFacadeImpl.getInstance().getGeneralComments(
					reviewItem.getServerData(), reviewItem.getPermId()));

			files = CrucibleServerFacadeImpl.getInstance().getFiles(reviewItem.getServerData(),
					reviewItem.getPermId());
			reviewItem.setFilesAndVersionedComments(files, comments);

		} catch (RemoteApiException e) {
			IdeaHelper.handleRemoteApiException(project, e);
			return;
		} catch (ServerPasswordNotProvidedException e) {
			IdeaHelper.handleMissingPassword(e);
			return;
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
			reviewFilesAndCommentsTree.requestFocus();
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
		if (dataId.equals(Constants.CRUCIBLE_FILE_NODE)) {
			final TreePath selectionPath = reviewFilesAndCommentsTree.getTreeComponent().getSelectionPath();
			if (selectionPath == null) {
				return null;
			}
			Object selection = selectionPath.getLastPathComponent();
			if (selection instanceof CrucibleFileNode) {
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
		public void createdOrEditedGeneralComment(final ReviewAdapter review, final GeneralComment comment) {
			refreshView(review);
		}

		@Override
		public void createdOrEditedGeneralCommentReply(final ReviewAdapter review, final GeneralComment parentComment,
				final GeneralComment comment) {
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
		public void publishedGeneralComment(final ReviewAdapter review, final GeneralComment comment) {
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
	}

}