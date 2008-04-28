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

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.intellij.ui.table.TableView;
import com.intellij.util.config.Storage;
import com.intellij.util.ui.ListTableModel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AtlassianTableView extends TableView {
    private static final int DEFAULT_ROW_HEIGHT = 20;

    public AtlassianTableView(ListTableModel listTableModel, final Storage storage) {
        super(listTableModel);

        setBorder(BorderFactory.createEmptyBorder());
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        getColumnModel().setColumnMargin(0);

		setMinRowHeight(DEFAULT_ROW_HEIGHT);
		setAutoResizeMode(TableView.AUTO_RESIZE_OFF);

		getTableHeader().addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				// stores table configuration in Storage object
				TableView.store(storage, AtlassianTableView.this);
			}
		});

	}

	public void prepareColumns(TableColumnInfo[] cols, TableCellRenderer[] renderers) {
		TableColumnModel model = getColumnModel();
		for (int i = 0; i < model.getColumnCount(); ++i) {
			model.getColumn(i).setResizable(true);
			model.getColumn(i).setPreferredWidth(cols[i].getPrefferedWidth());
			if (renderers[i] != null) {
				model.getColumn(i).setCellRenderer(renderers[i]);
			}
		}		
	}

		/**
	 * Restores table properties from Storage object into current table instance
	 * @param storage object with table properties
	 */
	public void restore(Storage storage) {
		TableView.restore(storage, this);
	}

	/**
	 * Stores current table properties into Storage object
	 * @param storage object to store table properties
	 */
	public void store(Storage storage) {
		TableView.store(storage, this);
	}
}

