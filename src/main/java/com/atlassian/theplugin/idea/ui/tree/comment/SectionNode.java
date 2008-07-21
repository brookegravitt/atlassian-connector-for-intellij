package com.atlassian.theplugin.idea.ui.tree.comment;

import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.tree.TreeCellRenderer;
import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 21, 2008
 * Time: 3:57:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class SectionNode extends AtlassianTreeNode {
	private String sectionName;
	private static final TreeCellRenderer MY_RENDERER = new MyRenderer();

	public SectionNode(String sectionName, AtlassianClickAction action) {
		super(action);
		this.sectionName = sectionName;
	}

	public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}

	public String getSectionName() {
		return sectionName;
	}

	private static class MyRenderer implements TreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			SectionNode node = (SectionNode) value;
			JPanel panel = new JPanel(new FormLayout("4dlu, left:pref:grow, 4dlu", "4dlu, pref:grow, 4dlu"));
			SimpleColoredComponent component = new SimpleColoredComponent();
			component.append(node.getSectionName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
			panel.add(component, new CellConstraints(2, 2));

			panel.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder")
					: BorderFactory.createEmptyBorder(1, 1, 1, 1));

			return panel;
		}
	}
}
