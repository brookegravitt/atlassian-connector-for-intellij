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

package com.atlassian.theplugin.idea.ui;

import com.atlassian.theplugin.configuration.ProjectToolWindowTableConfiguration;
import com.intellij.util.ui.ListTableModel;

public class CollapsibleTable<T> extends CollapsiblePanel {

    private ListTableModel<T> listTableModel;
	private AtlassianTableViewWithToolbar<T> table;

	public CollapsibleTable(TableColumnProvider tableColumnProvider,
                            ProjectToolWindowTableConfiguration projectToolWindowConfiguration,
                            String title, String toolbarPlace, String toolbarName,
                            final String popupMenuPlace, final String popupMenuName) {

        super(true, true, title);

        listTableModel = new ListTableModel<T>(tableColumnProvider.makeColumnInfo());
        listTableModel.setSortable(true);
        table = new AtlassianTableViewWithToolbar<T>(tableColumnProvider, listTableModel, projectToolWindowConfiguration,
				toolbarPlace, toolbarName, popupMenuPlace, popupMenuName);
		setContent(table);
		table.setPreferredScrollableViewportSize(table.getTableDimension());
	}

    public AtlassianTableView<T> getTable() {
        return table.getTable();
    }

    public ListTableModel<T> getListTableModel() {
        this.setName("Setting data");
        return listTableModel;
    }

    public Object getSelectedObject() {
        return table.getSelectedObject();
    }

    public void clearSelection() {
        table.clearSelection();
	}

    public void addItemSelectedListener(TableItemSelectedListener<T> listener) {
        table.addItemSelectedListener(listener);
    }

    public void removeItemSelectedListener(TableItemSelectedListener<T> listener) {
        table.removeItemSelectedListener(listener);
    }

	public void clear() {
		table.clear();
	}
}



