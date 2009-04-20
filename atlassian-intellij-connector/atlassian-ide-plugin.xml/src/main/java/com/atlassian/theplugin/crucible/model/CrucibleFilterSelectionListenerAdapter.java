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

import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;

import java.util.Collection;

/**
 * @author Jacek Jaroczynski
 */
public class CrucibleFilterSelectionListenerAdapter implements CrucibleFilterSelectionListener {
	public void filterSelectionChanged() {
	}

	public void selectionChangedRecentlyOpenFilter() {
	}

	public void selectionChangedPredefinedFilters(final Collection<PredefinedFilter> predefinedFilters) {
	}

	public void selectedCustomFilter(final CustomFilter customFilter) {
	}

	public void unselectedCustomFilter() {
	}
}
