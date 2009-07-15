package com.atlassian.theplugin.idea.ui.tree.paneltree;

import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * User: jgorycki
 * Date: Dec 4, 2008
 * Time: 11:52:04 AM
 */

public class SelectableLabel extends JLabel {
	private final int height;

	private final boolean enabled;

	public SelectableLabel(final boolean selected, final boolean enabled, final Font font, final String txt,
			final int iconHeight) {
		this(selected, enabled, font, txt, null, SwingConstants.LEADING, iconHeight, false, true);
	}

	public SelectableLabel(boolean selected, boolean enabled, String text, int height) {
		this(selected, enabled, text, null, SwingConstants.LEADING, height);
	}

	public SelectableLabel(boolean selected, boolean enabled, String text,
			int height, boolean bold, boolean doSizeAdjustment) {
		this(selected, enabled, null, text, null, SwingConstants.LEADING, height, bold, doSizeAdjustment);
	}

	public SelectableLabel(boolean selected, boolean enabled, String text, Icon icon, int alignment, int height) {
		this(selected, enabled, null, text, icon, alignment, height, false, true);
	}

	public SelectableLabel(boolean selected, boolean enabled, Font font, String text,
			Icon icon, int alignment, int height, boolean bold, boolean doSizeAdjustment) {
		super(text, SwingConstants.LEADING);
		this.enabled = enabled;
		this.height = height;

		if (font != null) {
			setFont(font);
		}

		if (bold) {
			setFont(getFont().deriveFont(Font.BOLD));
		}

		setIcon(icon);

		setEnabled(enabled);
		setSelected(selected);

		if (doSizeAdjustment) {
			adjustPreferredSize();
		}

		setHorizontalTextPosition(alignment);
		setOpaque(true);
	}

	public void setSelected(boolean selected) {
		Color fgColor = selected ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground();
		fgColor = enabled ? fgColor : UIUtil.getInactiveTextColor();
		setBackground(selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground());
		setForeground(fgColor);
	}

	public void setIcon(Icon icon) {
		if (icon != null) {
			super.setIcon(icon);
			setDisabledIcon(icon);
		}
	}

	public void adjustPreferredSize() {
		setPreferredSize(new Dimension((int) getPreferredSize().getWidth(), height));
	}
}

