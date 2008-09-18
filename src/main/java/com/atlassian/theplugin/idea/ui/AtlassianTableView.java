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
import javax.swing.event.TableModelEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AtlassianTableView<T> extends TableView<T> {
	private static final int DEFAULT_ROW_HEIGHT = 20;
	private boolean autoAdjustHeight = true;
	private static final int MAX_DISPLAYED_ROW_COUNT = 15;
	private final List<TableItemSelectedListener<T>> listenerList = new ArrayList<TableItemSelectedListener<T>>();
	private UserTableContext state = new UserTableContext();


	public AtlassianTableView(TableColumnProvider columnProvider, ListTableModel<T> listTableModel, final Storage storage) {
		super(listTableModel);
		setBorder(BorderFactory.createEmptyBorder());
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getColumnModel().setColumnMargin(0);

		setMinRowHeight(DEFAULT_ROW_HEIGHT);
		setAutoResizeMode(TableView.AUTO_RESIZE_OFF);
		prepareColumns(columnProvider);
		getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// stores table configuration in Storage object
				if (storage != null) {
					TableView.store(storage, AtlassianTableView.this);
				}
			}
		});
		getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				for (TableItemSelectedListener<T> tableItemSelectedListener : getListenerList()) {
					tableItemSelectedListener.itemSelected(AtlassianTableView.this);
				}
			}
		});
	}

	public AtlassianTableView(TableColumnProvider columnProvider, ListTableModel<T> listTableModel, final Storage storage,
							  final String popupMenuPlace, final String popupMenuName) {
		this(columnProvider, listTableModel, storage);
		if (popupMenuPlace != null && popupMenuName != null && popupMenuName.length() > 0) {
			addMouseListener(new ShowPopupMouseAdapter<T>(this, popupMenuName, popupMenuPlace));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ListTableModel<T> getListTableModel() {
		return super.getListTableModel();
	}


	public void prepareColumns(TableColumnProvider tableColumnProvider) {
		TableColumnInfo[] cols = tableColumnProvider.makeColumnInfo();
		TableCellRenderer[] renderers = tableColumnProvider.makeRendererInfo();
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
	 *
	 * @param storage object with table properties
	 */
	public void restore(Storage storage) {
		if (storage != null) {
			TableView.restore(storage, this);
		}
	}

	/**
	 * Stores current table properties into Storage object
	 *
	 * @param storage object to store table properties
	 */
	public void store(Storage storage) {
		TableView.store(storage, this);
	}


	public Dimension getTableDimension() {
		int tableWidth = 0;
		int tableHeight = Math.min(getModel().getRowCount(), MAX_DISPLAYED_ROW_COUNT) * getRowHeight();
		// Resize width
		for (int col = 0; col < getColumnModel().getColumnCount(); col++) {
			tableWidth += (getColumnModel().getColumn(col).getPreferredWidth());
		}

		if (getTableHeader() != null) {
			Dimension tableHeaderDimension = getTableHeader().getPreferredSize();
			tableHeight += tableHeaderDimension.height;
		}

		return new Dimension(tableWidth, tableHeight);
	}


	@Override
	public void tableChanged(TableModelEvent e) {

		Dimension prefered = getTableDimension();
		setPreferredScrollableViewportSize(prefered);
		super.tableChanged(e);
	}

	public void addItemSelectedListener(TableItemSelectedListener<T> listener) {
		listenerList.add(listener);
	}

	public void removeItemSelectedListener(TableItemSelectedListener<T> listener) {
		listenerList.remove(listener);
	}

	public List<TableItemSelectedListener<T>> getListenerList() {
		return Collections.unmodifiableList(listenerList);
	}

	public UserTableContext getStateContext() {
		return state;
	}

	public void setStateContext(UserTableContext context) {
		state = context;
	}
}