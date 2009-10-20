package com.atlassian.theplugin.idea.ui;

import javax.swing.*;
import java.awt.*;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 3:23:32 PM
 */
public class BoldLabel extends JLabel {
	public BoldLabel(String text) {
		super(text);
		setFont(getFont().deriveFont(Font.BOLD));
	}

	public BoldLabel() {
		this("");
	}
}
