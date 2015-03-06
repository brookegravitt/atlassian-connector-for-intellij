package com.atlassian.theplugin.idea.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: jgorycki
 * Date: May 27, 2009
 * Time: 4:04:28 PM
 */
public final class KeyPressGobbler {
	private static final int DELAY = 100;

	private KeyPressGobbler() { }

	public static void gobbleKeyPress(final JTextField field) {
		final Timer t = new Timer(DELAY, null);
		t.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (field.isShowing()) {
					field.setText("");
				} else {
					t.start();
				}
			}
		});
		t.setRepeats(false);
		t.start();

	}
}
