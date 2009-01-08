package com.atlassian.theplugin.idea.ui.tree.paneltree;

import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Icons;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public abstract class AbstractTreeNode extends DefaultMutableTreeNode {
	protected static final int GAP = 6;
	protected static final int ICON_WIDTH = 16;
    protected static final int ICON_HEIGHT = 16;
    protected static final int RIGHT_PADDING = 10;
	protected String name;
	protected Icon iconOpen;
	protected Icon iconClosed;
	protected Icon disabledIconOpen;
	protected Icon disabledIconClosed;

	public AbstractTreeNode(String name, Icon icon, Icon disabledIcon) {
		this.name = name;
		if (icon != null) {
			this.iconOpen = icon;
			this.iconClosed = icon;
		} else {
			this.iconOpen = Icons.DIRECTORY_OPEN_ICON;
			this.iconClosed = Icons.DIRECTORY_CLOSED_ICON;
		}
		if (disabledIcon != null) {
			this.disabledIconOpen = disabledIcon;
			this.disabledIconClosed = disabledIcon;
		} else {
			this.disabledIconOpen = this.iconOpen;
			this.disabledIconClosed = this.iconClosed;
		}
	}

	@Override
	public abstract String toString();
	public abstract JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus);
	public abstract void onSelect();

	@SuppressWarnings("UnusedDeclaration")
	public JComponent getDefaultRenderer(JComponent c, boolean selected, boolean expanded,
			boolean hasFocus) {
		JPanel panel = new JPanel(new FormLayout("left:pref, left:pref, pref:grow", "pref:grow"));
		JPanel mainPanel = new JPanel(new FormLayout("pref, pref:grow", "pref"));

		CellConstraints cc = new CellConstraints();
		Color bgColor = selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground();
		Color fgColor = selected ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground();

		fgColor = c.isEnabled() ? fgColor : UIUtil.getInactiveTextColor();

		panel.setBackground(bgColor);
		SimpleColoredComponent groupComponet = new SimpleColoredComponent();
		JLabel label = new JLabel();
		label.setBackground(UIUtil.getTreeTextBackground());

		if (c.isEnabled()) {
			label.setIcon(expanded ? iconOpen : iconClosed);
			mainPanel.add(label, cc.xy(1, 1));
		} else {
			label.setIcon(expanded ? disabledIconOpen : disabledIconClosed);
			mainPanel.add(label, cc.xy(1, 1));
		}

		groupComponet.append(name, new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, fgColor));
		panel.add(groupComponet, cc.xy(1, 1));

		int childCount = getChildCount();
		groupComponet.append(" (" + childCount + ")",
				new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, fgColor));
		panel.add(groupComponet, cc.xy(2, 1));


		mainPanel.setBackground(UIUtil.getTreeTextBackground());
		mainPanel.add(panel, cc.xy(2, 1));
		return mainPanel;
	}
}
