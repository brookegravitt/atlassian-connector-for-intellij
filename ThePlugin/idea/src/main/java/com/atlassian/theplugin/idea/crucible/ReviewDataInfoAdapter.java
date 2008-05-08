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

package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.crucible.ReviewDataInfo;
import com.atlassian.theplugin.crucible.api.PermId;
import com.atlassian.theplugin.crucible.api.State;
import com.atlassian.theplugin.commons.Server;

import java.util.List;


public class ReviewDataInfoAdapter {
	private ReviewDataInfo review;

	public ReviewDataInfoAdapter(ReviewDataInfo review) {
		this.review = review;
	}

	public String getReviewUrl() {
		return review.getReviewUrl();
	}

	public String getAuthor() {
		return review.getAuthor();
	}

	public String getCreator() {
		return review.getCreator();
	}

	public String getDescription() {
		return review.getDescription();
	}

	public String getModerator() {
		return review.getModerator();
	}

	public List<String> getReviewers() {
		return review.getReviewers();
	}

	public String getName() {
		return review.getName();
	}

	public PermId getParentReview() {
		return review.getParentReview();
	}

	public PermId getPermaId() {
		return review.getPermaId();
	}

	public String getProjectKey() {
		return review.getProjectKey();
	}

	public String getRepoName() {
		return review.getRepoName();
	}

	public State getState() {
		return review.getState();
	}

	public Server getServer() {
		return review.getServer();
	}
}
