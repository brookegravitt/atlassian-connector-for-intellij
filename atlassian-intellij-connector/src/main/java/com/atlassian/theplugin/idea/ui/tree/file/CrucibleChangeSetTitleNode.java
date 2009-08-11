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

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import static com.intellij.ui.SimpleTextAttributes.STYLE_ITALIC;
import com.intellij.util.Icons;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 15, 2008
 * Time: 6:03:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleChangeSetTitleNode extends FileNode {
	private ReviewAdapter review;
	private static final TreeCellRenderer MY_RENDERER = new CrucibleChangeSetTitleNodeRenderer();

	public CrucibleChangeSetTitleNode(ReviewAdapter review, AtlassianClickAction action) {
		super(review.getName(), action);
		this.review = review;
	}

	public CrucibleChangeSetTitleNode(final CrucibleChangeSetTitleNode node) {
		super(node.getReview().getName(), node.getAtlassianClickAction());
		this.review = node.review;
	}

	@Override
	public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}

	public ReviewAdapter getReview() {
		return review;
	}

	public void setReview(ReviewAdapter review) {
		this.review = review;
	}

	private static class CrucibleChangeSetTitleNodeRenderer extends ColoredTreeCellRenderer {
		private static final SimpleTextAttributes TEXT_ITALIC = new SimpleTextAttributes(STYLE_ITALIC, null);
		private static final SimpleTextAttributes RED_ITALIC = new SimpleTextAttributes(STYLE_ITALIC, Color.red);

		public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded,
										  boolean leaf, int row, boolean hasFocus) {
			CrucibleChangeSetTitleNode node = (CrucibleChangeSetTitleNode) value;
			append(node.getReview().getPermId().getId(), new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD,
					Color.GRAY));
			append(" ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
			append(node.getReview().getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
			try {
				int noOfDefects = node.getReview().getNumberOfVersionedCommentsDefects()
						+ node.getReview().getNumberOfGeneralCommentsDefects();

				int noOfComments = node.getReview().getNumberOfGeneralComments() 
						+ node.getReview().getNumberOfVersionedComments();

                int noOfUnreadComments = node.getReview().getNumberOfUnreadComments();

				append(" ",	TEXT_ITALIC);
				append(String.valueOf(noOfComments), TEXT_ITALIC);
				append(" comment", TEXT_ITALIC);
				if (noOfComments != 1) {
					append("s", TEXT_ITALIC);
				}

                if (noOfUnreadComments > 0 ) {
                    append(", ", TEXT_ITALIC);
                    append(String.valueOf(noOfUnreadComments), TEXT_ITALIC);
                    append(" unread", TEXT_ITALIC);
                }

				if (noOfDefects > 0) {
					append(", ", TEXT_ITALIC);
					append(String.valueOf(noOfDefects),	RED_ITALIC);
					append(" defect", RED_ITALIC);
					if (noOfDefects != 1) {
						append("s", RED_ITALIC);
					}
				}
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				// ignore
			}
			setIcon(expanded ? Icons.DIRECTORY_OPEN_ICON : Icons.DIRECTORY_CLOSED_ICON);
		}
	}

	public AtlassianTreeNode getClone() {
		return new CrucibleChangeSetTitleNode(this);
	}

	public int compareTo(Object o) {
		if (o instanceof CrucibleChangeSetTitleNode) {
			return 0;
		}
		// this node should always be the first node
		return -1;
	}
}
