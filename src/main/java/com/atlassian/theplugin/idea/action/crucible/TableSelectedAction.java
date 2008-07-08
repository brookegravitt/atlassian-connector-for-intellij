package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.idea.ui.AtlassianTableView;
import com.atlassian.theplugin.idea.ui.AtlassianTableViewWithToolbar;
import com.intellij.ide.navigationToolbar.NavBarPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.TableViewModel;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 20, 2008
 * Time: 2:23:17 PM
 * To change this template use File | Settings | File Templates.
 */

public abstract class TableSelectedAction extends AnAction {

	public void actionPerformed(AnActionEvent event) {

		Object row = getRowValue(event);
		if (row != null) {
			itemSelected(row);
		}
	}

	protected AtlassianTableView identifyTable(AnActionEvent event) {
		DataContext context = event.getDataContext();
		Object component = DataKeys.CONTEXT_COMPONENT.getData(context);

		AtlassianTableView table = null;
		if (component instanceof AtlassianTableView) {
			table = (AtlassianTableView) component;
		} else if (component instanceof AtlassianTableViewWithToolbar) {
			table = ((AtlassianTableViewWithToolbar) component).getTable();
		} else if (component instanceof NavBarPanel) {
//			NavBarPanel navBarPanel = (NavBarPanel) component;
			// todo lguminski to identify a table
			//table = ((AtlassianTableViewWithToolbar) pane).getTable();
		}
		return table;
	}

	protected Object getRowValue(AnActionEvent event) {
		TableView table = identifyTable(event);
		if (table == null) {
			return null;
		}
		int row = table.getSelectedRow();
		if (row == -1) {
			return null;
		}
		TableViewModel model = table.getTableViewModel();
		return model.getItems().get(row);
	}

	protected abstract void itemSelected(Object row);

}
