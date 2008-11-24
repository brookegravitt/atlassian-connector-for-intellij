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
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.cfg.ConfigurationListenerAdapter;
import com.atlassian.theplugin.commons.crucible.CrucibleReviewListener;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.CrucibleFilteredModelProvider;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import com.atlassian.theplugin.idea.ui.tree.file.FileNode;
import com.atlassian.theplugin.idea.ui.tree.file.FileTreeModelBuilder;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public final class ReviewItemTreePanel extends JPanel implements DataProvider, CrucibleReviewListener {

	//	ProjectView.
	private AtlassianTreeWithToolbar reviewFilesAndCommentsTree = null;

	private static final int WIDTH = 150;

	private static final int HEIGHT = 250;

	public static final Logger LOGGER = PluginUtil.getLogger();

	private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();

	private JLabel statusLabel;

	private CrucibleFilteredModelProvider.Filter filter;

	public static final String MENU_PLACE = "menu review files";

	private Project project;

	private ReviewAdapter crucibleReview = null;
	private final LocalConfigurationListener configurationListener = new LocalConfigurationListener();

	public synchronized ReviewAdapter getCrucibleReview() {
		return crucibleReview;
	}

	public synchronized void setCrucibleReview(ReviewAdapter crucibleReview) {
		this.crucibleReview = crucibleReview;
	}

	public ReviewItemTreePanel(final Project project, final CrucibleFilteredModelProvider.Filter filter) {
		initLayout();
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
		if (reviewFilesAndCommentsTree == null) {
			reviewFilesAndCommentsTree = new AtlassianTreeWithToolbar("ThePlugin.Crucible.ReviewFileListToolBar");
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
		IdeaHelper.getProjectComponent(project, ThePluginProjectComponent.class).getCfgManager().
				addProjectConfigurationListener(CfgUtil.getProjectId(project), configurationListener);
//				addConfigurationCredentialsListener(CfgUtil.getProjectId(project), this);
	}

	private void stopListeningForCredentialChanges() {
		IdeaHelper.getProjectComponent(project, ThePluginProjectComponent.class).getCfgManager().
				removeProjectConfigurationListener(CfgUtil.getProjectId(project), configurationListener);
//				removeConfigurationCredentialsListener(CfgUtil.getProjectId(project), configurationListener);
	}

	public void setStatus(String txt) {
		statusLabel.setText(txt);
	}

	public void createdOrEditedGeneralComment(final ReviewAdapter review, final GeneralComment comment) {
		reviewChanged(review);
	}


	public void createdOrEditedGeneralCommentReply(final ReviewAdapter review, final GeneralComment parentComment,
			final GeneralComment comment) {

		reviewChanged(review);
	}

	public void createdOrEditedVersionedComment(final ReviewAdapter review, final PermId filePermId,
			final VersionedComment comment) {

		reviewChanged(review);
	}

	public void createdOrEditedVersionedCommentReply(final ReviewAdapter review, final PermId filePermId,
			final VersionedComment parentComment, final VersionedComment comment) {

		reviewChanged(review);
	}

	public void removedComment(final ReviewAdapter review, final Comment comment) {
		reviewChanged(review);
	}

	public void publishedGeneralComment(final ReviewAdapter review, final GeneralComment comment) {
		reviewChanged(review);
	}

	public void publishedVersionedComment(final ReviewAdapter review, final PermId permId,
			final VersionedComment comment) {

		reviewChanged(review);
	}

	public void showReview(ReviewAdapter reviewItem) {

		setCrucibleReview(reviewItem);

		List<CrucibleFileInfo> files;
		try {
//			reviewItem.fillReview(
//					CrucibleServerFacadeImpl.getInstance().getReview(reviewItem.getServer(), reviewItem.getPermId()));

			List<VersionedComment> comments;
			comments = CrucibleServerFacadeImpl.getInstance().getVersionedComments(
					reviewItem.getServer(), reviewItem.getPermId());

			reviewItem.setGeneralComments(CrucibleServerFacadeImpl.getInstance().getGeneralComments(
					reviewItem.getServer(), reviewItem.getPermId()));

			files = CrucibleServerFacadeImpl.getInstance().getFiles(reviewItem.getServer(), reviewItem.getPermId());

			reviewItem.setFilesAndVersionedComments(files, comments);
		} catch (RemoteApiException e) {
			IdeaHelper.handleRemoteApiException(project, e);
			return;
		} catch (ServerPasswordNotProvidedException e) {
			IdeaHelper.handleMissingPassword(e);
			return;
		}
//		final List<CrucibleFileInfo> files1 = files;
		EventQueue.invokeLater(new MyRunnable(reviewItem));
	}


	private void reviewChanged(ReviewAdapter review) {
		this.crucibleReview = review;
		EventQueue.invokeLater(new MyRunnable(review));
	}

	public void reviewUpdated(final ReviewAdapter newReview) {
		if (newReview.equals(crucibleReview)) {
			this.crucibleReview.fillReview(newReview);
			showReview(crucibleReview);
		}
	}

	public void showFile(final ReviewAdapter review, final CrucibleFileInfo file) {
	}


	public void showDiff(final CrucibleFileInfo file) {
	}

	private String createGeneralInfoText(final ReviewAdapter reviewItem) {
		final StringBuilder buffer = new StringBuilder();
		buffer.append("<html>");
		buffer.append("<body>");
		buffer.append(reviewItem.getAuthor().getDisplayName());
		buffer.append(" ");
		buffer.append("<font size=-1 color=");
		buffer.append(CrucibleConstants.CRUCIBLE_AUTH_COLOR);
		buffer.append(">AUTH</font>");
		buffer.append(" ");
		if (!reviewItem.getAuthor().equals(reviewItem.getModerator())) {
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

		private final ReviewAdapter review;

		public MyRunnable(final ReviewAdapter review) {
			this.review = review;
		}

		public void run() {

			statusLabel.setText(createGeneralInfoText(getCrucibleReview()));
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
		}

	}


	private static class MyCrucibleFilteredModelProvider extends CrucibleFilteredModelProvider {
		private static final com.atlassian.theplugin.idea.ui.tree.Filter COMMENT_FILTER
				= new com.atlassian.theplugin.idea.ui.tree.Filter() {
			@Override
			public boolean isValid(final AtlassianTreeNode node) {
				if (node instanceof CrucibleFileNode) {
					CrucibleFileNode anode = (CrucibleFileNode) node;
					return anode.getFile().getNumberOfComments() > 0;
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
		public void serverConnectionDataUpdated(ServerId serverId) {
			if (getCrucibleReview().getServer().getServerId().equals(serverId)) {
				reviewFilesAndCommentsTree.clear();
				stopListeningForCredentialChanges();
			}
		}
	}
}
