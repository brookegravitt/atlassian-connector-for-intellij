package com.atlassian.theplugin.idea.ui;

import com.intellij.util.ui.UIUtil;

import javax.swing.*;

/**
 * User: jgorycki
 * Date: Mar 5, 2009
 * Time: 11:34:17 AM
 */
public class WhiteLabel extends JLabel {
	public WhiteLabel() {
		setForeground(UIUtil.getTableForeground());
	}
}

