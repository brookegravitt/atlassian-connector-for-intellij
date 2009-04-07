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

import com.atlassian.theplugin.idea.jira.renderers.JIRAQueryFragmentListRenderer;
import com.atlassian.theplugin.jira.api.JIRAActionField;
import com.atlassian.theplugin.jira.api.JIRAActionFieldBean;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.model.JIRAServerModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author Jacek Jaroczynski
 */
public abstract class AbstractFieldList extends JScrollPane implements ActionFieldEditor {
	protected boolean initialized;
	private JIRAActionField field;
	private JList list = new JList();

	public JList getList() {
		return list;
	}

	public AbstractFieldList(final JIRAServerModel serverModel, final JIRAIssue issue, final JIRAActionField field) {

		this.field = field;
		final DefaultListModel listModel = new DefaultListModel();
		listModel.addElement("Fetching...");
		initialized = false;
		list.setModel(listModel);
		setEnabled(false);
		list.setCellRenderer(new JIRAQueryFragmentListRenderer());
		list.setVisibleRowCount(6);
		setViewportView(list);

		fillList(listModel, serverModel, issue);
	}

	protected void setSelectedIndices(final ArrayList<Integer> selectedIndexes) {
		if (selectedIndexes.size() > 0) {
			int j = 0;
			int[] selected = new int[selectedIndexes.size()];
			for (Integer s : selectedIndexes) {
				selected[j] = s;
				j++;
			}
			getList().setSelectedIndices(selected);
			getList().ensureIndexIsVisible(selected[0]);
		}
	}

	protected abstract void fillList(final DefaultListModel listModel, final JIRAServerModel serverModel,
			final JIRAIssue issue);


	public JIRAActionField getEditedFieldValue() {
		if (!initialized) {
			return null;
		}

		JIRAActionField ret = new JIRAActionFieldBean(field);

		for (Object selected : list.getSelectedValues()) {
			JIRAQueryFragment selectedValue = (JIRAQueryFragment) selected;
			ret.addValue(Long.valueOf(selectedValue.getId()).toString());
		}
		return ret;
	}

	public Component getComponent() {
		return this;
	}
}
