/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
import com.intellij.util.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * @author Lukasz Guminski
 */
public class AtlassianTreeWithToolbar extends ComponentWithToolbar {
	private AtlassianTree tree;
	private ModelProvider modelProvider = ModelProvider.EMPTY_MODEL_PROVIDER;
	private State state = State.DIRED;

	public AtlassianTreeWithToolbar(final String toolbarName, @Nullable TreeUISetup treeUISetup,
									AtlassianTree.ViewStateListener viewStateListener) {
		super(toolbarName, treeUISetup == null);
		this.treeUISetup = treeUISetup;
		this.viewStateListener = viewStateListener;
		jScrollPane.setViewportView(getTreeComponent());
	}

	private TreeUISetup treeUISetup;
	private final AtlassianTree.ViewStateListener viewStateListener;

	@Override
	public AtlassianTree getTreeComponent() {
		if (tree == null) {
			if (treeUISetup == null) {
				tree = new AtlassianTree();
			} else {
				tree = new AtlassianTree() {
					private static final int SCROLLING_STEP = 10;
					@Override
					public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation,
							final int direction) {
						// looks like something much better than standard JTree behaviour for very high rows
						return SCROLLING_STEP;
					}
				};
			}
			tree.setShowsRootHandles(state == State.DIRED);
			tree.setRowHeight(0);
			if (treeUISetup != null) {
				treeUISetup.initializeUI(tree, jScrollPane);
			} else {
				tree.setCellRenderer(AtlassianTree.DISPATCHING_RENDERER);
			}
		}
		return tree;
	}


	public void clear() {
		setModel(new AtlassianTreeModel(null));
	}


	public void setRootVisible(final boolean isVisible) {
		tree.setRootVisible(isVisible);
	}

	public void setModel(final AtlassianTreeModel model) {
		tree.setModel(model);
		setViewState(getViewState());
	}

	public void expandAll() {
		tree.expandAll();
	}

	public void setModelProvider(final ModelProvider modelProvider) {
		this.modelProvider = modelProvider;
		triggerModelUpdated();
	}

	public ModelProvider getModelProvider() {
		return modelProvider;
	}

	public State getState() {
		return state;
	}

	public void setState(final State state) {
		this.state = state;

		tree.setShowsRootHandles(state == State.DIRED);

		triggerModelUpdated();
	}

	public void triggerModelUpdated() {
		setModel(modelProvider.getModel(state));
	}

	public void changeState() {
		setState(getState().getNextState());
	}

	public ViewState getViewState() {
		return tree.isExpanded(0) ? ViewState.EXPANDED : ViewState.COLLAPSED;
	}

	public void setViewState(final ViewState viewState) {
		switch (viewState) {
			case COLLAPSED:
				collapseAll();
				break;
			case EXPANDED:
				expandAll();
				break;
			default:
				throw new IllegalStateException("Unknown state of tree: " + viewState.toString());
		}
		if (viewStateListener != null) {
			viewStateListener.setViewState(viewState);
		}
	}

	public void collapseAll() {
		tree.collapseAll();
	}

	public AtlassianTreeNode getSelectedTreeNode() {
		if (tree != null) {
			TreePath path = tree.getSelectionPath();
			if (path != null) {
				return (AtlassianTreeNode) path.getLastPathComponent();
			}
		}
		return null;
	}

	@NotNull
	public AtlassianTreeModel getModel() {
		return (AtlassianTreeModel) tree.getModel();
	}

	public void focusOnNode(final AtlassianTreeNode node) {
		tree.focusOnNode(node);
	}

	public boolean isEmpty() {
		return !tree.isRootVisible() && (tree.getModel().getChildCount(tree.getModel().getRoot()) == 0);
	}

	public enum State {
		FLAT(Icons.DIRECTORY_CLOSED_ICON, "Show Flat") {
			@Override
			public State getNextState() {
				return DIRED;
			}},
		DIRED(Icons.DIRECTORY_OPEN_ICON, "Show Hierarchy") {
			@Override
			public State getNextState() {
				return FLAT;
			}};

		private Icon icon;
		private String string;

		State(final Icon icon, final String string) {
			this.icon = icon;
			this.string = string;
		}

		public abstract State getNextState();

		public Icon getIcon() {
			return icon;
		}

		@Override
		public String toString() {
			return string;
		}
	}

	public enum ViewState {
		COLLAPSED,
		EXPANDED
	}
}


