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
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.ConfigurationListenerAdapter;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.bamboo.BambooBuildInfo;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

interface BambooModelListener {
	void filterChanged();
	void buildsChanged();
}

interface BambooBuildFilter {
	boolean passes(BambooBuildAdapterIdea build);
	BambooFilterType getFilterType();
}

public class BambooModel {
	private BambooBuildFilter filter;

	private Collection<BambooModelListener> listeners = new CopyOnWriteArrayList<BambooModelListener>();

	private final Project project;

	private final CfgManager cfgManager;

	private final Collection<BambooBuildAdapterIdea> allBuilds = MiscUtil.buildArrayList();

	public BambooModel(@NotNull Project project, @NotNull CfgManager cfgManager) {
		this.project = project;
		this.cfgManager = cfgManager;
		cfgManager.addProjectConfigurationListener(CfgUtil.getProjectId(project), new MyConfigurationListenerAdapter());

		allBuilds.add(createBambooBuild("B1", "PR1", "Project One", true));
		allBuilds.add(createBambooBuild("B2", "PR2", "Project Two", true));
		allBuilds.add(createBambooBuild("B3", "PR3", "Project Three", false));
		allBuilds.add(createBambooBuild("B4", "PR1", "Project One", false));
		allBuilds.add(createBambooBuild("B5", "PR3", "Project Three", false));
		allBuilds.add(createBambooBuild("B6", "PR1", "Project One", false));
		allBuilds.add(createBambooBuild("B7", "PR4", "Project Four", true));
	}

	private BambooBuildAdapterIdea createBambooBuild(String buildKey, String key, String name, boolean isSuccessful) {
		final BambooBuildInfo buildInfo = new BambooBuildInfo();
		buildInfo.setBuildKey(buildKey);
		buildInfo.setProjectKey(key);
		buildInfo.setProjectName(name);
		buildInfo.setBuildState(isSuccessful ? BambooBuildInfo.BUILD_SUCCESSFUL : BambooBuildInfo.BUILD_FAILED);
		return new BambooBuildAdapterIdea(buildInfo);
	}

	public void update(Collection<BambooBuild> builds) {

		boolean haveErrors = false;
		List<BambooBuildAdapterIdea> buildAdapters = new ArrayList<BambooBuildAdapterIdea>();
		Date lastPollingTime = null;
		for (BambooBuild build : builds) {
			if (!haveErrors) {
				if (build.getStatus() == BuildStatus.UNKNOWN) {
//					setStatusMessage(build.getMessage(), true);
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

//		if (!haveErrors) {
//			StringBuffer sb = new StringBuffer();
//			sb.append("Loaded <b>");
//			sb.append(builds.size());
//			sb.append("</b> builds");
//			if (lastPollingTime != null) {
//				sb.append(" at  <b>");
//				sb.append(TIME_DF.print(lastPollingTime.getTime()));
//				sb.append("</b>");
//			}
//			sb.append(".");
//			setStatusMessage((sb.toString()));
//		}

		//setBuilds(buildStatuses);
		notifyListeners(new Notifier() {
			public void notify(final BambooModelListener listener) {
				listener.buildsChanged();
			}
		});
	}

	public BambooBuildFilter createProjectFilter(final Collection<String> aProjects) {
		return new BambooBuildFilter() {
			private Set<String> projects = MiscUtil.buildHashSet(aProjects);
			public boolean passes(final BambooBuildAdapterIdea build) {
				return projects.contains(build.getProjectName());
			}

			public BambooFilterType getFilterType() {
				return BambooFilterType.PROJECT;
			}
		};
	}

	public BambooBuildFilter createServerFilter(final Collection<ServerCfg> aServers) {
		return new BambooBuildFilter() {
			private Set<ServerCfg> servers = MiscUtil.buildHashSet(aServers);
			public boolean passes(final BambooBuildAdapterIdea build) {
				return servers.contains(build.getServer());
			}
			public BambooFilterType getFilterType() {
				return BambooFilterType.SERVER;
			}
		};
	}

	public BambooBuildFilter createStateFilter(final Collection<BuildStatus> aStatuses) {
		return new BambooBuildFilter() {
			private Set<BuildStatus> statuses = MiscUtil.buildHashSet(aStatuses);
			public boolean passes(final BambooBuildAdapterIdea build) {
				return statuses.contains(build.getStatus());
			}

			public BambooFilterType getFilterType() {
				return BambooFilterType.STATE;
			}
		};
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
			if (filter.passes(build)) {
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

	private class MyConfigurationListenerAdapter extends ConfigurationListenerAdapter {

		@Override
		public void projectUnregistered() {
			cfgManager.removeProjectConfigurationListener(CfgUtil.getProjectId(BambooModel.this.project), this);
		}

		@Override
		public void bambooServersChanged(final ProjectConfiguration newConfiguration) {
		}
	}
}
