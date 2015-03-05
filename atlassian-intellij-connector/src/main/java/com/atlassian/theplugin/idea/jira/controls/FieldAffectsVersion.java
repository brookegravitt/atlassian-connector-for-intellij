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


import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.beans.JIRAVersionBean;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.util.PluginUtil;
import com.google.common.collect.Lists;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public class FieldAffectsVersion extends AbstractFieldList {
	public FieldAffectsVersion(final JIRAServerModel jiraServerModel, final JiraIssueAdapter issue, final JIRAActionField field,
			final FreezeListener freezeListener) {
		super(jiraServerModel, issue, field, freezeListener);
	}

	protected void fillList(final DefaultListModel listModel, final JIRAServerModel serverModel, final JiraIssueAdapter issue) {
		freezeListener.freeze();
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
                    final List<JIRAVersionBean> versions = Lists.newArrayList();
                    List<JIRAProject> projects = serverModel.getProjects(issue.getJiraServerData());
					JIRAProject issueProject = null;
                    if (projects != null) {
                        for (JIRAProject project : projects) {
                            if (issue.getProjectKey().equals(project.getKey())) {
                                issueProject = project;
                                break;
                            }
                        }
                        versions.addAll(serverModel.getVersions(issue.getJiraServerData(), issueProject, false));
                    }
					SwingUtilities.invokeLater(new LocalVersionListFiller(listModel, versions, issue));
				} catch (JIRAException e) {
					PluginUtil.getLogger().error(e.getMessage());
				}
			}
		});
		t.start();
	}

	private class LocalVersionListFiller implements Runnable {
		private final DefaultListModel listModel;
		private final List<JIRAVersionBean> versions;
		private final JiraIssueAdapter issue;

		public LocalVersionListFiller(
				final DefaultListModel listModel, final List<JIRAVersionBean> versions, final JiraIssueAdapter issue) {
			this.listModel = listModel;
			this.versions = versions;
			this.issue = issue;
		}

		public void run() {
			ArrayList<Integer> selectedIndexes = new ArrayList<Integer>();
			listModel.removeAllElements();
			int i = 0;
			for (JIRAVersionBean v : versions) {
				listModel.addElement(v);
				if (issue.getAffectsVersions() != null && issue.getAffectsVersions().contains(v)) {
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