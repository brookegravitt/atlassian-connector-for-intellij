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

package com.atlassian.theplugin.commons.crucible;

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.crucible.api.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.PermId;
import com.atlassian.theplugin.commons.crucible.api.State;
import com.atlassian.theplugin.commons.crucible.api.UserData;

import java.util.List;


public class ReviewDataInfoImpl implements ReviewDataInfo {
	private final ReviewData reviewData;
	private final List<UserData> reviewers;
	private final Server server;

	public ReviewDataInfoImpl(ReviewData reviewData, List<UserData> reviewers, Server server) {
		this.reviewData = reviewData;
		this.reviewers = reviewers;
		this.server = server;
	}

	public String getReviewUrl() {
		String baseUrl = server.getUrlString();
		while (baseUrl.length() > 0 && baseUrl.charAt(baseUrl.length() - 1) == '/') {
			// quite ineffective, I know ...
			baseUrl = baseUrl.substring(0,  baseUrl.length() - 1);
		}
		return baseUrl + "/cru/" + reviewData.getPermaId().getId();

	}

	public List<UserData> getReviewers() {
		return reviewers;
	}

	public Server getServer() {
		return server;
	}


	public String getAuthor() {
		return reviewData.getAuthor();
	}

	public String getCreator() {
		return reviewData.getCreator();
	}

	public String getDescription() {
		return reviewData.getDescription();
	}

	public String getModerator() {
		return reviewData.getModerator();
	}

	public String getName() {
		return reviewData.getName();
	}

	public PermId getParentReview() {
		return reviewData.getParentReview();
	}

	public PermId getPermaId() {
		return reviewData.getPermaId();
	}

	public String getProjectKey() {
		return reviewData.getProjectKey();
	}

	public String getRepoName() {
		return reviewData.getRepoName();
	}

	public State getState() {
		return reviewData.getState();
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ReviewDataInfoImpl that = (ReviewDataInfoImpl) o;

		if (!reviewData.getPermaId().getId().equals(that.reviewData.getPermaId().getId())) {
			return false;
		}
		if (server != null ? !server.equals(that.server) : that.server != null) {
			return false;
		}

		return true;
	}

	private static final int ONE_EFF = 31;

	public int hashCode() {
		int result;
		result = reviewData.getPermaId().getId().hashCode();
		result = ONE_EFF * result + (server != null ? server.hashCode() : 0);
		return result;
	}

}
