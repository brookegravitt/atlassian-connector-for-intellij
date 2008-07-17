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

package com.atlassian.theplugin.commons.crucible.api.model;

import java.util.List;
import java.util.ArrayList;

public class VersionedCommentBean extends CommentBean implements VersionedComment {
	private PermId permId;
	private PermId reviewItemId;
	private int fromStartLine = 0;
	private int fromEndLine = 0;
	private boolean fromLineInfo = false;
	private int toStartLine = 0;
	private int toEndLine = 0;
	private boolean toLineInfo = false;
	private List<VersionedComment> replies = new ArrayList<VersionedComment>();

	public VersionedCommentBean() {
	}

	public PermId getPermId() {
		return permId;
	}

	public void setPermId(PermId permId) {
		this.permId = permId;
	}

	public PermId getReviewItemId() {
		return reviewItemId;
	}

	public void setReviewItemId(PermId reviewItemId) {
		this.reviewItemId = reviewItemId;
	}

	public int getFromStartLine() {
		return fromStartLine;
	}

	public void setFromStartLine(int startLine) {
		this.fromStartLine = startLine;
	}

	public int getFromEndLine() {
		return fromEndLine;
	}

	public List<VersionedComment> getReplies() {
		return replies;
	}

	public void setReplies(List<VersionedComment> replies) {
		this.replies = replies;
	}

	public void addReply(VersionedComment comment) {
		replies.add(comment);
	}

	public void setFromEndLine(int endLine) {
		this.fromEndLine = endLine;
	}

	public int getToStartLine() {
		return toStartLine;
	}

	public void setToStartLine(int startLine) {
		this.toStartLine = startLine;
	}

	public int getToEndLine() {
		return toEndLine;
	}

	public void setToEndLine(int endLine) {
		this.toEndLine = endLine;
	}

	public boolean isFromLineInfo() {
		return fromLineInfo;
	}

	public void setFromLineInfo(boolean fromLineInfo) {
		this.fromLineInfo = fromLineInfo;
	}

	public boolean isToLineInfo() {
		return toLineInfo;
	}

	public void setToLineInfo(boolean toLineInfo) {
		this.toLineInfo = toLineInfo;
	}
}