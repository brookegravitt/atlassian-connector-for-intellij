package com.atlassian.theplugin.idea.ui;

import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.TableViewModel;
import com.intellij.util.config.Storage;
import com.atlassian.theplugin.idea.Constants;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 18, 2008
 * Time: 6:11:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class AtlassianTableViewWithToolbar extends JPanel {
	private AtlassianTableView table;
	private JPanel toolBarPanel;
	private JLabel statusLabel;
	private JLabel headerLabel;


	public AtlassianTableViewWithToolbar(TableColumnProvider tableColumnProvider, ListTableModel listTableModel,
										 Storage storage,
										 String toolbarPlace, String toolbarName,
										 String popupMenuPlace, String popupMenuName) {
		super(new BorderLayout());
		toolBarPanel = new AtlassianToolbar(toolbarPlace, toolbarName);
		table = new AtlassianTableView(tableColumnProvider, listTableModel, storage, popupMenuPlace, popupMenuName);
		statusLabel = new JLabel();
		headerLabel = new JLabel();

		JScrollPane scrollTable = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollTable.setWheelScrollingEnabled(true);
		add(toolBarPanel, BorderLayout.NORTH);

		//add(headerLabel, BorderLayout.NORTH);
		add(scrollTable, BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);

		/*
						GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, Constants.DIALOG_MARGIN);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.PAGE_START;
		add(toolBarPanel, gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, Constants.DIALOG_MARGIN);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTH;
		add(headerLabel, gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, Constants.DIALOG_MARGIN);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
		add(scrollTable, gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 0, Constants.DIALOG_MARGIN);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.PAGE_START;
		add(statusLabel, gbc);
		*/
	}

	public void setStatusText(String message) {
		statusLabel.setText(message);
	}

	public void prepareColumns(TableColumnProvider tableColumnProvider) {
		table.prepareColumns(tableColumnProvider);
	}

	public ListTableModel getListTableModel()
	{
		return table.getListTableModel();
	}

	public JLabel getHeaderLabel() {
		return headerLabel;
	}

	public Dimension getTableDimension() {
		return table.getTableDimension();
	}

	public void setPreferredScrollableViewportSize(Dimension tableDimension) {
		table.setPreferredScrollableViewportSize(tableDimension);
	}

	public Object getSelectedObject() {
		return table.getSelectedObject();
	}

	public void addItemSelectedListener(TableItemSelectedListener listener) {
		table.addItemSelectedListener(listener);
	}

	public void removeItemSelectedListener(TableItemSelectedListener listener) {
		table.removeItemSelectedListener(listener);
	}

	public AtlassianTableView getTable() {
		return table;
	}

	public TableViewModel getTableViewModel() {
		return table.getTableViewModel();
	}

	public ListSelectionModel getSelectionModel() {
		return table.getSelectionModel();
	}



	public TableColumnModel getColumnModel() {
		return table.getColumnModel();
	}

	public void setMinRowHeight(int i) {
		table.setMinRowHeight(i);
	}

	public void removeAll() {
		table.removeAll();
	}

}
