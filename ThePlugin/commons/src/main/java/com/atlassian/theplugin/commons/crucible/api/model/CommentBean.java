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

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 16, 2008
 * Time: 11:42:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommentBean implements Comment {
	private PermId permId;
	private String message = null;
	private boolean draft = false;
	private boolean deleted = false;
	private boolean defectRaised = false;
	private boolean defectApproved = false;
	private String user = null;
	private String displayUser = null;
	private Date createDate = new Date();


	private boolean isReply = false;

	private Map<String, CustomField> customFields;

	public CommentBean() {
		customFields = new HashMap<String, CustomField>();
	}

	public PermId getPermId() {
		return permId;
	}

	public void setPermId(PermId permId) {
		this.permId = permId;
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

	public boolean isReply() {
		return isReply;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public void setReply(boolean reply) {
		isReply = reply;
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
		if (createDate != null) {
			this.createDate = new Date(createDate.getTime());
		}
	}

	public Map<String, CustomField> getCustomFields() {
		return customFields;
	}

	public STATE getState() {
		if (isDraft()) {
			return STATE.DRAFT;
		} else if (isDefectApproved()) {
			return STATE.DEFECT_APPROVED;
		} else if (isDefectRaised()) {
			return STATE.DEFECT_RAISED;
		} else if (isDeleted()) {
			return STATE.DELETED;
		}
		return STATE.REVIEW;
	}

	@Override
	public String toString() {
		return getMessage();
	}

}
