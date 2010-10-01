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

import com.atlassian.connector.intellij.crucible.ReviewAdapter;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * User: jgorycki
 * Date: Dec 16, 2008
 * Time: 2:03:27 PM
 */
public class SortingByKeyCrucibleReviewListModel extends AbstractSortingCrucibleReviewListModel {
	private static final Comparator<ReviewAdapter> COMPARATOR = Collections.reverseOrder(new ReviewKeyComparator());

	public SortingByKeyCrucibleReviewListModel(CrucibleReviewListModel parent) {
		super(parent);
	}

	protected Comparator<ReviewAdapter> getComparator() {
		return COMPARATOR;
	}

	public Collection<ReviewAdapter> getOpenInIdeReviews() {
		return super.getOpenInIdeReviews();
	}

	public void rebuildModel(UpdateReason updateReason) {
	}

	public boolean isRequestObsolete(long epoch) {
		return false;
	}

}
