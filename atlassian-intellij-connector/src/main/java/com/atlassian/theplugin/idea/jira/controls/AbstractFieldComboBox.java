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

import com.atlassian.theplugin.commons.jira.api.JIRAActionField;
import com.atlassian.theplugin.commons.jira.api.JIRAActionFieldBean;
import com.atlassian.theplugin.commons.jira.api.JIRAConstant;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.idea.jira.renderers.JIRAConstantListRenderer;
import com.atlassian.theplugin.idea.jira.renderers.JIRAQueryFragmentListRenderer;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jacek Jaroczynski
 */
public abstract class AbstractFieldComboBox extends JComboBox implements ActionFieldEditor {

	protected boolean initialized;
	private JIRAActionField field;
	protected FreezeListener freezeListener;

	public AbstractFieldComboBox(final JIRAServerModel serverModel, final JiraIssueAdapter issue,
			final JIRAActionField field, boolean showIcon, final FreezeListener freezeListener) {

		this.field = field;
		this.freezeListener = freezeListener;
		final DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
		comboBoxModel.addElement("Fetching...");
		initialized = false;
		setModel(comboBoxModel);
		setEditable(false);
		setEnabled(false);
		if (showIcon) {
			setRenderer(new JIRAConstantListRenderer());
		} else {
			setRenderer(new JIRAQueryFragmentListRenderer());
		}

		fillCombo(comboBoxModel, serverModel, issue);
	}

	protected abstract void fillCombo(final DefaultComboBoxModel comboModel, final JIRAServerModel serverModel,
			final JiraIssueAdapter issue);


	public JIRAActionField getEditedFieldValue() {
		if (!initialized) {
			return null;
		}
		JIRAConstant type = (JIRAConstant) getSelectedItem();
		JIRAActionField ret = new JIRAActionFieldBean(field);
		ret.addValue(Long.valueOf(type.getId()).toString());
		return ret;
	}

	public Component getComponent() {
		return this;
	}

	public String getFieldName() {
		return field.getName();
	}
}
