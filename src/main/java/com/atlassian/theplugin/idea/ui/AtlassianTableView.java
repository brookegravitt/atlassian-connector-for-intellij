package com.atlassian.theplugin.idea.ui;

import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ListTableModel;

import javax.swing.*;

public class AtlassianTableView extends TableView {
    private static final int DEFAULT_ROW_HEIGHT = 20;

    public AtlassianTableView(ListTableModel listTableModel) {
        super(listTableModel);

        setBorder(BorderFactory.createEmptyBorder());
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        getColumnModel().setColumnMargin(0);
        setRowHeight(DEFAULT_ROW_HEIGHT);
    }
}
