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
package com.atlassian.connector.intellij.crucible;

import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFilter;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewRecentlyOpenBean;
import java.util.LinkedList;

/**
 * @author Jacek Jaroczynski
 */
public class RecentlyOpenReviewsFilter implements CrucibleFilter {

	private LinkedList<ReviewRecentlyOpenBean> recentlyOpenReviewss = new LinkedList<ReviewRecentlyOpenBean>();
	private boolean enabled = false;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public LinkedList<ReviewRecentlyOpenBean> getRecentlyOpenReviewss() {
		return recentlyOpenReviewss;
	}

	public void setRecentlyOpenReviewss(final LinkedList<ReviewRecentlyOpenBean> recentlyOpenReviewss) {
		this.recentlyOpenReviewss = recentlyOpenReviewss;
	}

	public String getFilterName() {
		return "Recently Viewed Reviews";
	}

	public String getFilterUrl() {
		return null;
	}

	public void addRecentlyOpenReview(final ReviewAdapter review) {
		if (review != null) {
			String reviewId = review.getPermId().getId();
			ServerId serverId = review.getServerData().getServerId();

			// add element and make sure it is not duplicated and it is inserted at the top
			ReviewRecentlyOpenBean r = new ReviewRecentlyOpenBean(serverId, reviewId);

			recentlyOpenReviewss.remove(r);
			recentlyOpenReviewss.addFirst(r);

			while (recentlyOpenReviewss.size() > 10) {
				recentlyOpenReviewss.removeLast();
			}
		}
	}
}
