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

import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.file.FileNode;

public abstract class CommentTreeNode extends FileNode {
	private boolean editable = false;
	protected ReviewData review;

	protected CommentTreeNode(AtlassianClickAction action) {
		super("comment", action);
	}

	public void setEditable(boolean isEditable) {
		editable = isEditable;
	}

	public boolean isEditable() {
		return editable;
	}

	public ReviewData getReview() {
		return review;
	}

	public boolean isCompactable() {
		return false;
	}
}
