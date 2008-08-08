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

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.*;
import com.atlassian.theplugin.idea.ui.AtlassianToolbar;
import com.atlassian.theplugin.idea.ui.tree.*;
import com.atlassian.theplugin.idea.ui.tree.comment.FileNameNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralSectionNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.FolderNode;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 15, 2008
 * Time: 1:26:58 PM
 * To change this template use File | Settings | File Templates.
 */
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

	public CommentTreePanel(Project project) {
		this.project = project;
		IdeaHelper.getReviewActionEventBroker().registerListener(crucibleAgent);
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

	private void addGeneralCommentTree(AtlassianTreeNode root, final ReviewData review,
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

	private void addVersionedCommentTree(AtlassianTreeNode root, final ReviewData review,
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


	private AtlassianTreeModel createTreeModel(final ReviewData review) {
		ROOT.removeAllChildren();
		AtlassianTreeModel model = new AtlassianTreeModel(ROOT);

		List<GeneralComment> generalComments;
		try {
			generalComments = review.getGeneralComments();
			AtlassianTreeNode generalNode = new GeneralSectionNode(review, new AtlassianClickAction() {
				public void execute(final AtlassianTreeNode node, final int noOfClicks) {
					switch (noOfClicks) {
						case 1:
							GeneralSectionNode anode = (GeneralSectionNode) node;
							IdeaHelper.getReviewActionEventBroker().trigger(
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
			for (CrucibleFileInfo file : review.getFiles()) {
					AtlassianTreeNode fileNode = new FileNameNode(review, file, new AtlassianClickAction() {
						public void execute(final AtlassianTreeNode node, final int noOfClicks) {
							switch (noOfClicks) {
								case 1:
									FileNameNode anode = (FileNameNode) node;
									IdeaHelper.getReviewActionEventBroker().trigger(
											new FocusOnFileEvent(crucibleAgent, anode.getReview(), anode.getFile()));
									break;
								default:
									// do nothing
							}

						}
					});
					ROOT.addNode(fileNode);
					for (VersionedComment comment : file.getVersionedComments()) {
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

	public void filterTreeNodes(Filter filter) {
			commentTree.setModel(fullModel.getFilteredModel(filter));
			refreshTree();
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
		public void showReview(final ReviewData review) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					commentTree.setVisible(false);
					fullModel = createTreeModel(review);
					commentTree = new CommentTree(fullModel);
					refreshTree();
				}
			});
		}

		@Override
		public void focusOnGeneralComments(final ReviewData review) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					AtlassianTreeModel model = (AtlassianTreeModel) commentTree.getModel();
					AtlassianTreeNode node = model.locateNode(new NodeSearchAlgorithm() {
						@Override
						public boolean check(AtlassianTreeNode node1) {
							return node1 instanceof GeneralSectionNode;
						}
					});
					commentTree.focusOnNode(node);
				}
			});
		}

		@Override
		public void createdGeneralComment(final ReviewData review, final GeneralComment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					AtlassianTreeNode newCommentNode
							= new GeneralCommentTreeNode(review, comment, new GeneralCommentClickAction());

					if (!replaceNode(new NodeSearchAlgorithm() {
						public boolean check(final AtlassianTreeNode node) {
							if (node instanceof GeneralCommentTreeNode) {
								GeneralCommentTreeNode anode = (GeneralCommentTreeNode) node;
								if (anode.getComment().getPermId().equals(comment.getPermId())) {
									return true;
								}
							}
							return false;
						}
					}, newCommentNode)) {
						addNewNode(new NodeSearchAlgorithm() {
							@Override
							public boolean check(AtlassianTreeNode node) {
								return node instanceof GeneralSectionNode;
							}
						}, newCommentNode);
					}
					addReplyNodes(review, newCommentNode, comment);
				}
			}
			);
		}

		private void addReplyNodes(final ReviewData review, final AtlassianTreeNode parentNode, final GeneralComment comment) {
			for (GeneralComment reply : comment.getReplies()) {
				GeneralCommentTreeNode childNode = new GeneralCommentTreeNode(review, reply, AtlassianClickAction.EMPTY_ACTION);
				addNewNode(parentNode, childNode);
				addReplyNodes(review, childNode, reply);
			}
		}

		@Override
		public void createdGeneralCommentReply(final ReviewData review, final GeneralComment parentComment,
				final GeneralComment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					GeneralCommentTreeNode newCommentNode = new GeneralCommentTreeNode(review, comment,
							AtlassianClickAction.EMPTY_ACTION);
					if (!replaceNode(new NodeSearchAlgorithm() {
						public boolean check(final AtlassianTreeNode node) {
							if (node instanceof GeneralCommentTreeNode) {
								GeneralCommentTreeNode anode = (GeneralCommentTreeNode) node;
								if (anode.getComment().getPermId().equals(comment.getPermId())) {
									return true;
								}
							}
							return false;
						}
					}, newCommentNode)) {
						addNewNode(new NodeSearchAlgorithm() {
							@Override
							public boolean check(AtlassianTreeNode node) {
								if (node instanceof GeneralCommentTreeNode) {
									GeneralCommentTreeNode vnode = (GeneralCommentTreeNode) node;
									if (vnode.getReview().equals(review) && vnode.getComment().equals(parentComment)) {
										return true;
									}
								}
								return false;
							}
						}, newCommentNode);
					}
					addReplyNodes(review, newCommentNode, comment);
				}
			}
			);
		}

		@Override
		public void createdVersionedComment(final ReviewData review, final CrucibleFileInfo file,
				final VersionedComment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {

					AtlassianTreeNode newCommentNode
							= new VersionedCommentTreeNode(review, file, comment, new VersionedCommentClickAction());

					if (!replaceNode(new NodeSearchAlgorithm() {
						public boolean check(final AtlassianTreeNode node) {
							if (node instanceof VersionedCommentTreeNode) {
								VersionedCommentTreeNode anode = (VersionedCommentTreeNode) node;
								if (anode.getComment().getPermId().equals(comment.getPermId())) {
									return true;
								}
							}
							return false;
						}
					}, newCommentNode)) {
						addNewNode(new NodeSearchAlgorithm() {
							@Override
							public boolean check(AtlassianTreeNode node) {
								if (node instanceof FileNameNode) {
									FileNameNode vnode = (FileNameNode) node;
									if (vnode.getReview().getPermId().equals(review.getPermId())
											&& vnode.getFile().getPermId().equals(file.getPermId())) {
										return true;
									}
								}
								return false;
							}
						}, newCommentNode);
					}
					addReplyNodes(review, file, newCommentNode, comment);

					Editor editor = CrucibleHelper.getEditorForCrucibleFile(review, file);
					if (editor != null) {
						CrucibleHelper.openFileOnComment(project, review, file, comment);
					}
				}
			});
		}

		private void addReplyNodes(final ReviewData review, final CrucibleFileInfo file, final AtlassianTreeNode parentNode,
				final VersionedComment comment) {
			for (VersionedComment reply : comment.getReplies()) {
				VersionedCommentTreeNode childNode = new VersionedCommentTreeNode(review, file, reply,
						AtlassianClickAction.EMPTY_ACTION);
				addNewNode(parentNode, childNode);
				addReplyNodes(review, file, childNode, reply);
			}

		}

		@Override
		public void createdVersionedCommentReply(final ReviewData review, final CrucibleFileInfo file,
				final VersionedComment parentComment, final VersionedComment comment) {

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					VersionedCommentTreeNode newCommentNode = new VersionedCommentTreeNode(review, file, comment,
							AtlassianClickAction.EMPTY_ACTION);
					if (!replaceNode(new NodeSearchAlgorithm() {
						public boolean check(final AtlassianTreeNode node) {
							if (node instanceof VersionedCommentTreeNode) {
								VersionedCommentTreeNode anode = (VersionedCommentTreeNode) node;
								if (anode.getComment().getPermId().equals(comment.getPermId())) {
									return true;
								}
							}
							return false;
						}
					}, newCommentNode)) {
						addNewNode(new NodeSearchAlgorithm() {
							@Override
							public boolean check(AtlassianTreeNode node) {
								if (node instanceof VersionedCommentTreeNode) {
									VersionedCommentTreeNode vnode = (VersionedCommentTreeNode) node;
									if (vnode.getReview().equals(review)
											&& vnode.getFile().equals(file)
											&& vnode.getComment().equals(parentComment)) {
										return true;
									}
								}
								return false;
							}
						}, newCommentNode);
					}
					addReplyNodes(review, file, newCommentNode, comment);
				}
			});
		}

		@Override
		public void updatedVersionedComment(final ReviewData review, final CrucibleFileInfo file,
				final VersionedComment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					replaceNode(new NodeSearchAlgorithm() {
						@Override
						public boolean check(AtlassianTreeNode node) {
							if (node instanceof VersionedCommentTreeNode) {
								VersionedCommentTreeNode vnode = (VersionedCommentTreeNode) node;
								if (vnode.getReview().equals(review)
										&& vnode.getFile().equals(file)
										&& vnode.getComment().equals(comment)) {
									return true;
								}
							}
							return false;
						}
					}, new VersionedCommentTreeNode(review, file, comment, AtlassianClickAction.EMPTY_ACTION));
				}
			});
		}

		@Override
		public void updatedGeneralComment(final ReviewData review, final GeneralComment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					replaceNode(new NodeSearchAlgorithm() {
						@Override
						public boolean check(AtlassianTreeNode node) {
							if (node instanceof GeneralCommentTreeNode) {
								GeneralCommentTreeNode vnode = (GeneralCommentTreeNode) node;
								if (vnode.getReview().equals(review)
										&& vnode.getComment().equals(comment)) {
									return true;
								}
							}
							return false;
						}
					}, new GeneralCommentTreeNode(review, comment, AtlassianClickAction.EMPTY_ACTION));
				}
			});
		}

		@Override
		public void removedComment(final ReviewData review, final Comment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					removeNode(new NodeSearchAlgorithm() {
						@Override
						public boolean check(AtlassianTreeNode node) {
							if (node instanceof VersionedCommentTreeNode) {
								VersionedCommentTreeNode vnode = (VersionedCommentTreeNode) node;
								if (vnode.getReview().equals(review)
										&& vnode.getComment().equals(comment)) {
									return true;
								}
							} else if (node instanceof GeneralCommentTreeNode) {
								GeneralCommentTreeNode vnode = (GeneralCommentTreeNode) node;
								if (vnode.getReview().equals(review)
										&& vnode.getComment().equals(comment)) {
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
		public void publishedGeneralComment(final ReviewData review, final GeneralComment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {

					replaceNode(new NodeSearchAlgorithm() {
						@Override
						public boolean check(AtlassianTreeNode node) {
							if (node instanceof GeneralCommentTreeNode) {
								GeneralCommentTreeNode vnode = (GeneralCommentTreeNode) node;
								if (vnode.getReview().equals(review)
										&& vnode.getComment().equals(comment)) {
									return true;
								}
							}
							return false;
						}
					}, new GeneralCommentTreeNode(review, comment, AtlassianClickAction.EMPTY_ACTION));
				}
			}
			);
		}

		@Override
		public void publishedVersionedComment(final ReviewData review, final CrucibleFileInfo file,
				final VersionedComment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {

					replaceNode(new NodeSearchAlgorithm() {
						@Override
						public boolean check(AtlassianTreeNode node) {
							if (node instanceof VersionedCommentTreeNode) {
								VersionedCommentTreeNode vnode = (VersionedCommentTreeNode) node;
								if (vnode.getReview().equals(review)
										&& vnode.getComment().equals(comment)) {
									return true;
								}
							}
							return false;
						}
					}, new VersionedCommentTreeNode(review, file, comment,
							AtlassianClickAction.EMPTY_ACTION));
				}

			}
			);
		}

		public void commentsChanged(final ReviewData review, final CrucibleFileInfo file) {
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				public void run() {
				}
			});
		}

		@Override
		public void focusOnFileComments(final ReviewData review, final CrucibleFileInfo file) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					AtlassianTreeModel model = (AtlassianTreeModel) commentTree.getModel();
					AtlassianTreeNode node = model.locateNode(new NodeSearchAlgorithm() {
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

		private boolean replaceNode(final NodeSearchAlgorithm nodeLocator,
				final AtlassianTreeNode newNode) {
			boolean replaced = false;
			AtlassianTreeModel model = (AtlassianTreeModel) commentTree.getModel();
			AtlassianTreeNode node = model.locateNode(nodeLocator);
			if (node != null) {
				AtlassianTreeNode parent = (AtlassianTreeNode) node.getParent();
				int index = model.getIndexOfChild(parent, node);
				model.removeNodeFromParent(node);
				model.insertNodeInto(newNode, parent, index);
				commentTree.expandPath(new TreePath(newNode.getPath()));
				commentTree.focusOnNode(newNode);
				replaced = true;
			}
			return replaced;
		}

		private void addNewNode(NodeSearchAlgorithm parentLocator, AtlassianTreeNode newCommentNode) {
			AtlassianTreeModel model = (AtlassianTreeModel) commentTree.getModel();
			AtlassianTreeNode parentNode = model.locateNode(parentLocator);
			if (parentNode != null) {
				model.insertNodeInto(newCommentNode, parentNode, parentNode.getChildCount());
				commentTree.expandPath(new TreePath(newCommentNode.getPath()));
				commentTree.expandPath(new TreePath(parentNode.getPath()));
				commentTree.focusOnNode(newCommentNode);
			}
		}


		private void addNewNode(AtlassianTreeNode parentNode, AtlassianTreeNode newCommentNode) {
			AtlassianTreeModel model = (AtlassianTreeModel) commentTree.getModel();
			if (parentNode != null) {
				model.insertNodeInto(newCommentNode, parentNode, parentNode.getChildCount());
				commentTree.expandPath(new TreePath(newCommentNode.getPath()));
				commentTree.expandPath(new TreePath(parentNode.getPath()));
				commentTree.focusOnNode(newCommentNode);
			}
		}

	}

	private class PopupMouseAdapter extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			processPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			processPopup(e);
		}

		public void processPopup(MouseEvent e) {
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
					IdeaHelper.getReviewActionEventBroker().trigger(
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
					IdeaHelper.getReviewActionEventBroker().trigger(
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
					IdeaHelper.getReviewActionEventBroker().trigger(
							new FocusOnReviewEvent(crucibleAgent, anode.getReview()));
					break;
				default:
					// do nothing
			}

		}
	}
}
