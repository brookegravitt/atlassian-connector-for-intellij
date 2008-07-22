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

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
* User: lguminski
* Date: Jun 18, 2008
* Time: 5:10:12 PM
* To change this template use File | Settings | File Templates.
*/
public class ShowPopupMouseAdapter extends MouseAdapter {
	private final String popupMenuName;
	private AtlassianTableView tableView;

	public ShowPopupMouseAdapter(AtlassianTableView tableView, String popupMenuName) {
		this.tableView = tableView;
		this.popupMenuName = popupMenuName;
	}

	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger() && tableView.isEnabled()) {

			for (TableItemSelectedListener tableItemSelectedListener : tableView.getListenerList()) {
				tableItemSelectedListener.itemSelected(tableView.getSelectedObject(), 1);
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
}
