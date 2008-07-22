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

package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.idea.ui.AtlassianTableView;
import com.atlassian.theplugin.idea.ui.AtlassianTableViewWithToolbar;
import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.TableViewModel;

import java.awt.*;

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
		} else if (component instanceof ActionToolbar) {
			Container mComponent = ((ActionToolbar) component).getComponent();
			while (mComponent != null) {
				if (mComponent instanceof AtlassianTableViewWithToolbar) {
					break;
				}
				mComponent = mComponent.getParent();
			}
			if (mComponent != null) {
				table = ((AtlassianTableViewWithToolbar) mComponent).getTable();
			}

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
