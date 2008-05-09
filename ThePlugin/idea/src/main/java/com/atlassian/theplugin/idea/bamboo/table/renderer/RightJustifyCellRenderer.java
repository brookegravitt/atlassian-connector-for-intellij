package com.atlassian.theplugin.idea.bamboo.table.renderer;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.*;

public class RightJustifyCellRenderer extends DefaultTableCellRenderer {
	public RightJustifyCellRenderer() {
		super();
		setHorizontalAlignment(SwingConstants.RIGHT);
	}
}
