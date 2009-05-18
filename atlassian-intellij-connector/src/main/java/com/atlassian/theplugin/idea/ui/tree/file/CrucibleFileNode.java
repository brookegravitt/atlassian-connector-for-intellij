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

import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Icons;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class CrucibleFileNode extends FileNode {

	private CrucibleFileInfo file;
	private static final ColoredTreeCellRenderer MY_RENDERER = new CrucibleFileNodeRenderer();
	private ReviewAdapter review;

	public CrucibleFileNode(final ReviewAdapter review, final CrucibleFileInfo file) {
		this(review, file, AtlassianClickAction.EMPTY_ACTION);
	}

	public CrucibleFileNode(final ReviewAdapter review, final CrucibleFileInfo file,
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

	public ReviewAdapter getReview() {
		return review;
	}

	public void setReview(ReviewAdapter review) {
		this.review = review;
		for (CrucibleFileInfo info : review.getFiles()) {
			if (info.getPermId().equals(file.getPermId())) {
				file.setVersionedComments(info.getVersionedComments());
				break;
			}
		}
	}

	private static class CrucibleFileNodeRenderer extends ColoredTreeCellRenderer {
		private static final SimpleTextAttributes TEXT_ITALIC =
				new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, null);
		private static final SimpleTextAttributes RED_ITALIC =
				new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, Color.red);

		@Override
		public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			CrucibleFileNode node = (CrucibleFileNode) value;
			append(node.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

			StringBuilder txt = new StringBuilder();
			final CommitType commitType = node.getFile().getCommitType();
			switch (commitType) {
				case Moved:
					txt.append(" moved, ");
					break;
				default:
					break;
			}
			txt.append(" (rev: ");
			switch (commitType) {
				case Added:
					txt.append(node.getFile().getFileDescriptor().getRevision());
					break;
				case Deleted:
					txt.append(node.getFile().getOldFileDescriptor().getRevision());
					break;
				case Modified:
				case Copied:
				case Moved:
				case Unknown:
				default:
					String oldRev = node.getFile().getOldFileDescriptor().getRevision();
					if (!StringUtils.isEmpty(oldRev)) {
						txt.append(oldRev);
					} else {
						txt.append("Unknown");
					}
					txt.append("-");
					String newRev = node.getFile().getFileDescriptor().getRevision();
					if (!StringUtils.isEmpty(newRev)) {
						txt.append(newRev);
					} else {
						txt.append("Unknown");
					}
					break;
			}
			txt.append(")");
			append(txt.toString(), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);

			int noOfComments = node.getFile().getNumberOfComments();
			if (noOfComments > 0) {
				int noOfDefects = node.getFile().getNumberOfCommentsDefects();
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

			if (node.getFile().getFileType() == com.atlassian.theplugin.commons.crucible.api.model.FileType.Directory) {
				setIcon(Icons.DIRECTORY_OPEN_ICON);
			} else {
				FileTypeManager mgr = FileTypeManager.getInstance();
				FileType type = mgr.getFileTypeByFileName(node.getName());
				setIcon(type.getIcon());
			}
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

	@Override
	public AtlassianTreeNode getClone() {
		return new CrucibleFileNode(this);
	}

	@Override
	public boolean isCompactable() {
		return false;
	}
}
