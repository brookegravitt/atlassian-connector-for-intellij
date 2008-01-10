package com.atlassian.theplugin.idea;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 3:21:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class ThePluginConfigurationForm {
    private JPanel rootComponent;
    private JTextField phrase;
    private JLabel label;

    public void setData(ThePluginApplicationComponent data) {
        phrase.setText(data.getPhrase());
    }

    public void getData(ThePluginApplicationComponent data) {
        data.setPhrase(phrase.getText());
    }

    public boolean isModified(ThePluginApplicationComponent data) {
        if (phrase.getText() != null ? !phrase.getText().equals(data.getPhrase()) : data.getPhrase() != null)
            return true;
        return false;
    }

    public JComponent getRootComponent() {
        return rootComponent;
    }

}
