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
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFilter;

import java.util.*;

/**
 * User: jgorycki
 * Date: Dec 16, 2008
 * Time: 1:53:24 PM
 */
public abstract class AbstractSortingCrucibleReviewListModel extends CrucibleReviewListModelListenerHolder {
	public AbstractSortingCrucibleReviewListModel(CrucibleReviewListModel parent) {
		super(parent);
	}

	protected Collection<ReviewAdapter> sort(Collection<ReviewAdapter> col) {
		List<ReviewAdapter> list = new ArrayList<ReviewAdapter>();
		for (ReviewAdapter r : col) {
			list.add(r);
		}
		Collections.sort(list, getComparator());
		return list;
	}

	protected abstract Comparator<ReviewAdapter> getComparator();

	public Collection<ReviewAdapter> getReviews() {
		return sort(parent.getReviews());
	}

	public int getReviewCount(CrucibleFilter filter) {
		return parent.getReviewCount(filter);
	}

	public int getPredefinedFiltersReviewCount() {
		return parent.getPredefinedFiltersReviewCount();
	}
}
