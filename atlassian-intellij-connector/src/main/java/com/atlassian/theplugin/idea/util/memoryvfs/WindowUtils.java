package com.atlassian.theplugin.idea.util.memoryvfs;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 22, 2009
 * Time: 11:36:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class WindowUtils {
    /**
     * Set the component to have focus when the given window is first shown.
     *
     * @param window    the window
     * @param component the component to have focus
     */
    public static void setInitialFocusComponent(Window window, Component component) {
        window.addWindowListener(new InitialFocusSetter(component));
    }
}
class InitialFocusSetter extends WindowAdapter {
    private Component component;
    public InitialFocusSetter(Component component) {
        this.component = component;
    }

    public void windowActivated(WindowEvent e) {

        if (component instanceof JComboBox)
            component = ((JComboBox) component).getEditor().getEditorComponent();
        component.requestFocus();

        if (component instanceof TextComponent)
            ((TextComponent) component).selectAll();
        else if (component instanceof JTextComponent)
            ((JTextComponent) component).selectAll();
        e.getWindow().removeWindowListener(this);
    }
}
