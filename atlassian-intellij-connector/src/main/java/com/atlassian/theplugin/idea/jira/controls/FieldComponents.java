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
package com.atlassian.theplugin.idea.jira.controls;

import com.atlassian.theplugin.commons.jira.api.*;
import com.atlassian.theplugin.commons.jira.api.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.util.PluginUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public class FieldComponents extends AbstractFieldList {
	public FieldComponents(final JIRAServerModel jiraServerModel, final JIRAIssue issue, final JIRAActionField field,
			final FreezeListener freezeListener) {
		super(jiraServerModel, issue, field, freezeListener);
	}

	protected void fillList(final DefaultListModel listModel, final JIRAServerModel serverModel, final JIRAIssue issue) {
		freezeListener.freeze();
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					List<JIRAProject> projects = serverModel.getProjects(issue.getServer());
					JIRAProject issueProject = null;
					for (JIRAProject project : projects) {
						if (issue.getProjectKey().equals(project.getKey())) {
							issueProject = project;
							break;
						}
					}
					final List<JIRAComponentBean> versions =
							serverModel.getComponents(issue.getServer(), issueProject, false);
					SwingUtilities.invokeLater(new LocalComponentListFiller(listModel, versions, issue));
				} catch (JIRAException e) {
					PluginUtil.getLogger().error(e.getMessage());
				}
			}
		});
		t.start();
	}

	private class LocalComponentListFiller implements Runnable {
		private DefaultListModel listModel;
		private List<JIRAComponentBean> components;
		private JIRAIssue issue;

		public LocalComponentListFiller(final DefaultListModel listModel,
				final List<JIRAComponentBean> components, final JIRAIssue issue) {

			this.listModel = listModel;
			this.components = components;
			this.issue = issue;
		}

		public void run() {
			ArrayList<Integer> selectedIndexes = new ArrayList<Integer>();
			listModel.removeAllElements();
			int i = 0;
			for (JIRAComponentBean v : components) {
				listModel.addElement(v);
				if (issue.getComponents() != null && issue.getComponents().contains(v)) {
					selectedIndexes.add(i);
				}
				i++;
			}

			initialized = true;
			setEnabled(true);

			setSelectedIndices(selectedIndexes);

			freezeListener.unfreeze();
		}
	}
}
