package com.atlassian.theplugin.idea.ui;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * User: jgorycki
 * Date: Mar 5, 2009
 * Time: 10:49:26 AM
 */

public class ShowHideButton extends JLabel {
	private JComponent body;
	private JComponent container;
	private final boolean useText;

	private Icon right = IconLoader.findIcon("/icons/navigate_right_10.gif");
	private Icon down = IconLoader.findIcon("/icons/navigate_down_10.gif");
	private boolean shown = true;

	public void setState(boolean visible) {
		shown = visible;
		if (useText) {
			setText(shown ? "V" : ">");
		} else {
			setIcon(shown ? down : right);
		}
		setComponentVisible(shown);
	}

	public void click() {
		shown = !shown;
		setState(shown);
	}

	public ShowHideButton(JComponent body, JComponent container) {
		this(body, container,  false);
	}
	
	public ShowHideButton(JComponent body, JComponent container, boolean useText) {
		this.body = body;
		this.container = container;
		this.useText = useText;

		setHorizontalAlignment(0);
		if (useText) {
			setText("V");
		} else {
			setIcon(down);
		}
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				click();
			}
		});
	}

	protected void setComponentVisible(boolean visible) {
		body.setVisible(visible);
		container.validate();
		if (container.getParent() != null) {
			container.getParent().validate();
		}
	}

//	protected String getTooltip() {
//		return "Collapse/Expand";
//	}
}
