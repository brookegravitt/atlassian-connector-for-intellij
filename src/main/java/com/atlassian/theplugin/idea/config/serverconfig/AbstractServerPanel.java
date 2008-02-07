package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.configuration.ServerBean;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-02-06
 * Time: 21:26:37
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractServerPanel extends JComponent {
    public abstract JComponent getRootComponent();

    public abstract boolean isModified();

    public abstract ServerBean getData();

    public abstract void setData(ServerBean server);
}
