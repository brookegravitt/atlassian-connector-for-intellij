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

package com.atlassian.theplugin.idea.bamboo;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import thirdparty.javaworld.ClasspathHTMLEditorKit;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class BambooToolWindowPanel extends JPanel {
	private ToolWindowBambooContent bambooContent;

	public BambooToolWindowPanel() {
        super(new BorderLayout());

        setBackground(UIUtil.getTreeTextBackground());

        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup toolbar = (ActionGroup) actionManager.getAction("ThePlugin.BambooToolWindowToolBar");
        add(actionManager.createActionToolbar(
                "atlassian.toolwindow.toolbar", toolbar, true).getComponent(), BorderLayout.NORTH);

        bambooContent = new ToolWindowBambooContent();
        bambooContent.setEditorKit(new ClasspathHTMLEditorKit());
        JScrollPane pane = setupPane(bambooContent, "Waiting for Bamboo build statuses.");
        add(pane, BorderLayout.CENTER);
    }

    private JScrollPane setupPane(JEditorPane editorPane, String initialText) {
        editorPane.setText(
                "<div style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">" + initialText + "</div>");
        JScrollPane scrollPane = new JScrollPane(
                editorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setWheelScrollingEnabled(true);
        return scrollPane;
    }

    public ToolWindowBambooContent getBambooContent() {
		return bambooContent;
	}
}
