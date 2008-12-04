package com.atlassian.theplugin.idea.ui.tree.paneltree;

import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * User: jgorycki
 * Date: Dec 4, 2008
 * Time: 11:52:04 AM
 */

public final class SelectableLabel extends JLabel {
	public SelectableLabel(boolean selected, boolean enabled, String text, int height) {
		this(selected, enabled, text, null, SwingConstants.LEADING, height);
	}

	public SelectableLabel(boolean selected, boolean enabled, String text, Icon icon, int alignment, int height) {
		super(text, SwingConstants.LEADING);

		if (icon != null) {
			if (enabled) {
				setIcon(icon);
			} else {
				setDisabledIcon(icon);
			}
		}

		setEnabled(enabled);
		Color fgColor = selected ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground();
		fgColor = enabled ? fgColor : UIUtil.getInactiveTextColor();

		setHorizontalTextPosition(alignment);
		setPreferredSize(new Dimension((int) getPreferredSize().getWidth(), height));
		setOpaque(true);
		setBackground(selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground());
		setForeground(fgColor);
	}
}

