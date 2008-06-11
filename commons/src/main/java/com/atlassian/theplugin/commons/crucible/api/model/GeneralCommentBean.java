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

import com.jniwrapper.util.SoftCache;

import java.util.*;

public class GeneralCommentBean implements GeneralComment {
    private String message = null;
    private boolean draft = false;
    private boolean deleted = false;
    private boolean defectRaised = false;
    private boolean defectApproved = false;
    private String user = null;
	private String displayUser = null;
	private Date createDate = new Date();

	private List<GeneralComment> replies = new ArrayList<GeneralComment>();
    private Map<String, CustomField> customFields;

    public GeneralCommentBean() {
        customFields = new HashMap<String, CustomField>();
    }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isDefectRaised() {
		return defectRaised;
	}

	public void setDefectRaised(boolean defectRaised) {
		this.defectRaised = defectRaised;
	}

	public boolean isDefectApproved() {
		return defectApproved;
	}

	public void setDefectApproved(boolean defectApproved) {
		this.defectApproved = defectApproved;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getDisplayUser() {
		return displayUser;
	}

	public void setDisplayUser(String displayUser) {
		this.displayUser = displayUser;
	}


	public Date getCreateDate() {
		return new Date(createDate.getTime());
	}

	public void setCreateDate(Date createDate) {
		this.createDate = new Date(createDate.getTime());
	}

	public List<GeneralComment> getReplies() {
		return replies;
	}

	public void setReplies(List<GeneralComment> replies) {
		this.replies = replies;
	}

	public void addReply(GeneralComment comment) {
		replies.add(comment);
	}

    public Map<String, CustomField> getCustomFields() {
        return customFields;
    }
}