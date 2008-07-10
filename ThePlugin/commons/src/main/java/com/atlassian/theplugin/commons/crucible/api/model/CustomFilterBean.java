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

import java.util.Arrays;


public class CustomFilterBean implements CustomFilter {
	private long serverUid = 0;    
    private String title;
    private String[] state;
    private String author;
    private String moderator;
    private String creator;
    private String reviewer;
    private boolean orRoles = false;
    private boolean complete;
    private boolean allReviewersComplete;
    private String projectKey;
    private boolean enabled;
    private static final double ID_DISCRIMINATOR = 1002d;
    private static final int HASHCODE_CONSTANT = 31;
    private static final int SHIFT_32 = 32;

    public boolean equals(Object o) {
		if (this == o) {
            return true;
        }
		if (o == null || getClass() != o.getClass()) {
            return false;
        }

		CustomFilterBean that = (CustomFilterBean) o;

		if (uid != that.uid) {
            return false;
        }

		return true;
	}

	public int hashCode() {
		int result;
		result = (title != null ? title.hashCode() : 0);
		result = HASHCODE_CONSTANT * result + (state != null ? Arrays.hashCode(state) : 0);
		result = HASHCODE_CONSTANT * result + (author != null ? author.hashCode() : 0);
		result = HASHCODE_CONSTANT * result + (moderator != null ? moderator.hashCode() : 0);
		result = HASHCODE_CONSTANT * result + (creator != null ? creator.hashCode() : 0);
		result = HASHCODE_CONSTANT * result + (reviewer != null ? reviewer.hashCode() : 0);
		result = HASHCODE_CONSTANT * result + (orRoles ? 1 : 0);
		result = HASHCODE_CONSTANT * result + (complete ? 1 : 0);
		result = HASHCODE_CONSTANT * result + (allReviewersComplete ? 1 : 0);
		result = HASHCODE_CONSTANT * result + (projectKey != null ? projectKey.hashCode() : 0);
		result = HASHCODE_CONSTANT * result + (int) (uid ^ (uid >>> SHIFT_32));
		result = HASHCODE_CONSTANT * result + (int) (serverUid ^ (serverUid >>> SHIFT_32));
		return result;
	}

	private transient long uid = System.currentTimeMillis() + (long) (Math.random() * ID_DISCRIMINATOR);;

	public long getServerUid() {
		return serverUid;
	}

	public void setServerUid(long serverUid) {
		this.serverUid = serverUid;
	}

	public CustomFilterBean() {
	}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getState() {
        return state;
    }

    public void setState(String[] state) {
        this.state = state;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getModerator() {
        return moderator;
    }

    public void setModerator(String moderator) {
        this.moderator = moderator;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isAllReviewersComplete() {
        return allReviewersComplete;
    }

    public void setAllReviewersComplete(boolean allReviewersComplete) {
        this.allReviewersComplete = allReviewersComplete;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public boolean isOrRoles() {
        return orRoles;
    }

    public void setOrRoles(boolean orRoles) {
        this.orRoles = orRoles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
