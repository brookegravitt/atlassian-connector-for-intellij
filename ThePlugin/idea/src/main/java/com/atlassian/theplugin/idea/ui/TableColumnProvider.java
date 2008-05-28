package com.atlassian.theplugin.idea.ui;

import com.atlassian.theplugin.idea.TableColumnInfo;

import javax.swing.table.TableCellRenderer;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: May 19, 2008
 * Time: 8:52:01 AM
 * To change this template use File | Settings | File Templates.
 */
public interface TableColumnProvider {
	TableColumnInfo[] makeColumnInfo();
	TableCellRenderer[]  makeRendererInfo();
}
