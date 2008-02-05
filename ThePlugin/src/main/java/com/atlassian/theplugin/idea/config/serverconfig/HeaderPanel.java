package com.atlassian.theplugin.idea.config.serverconfig;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-02-05
 * Time: 11:42:01
 * To change this template use File | Settings | File Templates.
 */
public class HeaderPanel extends JPanel {
	private JCheckBox enablePlugin;

	public HeaderPanel() {
		initLayout();
    }

	private void initLayout() {
        BorderLayout gb = new BorderLayout();
		setLayout(gb);
		enablePlugin = new JCheckBox("Enable plugin");
		add(enablePlugin, BorderLayout.WEST);
	}
}