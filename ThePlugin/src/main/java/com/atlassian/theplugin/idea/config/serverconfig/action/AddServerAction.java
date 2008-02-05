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
    public void actionPerformed(AnActionEvent event) {
        ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction("ThePlugin.ServerTypePopup");
        ActionPopupMenu popup = ActionManager.getInstance().createActionPopupMenu("Server type", actionGroup);

        MouseEvent me = (MouseEvent) event.getInputEvent();

        JPopupMenu jPopupMenu = popup.getComponent();
        jPopupMenu.show(me.getComponent(), me.getX(), me.getY());
    }
}
