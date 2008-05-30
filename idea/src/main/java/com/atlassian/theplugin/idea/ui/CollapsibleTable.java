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
    private JScrollPane scrollTable;

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
            table.addMouseListener(new MouseAdapter() {
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
            });
        }

        scrollTable = new JScrollPane(table);
        setContent(scrollTable);
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
}

