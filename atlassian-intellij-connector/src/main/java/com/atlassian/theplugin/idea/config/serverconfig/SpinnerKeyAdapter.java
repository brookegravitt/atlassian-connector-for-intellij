package com.atlassian.theplugin.idea.config.serverconfig;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: May 14, 2009
 * Time: 5:02:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpinnerKeyAdapter extends KeyAdapter {
    private final JSpinner spinner;
    private SpinnerModel model;
    private int maxValue;
    private int defaultValue;

    SpinnerKeyAdapter(final JSpinner spinner, final SpinnerModel model, final int maxValue, final int defaultValue) {

        this.spinner = spinner;
        this.model = model;
        this.maxValue = maxValue;
        this.defaultValue = defaultValue;

        JFormattedTextField jft = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        jft.addKeyListener(this);
    }

    public void keyTyped(KeyEvent keyEvent) {
        refreshModel();
    }

    private void refreshModel() {
        int value = defaultValue;
        try {
            value = Integer.valueOf(
                    ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().getText());
        } catch (NumberFormatException e) {
            spinner.setToolTipText("Type number less than 1000");
        }
        model.setValue(value % maxValue);
    }

    public void keyPressed(KeyEvent keyEvent) {
        refreshModel();
    }

    public void keyReleased(KeyEvent keyEvent) {
        refreshModel();
    }


}
