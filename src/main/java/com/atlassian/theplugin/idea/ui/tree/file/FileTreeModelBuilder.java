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

package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.commons.BambooFileInfo;
import com.atlassian.theplugin.commons.VersionedFileInfo;
import com.atlassian.theplugin.commons.bamboo.BambooChangeSet;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.clickaction.CrucibleChangeSetClickAction;
import com.atlassian.theplugin.idea.ui.tree.clickaction.CrucibleFileClickAction;
import com.atlassian.theplugin.idea.ui.tree.clickaction.CrucibleVersionedCommentClickAction;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class FileTreeModelBuilder {

	private FileTreeModelBuilder() {
		// this is a utility class
	}

	public static AtlassianTreeModel buildTreeModelFromBambooChangeSet(Project project, BambooChangeSet changeSet) {
		FileNode root = new FolderNode("/");
		FileTreeModel model = new FileTreeModel(root);
		for (BambooFileInfo f : changeSet.getFiles()) {
			model.addFile(project, root, f);
		}
		model.compactModel(model.getRoot());
		return model;
	}

	@Nullable
	private static PsiFile guessCorrespondingPsiFile(final Project project, BambooFileInfo file) {
		final PsiFile[] psifiles = PsiManager.getInstance(project).getShortNamesCache().getFilesByName(
				file.getFileDescriptor().getName());
		return CodeNavigationUtil.guessMatchingFile(file.getFileDescriptor().getUrl(), psifiles, project.getBaseDir());
	}

	public static AtlassianTreeModel buildFlatTreeModelFromBambooChangeSet(final Project project, BambooChangeSet changeSet) {
		FileTreeModel model = new FileTreeModel(new FolderNode("/"));
		for (BambooFileInfo f : changeSet.getFiles()) {
			PsiFile psiFile = guessCorrespondingPsiFile(project, f);
			model.getRoot().addChild(new BambooFileNode(f, psiFile));
		}
		return model;
	}


	public static AtlassianTreeModel buildFlatModelFromCrucibleChangeSet(final Project project, final ReviewData review,
			List<CrucibleFileInfo> files) {
		AtlassianTreeModel model = new FileTreeModel(new CrucibleChangeSetTitleNode(review,
				new CrucibleChangeSetClickAction(project, review)));
		model.getRoot().addNode(new CrucibleGeneralCommentsNode(review, null));
		AtlassianTreeNode filesNode = new CrucibleFilesNode(review);
		model.getRoot().addNode(filesNode);
		for (final CrucibleFileInfo file : files) {
			//according to filter show only "proper files"
			CrucibleFileNode childNode = new CrucibleFileNode(review, file,
					new CrucibleFileClickAction(project, review, file));

			fillFileComments(childNode, review, file, project);
			filesNode.addNode(childNode);
		}
		return model;
	}

	public static AtlassianTreeModel buildTreeModelFromCrucibleChangeSet(final Project project, final ReviewData review,
			final List<CrucibleFileInfo> files) {
		FileNode root = new CrucibleChangeSetTitleNode(review, new CrucibleChangeSetClickAction(project, review));

		FileTreeModel model = new FileTreeModel(root);

		model.getRoot().addNode(new CrucibleGeneralCommentsNode(review, null));
		FileNode filesNode = new CrucibleFilesNode(review);
		model.getRoot().addNode(filesNode);
 
		for (final CrucibleFileInfo file : files) {
			//according to filter show only "proper files"
			CrucibleFileNode childNode = new CrucibleFileNode(review, file,
					new CrucibleFileClickAction(project, review, file));

			FileNode node = model.createPlace(filesNode, file);

			fillFileComments(childNode, review, file, project);
			node.addChild(childNode);
		}
		model.compactModel(filesNode);
		return model;
	}

	private static void fillFileComments(CrucibleFileNode node, ReviewData review, CrucibleFileInfo file, Project project) {
		List<VersionedComment> fileComments = getFileVersionedComments(file);
		CrucibleVersionedCommentClickAction action = new CrucibleVersionedCommentClickAction(project);
		for (VersionedComment c : fileComments) {
			if (!c.isDeleted()) {
				VersionedCommentTreeNode commentNode = new VersionedCommentTreeNode(review, file, c, action);
				node.addNode(commentNode);

				for (VersionedComment reply : c.getReplies()) {
					commentNode.addNode(new VersionedCommentTreeNode(review, file, reply, action));
				}
			}
		}

		List<VersionedComment> lineComments = getLineVersionedComments(file);
		for (VersionedComment c : lineComments) {
			if (!c.isDeleted()) {
				VersionedCommentTreeNode commentNode = new VersionedCommentTreeNode(review, file, c, action);
				node.addNode(commentNode);

				for (VersionedComment reply : c.getReplies()) {
					commentNode.addNode(new VersionedCommentTreeNode(review, file, reply, action));
				}
			}
		}
	}

	private static List<VersionedComment> getFileVersionedComments(CrucibleFileInfo file) {
		List<VersionedComment> list = new ArrayList<VersionedComment>();
		List<VersionedComment> comments = file.getItemInfo().getComments();
		if (comments == null) {
			return null;
		}

		for (VersionedComment c : comments) {
			if (c.getFromStartLine() + c.getFromEndLine() + c.getToStartLine() + c.getToEndLine() == 0) {
				if (!c.isReply()) {
					list.add(c);
				}
			}
		}
		return list;
	}

	private static List<VersionedComment> getLineVersionedComments(CrucibleFileInfo file) {
		List<VersionedComment> list = new ArrayList<VersionedComment>();
		List<VersionedComment> thisFileComments = file.getItemInfo().getComments();
		if (thisFileComments == null) {
			return null;
		}

		for (VersionedComment c : thisFileComments) {
			if (c.getFromStartLine() + c.getFromEndLine() + c.getToStartLine() + c.getToEndLine() != 0) {
				if (!c.isReply()) {
					list.add(c);
				}
			}
		}
		return list;
	}

	private static class FileTreeModel extends AtlassianTreeModel {
		public FileTreeModel(FileNode root) {
			super(root);
		}

		@Override
		public FileNode getRoot() {
			return (FileNode) super.getRoot();
		}

		public void addFile(Project project, FileNode root, BambooFileInfo file) {
			PsiFile psiFile = guessCorrespondingPsiFile(project, file);
			FileNode node = createPlace(root, file);
			node.addChild(new BambooFileNode(file, AtlassianClickAction.EMPTY_ACTION, psiFile));
		}

		private FileNode createPlace(FileNode root, VersionedFileInfo file) {
			int idx = 0;
			String fileName = file.getFileDescriptor().getUrl();
			FileNode node = root;
			do {
				int newIdx = fileName.indexOf('/', idx);
				if (newIdx != -1) {
					String newNodeName = fileName.substring(idx, newIdx);
					if (newNodeName.length() > 0) {
						if (!node.hasNode(newNodeName)) {
							FileNode newNode = new FolderNode(newNodeName);
							node.addChild(newNode);
							node = newNode;
						} else {
							node = node.getNode(newNodeName);
						}
					}
				}
				idx = newIdx + 1;
			} while (idx > 0);
			return node;
		}

		private void compactModel(FileNode node) {
			if (node.isLeaf() || !node.isCompactable()) {
				return;
			}

			java.util.List<FileNode> ch = new ArrayList<FileNode>();

			for (FileNode n : node.getChildren().values()) {
				ch.add(n);
			}

			node.removeChildren();

			for (FileNode n : ch) {
				compactModel(n);
				if (n.getChildCount() == 1) {
					FileNode cn = (FileNode) n.getFirstChild();
					if (!cn.isLeaf() && cn.isCompactable()) {
						String newName = n.getName() + "/" + cn.getName();
						cn.setName(newName);
						node.addChild(cn);
					} else {
						node.addChild(n);
					}
				} else {
					node.addChild(n);
				}
			}
		}
	}
}
