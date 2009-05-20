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
package com.atlassian.theplugin.idea.bamboo;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public class SearchBuildListModel extends AbstractBuildListModelDecorator {
	private String searchTerm = "";

	public SearchBuildListModel(BuildListModelImpl buildModel) {
		super(buildModel);
	}

    public void generalProblemsHappened(@Nullable Collection<Exception> generalExceptions) {
    }

    @Override
	public Collection<BambooBuildAdapterIdea> getBuilds() {
		return search(parent.getBuilds());
	}


	private Collection<BambooBuildAdapterIdea> search(Collection<BambooBuildAdapterIdea> col) {
		if (searchTerm.length() == 0) {
			return col;
		}
		List<BambooBuildAdapterIdea> list = new ArrayList<BambooBuildAdapterIdea>();
		for (BambooBuildAdapterIdea r : col) {
			if ((r.getPlanKey() + "-" + r.getBuildNumberAsString()).toLowerCase().indexOf(searchTerm) > -1) {
				list.add(r);
			}
		}
		return list;
	}

	public void setSearchTerm(final String searchTerm) {
		if (this.searchTerm.equals(searchTerm)) {
			return;
		}

		this.searchTerm = searchTerm.toLowerCase();

		notifyDecoratorListeners();
	}
}
