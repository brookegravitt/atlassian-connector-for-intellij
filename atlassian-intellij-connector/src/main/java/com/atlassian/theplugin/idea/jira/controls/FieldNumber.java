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
import com.atlassian.connector.commons.jira.JIRAActionFieldBean;

import javax.swing.*;
import java.awt.*;

/**
 * @autrhor pmaruszak
 * @date Jul 7, 2010
 */
public class FieldNumber extends JTextField implements ActionFieldEditor {

	private JIRAActionField field;

    public FieldNumber(final String text, final JIRAActionField field) {
		super(text);
        setCaretPosition(0);
		this.field = field;
	}
    public JIRAActionField getEditedFieldValue() {
        JIRAActionField ret = new JIRAActionFieldBean(field);
		ret.addValue(getText());
		return ret;
    }

    public Component getComponent() {
        return this;
    }

    public String getFieldName() {
        return field.getName();
    }
}
