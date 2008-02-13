package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.idea.bamboo.ToolWindowBambooContent;
import com.atlassian.theplugin.idea.crucible.ToolWindowCrucibleContent;
import com.intellij.util.ui.UIUtil;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionGroup;

import javax.swing.*;
import java.awt.*;

public class BambooToolWindowPanel extends JPanel {
	private ToolWindowBambooContent bambooContent;

	public BambooToolWindowPanel() {
        super(new BorderLayout());

        setBackground(UIUtil.getTreeTextBackground());

        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup toolbar = (ActionGroup)actionManager.getAction("ThePlugin.ToolWindowToolBar");
        add(actionManager.createActionToolbar("atlassian.toolwindow.toolbar", toolbar, true).getComponent(), BorderLayout.NORTH);

        bambooContent = new ToolWindowBambooContent();
        JScrollPane pane = setupPane(bambooContent, "Waiting for Bamboo build statuses.");
        add(pane, BorderLayout.CENTER);
    }

    private JScrollPane setupPane(JEditorPane editorPane, String initialText) {
        editorPane.setText("<div style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">" + initialText + "</div>");
        JScrollPane scrollPane = new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setWheelScrollingEnabled(true);
        return scrollPane;
    }

    public ToolWindowBambooContent getBambooContent() {
		return bambooContent;
	}
}
