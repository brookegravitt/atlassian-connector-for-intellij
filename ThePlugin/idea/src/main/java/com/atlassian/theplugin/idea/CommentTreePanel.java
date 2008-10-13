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

package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.crucible.CrucibleFileInfoManager;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.idea.crucible.CrucibleFilteredModelProvider;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.idea.crucible.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.*;
import com.atlassian.theplugin.idea.ui.AtlassianToolbar;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.idea.ui.tree.*;
import com.atlassian.theplugin.idea.ui.tree.comment.*;
import com.atlassian.theplugin.idea.ui.tree.file.FolderNode;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class CommentTreePanel extends JPanel {
	private CrucibleReviewActionListener crucibleAgent = new MyCrucibleReviewActionListener();
	private JScrollPane commentScroll;
	private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
	private CommentTree commentTree = new CommentTree();

	private AtlassianTreeModel fullModel;

	public static final AtlassianTreeNode ROOT = new FolderNode("/", AtlassianClickAction.EMPTY_ACTION);
	private Project project;
	private static final String TOOLBAR_ID = "ThePlugin.Crucible.Comment.ToolBar";
	public static final String MENU_PLACE = "menu comments";
	private static final String TOOLBAR_PLACE = "toolbar comments";
	private CrucibleFilteredModelProvider.Filter filter;

	private ReviewAdapter thisReview;

	public CommentTreePanel(Project project, CrucibleFilteredModelProvider.Filter filter) {
		this.project = project;
		this.filter = filter;
		IdeaHelper.getReviewActionEventBroker(project).registerListener(crucibleAgent);
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());
		setBackground(UIUtil.getTreeTextBackground());
		progressAnimation.configure(this, commentScroll, BorderLayout.CENTER);
		commentScroll = new JScrollPane();
		add(AtlassianToolbar.createToolbar(TOOLBAR_PLACE, TOOLBAR_ID), BorderLayout.NORTH);
		add(commentScroll, BorderLayout.CENTER);
	}

	private void addGeneralCommentTree(AtlassianTreeNode root, final ReviewAdapter review,
			GeneralComment generalComment, int depth) {
		if (generalComment.isDeleted()) {
			return;
		}
		GeneralCommentTreeNode commentNode
				= new GeneralCommentTreeNode(review, generalComment, new GeneralCommentClickAction());
		root.addNode(commentNode);
		for (GeneralComment comment : generalComment.getReplies()) {
			addGeneralCommentTree(commentNode, review, comment, depth + 1);
		}


	}

	private void addVersionedCommentTree(AtlassianTreeNode root, final ReviewAdapter review,
			final CrucibleFileInfo file, VersionedComment versionedComment, int depth) {
		if (versionedComment.isDeleted()) {
			return;
		}
		VersionedCommentTreeNode commentNode = new VersionedCommentTreeNode(review, file, versionedComment,
				new VersionedCommentClickAction());
		root.addNode(commentNode);
		for (VersionedComment comment : versionedComment.getReplies()) {
			addVersionedCommentTree(commentNode, review, file, comment, depth + 1);
		}
	}


	private AtlassianTreeModel createTreeModel(final ReviewAdapter review) {
		ROOT.removeAllChildren();
		AtlassianTreeModel model = new AtlassianTreeModel(ROOT);

		if (review.getDescription() != null && review.getDescription().length() != 0) {
			ROOT.addNode(new CrucibleStatementOfObjectivesNode(review.getDescription(), AtlassianClickAction.EMPTY_ACTION));
		}

		List<GeneralComment> generalComments;
		try {
			generalComments = review.getGeneralComments();
			AtlassianTreeNode generalNode = new GeneralSectionNode(review, new AtlassianClickAction() {
				public void execute(final AtlassianTreeNode node, final int noOfClicks) {
					switch (noOfClicks) {
						case 1:
							GeneralSectionNode anode = (GeneralSectionNode) node;
							IdeaHelper.getReviewActionEventBroker(project).trigger(
									new FocusOnReviewEvent(crucibleAgent, anode.getReview()));
							break;
						default:
							// do nothing
					}

				}
			});
			ROOT.addNode(generalNode);
			for (GeneralComment comment : generalComments) {
				addGeneralCommentTree(generalNode, review, comment, 0);
			}
			for (CrucibleFileInfo file : CrucibleFileInfoManager.getInstance().getFiles(review.getInnerReviewObject())) {
				AtlassianTreeNode fileNode = new FileNameNode(review, file, new AtlassianClickAction() {
					public void execute(final AtlassianTreeNode node, final int noOfClicks) {
						switch (noOfClicks) {
							case 1:
								FileNameNode anode = (FileNameNode) node;
								IdeaHelper.getReviewActionEventBroker(project).trigger(
										new FocusOnFileEvent(crucibleAgent, anode.getReview(), anode.getFile()));
								break;
							default:
								// do nothing
						}

					}
				});
				ROOT.addNode(fileNode);
				for (VersionedComment comment : file.getItemInfo().getComments()) {
					addVersionedCommentTree(fileNode, review, file, comment, 0);
				}

			}
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			PluginUtil.getLogger().error(valueNotYetInitialized.getMessage());
		}
		return model;
	}

	public CommentTree getCommentTree() {
		return commentTree;
	}

	public void filterTreeNodes(CrucibleFilteredModelProvider.Filter aFilter) {
		this.filter = aFilter;
		commentTree.setModel(fullModel.getFilteredModel(getFilter(aFilter)));
		refreshTree();
	}

	private Filter getFilter(final CrucibleFilteredModelProvider.Filter aFilter) {
		switch (aFilter) {
			case FILES_ALL:
				return com.atlassian.theplugin.idea.ui.tree.Filter.ALL;
			case FILES_WITH_COMMENTS_ONLY:
				return new Filter() {
					@Override
					public boolean isValid(final AtlassianTreeNode node) {
						if (node instanceof FileNameNode) {
							FileNameNode anode = (FileNameNode) node;
							return anode.getFile().getItemInfo().getNumberOfComments() > 0;
						}
						return true;
					}
				};
			default:
				throw new IllegalStateException("Unknow filtering requested (" + aFilter.toString() + ")");
		}
	}

	private void refreshTree() {
		commentTree.setRootVisible(false);
		commentTree.expandAll();
		commentScroll.setViewportView(commentTree);
		commentTree.initializeUI();
		commentTree.setVisible(true);
		commentTree.setEnabled(true);
		commentTree.revalidate();
		commentTree.repaint();
		commentTree.addMouseListener(new PopupMouseAdapter());
	}

	private class MyCrucibleReviewActionListener extends CrucibleReviewActionListener {

		@Override
		public void commentsDownloaded(final ReviewAdapter review) {
			thisReview = review;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					commentTree.setVisible(false);
					fullModel = createTreeModel(review);
					commentTree = new CommentTree(fullModel.getFilteredModel(getFilter(filter)));
					refreshTree();
				}
			});
		}

		@Override
		public void focusOnGeneralComments(final ReviewAdapter review) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					AtlassianTreeModel model = (AtlassianTreeModel) commentTree.getModel();
					AtlassianTreeNode node = model.locateNode(new SearchGeneralSectionAlgorithm());
					commentTree.focusOnNode(node);
				}
			});
		}

		@Override
		public void createdOrEditedGeneralComment(final ReviewAdapter review, final GeneralComment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					AtlassianTreeNode newCommentNode
							= new GeneralCommentTreeNode(review, comment, new GeneralCommentClickAction());

					SearchGeneralCommentAlgorithm replacementLocator = new SearchGeneralCommentAlgorithm(review, comment);
					AtlassianTreeNode changedNode = replaceNode(replacementLocator,
							newCommentNode);
					if (changedNode ==  null) {
						SearchGeneralSectionAlgorithm parentLocator = new SearchGeneralSectionAlgorithm();
						changedNode = addNewNode(parentLocator, newCommentNode);
					}
					addReplyNodes(review, newCommentNode, comment);
					refreshNode(changedNode);
				}
			}
			);
		}

		private void refreshNode(final AtlassianTreeNode node) {
			if (node == null) {
				return;
			}
			commentTree.expandFromNode(node);
			((AtlassianTreeModel) commentTree.getModel()).nodeChanged(node.getParent());
		}

		@Override
		public void createdOrEditedGeneralCommentReply(final ReviewAdapter review, final GeneralComment parentComment,
				final GeneralComment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					GeneralCommentTreeNode newCommentNode = new GeneralCommentTreeNode(review, comment,
							AtlassianClickAction.EMPTY_ACTION);
					AtlassianTreeNode changedNode = replaceNode(new SearchGeneralCommentAlgorithm(review, comment),
							newCommentNode);
					if (changedNode == null) {
						changedNode = addNewNode(new SearchGeneralCommentAlgorithm(review, parentComment), newCommentNode);
					}
					addReplyNodes(review, newCommentNode, comment);
					refreshNode(changedNode);
				}
			}
			);
		}

		@Override
		public void createdOrEditedVersionedComment(final ReviewAdapter review, final CrucibleReviewItemInfo info,
				final VersionedComment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {

					final CrucibleFileInfo file = review.getFileByReviewInfo(info);

					AtlassianTreeNode newCommentNode = new VersionedCommentTreeNode(
							review, file, comment, new VersionedCommentClickAction());

					AtlassianTreeNode changedNode = replaceNode(
							new SearchVersionedCommentAlgorithm(review, file, comment),
							newCommentNode);
					if (changedNode == null) {
						changedNode = addNewNode(new NodeSearchAlgorithm() {
							@Override
							public boolean check(AtlassianTreeNode node) {
								if (node instanceof FileNameNode) {
									FileNameNode vnode = (FileNameNode) node;
									if (vnode.getReview().getPermId().equals(review.getPermId())
											&& vnode.getFile().getItemInfo().getId().equals(file.getItemInfo().getId())) {
										return true;
									}
								}
								return false;
							}
						}, newCommentNode);
					}
					addReplyNodes(review, file, newCommentNode, comment);
					refreshNode(changedNode);

					Editor editor = CrucibleHelper.getEditorForCrucibleFile(review, file);
					if (editor != null) {
						CrucibleHelper.openFileOnComment(project, review, file, comment);
					}
				}
			});
		}

		@Override
		public void createdOrEditedVersionedCommentReply(final ReviewAdapter review, final CrucibleReviewItemInfo info,
				final VersionedComment parentComment, final VersionedComment comment) {

			if (!thisReview.getPermId().equals(review.getPermId())) {
				// now what?
				return;
			}
			final CrucibleFileInfo file = thisReview.getFileByReviewInfo(info);

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					VersionedCommentTreeNode newCommentNode = new VersionedCommentTreeNode(thisReview, file, comment,
							AtlassianClickAction.EMPTY_ACTION);
					AtlassianTreeNode changedNode = replaceNode(new SearchVersionedCommentAlgorithm(thisReview, file, comment),
							newCommentNode);
					if (changedNode == null) {
						changedNode = addNewNode(new SearchVersionedCommentAlgorithm(thisReview, file, parentComment),
								newCommentNode);
					}
					addReplyNodes(thisReview, file, newCommentNode, comment);
					refreshNode(changedNode);
				}
			});
		}

		@Override
		public void updatedVersionedComment(final ReviewAdapter review, final CrucibleReviewItemInfo info,
				final VersionedComment comment) {

			final CrucibleFileInfo file = review.getFileByReviewInfo(info);

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					VersionedCommentTreeNode newCommentNode = new VersionedCommentTreeNode(review, file, comment,
							AtlassianClickAction.EMPTY_ACTION);
					AtlassianTreeNode changedNode = replaceNode(new SearchVersionedCommentAlgorithm(review, file, comment),
							newCommentNode);
					refreshNode(changedNode);
				}
			});
		}

		@Override
		public void updatedGeneralComment(final ReviewAdapter review, final GeneralComment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					GeneralCommentTreeNode newCommentNode = new GeneralCommentTreeNode(review, comment,
							AtlassianClickAction.EMPTY_ACTION);
					AtlassianTreeNode changedNode = replaceNode(new SearchGeneralCommentAlgorithm(review, comment),
							newCommentNode);
					refreshNode(changedNode);
				}
			});
		}

		@Override
		public void removedComment(final ReviewAdapter review, final Comment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					removeNode(new NodeSearchAlgorithm() {
						@Override
						public boolean check(AtlassianTreeNode node) {
							if (node instanceof VersionedCommentTreeNode) {
								VersionedCommentTreeNode vnode = (VersionedCommentTreeNode) node;
								if (vnode.getReview().getPermId().equals(review.getPermId())
										&& vnode.getComment().getPermId().equals(comment.getPermId())) {
									return true;
								}
							} else if (node instanceof GeneralCommentTreeNode) {
								GeneralCommentTreeNode vnode = (GeneralCommentTreeNode) node;
								if (vnode.getReview().getPermId().equals(review.getPermId())
										&& vnode.getComment().getPermId().equals(comment.getPermId())) {
									return true;
								}
							}
							return false;
						}
					});
				}
			}
			);

		}

		@Override
		public void publishedGeneralComment(final ReviewAdapter review, final GeneralComment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					GeneralCommentTreeNode newCommentNode = new GeneralCommentTreeNode(review, comment,
							AtlassianClickAction.EMPTY_ACTION);
					AtlassianTreeNode changedNode = replaceNode(new SearchGeneralCommentAlgorithm(review, comment),
							newCommentNode);
					refreshNode(changedNode);
				}
			}
			);
		}

		@Override
		public void publishedVersionedComment(final ReviewAdapter review, final CrucibleReviewItemInfo info,
				final VersionedComment comment) {
			final CrucibleFileInfo file = review.getFileByReviewInfo(info);
			
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					VersionedCommentTreeNode newCommentNode = new VersionedCommentTreeNode(review, file, comment,
							AtlassianClickAction.EMPTY_ACTION);
					AtlassianTreeNode changedNode = replaceNode(new SearchVersionedCommentAlgorithm(review, file, comment),
							newCommentNode);
					refreshNode(changedNode);
				}

			}
			);
		}

		@Override
		public void focusOnFileComments(final ReviewAdapter review, final CrucibleFileInfo file) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					AtlassianTreeModel model = (AtlassianTreeModel) commentTree.getModel();
					AtlassianTreeNode node = model.locateNode(new NodeSearchAlgorithm() {
						@Override
						public boolean check(AtlassianTreeNode node) {
							if (node instanceof FileNameNode) {
								FileNameNode vnode = (FileNameNode) node;
								if (vnode.getReview().equals(review)
										&& vnode.getFile().equals(file)) {
									return true;
								}
							}
							return false;
						}
					});
					commentTree.focusOnNode(node);
				}
			});
		}

		private void removeNode(final NodeSearchAlgorithm nodeLocator) {
			AtlassianTreeModel model = (AtlassianTreeModel) commentTree.getModel();
			AtlassianTreeNode node = model.locateNode(nodeLocator);
			if (node != null) {
				model.removeNodeFromParent(node);
			}
		}

		private AtlassianTreeNode replaceNode(final NodeSearchAlgorithm nodeLocator,
				final AtlassianTreeNode newNode) {
			AtlassianTreeModel model = (AtlassianTreeModel) commentTree.getModel();
			AtlassianTreeNode node = model.locateNode(nodeLocator);
			if (node != null) {
				AtlassianTreeNode parent = (AtlassianTreeNode) node.getParent();
				parent.remove(node);
				parent.addNode(newNode);
				return newNode;
			}
			return null;
		}

		private void addReplyNodes(final ReviewAdapter review, final AtlassianTreeNode parentNode,
				final GeneralComment comment) {
			for (GeneralComment reply : comment.getReplies()) {
				GeneralCommentTreeNode childNode = new GeneralCommentTreeNode(review, reply, AtlassianClickAction.EMPTY_ACTION);
				addNewNode(parentNode, childNode);
				addReplyNodes(review, childNode, reply);
			}
		}

		private void addReplyNodes(final ReviewAdapter review, final CrucibleFileInfo file, final AtlassianTreeNode parentNode,
				final VersionedComment comment) {
			for (VersionedComment reply : comment.getReplies()) {
				VersionedCommentTreeNode childNode = new VersionedCommentTreeNode(review, file, reply,
						AtlassianClickAction.EMPTY_ACTION);
				addNewNode(parentNode, childNode);
				addReplyNodes(review, file, childNode, reply);
			}

		}

		private AtlassianTreeNode addNewNode(NodeSearchAlgorithm parentLocator, AtlassianTreeNode newCommentNode) {
			AtlassianTreeModel model = (AtlassianTreeModel) commentTree.getModel();
			return addNewNode(model.locateNode(parentLocator), newCommentNode);
		}

		private AtlassianTreeNode addNewNode(AtlassianTreeNode parentNode, AtlassianTreeNode newCommentNode) {
			if (parentNode != null) {
				AtlassianTreeModel model = (AtlassianTreeModel) commentTree.getModel();
				model.insertNodeInto(newCommentNode, parentNode, parentNode.getChildCount());
				parentNode.addNode(newCommentNode);
				commentTree.expandPath(new TreePath(newCommentNode.getPath()));
				commentTree.expandPath(new TreePath(parentNode.getPath()));
				commentTree.focusOnNode(newCommentNode);
				return parentNode;
			}
			return null;
		}

		private class SearchVersionedCommentAlgorithm extends NodeSearchAlgorithm {
			private final ReviewAdapter review;
			private final CrucibleFileInfo file;
			private final VersionedComment parentComment;

			public SearchVersionedCommentAlgorithm(final ReviewAdapter review, final CrucibleFileInfo file,
					final VersionedComment parentComment) {
				this.review = review;
				this.file = file;
				this.parentComment = parentComment;
			}

			@Override
			public boolean check(AtlassianTreeNode node) {
				if (node instanceof VersionedCommentTreeNode) {
					VersionedCommentTreeNode vnode = (VersionedCommentTreeNode) node;
					if (vnode.getReview().getPermId().equals(review.getPermId())
							&& vnode.getFile().getItemInfo().getId().equals(file.getItemInfo().getId())
							&& vnode.getComment().getPermId().equals(parentComment.getPermId())) {
						return true;
					}
				}
				return false;
			}
		}

		private class SearchGeneralCommentAlgorithm extends NodeSearchAlgorithm {
			private final ReviewAdapter review;
			private final GeneralComment comment;

			public SearchGeneralCommentAlgorithm(final ReviewAdapter review, final GeneralComment comment) {
				this.review = review;
				this.comment = comment;
			}

			@Override
			public boolean check(AtlassianTreeNode node) {
				if (node instanceof GeneralCommentTreeNode) {
					GeneralCommentTreeNode vnode = (GeneralCommentTreeNode) node;
					if (vnode.getReview().getPermId().equals(review.getPermId())
							&& vnode.getComment().getPermId().equals(comment.getPermId())) {
						return true;
					}
				}
				return false;
			}
		}

		private class SearchGeneralSectionAlgorithm extends NodeSearchAlgorithm {
			@Override
			public boolean check(AtlassianTreeNode node) {
				return node instanceof GeneralSectionNode;
			}
		}
	}

	private class PopupMouseAdapter extends PopupAwareMouseAdapter {

		@Override
		public void onPopup(MouseEvent e) {
			if (!e.isPopupTrigger()) {
				return;
			}

			final CommentTree tree = (CommentTree) e.getComponent();

			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			if (path == null) {
				return;
			}
			tree.setSelectionPath(path);
			Object o = path.getLastPathComponent();
			if (!(o instanceof AtlassianTreeNode)) {
				return;
			}
			ActionManager aManager = ActionManager.getInstance();
			ActionGroup menu = (ActionGroup) aManager.getAction(TOOLBAR_ID);
			if (menu == null) {
				return;
			}
			aManager.createActionPopupMenu(MENU_PLACE, menu).getComponent().show(e.getComponent(), e.getX(), e.getY());
		}
	}

	private class VersionedCommentClickAction implements AtlassianClickAction {
		public void execute(final AtlassianTreeNode node, final int noOfClicks) {
			VersionedCommentTreeNode anode = (VersionedCommentTreeNode) node;
			CrucibleEvent event;
			switch (noOfClicks) {
				case 1:
					if (anode.getComment().isFromLineInfo()
							|| anode.getComment().isToLineInfo()) {
						event = new FocusOnLineCommentEvent(crucibleAgent,
								anode.getReview(),
								anode.getFile(), anode.getComment(), false);
					} else {
						event = new FocusOnVersionedCommentEvent(crucibleAgent,
								anode.getReview(),
								anode.getFile(), anode.getComment());
					}
					IdeaHelper.getReviewActionEventBroker(project).trigger(
							event);
					break;
				case 2:
					if (anode.getComment().isFromLineInfo()
							|| anode.getComment().isToLineInfo()) {
						event = new FocusOnLineCommentEvent(crucibleAgent,
								anode.getReview(),
								anode.getFile(), anode.getComment(), true);
					} else {
						event = new FocusOnVersionedCommentEvent(crucibleAgent,
								anode.getReview(),
								anode.getFile(), anode.getComment());
					}
					IdeaHelper.getReviewActionEventBroker(project).trigger(
							event);
					break;
				default:
					// do nothing
			}

		}
	}

	private class GeneralCommentClickAction implements AtlassianClickAction {
		public void execute(final AtlassianTreeNode node, final int noOfClicks) {
			switch (noOfClicks) {
				case 1:
					GeneralCommentTreeNode anode = (GeneralCommentTreeNode) node;
					IdeaHelper.getReviewActionEventBroker(project).trigger(
							new FocusOnReviewEvent(crucibleAgent, anode.getReview()));
					break;
				default:
					// do nothing
			}

		}
	}
}
