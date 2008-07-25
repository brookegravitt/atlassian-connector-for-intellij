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

import com.intellij.util.config.Storage;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.TableViewModel;

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
    private JComponent toolBarPanel;
    private JLabel statusLabel;
    private JLabel headerLabel;


    public AtlassianTableViewWithToolbar(TableColumnProvider tableColumnProvider, ListTableModel listTableModel,
                                         Storage storage,
                                         String toolbarPlace, String toolbarName,
                                         String popupMenuPlace, String popupMenuName) {
        super(new GridBagLayout());
        table = new AtlassianTableView(tableColumnProvider, listTableModel, storage, popupMenuPlace, popupMenuName);
        statusLabel = new JLabel();
        headerLabel = new JLabel();

        JScrollPane scrollTable = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTable.setWheelScrollingEnabled(true);

        int gridy = 0;
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = gridy++;

        if (toolbarName != null && toolbarPlace != null) {
            toolBarPanel = AtlassianToolbar.createToolbar(toolbarPlace, toolbarName);
            add(toolBarPanel, gbc);
            gbc.weightx = 1;
        }

        gbc.gridx = 0;
        gbc.gridy = gridy++;
        add(headerLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.weighty = 1;

        add(scrollTable, gbc);

        gbc = new GridBagConstraints();
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        add(statusLabel, gbc);

    }

    public void setStatusText(String message) {
        statusLabel.setText(message);
    }

    public void prepareColumns(TableColumnProvider tableColumnProvider) {
        table.prepareColumns(tableColumnProvider);
    }

    public ListTableModel getListTableModel() {
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

    public void clearSelection() {
        table.clearSelection();
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

    public void setHeaderText(String msg) {
        headerLabel.setText(msg);
    }
}
