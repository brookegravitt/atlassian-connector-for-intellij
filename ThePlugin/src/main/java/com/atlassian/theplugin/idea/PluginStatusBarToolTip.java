package com.atlassian.theplugin.idea;

import prefuse.util.ui.JCustomTooltip;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-01-16
 * Time: 11:40:02
 * To change this template use File | Settings | File Templates.
 */
public class PluginStatusBarToolTip extends JCustomTooltip {
    
    public PluginStatusBarToolTip(JComponent jComponent, JComponent jComponent1, boolean b) {
        super(jComponent, jComponent1, b);
    }

    /*
    public void setTipText(String tipText) {
        super.setTipText("test tooltip");
    }

    public void paint(Graphics g) {
        g.drawString("xxx test tooltip xxx", 2, 2);
    }

    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        return new Dimension((int)dim.getWidth()+20,(int)dim.getHeight()+20);
    }
    */
}
