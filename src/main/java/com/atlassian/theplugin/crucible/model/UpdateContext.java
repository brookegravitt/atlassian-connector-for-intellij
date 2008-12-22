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

package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;

public class UpdateContext {
	private UpdateReason updateReason;
	private ReviewAdapter reviewAdapter;

	private ReviewAdapter oldReviewAdapter;

	public UpdateContext(final UpdateReason updateReason, final ReviewAdapter reviewAdapter) {
		this.updateReason = updateReason;
		this.reviewAdapter = reviewAdapter;
	}


	public ReviewAdapter getOldReviewAdapter() {
		return oldReviewAdapter;
	}

	public void setOldReviewAdapter(ReviewAdapter oldReviewAdapter) {
		this.oldReviewAdapter = oldReviewAdapter;
	}


	public UpdateReason getUpdateReason() {
		return updateReason;
	}

	public ReviewAdapter getReviewAdapter() {
		if (reviewAdapter == null) {
			throw new IllegalStateException();
		}
		return reviewAdapter;
	}
}
