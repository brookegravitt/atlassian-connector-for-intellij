package com.atlassian.theplugin.idea.ui.tree.paneltree;

import com.atlassian.theplugin.idea.BasicWideNodeTreeUI;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreeCellRenderer;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * @author jgorycki
 * @author wseliga
 */
public class TreeUISetup {
	private final TreeCellRenderer renderer;
//	private final MyTreeUI ui = new MyTreeUI();
	private boolean isTreeRebuilding;

	//
	// voodoo magic below - makes the lastTree node as wide as the whole panel. Somehow. Like I said - it is magic.
	//
	// from wseliga: it's not magic. It's all about preferring width as great as possible
	// see com.atlassian.theplugin.idea.BasicWideNodeTreeUI.NodeDimensionsHandler.getNodeDimensions

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
//					tree.setUI(null);
					tree.setUI(new MyTreeUI());
				}
			}
		});
		tree.addTreeExpansionListener(new TreeExpansionListener() {
			public void treeExpanded(final TreeExpansionEvent event) {
				if (!isTreeRebuilding) {
					forceTreePrefSizeRecalculation(tree);
				}
			}

			public void treeCollapsed(final TreeExpansionEvent event) {
				if (!isTreeRebuilding) {
					forceTreePrefSizeRecalculation(tree);
				}
			}
		});
	}

	public void forceTreePrefSizeRecalculation(final JTree tree) {
		// we have to call it asynchronously, as otherwise it would be ignored
		// as there would be already in EDT some stuff which recalculates preferred with
		// so the changes to prefered size triggered by the magic below would be effectively overwritten
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
//				tree.setUI(null);
				tree.setUI(new MyTreeUI());
			}
		});
	}

	public void registerUI(JTree tree) {
		tree.setUI(new MyTreeUI());
	}

	public void setTreeRebuilding(final boolean treeRebuilding) {
		isTreeRebuilding = treeRebuilding;
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
