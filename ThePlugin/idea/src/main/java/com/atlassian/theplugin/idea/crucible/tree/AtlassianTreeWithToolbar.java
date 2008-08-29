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
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;

/**
 * @author Lukasz Guminski
 */
public class AtlassianTreeWithToolbar extends ComponentWithToolbar {
	private AtlassianTree tree;
	private ModelProvider modelProvider = ModelProvider.EMPTY_MODEL_PROVIDER;
	private State state = State.DIRED;


	private ViewState viewState = ViewState.EXPANDED;

	public AtlassianTreeWithToolbar(String toolbar, final ModelProvider modelProvider) {
		this(toolbar);
		setModelProvider(modelProvider);
	}

	public AtlassianTreeWithToolbar(final String toolbarName) {
		super(toolbarName);
	}

	@Override
	public AtlassianTree getTreeComponent() {
		if (tree == null) {
			tree = new AtlassianTree();
		}
		return tree;
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
		this.viewState = viewState;
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
		COLLAPSED(IconLoader.getIcon("/actions/collapseall.png"), "Collapse All") {

			@Override
			public ViewState getNextState() {
				return EXPANDED;
			}
		},
		EXPANDED(IconLoader.getIcon("/actions/expandall.png"), "Expand All") {

			@Override
			public ViewState getNextState() {
				return COLLAPSED;
			}
		};

		private Icon icon;
		private String string;

		ViewState(final Icon icon, final String string) {
			this.icon = icon;
			this.string = string;
		}

		public abstract ViewState getNextState();

		public Icon getIcon() {
			return icon;
		}

		@Override
		public String toString() {
			return string;
		}
	}
}


