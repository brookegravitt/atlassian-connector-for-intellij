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

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.util.MiscUtil;
import org.jetbrains.annotations.NotNull;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

public class BambooModel {
	/**
	 * null means no filter - all builds matchs
	 */
	private BambooBuildFilter filter;

	private final Collection<BambooModelListener> listeners = new CopyOnWriteArrayList<BambooModelListener>();

	private final Collection<BambooBuildAdapterIdea> allBuilds = MiscUtil.buildArrayList();

	private static final DateTimeFormatter TIME_DF = DateTimeFormat.forPattern("hh:mm a");


	// for unit tests only
	void setBuilds(Collection<BambooBuildAdapterIdea> builds) {
		allBuilds.clear();
		allBuilds.addAll(builds);
	}

	public void update(Collection<BambooBuild> builds) {

		boolean haveErrors = false;
		List<BambooBuildAdapterIdea> buildAdapters = new ArrayList<BambooBuildAdapterIdea>();
		Date lastPollingTime = null;
		final Collection<String> errors = MiscUtil.buildArrayList();
		for (BambooBuild build : builds) {
			if (!haveErrors) {
				if (build.getStatus() == BuildStatus.UNKNOWN) {
					errors.add(build.getMessage());
					haveErrors = true;
				}
			}
			if (build.getPollingTime() != null) {
				lastPollingTime = build.getPollingTime();
			}
			buildAdapters.add(new BambooBuildAdapterIdea(build));
		}
		allBuilds.clear();
		allBuilds.addAll(buildAdapters);

		final StringBuilder info = new StringBuilder();
		info.append("Loaded <b>");
		info.append(builds.size());
		info.append("</b> builds");
		if (lastPollingTime != null) {
			info.append(" at  <b>");
			info.append(TIME_DF.print(lastPollingTime.getTime()));
			info.append("</b>");
		}
		info.append(".");

		//setBuilds(buildStatuses);
		notifyListeners(new Notifier() {
			public void notify(final BambooModelListener listener) {
				listener.buildsChanged(Collections.singleton(info.toString()), errors);
			}
		});
	}

	public Collection<BambooBuildAdapterIdea> getAllBuilds() {
		return allBuilds;
	}


	@NotNull
	public Collection<BambooBuildAdapterIdea> getBuilds() {
		if (filter == null) {
			return allBuilds;
		}
		Collection<BambooBuildAdapterIdea> res = MiscUtil.buildArrayList();
		for (BambooBuildAdapterIdea build : allBuilds) {
			if (filter.doesMatch(build)) {
				res.add(build);
			}
		}
		return res;
	}

	public BambooBuildFilter getFilter() {
		return filter;
	}

	public void setFilter(BambooBuildFilter filter) {
		if (this.filter != filter) {
			this.filter = filter;
			notifyListeners(new Notifier() {
				public void notify(final BambooModelListener listener) {
					listener.filterChanged();
				}
			});
		}
	}

	private interface Notifier {
		void notify(BambooModelListener listener);
	}

	public void addListener(BambooModelListener listener) {
		listeners.add(listener);
	}

	private void notifyListeners(Notifier notifier) {
		for (BambooModelListener listener : listeners) {
			notifier.notify(listener);
		}

	}

}
