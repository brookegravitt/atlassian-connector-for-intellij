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

import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.idea.config.GenericComboBoxItemWrapper;
import com.atlassian.theplugin.idea.config.ProjectCfgManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class BambooFilterPanel extends JList {
	private final ProjectCfgManager projectCfgManager;
	private final ProjectId projectId;
	private final BambooModel bambooModel;
	private BambooFilterType bambooFilterType; // = BambooFilterType.NONE;


	public BambooFilterPanel(final ProjectCfgManager projectCfgManager, final ProjectId projectId, final BambooModel model) {
		super(new DefaultListModel());
		this.projectCfgManager = projectCfgManager;
		this.projectId = projectId;
		bambooModel = model;
		updateModel(model.getAllBuilds());
	}

//	void setSelection(BambooBuildFilter filter) {
//
//	}

	public BambooBuildFilter getSelection() {
		return getBuildFilter(getSelectedValues());
	}


	public void setBambooFilterType(final BambooFilterType bambooFilterType) {
		this.bambooFilterType = bambooFilterType;
		final DefaultListModel listModel = (DefaultListModel) getModel();
		listModel.clear();
		updateModel(bambooModel.getAllBuilds());
	}

	public BambooFilterType getBambooFilterType() {
		return bambooFilterType;
	}

	public void update() {
		updateModel(bambooModel.getAllBuilds());
		repaint();
	}

	private void updateModel(@NotNull final Collection<BambooBuildAdapterIdea> buildStatuses) {

		final DefaultListModel listModel = (DefaultListModel) getModel();
		if (bambooFilterType == null) {
			return;
		}
		switch (bambooFilterType) {
			case PROJECT:
				Set<String> uniqueBambooProjects = new LinkedHashSet<String>();
				for (BambooBuildAdapterIdea buildStatuse : buildStatuses) {
					uniqueBambooProjects.add(buildStatuse.getProjectName());
				}
				for (String bambooProject : uniqueBambooProjects) {
//					if (listModel.contains())
					final BamboProjectFilterWrapper obj = new BamboProjectFilterWrapper(new BambooProjectFilter(bambooProject),
							bambooModel);
					// add those projects which don't exist yet
					if (!listModel.contains(obj)) {
						listModel.addElement(obj);
					}
				}
				break;
			case SERVER:
				final Collection<BambooServerCfg> bambooServers = projectCfgManager.getCfgManager()
						.getAllEnabledBambooServers(projectId);

				for (BambooServerCfg bambooServer : bambooServers) {
					final BamboServerFilterWrapper obj = new BamboServerFilterWrapper(new BambooServerFilter(bambooServer),
							bambooModel);
					if (!listModel.contains(obj)) {
						listModel.addElement(obj);
					}
				}

			break;
			case STATE:
				for (BuildStatus buildStatus : BuildStatus.values()) {
					final BuildStatusFilterWrapper obj = new BuildStatusFilterWrapper(new BuildStatusFilter(buildStatus),
							bambooModel);
					if (!listModel.contains(obj)) {
						listModel.addElement(obj);
					}
				}
				break;
			default:
				break;
		}
//		return listModel;
	}


//	private DefaultListModel updateFilterModel(@NotNull final Collection<BambooBuildAdapterIdea> buildStatuses) {
//		final DefaultListModel listModel = new DefaultListModel();
//		if (bambooFilterType == null) {
//			return listModel;
//		}
//		switch (bambooFilterType) {
//			case PROJECT:
//				break;
//			case SERVER: {
//				Map<BambooServerCfg, Integer> hitMap = new LinkedHashMap<BambooServerCfg, Integer>();
//				final Collection<BambooServerCfg> bambooServers = projectCfgManager.getCfgManager()
//						.getAllEnabledBambooServers(projectId);
//
//				for (BambooServerCfg bambooServer : bambooServers) {
//					hitMap.put(bambooServer, 0);
//				}
//				for (BambooBuildAdapterIdea buildStatus : buildStatuses) {
//					final Integer integer = hitMap.get(buildStatus.getServer());
//					hitMap.put(buildStatus.getServer(), integer != null ? integer + 1 : 1);
//				}
//
//				for (Map.Entry<BambooServerCfg, Integer> entry : hitMap.entrySet()) {
//					listModel.addElement(new BamboServerCfgWrapper(entry.getKey(), entry.getValue()));
//				}
//			}
//			break;
//			case STATE: {
//
//				Map<BuildStatus, Integer> hitMap = new LinkedHashMap<BuildStatus, Integer>();
//				for (BuildStatus buildStatus : BuildStatus.values()) {
//					hitMap.put(buildStatus, 0);
//				}
//				for (BambooBuildAdapterIdea buildAdapterIdea : buildStatuses) {
//					final Integer integer = hitMap.get(buildAdapterIdea.getStatus());
//					hitMap.put(buildAdapterIdea.getStatus(), integer != null ? integer + 1 : 1);
//				}
//
//				for (Map.Entry<BuildStatus, Integer> entry : hitMap.entrySet()) {
//					listModel.addElement(new BuildStatusWrapper(entry.getKey(), entry.getValue()));
//
//				}
//			}
//		}
//		return listModel;
//	}

	private static class BambooServerFilter implements BambooBuildFilter {
		private final BambooServerCfg bambooServerCfg;

		public BambooServerFilter(final BambooServerCfg bambooServerCfg) {
			this.bambooServerCfg = bambooServerCfg;
		}

		public boolean passes(final BambooBuildAdapterIdea build) {
			return bambooServerCfg.equals(build.getServer());
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			final BambooServerFilter that = (BambooServerFilter) o;

			//noinspection RedundantIfStatement
			if (!bambooServerCfg.equals(that.bambooServerCfg)) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return bambooServerCfg.hashCode();
		}
	}


	private static class BambooProjectFilter implements BambooBuildFilter {
		private final String projectName;

		public BambooProjectFilter(final String projectName) {
			this.projectName = projectName;
		}

		public boolean passes(final BambooBuildAdapterIdea build) {
			return projectName.equals(build.getProjectName());
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			final BambooProjectFilter that = (BambooProjectFilter) o;

			//noinspection RedundantIfStatement
			if (!projectName.equals(that.projectName)) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return projectName.hashCode();
		}
	}


	private static class BuildStatusFilter implements BambooBuildFilter {

		private final BuildStatus status;

		public BuildStatusFilter(final BuildStatus status) {
			this.status = status;
		}


		public boolean passes(final BambooBuildAdapterIdea build) {
			return status == build.getStatus();
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			final BuildStatusFilter that = (BuildStatusFilter) o;

			//noinspection RedundantIfStatement
			if (status != that.status) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return status.hashCode();
		}
	}

	private abstract static class AbstractBambooBuildFilterWrapper<T extends BambooBuildFilter>
			extends GenericComboBoxItemWrapper<T> {
		private final BambooModel model;

		public AbstractBambooBuildFilterWrapper(final T wrapped, final BambooModel model) {
			super(wrapped);
			this.model = model;
		}

		protected int getCount() {
			int i = 0;
			for (BambooBuildAdapterIdea build : model.getAllBuilds()) {
				if (wrapped.passes(build)) {
					i++;
				}
			}
			return i;
		}

	}

	private static class BamboServerFilterWrapper extends AbstractBambooBuildFilterWrapper<BambooServerFilter> {
		public BamboServerFilterWrapper(final BambooServerFilter wrapped, final BambooModel model) {
			super(wrapped, model);
		}

		@Override
		public String toString() {
			if (wrapped != null) {
				return wrapped.bambooServerCfg.getName() + " (" + getCount() + ")";
			}
			return "None";
		}
	}

	private static class BamboProjectFilterWrapper extends AbstractBambooBuildFilterWrapper<BambooProjectFilter> {
		public BamboProjectFilterWrapper(final BambooProjectFilter wrapped, final BambooModel model) {
			super(wrapped, model);
		}

		@Override
		public String toString() {
			if (wrapped != null) {
				final String name = (wrapped.projectName == null || wrapped.projectName.length() == 0)
						? "Unknown Project" : wrapped.projectName;
				return name + " (" + getCount() + ")";
			}
			return "None";
		}
	}


	private static class BuildStatusFilterWrapper extends AbstractBambooBuildFilterWrapper<BuildStatusFilter> {
		public BuildStatusFilterWrapper(final BuildStatusFilter wrapped, final BambooModel model) {
			super(wrapped, model);
		}


		@Override
		public String toString() {
			if (wrapped == null) {
				return "None";
			}
			return getString() + " (" + getCount() + ")";
		}

		private String getString() {
			switch (wrapped.status) {
				case BUILD_DISABLED:
					return "Build Disabled";
				case BUILD_FAILED:
					return "Build Failed";
				case BUILD_SUCCEED:
					return "Build Succeeded";
				case UNKNOWN:
					return "Unknown";
				default:
					return "???";
			}
		}
	}

	@Nullable
	private BambooBuildFilter getBuildFilter(
			@Nullable final Object[] selectedValues) {
		if (bambooFilterType == null || selectedValues == null || selectedValues.length == 0) {
			return null; // empty filter
		}

		Collection<BambooBuildFilter> filters = MiscUtil.buildArrayList();
		for (Object selectedPath : selectedValues) {
			if (selectedPath instanceof AbstractBambooBuildFilterWrapper) {
				@SuppressWarnings({"RawUseOfParameterizedType"})
				final AbstractBambooBuildFilterWrapper projectBean = (AbstractBambooBuildFilterWrapper) selectedPath;
				filters.add((BambooBuildFilter) projectBean.getWrapped());
			}
		}

		return new BambooCompositeOrFilter(filters);
/*		switch (filterType) {
			case PROJECT:
				if (selectedPaths != null) {
					for (Object selectedPath : selectedPaths) {
						if (selectedPath instanceof BamboProjectFilterWrapper) {
							final BamboProjectFilterWrapper projectBean = (BamboProjectFilterWrapper) selectedPath;
							filters.add(projectBean.getWrapped());
						}

					}
				}
				return bambooModel.createProjectFilter(projectKeys);
			case SERVER:
				Collection<ServerCfg> serverCfgs = MiscUtil.buildArrayList();
				if (selectedPaths != null) {
					for (Object selectedPath : selectedPaths) {
						if (selectedPath instanceof BamboServerCfgWrapper) {
							final BamboServerCfgWrapper bamboServerCfgWrapper = (BamboServerCfgWrapper) selectedPath;
							serverCfgs.add(bamboServerCfgWrapper.getWrapped());
						}
					}
				}
				return bambooModel.createServerFilter(serverCfgs);
			case STATE:
				Collection<BuildStatus> buildStatuses = MiscUtil.buildArrayList();
				if (selectedPaths != null) {
					for (Object selectedPath : selectedPaths) {
						if (selectedPath instanceof BuildStatusWrapper) {
							final BuildStatusWrapper buildStatusWrapper = (BuildStatusWrapper) selectedPath;
							buildStatuses.add(buildStatusWrapper.getWrapped());
						}
					}
				}
				return bambooModel.createStateFilter(buildStatuses);
			default:
				throw new UnsupportedOperationException("Method not implemented for " + filterType);
		}*/
	}

}
