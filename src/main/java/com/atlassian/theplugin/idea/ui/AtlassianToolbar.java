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

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionToolbar;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 18, 2008
 * Time: 7:35:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class AtlassianToolbar extends JPanel {
	private AtlassianToolbar() {
	}

	public static JPanel createToolbar(String toolbarPlace, String toolbarName) {
		JPanel panel = new JPanel(new BorderLayout());
		if (toolbarName != null && toolbarPlace.length() > 0 && toolbarName.length() > 0) {
			ActionManager aManager = ActionManager.getInstance();
			ActionGroup serverToolBar = (ActionGroup) aManager.getAction(toolbarName);
			if (serverToolBar != null) {
				ActionToolbar actionToolbar = aManager.createActionToolbar(
						toolbarPlace, serverToolBar, true);
				panel.add(actionToolbar.getComponent(), BorderLayout.NORTH);
			}
		}
		return panel;
	}
}
