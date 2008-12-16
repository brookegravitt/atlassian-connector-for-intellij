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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: jgorycki
 * Date: Dec 15, 2008
 * Time: 4:05:54 PM
 */
public class SearchingCrucibleReviewListModel extends CrucibleReviewListModelListenerHolder {

	private String searchTerm;

	public SearchingCrucibleReviewListModel(CrucibleReviewListModel parent) {
		super(parent);
		searchTerm = "";
	}

	public void setSearchTerm(@NotNull String searchTerm) {

		if (this.searchTerm.equals(searchTerm)) {
			return;
		}
		this.searchTerm = searchTerm.toLowerCase();

		modelChanged();
	}

	private Collection<ReviewAdapter> search(Collection<ReviewAdapter> col) {
		if (searchTerm.length() == 0) {
			return col;
		}
		List<ReviewAdapter> list = new ArrayList<ReviewAdapter>();
		for (ReviewAdapter r : col) {
			if (r.getPermId().getId().toLowerCase().indexOf(searchTerm) > -1
					|| r.getName().toLowerCase().indexOf(searchTerm) > -1) {
				list.add(r);
			}
		}
		return list;
	}

	public Collection<ReviewAdapter> getReviews() {
		return search(parent.getReviews());
	}
}
