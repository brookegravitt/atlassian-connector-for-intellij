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

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class BuildListModelImpl implements BuildListModel {
	/**
	 * null means no filter - all builds matchs
	 */
	private BambooBuildFilter filter;

	private final Collection<BuildListModelListener> listeners = new CopyOnWriteArrayList<BuildListModelListener>();

	private final Collection<BambooBuildAdapterIdea> allBuilds = MiscUtil.buildArrayList();

	private static final DateTimeFormatter TIME_DF = DateTimeFormat.forPattern("hh:mm a");
	private Project project;
	private CfgManager cfgManager;


	public BuildListModelImpl(Project project, CfgManager cfgManager) {
		this.project = project;
		this.cfgManager = cfgManager;
	}

	// for unit tests only
	void setBuilds(Collection<BambooBuildAdapterIdea> builds) {
		allBuilds.clear();
		allBuilds.addAll(builds);
	}

	public void update(Collection<BambooBuild> builds, final Collection<Exception> generalExceptions) {

		boolean haveErrors = false;
		List<BambooBuildAdapterIdea> buildAdapters = new ArrayList<BambooBuildAdapterIdea>();
		Date lastPollingTime = null;
		final Collection<Pair<String, Throwable>> errors = MiscUtil.buildArrayList();
		for (BambooBuild build : builds) {
			if (!haveErrors) {
				if (build.getStatus() == BuildStatus.UNKNOWN && build.getErrorMessage() != null) {
					errors.add(new Pair<String, Throwable>(build.getPlanKey() + ": " + build.getErrorMessage(),
							build.getException()));
					haveErrors = true;
				}
			}
			if (build.getPollingTime() != null) {
				lastPollingTime = build.getPollingTime();
			}
			final BambooBuildAdapterIdea buildAdapterIdea = new BambooBuildAdapterIdea(build);
			if (cfgManager != null && project != null) {
				cfgManager.addProjectConfigurationListener(CfgUtil.getProjectId(project), buildAdapterIdea);
			}
			buildAdapters.add(buildAdapterIdea);
		}
		allBuilds.clear();
		allBuilds.addAll(buildAdapters);

		final StringBuilder info = new StringBuilder();
		info.append("Loaded ");
		info.append(builds.size());
		info.append(" builds");
		if (lastPollingTime != null) {
			info.append(" at ");
			info.append(TIME_DF.print(lastPollingTime.getTime()));
		}
		info.append(".");

		//setBuilds(buildStatuses);
		notifyListeners(new Notifier() {
			public void notify(final BuildListModelListener listener) {
				listener.buildsChanged(Collections.singleton(info.toString()), errors);
                listener.generalProblemsHappened(generalExceptions);
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
				public void notify(final BuildListModelListener listener) {
					listener.modelChanged();
				}
			});
		}
	}

	private interface Notifier {
		void notify(BuildListModelListener listener);
	}

	public void addListener(BuildListModelListener listener) {
		listeners.add(listener);
	}

	private void notifyListeners(Notifier notifier) {
		for (BuildListModelListener listener : listeners) {
			notifier.notify(listener);
		}

	}

}
