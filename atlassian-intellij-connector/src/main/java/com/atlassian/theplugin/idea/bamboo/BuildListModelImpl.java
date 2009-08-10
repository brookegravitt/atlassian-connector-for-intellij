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

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.intellij.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BuildListModelImpl implements BuildListModel {
	/**
	 * null means no filter - all builds matchs
	 */
	private BambooBuildFilter filter;

	private final Collection<BuildListModelListener> listeners = new CopyOnWriteArrayList<BuildListModelListener>();

	private final Collection<BambooBuildAdapter> allBuilds = MiscUtil.buildArrayList();

	private static final DateTimeFormatter TIME_DF = DateTimeFormat.forPattern("hh:mm a");
	private final Project project;
	private final ProjectCfgManager cfgManager;

	public BuildListModelImpl(Project project, ProjectCfgManagerImpl cfgManager) {
		this.project = project;
		this.cfgManager = cfgManager;

//		for (BambooBuildAdapter build : allBuilds) {
//			if (build.getServer().getServerId().equals(serverId)) {
//				allBuilds.remove(build); // todo copy on write
//				BambooBuild b = new BambooBuildInfo();
//				allBuilds.add(new BambooBuildAdapter())
//			}
//		}
	}

	// for unit tests only
	void setBuilds(Collection<BambooBuildAdapter> builds) {
		allBuilds.clear();
		allBuilds.addAll(builds);
	}

	public void update(Collection<BambooBuildAdapter> builds, final Collection<Exception> generalExceptions) {

		boolean haveErrors = false;
		List<BambooBuildAdapter> buildAdapters = new ArrayList<BambooBuildAdapter>();
		Date lastPollingTime = null;
		final Collection<Pair<String, Throwable>> errors = MiscUtil.buildArrayList();
		for (BambooBuildAdapter build : builds) {
			if (!haveErrors) {
				if (build.getStatus() == BuildStatus.UNKNOWN && build.getErrorMessage() != null) {
					//noinspection ThrowableResultOfMethodCallIgnored
					errors.add(new Pair<String, Throwable>(build.getPlanKey() + ": " + build.getErrorMessage(),
							build.getException()));
					haveErrors = true;
				}
			}
			if (build.getPollingTime() != null) {
				lastPollingTime = build.getPollingTime();
			}
			if (cfgManager != null && project != null) {
				cfgManager.addProjectConfigurationListener(build);
			}
			buildAdapters.add(build);
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

	public Collection<BambooBuildAdapter> getAllBuilds() {
		return allBuilds;
	}


	@NotNull
	public Collection<BambooBuildAdapter> getBuilds() {
		if (filter == null) {
			return allBuilds;
		}
		Collection<BambooBuildAdapter> res = MiscUtil.buildArrayList();
		for (BambooBuildAdapter build : allBuilds) {
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
