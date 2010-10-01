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

package com.atlassian.theplugin.idea.ui.tree.comment;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class FileNameNode extends AtlassianTreeNode {
	private ReviewAdapter review;
	private CrucibleFileInfo file;
	private static final TreeCellRenderer MY_RENDERER = new MyRenderer();

	public FileNameNode(ReviewAdapter review, CrucibleFileInfo file, AtlassianClickAction action) {
		super(action);
		this.review = review;
		this.file = file;
	}

	public FileNameNode(final FileNameNode node) {
		super(node.getAtlassianClickAction());
		this.review = node.review;
		this.file = node.file;
	}

	public CrucibleFileInfo getFile() {
		return file;
	}

	public ReviewAdapter getReview() {
		return review;
	}

	public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}

	public AtlassianTreeNode getClone() {
		return new FileNameNode(this);
	}

	public int compareTo(Object o) {
		if (o instanceof FileNameNode) {
			FileNameNode fnn = (FileNameNode) o;
			return file.getFileDescriptor().getUrl().compareTo(fnn.getFile().getFileDescriptor().getUrl());
		}
		return -1;  // fixme
	}

	private static class MyRenderer implements TreeCellRenderer {

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
			JPanel panel = new JPanel(new FormLayout("2dlu, left:pref:grow, 2dlu", "2dlu, pref:grow, 2dlu"));
			SimpleColoredComponent component = new SimpleColoredComponent();			
			FileNameNode node = (FileNameNode) value;
			CrucibleFileInfo file = node.getFile();
			component.append(file.getFileDescriptor().getUrl(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

			StringBuilder txt = new StringBuilder();
			txt.append(" (rev: ");
			txt.append(file.getOldFileDescriptor().getRevision());
			txt.append("-");
			txt.append(file.getFileDescriptor().getRevision());
			txt.append(")");
			component.append(txt.toString(), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
			panel.add(component, new CellConstraints(2, 2));
			

			panel.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder")
					: BorderFactory.createEmptyBorder(1, 1, 1, 1));
			return panel;

		}
	}
}
