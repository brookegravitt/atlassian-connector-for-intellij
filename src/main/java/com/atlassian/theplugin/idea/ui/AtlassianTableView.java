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
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;

public class AtlassianTableView extends TableView {
    private static final int DEFAULT_ROW_HEIGHT = 20;
	private boolean autoAdjustHeight = true;
	private int rowHeight = 20;
	private int maxTableDisplayedRowCount = 5;

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

		doLayout();

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

	void setAutoAdjustHeight(boolean adjust, int maxTableDiaplayedRowCount, int rowHeight ){
		this.rowHeight = rowHeight;
		autoAdjustHeight = adjust;
		this.maxTableDisplayedRowCount = maxTableDiaplayedRowCount;
		doLayout();

	}

	public void doLayout() {
		if (autoAdjustHeight){
			if (maxTableDisplayedRowCount > 0) {
				this.setRowHeight(rowHeight <= 0 || rowHeight < DEFAULT_ROW_HEIGHT ? DEFAULT_ROW_HEIGHT : rowHeight);
				setPreferredSize(getTableDimension());
				setMaximumSize(getTableDimension());
			}
		} else {

		}
	}

   public Dimension getTableDimension(){
	  int tableWidth = 0, tableHeight = 0;


		// Resize width
		TableColumnModel model = getColumnModel();
		for (int i = 0; i < model.getColumnCount(); ++i) {

			tableWidth += model.getColumn(i).getPreferredWidth();
		}
	   for (int i = 0; i < getColumnCount() && i < maxTableDisplayedRowCount; ++i){
			tableHeight += getColumnCount() * getRowHeight(i);


	   }

	   return new Dimension(tableWidth, tableHeight);


   }

private class CustomTableCellRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent (JTable table,
Object obj, boolean isSelected, boolean hasFocus, int row, int column) {
      Component cell = super.getTableCellRendererComponent(
                         table, obj, isSelected, hasFocus, row, column);
      if (!isSelected) {
        if (!(row % 2 == 0)) {
          cell.setBackground(Color.lightGray);
        }
      }
      return cell;
    }
  }
};