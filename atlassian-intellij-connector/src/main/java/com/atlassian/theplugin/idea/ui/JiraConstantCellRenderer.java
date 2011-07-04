package com.atlassian.theplugin.idea.ui;

import com.atlassian.connector.commons.jira.beans.JIRAConstant;
import com.atlassian.theplugin.commons.jira.cache.CachedIconLoader;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.JList;

/**
 * User: pstefaniak
 * Date: Mar 22, 2010
 */

public class JiraConstantCellRenderer extends ColoredListCellRenderer {

	@Override
	protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
		if (value != null) {
			JIRAConstant type = (JIRAConstant) value;
			append(type.getName(), SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
			setIcon(CachedIconLoader.getIcon(type.getIconUrl()));
		}
	}
}
