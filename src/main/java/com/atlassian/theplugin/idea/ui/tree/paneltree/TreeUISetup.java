package com.atlassian.theplugin.idea.ui.tree.paneltree;

import com.atlassian.theplugin.idea.BasicWideNodeTreeUI;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * User: jgorycki
 * Date: Dec 4, 2008
 * Time: 12:52:50 PM
 */
public class TreeUISetup {
	private final TreeCellRenderer renderer;

	//
	// voodoo magic below - makes the lastTree node as wide as the whole panel. Somehow. Like I said - it is magic.
	//

	public TreeUISetup(TreeCellRenderer renderer) {
		this.renderer = renderer;
	}

	public void initializeUI(final JTree tree, final JComponent treeParent) {
		ToolTipManager.sharedInstance().registerComponent(tree);
		registerUI(tree);
		treeParent.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (tree.isVisible()) {
					registerUI(tree);
				}
			}
		});
	}

	public void registerUI(JTree tree) {
		tree.setUI(new MyTreeUI());
	}

	private class MyTreeUI extends BasicWideNodeTreeUI {
		@Override
		protected TreeCellRenderer createDefaultCellRenderer() {
			return renderer;
		}
	}

	//
	// end of voodoo magic
	//
}
