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

import com.atlassian.theplugin.commons.bamboo.AdjustedBuildStatus;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.idea.config.GenericComboBoxItemWrapper;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.intellij.ui.ListSpeedSearch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class BambooFilterList extends JList {
	private final ProjectCfgManagerImpl projectCfgManager;
	private final BuildListModelImpl bambooModel;
	private BambooFilterType bambooFilterType; // = BambooFilterType.NONE;
	private final BamboAllFilterWrapper allFilterWrapper;


	public BambooFilterList(final ProjectCfgManagerImpl projectCfgManager, final BuildListModelImpl model) {
		super(new DefaultListModel());
		this.projectCfgManager = projectCfgManager;
		bambooModel = model;
		allFilterWrapper = new BamboAllFilterWrapper(model);
		new ListSpeedSearch(this);
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
		if (!listModel.contains(allFilterWrapper)) {
			listModel.addElement(allFilterWrapper);
		}
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
					final BamboProjectFilterWrapper obj = new BamboProjectFilterWrapper(new BambooProjectFilter(bambooProject),
							bambooModel);
					// add those projects which don't exist yet
					if (!listModel.contains(obj)) {
						listModel.addElement(obj);
					}
				}
				break;
			case SERVER:
				final Collection<ServerData> bambooServers = projectCfgManager.getAllEnabledBambooServerss();

				for (ServerData bambooServer : bambooServers) {
					final BambooServerFilter serverFilter = new BambooServerFilter(bambooServer);
					final BamboServerFilterWrapper obj = new BamboServerFilterWrapper(serverFilter,
							bambooModel);
					if (!listModel.contains(obj)) {
						listModel.addElement(obj);
					} else {
						// as BambooServerCfg objects are mutable and could be just changed in user settings, we need to up
						// update them basing on ServerId - the only immutable link.
						// I don't like this code, but I have no time now to fight with restoration of user selection
						// if I was to reset totally the model of this list
						final int i = listModel.indexOf(obj);
						if (i != -1) {
							final Object o = listModel.get(i);
							if (o instanceof BamboServerFilterWrapper) {
								BamboServerFilterWrapper wrapper = (BamboServerFilterWrapper) o;
								wrapper.updateWrapped(serverFilter);
							}
						}
					}
				}

				break;
			case STATE:
				for (AdjustedBuildStatus buildStatus : AdjustedBuildStatus.values()) {
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
	}

	public static class BambooServerFilter implements BambooBuildFilter {
		@NotNull
		private final ServerData bambooServerCfg;

		public BambooServerFilter(@NotNull final ServerData bambooServerCfg) {
			this.bambooServerCfg = bambooServerCfg;
		}

		@NotNull
		public ServerData getBambooServerCfg() {
			return bambooServerCfg;
		}

		public boolean doesMatch(final BambooBuildAdapterIdea build) {
			return bambooServerCfg.getServerId().equals(build.getServer().getServerId());
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

			// BambooServerCfg is mutable so we need to base on something immutable
			//noinspection RedundantIfStatement
			if (!bambooServerCfg.getServerId().equals(that.bambooServerCfg.getServerId())) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return bambooServerCfg.getServerId().hashCode();
		}
	}

	private static final BambooBuildFilter ALL_FILTER = new BambooBuildFilter() {
		public boolean doesMatch(final BambooBuildAdapterIdea build) {
			return true;
		}
	};


	private static class BambooProjectFilter implements BambooBuildFilter {
		private final String projectName;

		public BambooProjectFilter(final String projectName) {
			this.projectName = projectName;
		}

		public boolean doesMatch(final BambooBuildAdapterIdea build) {
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

		private final AdjustedBuildStatus status;

		public BuildStatusFilter(final AdjustedBuildStatus status) {
			this.status = status;
		}


		public boolean doesMatch(final BambooBuildAdapterIdea build) {
			return status == build.getAdjustedStatus();
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
		private final BuildListModelImpl model;

		public AbstractBambooBuildFilterWrapper(final T wrapped, final BuildListModelImpl model) {
			super(wrapped);
			this.model = model;
		}

		protected int getCount() {
			int i = 0;
			for (BambooBuildAdapterIdea build : model.getAllBuilds()) {
				if (wrapped.doesMatch(build)) {
					i++;
				}
			}
			return i;
		}

	}

	private static class BamboAllFilterWrapper extends AbstractBambooBuildFilterWrapper<BambooBuildFilter> {
		public BamboAllFilterWrapper(final BuildListModelImpl model) {
			super(ALL_FILTER, model);
		}

		@Override
		public String toString() {
			return "All (" + getCount() + ")";
		}
	}

	private static class BamboServerFilterWrapper extends AbstractBambooBuildFilterWrapper<BambooServerFilter> {
		public BamboServerFilterWrapper(final BambooServerFilter wrapped, final BuildListModelImpl model) {
			super(wrapped, model);
		}

		public void updateWrapped(BambooServerFilter newWrapped) {
			wrapped = newWrapped;
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
		public BamboProjectFilterWrapper(final BambooProjectFilter wrapped, final BuildListModelImpl model) {
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
		public BuildStatusFilterWrapper(final BuildStatusFilter wrapped, final BuildListModelImpl model) {
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
				case FAILURE:
					return "Build Failed";
				case SUCCESS:
					return "Build Succeeded";
				case UNKNOWN:
					return "Unknown State";
				case DISABLED:
					return "Build Disabled";
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
	}

}
