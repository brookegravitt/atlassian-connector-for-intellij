package com.atlassian.theplugin.idea.util.memoryvfs;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
