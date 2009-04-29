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

package com.atlassian.theplugin.idea.config.serverconfig.action;

import com.intellij.openapi.actionSystem.*;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-28
 * Time: 10:54:31
 * To change this template use File | Settings | File Templates.
 */
public class AddServerAction extends AnAction {

	@Override
	public void actionPerformed(AnActionEvent event) {
        MouseEvent me = (MouseEvent) event.getInputEvent();
        showAddServerPopup(me);
	}

    public static void showAddServerPopup(MouseEvent event) {
        ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction("ThePlugin.ServerTypePopup");
        ActionPopupMenu popup = ActionManager.getInstance().createActionPopupMenu("Server type", actionGroup);
        JPopupMenu jPopupMenu = popup.getComponent();
        jPopupMenu.show(event.getComponent(), event.getX(), event.getY());
    }
}
