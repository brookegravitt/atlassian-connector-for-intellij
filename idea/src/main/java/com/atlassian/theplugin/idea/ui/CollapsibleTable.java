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
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.util.ui.ListTableModel;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class CollapsibleTable extends CollapsiblePanel {
    private final List<TableItemSelectedListener> listenerList = new ArrayList<TableItemSelectedListener>();

    private ListTableModel listTableModel;
    private AtlassianTableView table;

	public CollapsibleTable(TableColumnProvider tableColumnProvider,
                            ProjectToolWindowTableConfiguration projectToolWindowConfiguration,
                            String title, String toolbarPlace, String toolbarName,
                            final String popupMenuPlace, final String popupMenuName) {

        super(true, true, title, toolbarPlace, toolbarName);

        TableColumnInfo[] columns = tableColumnProvider.makeColumnInfo();
        listTableModel = new ListTableModel(columns);
        listTableModel.setSortable(true);

        table = new AtlassianTableView(listTableModel, projectToolWindowConfiguration);
        table.prepareColumns(columns, tableColumnProvider.makeRendererInfo());

        if (popupMenuPlace != null && popupMenuName != null && popupMenuName.length() > 0) {
            table.addMouseListener(new ShowPopupMouseAdapter(popupMenuName));
        }

		JScrollPane scrollTable = new JScrollPane(table);
		
		//table.setPreferredScrollableViewportSize(super.calculatePreferedSize());
		//table.setPreferredSize(table.getTableDimension());
		setContent(scrollTable);

		//scrollTable.setPreferredSize(super.calculatePreferedSize());
		
		//scrollTable.revalidate();
		

	}

    public AtlassianTableView getTable() {
        return table;
    }

    public ListTableModel getListTableModel() {
        this.setName("Setting data");
        return listTableModel;
    }

    public Object getSelectedObject() {
        return table.getSelectedObject();
    }

    public void addItemSelectedListener(TableItemSelectedListener listener) {
        listenerList.add(listener);
    }

    public void removeItemSelectedListener(TableItemSelectedListener listener) {
        listenerList.remove(listener);
    }

	private class ShowPopupMouseAdapter extends MouseAdapter {
		private final String popupMenuName;

		public ShowPopupMouseAdapter(String popupMenuName) {
			this.popupMenuName = popupMenuName;
		}

		public void mouseClicked(MouseEvent e) {
			for (TableItemSelectedListener tableItemSelectedListener : listenerList) {
				tableItemSelectedListener.itemSelected(table.getSelectedObject(), e.getClickCount());
			}
		}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger() && table.isEnabled()) {

				for (TableItemSelectedListener tableItemSelectedListener : listenerList) {
					tableItemSelectedListener.itemSelected(table.getSelectedObject(), 1);
				}

				final DefaultActionGroup actionGroup = new DefaultActionGroup();

				final ActionGroup configActionGroup = (ActionGroup) ActionManager
						.getInstance().getAction(popupMenuName);
				actionGroup.addAll(configActionGroup);

				final ActionPopupMenu popup =
						ActionManager.getInstance().createActionPopupMenu("Context menu", actionGroup);

				final JPopupMenu jPopupMenu = popup.getComponent();
				jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}


}



