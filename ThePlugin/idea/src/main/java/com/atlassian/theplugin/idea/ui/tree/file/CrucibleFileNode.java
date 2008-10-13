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

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfoImpl;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleReviewItemInfo;
import com.atlassian.theplugin.idea.crucible.ReviewDataImpl;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class CrucibleFileNode extends FileNode {

	private CrucibleFileInfo file;
	private static final ColoredTreeCellRenderer MY_RENDERER = new CrucibleFileNodeRenderer();
	private ReviewDataImpl review;

	public CrucibleFileNode(final ReviewDataImpl review, final CrucibleFileInfo file) {
		this(review, file, AtlassianClickAction.EMPTY_ACTION);
	}

	public CrucibleFileNode(final ReviewDataImpl review, final CrucibleFileInfo file,
			final AtlassianClickAction action) {
		super(FilenameUtils.getName(file.getFileDescriptor().getUrl()), action);
		this.review = review;
		this.file = file;
	}

	public CrucibleFileNode(final CrucibleFileNode node) {
		this(node.getReview(), node.getFile(), node.getAtlassianClickAction());
	}

	@Override
	public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}

	public CrucibleFileInfo getFile() {
		return file;
	}

	public ReviewDataImpl getReview() {
		return review;
	}

	public void setReview(ReviewDataImpl review) {
		this.review = review;
		for (CrucibleReviewItemInfo info : review.getReviewItems()) {
			if (info.getId().equals(file.getItemInfo().getId())) {
				((CrucibleFileInfoImpl) file).setItemInfo(info);
				break;
			}
		}
	}

	private static class CrucibleFileNodeRenderer extends ColoredTreeCellRenderer {
		private static final SimpleTextAttributes TEXT_ITALIC =
				new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, null);
		private static final SimpleTextAttributes RED_ITALIC =
				new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, Color.red);

		public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			CrucibleFileNode node = (CrucibleFileNode) value;
			append(node.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

			StringBuilder txt = new StringBuilder();
			txt.append(" (rev: ");
			switch (node.getFile().getCommitType()) {
				case Added:
					txt.append(node.getFile().getFileDescriptor().getRevision());
					break;
				case Deleted:
					txt.append(node.getFile().getOldFileDescriptor().getRevision());
					break;
				case Modified:
				case Copied:
				case Moved:
					txt.append(node.getFile().getOldFileDescriptor().getRevision());
					txt.append("-");
					txt.append(node.getFile().getFileDescriptor().getRevision());
					break;
				case Unknown:
				default:
					txt.append(node.getFile().getOldFileDescriptor().getRevision());
					txt.append("-");
					txt.append(node.getFile().getFileDescriptor().getRevision());
					break;
			}
			txt.append(")");
			append(txt.toString(), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);

			int noOfComments = node.getFile().getItemInfo().getNumberOfComments();
			if (noOfComments > 0) {
				int noOfDefects = node.getFile().getItemInfo().getNumberOfDefects();
				append(" ",
						TEXT_ITALIC);
				append(String.valueOf(noOfComments),
						TEXT_ITALIC);
				append(" comment", TEXT_ITALIC);
				if (noOfComments != 1) {
					append("s", TEXT_ITALIC);
				}

				if (noOfDefects > 0) {
					append(" (", TEXT_ITALIC);
					append(String.valueOf(noOfDefects),
							RED_ITALIC);
					append(" defect",
							RED_ITALIC);
					if (noOfDefects != 1) {
						append("s",
								RED_ITALIC);
					}
					append(")", TEXT_ITALIC);
				}
			}

			FileTypeManager mgr = FileTypeManager.getInstance();
			FileType type = mgr.getFileTypeByFileName(node.getName());
			setIcon(type.getIcon());
			switch (node.getFile().getCommitType()) {
				case Added:
					setForeground(FileStatus.COLOR_ADDED);
					break;
				case Deleted:
					setForeground(FileStatus.COLOR_MISSING);
					break;
				case Modified:
				case Moved:
				case Copied:
					setForeground(FileStatus.COLOR_MODIFIED);
					break;
				case Unknown:
				default:
					setForeground(FileStatus.COLOR_UNKNOWN);
					break;
			}
		}
	}

	public AtlassianTreeNode getClone() {
		return new CrucibleFileNode(this);
	}

	public boolean isCompactable() {
		return false;
	}
}
