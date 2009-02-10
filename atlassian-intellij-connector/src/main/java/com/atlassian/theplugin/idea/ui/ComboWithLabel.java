package com.atlassian.theplugin.idea.ui;

import javax.swing.*;
import java.awt.*;

/**
 * User: jgorycki
 * Date: Feb 10, 2009
 * Time: 12:04:30 PM
 */
public class ComboWithLabel extends JPanel {

	private final JComboBox combo;

	public ComboWithLabel(JComboBox combo, String label) {
		this.combo = combo;
		FlowLayout layout = new FlowLayout();
		layout.setVgap(0);
		setLayout(layout);

		add(new JLabel(label));
		add(combo);
	}

	public JComboBox getCombo() {
		return combo;
	}
}
