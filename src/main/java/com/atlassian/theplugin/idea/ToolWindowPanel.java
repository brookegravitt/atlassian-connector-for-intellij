package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.idea.bamboo.ToolWindowBambooContent;
import com.atlassian.theplugin.idea.crucible.ToolWindowCrucibleContent;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-01
 * Time: 11:39:50
 * To change this template use File | Settings | File Templates.
 */
public class ToolWindowPanel extends JPanel {
	private ToolWindowBambooContent bambooContent;
	private ToolWindowCrucibleContent crucibleContent;

	ToolWindowPanel() {
		super(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        bambooContent = new ToolWindowBambooContent();
        tabs.add("Bamboo", setupPane(bambooContent, "Waiting for Bamboo build statuses."));
        crucibleContent = new ToolWindowCrucibleContent();
        tabs.add("Crucible", setupPane(crucibleContent, "Waiting for Crucible review data."));
        add(tabs, BorderLayout.CENTER);
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

    public ToolWindowCrucibleContent getCrucibleContent() {
        return crucibleContent;
    }
}
