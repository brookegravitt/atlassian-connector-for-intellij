package com.atlassian.theplugin.idea.ui.tree.paneltree;

import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.google.common.base.Objects;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public abstract class AbstractTreeNode extends DefaultMutableTreeNode {
	protected static final int GAP = 6;
	public static final int ICON_WIDTH = 16;
	public static final int ICON_HEIGHT = 16;
	public static final int RIGHT_PADDING = 10;
	protected String name;
	protected Icon iconOpen;
	protected Icon iconClosed;
	protected Icon disabledIconOpen;
	protected Icon disabledIconClosed;

	private RendererPanel renderer;

	public AbstractTreeNode(String name, Icon icon, Icon disabledIcon) {
		this.name = name;
		if (icon != null) {
			this.iconOpen = icon;
			this.iconClosed = icon;
		} else {
			this.iconOpen = IdeaVersionFacade.getInstance().getIcon(IdeaVersionFacade.IconType.DIRECTORY_OPEN_ICON);
			this.iconClosed = IdeaVersionFacade.getInstance().getIcon(IdeaVersionFacade.IconType.DIRECTORY_CLOSED_ICON);
		}
		if (disabledIcon != null) {
			this.disabledIconOpen = disabledIcon;
			this.disabledIconClosed = disabledIcon;
		} else {
			this.disabledIconOpen = this.iconOpen;
			this.disabledIconClosed = this.iconClosed;
		}

		renderer = new RendererPanel();
	}

	@Override
	public abstract String toString();

	public abstract JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus);

//	public void onSelect() {
//	}

	private static final class RendererPanel extends JPanel {

		private SimpleColoredComponent groupComponent;
		private JLabel iconLabel;
		private JPanel panel;
		private String oldName = "";
		private int oldChildCount;
		private boolean oldSelected;
		private boolean oldEnabled;

		private RendererPanel() {
			super(new FormLayout("pref, pref:grow", "pref"));
			panel = new JPanel(new FormLayout("left:pref, left:pref, pref:grow", "pref:grow"));

			CellConstraints cc = new CellConstraints();
			groupComponent = new SimpleColoredComponent();
			iconLabel = new JLabel();
			iconLabel.setBackground(UIUtil.getTreeTextBackground());

			add(iconLabel, cc.xy(1, 1));

			panel.add(groupComponent, cc.xy(2, 1));

			setBackground(UIUtil.getTreeTextBackground());

			add(panel, cc.xy(2, 1));
		}

		public void setIcon(Icon icon) {
			if (icon != iconLabel.getIcon()) {
				iconLabel.setIcon(icon);
			}
		}

		public void setParameters(String name, int childCount, boolean selected, boolean enabled) {
			if (Objects.equal(name, oldName) && childCount == oldChildCount && selected == oldSelected && enabled == oldEnabled) {
				return;
			}
			groupComponent.clear();
			Color bgColor = selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground();
			Color fgColor = selected ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground();

			fgColor = enabled ? fgColor : UIUtil.getInactiveTextColor();
			groupComponent.append(name != null ? name : "", new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, fgColor));

			panel.setBackground(bgColor);

			groupComponent.append(" (" + childCount + ")",
					new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, fgColor));

			oldName = name != null ? name : "";
			oldChildCount = childCount;
			oldSelected = selected;
			oldEnabled = enabled;
		}
	}

	@SuppressWarnings("UnusedDeclaration")
	public JComponent getDefaultRenderer(JComponent c, boolean selected, boolean expanded,
			boolean hasFocus) {
		if (c.isEnabled()) {
			renderer.setIcon(expanded ? iconOpen : iconClosed);
		} else {
			renderer.setIcon(expanded ? disabledIconOpen : disabledIconClosed);
		}

		renderer.setParameters(toString(), getChildCount(), selected, c.isEnabled());
		return renderer;

	}
}
