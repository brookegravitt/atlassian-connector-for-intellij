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

package com.atlassian.theplugin.idea.jira.table.columns;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;

import java.util.Comparator;

public class IssuePriorityColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 20;
	public static final String COLUMN_NAME = "Priority";

	public Object valueOf(Object o) {
		return (JiraIssueAdapter) o;
	}

	public Class getColumnClass() {
		return JiraIssueAdapter.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				Long ol = Long.valueOf(((JiraIssueAdapter) o).getPriorityId());
				Long ol1 = Long.valueOf(((JiraIssueAdapter) o1).getPriorityId());
				return ol.compareTo(ol1);
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}

	public String getColumnName() {
		return COLUMN_NAME;
	}
}