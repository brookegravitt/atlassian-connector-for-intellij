package com.atlassian.theplugin.idea.jira.controls;

import com.atlassian.theplugin.jira.api.JIRAActionField;

import java.awt.*;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 12:13:53 PM
 */
public interface ActionFieldEditor {
	JIRAActionField getEditedFieldValue();

	Component getComponent();

	String getFieldName();
}
