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

import com.atlassian.theplugin.commons.crucible.ReviewInfo;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.Server;

import java.util.List;


public class ReviewDataInfoAdapter {
	private ReviewInfo review;

	public ReviewDataInfoAdapter(ReviewInfo review) {
		this.review = review;
	}

	public String getReviewUrl() {
		return review.getReviewUrl();
	}

	public User getAuthor() {
		return review.getAuthor();
	}

	public User getCreator() {
		return review.getCreator();
	}

	public String getDescription() {
		return review.getDescription();
	}

	public User getModerator() {
		return review.getModerator();
	}

	public List<Reviewer> getReviewers() {
		return review.getReviewers();
	}

	public int getNumOfCompletedReviewers() {
		int numCompleted = 0;

		for (Reviewer reviewer : getReviewers()) {
			if (reviewer.isCompleted()) {
				numCompleted++;
			}
		}
		return numCompleted;
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

    public int getMetricsVersion() {
        return review.getMetricsVersion();
    }

    public Server getServer() {
		return review.getServer();
	}

	@Override
	public String toString() {
		return getPermaId().getId() + ": " + getName();
	}
}
