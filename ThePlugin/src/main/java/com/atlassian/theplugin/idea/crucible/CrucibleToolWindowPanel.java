package com.atlassian.theplugin.idea.crucible;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.util.ui.UIUtil;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.*;

public class CrucibleToolWindowPanel extends JPanel {
    private ToolWindowCrucibleContent crucibleContent;

    public CrucibleToolWindowPanel() {
        super(new BorderLayout());

        setBackground(UIUtil.getTreeTextBackground());

        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup toolbar = (ActionGroup) actionManager.getAction("ThePlugin.CrucibleToolWindowToolBar");
        add(actionManager.createActionToolbar(
                "atlassian.toolwindow.toolbar", toolbar, true).getComponent(), BorderLayout.NORTH);

        crucibleContent = new ToolWindowCrucibleContent();
        crucibleContent.setEditorKit(new ClasspathHTMLEditorKit());
        JScrollPane pane = setupPane(crucibleContent, "No reviews at this time.");
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

    public ToolWindowCrucibleContent getCrucibleContent() {
        return crucibleContent;
    }
}
