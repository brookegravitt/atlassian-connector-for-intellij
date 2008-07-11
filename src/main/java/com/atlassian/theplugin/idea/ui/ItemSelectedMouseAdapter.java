package com.atlassian.theplugin.idea.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ItemSelectedMouseAdapter extends MouseAdapter {
	private AtlassianTableView tableView;

	public ItemSelectedMouseAdapter(AtlassianTableView tableView) {
		this.tableView = tableView;
	}

	public void mouseClicked(MouseEvent e) {
		for (TableItemSelectedListener tableItemSelectedListener : tableView.getListenerList()) {
			tableItemSelectedListener.itemSelected(tableView.getSelectedObject(), e.getClickCount());
		}
	}

}
