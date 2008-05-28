package com.atlassian.theplugin.idea.ui;

import com.atlassian.theplugin.idea.ui.TableColumnProvider;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.configuration.ProjectToolWindowTableConfiguration;
import com.intellij.util.ui.ListTableModel;

import javax.swing.*;

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
					 String title, String toolbarPlace, String toolbarName,
					 String popupMenuPlace, String popupMenuName){

		super(true, true, title, toolbarPlace, toolbarName, popupMenuPlace, popupMenuName);

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

    public ListTableModel getListTableModel() {
        this.setName("Setting data");
        return listTableModel;
    }

}

