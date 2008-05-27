package com.atlassian.theplugin.idea.ui;

import com.atlassian.theplugin.idea.jira.table.TableColumnProvider;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.configuration.ProjectToolWindowTableConfiguration;
import com.intellij.util.ui.ListTableModel;
import com.intellij.ide.BrowserUtil;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: May 19, 2008
 * Time: 8:57:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class CollapsibleTable extends CollapsiblePanel {
	private ListTableModel listTableModel;
	private AtlassianTableView table;
	private JScrollPane scrollTable;

	public CollapsibleTable(TableColumnProvider tableColumnProvider,
					 ProjectToolWindowTableConfiguration projectToolWindowConfiguration,
					 String title){

		super(true, true, title);

		TableColumnInfo[] columns = tableColumnProvider.makeColumnInfo();
		listTableModel = new ListTableModel(columns);
		listTableModel.setSortable(true);


		table = new AtlassianTableView(listTableModel, projectToolWindowConfiguration);
		table.prepareColumns(columns, tableColumnProvider.makeRendererInfo());

		scrollTable = new JScrollPane(table);
		setContent(scrollTable);
	}

	public AtlassianTableView getTable(){
		return table;
	}
}

