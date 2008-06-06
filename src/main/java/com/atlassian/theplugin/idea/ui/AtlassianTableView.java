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
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ContainerListener;
import java.awt.event.ComponentListener;
import java.awt.*;

public class AtlassianTableView extends TableView {
    private static final int DEFAULT_ROW_HEIGHT = 20;
	private boolean autoAdjustHeight = true;
	private int preferredWidth = -1;

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

		//doLayout();


		
	}
	
	public void prepareColumns(TableColumnInfo[] cols, TableCellRenderer[] renderers) {
		TableColumnModel model = getColumnModel();
		preferredWidth = 0;
		for (int i = 0; i < model.getColumnCount(); ++i) {
			model.getColumn(i).setResizable(true);
			model.getColumn(i).setPreferredWidth(cols[i].getPrefferedWidth());
			preferredWidth +=  cols[i].getPrefferedWidth();
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

//	void setAutoAdjustHeight(boolean adjust){
//		autoAdjustHeight = adjust;
//
//		doLayout();
//
//	}
//
//	public void doLayout() {
//		if (autoAdjustHeight){
//
//				this.setRowHeight(DEFAULT_ROW_HEIGHT);
//				setPreferredSize(getTableDimension());
//				setMaximumSize(getTableDimension());
//			}
//	}

    public Dimension getTableDimension(){
		   int maxHeight = -1;
		   int maxWidth = -1;
		   Dimension compSize;
		   int tableWidth = 0, tableHeight = 0;

			// for (int row = 0; row < getModel().getRowCount(); row++) {
				 //for (int col = 0; col < getModel().getColumnCount(); col++) {
					//JComponent it = (JComponent)getModel().getValueAt(row, col);
						//getColumnModel().getColumn(col).getPreferredWidth();
					 
					 //if (it != null && getModel().getValueAt(row, col) != null) {  // Here we got an actual component

					//	 compSize = ((JComponent)(getModel().getValueAt(row, col))).getPreferredSize();
					//	 maxHeight = Math.max((int)compSize.getHeight(), maxHeight);
					// }
				 //}
				 //int cellHeight = getRowHeight(row));
				 //tableHeight += getRowHeight(row);
				 //maxHeight = -1;
			 //}

			 tableHeight = getModel().getRowCount() * getRowHeight();
			 // Resize width
			 for (int col = 0; col < getColumnModel().getColumnCount(); col++) {
//				 for (int row = 0; row < getModel().getRowCount(); row++) {
//					 JComponent it = (JComponent)getModel().getValueAt(row, col);
//
//					 if(it != null && getModel().getValueAt(row, col) != null) {
//						 compSize = it.getPreferredSize();
//						 maxWidth = Math.max((int)compSize.getWidth(), maxWidth);
//
//					 }
//				 int cellWidth = Math.max(maxWidth,
//						 ((getColumnModel().getColumn(col).getPreferredWidth())));

				 tableWidth += (getColumnModel().getColumn(col).getPreferredWidth());
				 //}


			 }




		if (getTableHeader() != null) {
			Dimension tableHeaderDimension = getTableHeader().getPreferredSize();			
			tableHeight += tableHeaderDimension.height;
		}

	   return new Dimension( preferredWidth, tableHeight);
   }



	public void tableChanged(TableModelEvent e) {

		Dimension prefered = getTableDimension();
		setPreferredScrollableViewportSize(prefered);
		//setPreferredSize(prefered);
		
		super.tableChanged(e);
	}
};