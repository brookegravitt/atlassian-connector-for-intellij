package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 30, 2008
 * Time: 10:35:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class AtlassianTreeWithToolbar extends ComponentWithToolbar {
	private AtlassianTree tree;
	private ModelProvider modelProvider = ModelProvider.EMPTY_MODEL_PROVIDER;
	private STATE state = STATE.DIRED;
	private VIEW_STATE viewState = VIEW_STATE.EXPANDED;

	public AtlassianTreeWithToolbar(String toolbar, final ModelProvider modelProvider) {
		this(toolbar);
		setModelProvider(modelProvider);
	}

	public AtlassianTreeWithToolbar(final String toolbarName) {
		super(toolbarName);
	}

	protected Component getTreeComponent() {
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

	@NotNull
	public void setModelProvider(final ModelProvider modelProvider) {
		this.modelProvider = modelProvider;
		setState(state);
	}

	public STATE getState() {
		return state;
	}

	public void setState(final STATE state) {
		setModel(modelProvider.getModel(state));
		this.state = state;
	}

	public void changeState() {
		setState(getState().getNextState());
	}

	public VIEW_STATE getViewState() {
		return viewState;
	}

	public void setViewState(final VIEW_STATE viewState) {
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
			return (AtlassianTreeNode) tree.getSelectionPath().getLastPathComponent();
		} else {
			return null;
		}
	}

	public enum STATE {
		FLAT(Icons.DIRECTORY_CLOSED_ICON, "Show flat") {
			@Override
			public STATE getNextState() {
				return DIRED;
			}},
		DIRED(Icons.DIRECTORY_OPEN_ICON, "Show dired") {
			public STATE getNextState() {
				return FLAT;
			}};

		private Icon icon;
		private String string;

		STATE(final Icon icon, final String string) {
			this.icon = icon;
			this.string = string;
		}

		public abstract STATE getNextState();

		public Icon getIcon() {
			return icon;
		}

		@Override
		public String toString() {
			return string;
		}
	}

	public enum VIEW_STATE {
		COLLAPSED(IconLoader.getIcon("/actions/collapseall.png"), "Collapse all") {

			public VIEW_STATE getNextState() {
				return EXPANDED;
			}
		},
		EXPANDED(IconLoader.getIcon("/actions/expandall.png"), "Expand all") {

			public VIEW_STATE getNextState() {
				return COLLAPSED;
			}
		};

		private Icon icon;
		private String string;

		VIEW_STATE(final Icon icon, final String string) {
			this.icon = icon;
			this.string = string;
		}

		public abstract VIEW_STATE getNextState();

		public Icon getIcon() {
			return icon;
		}

		@Override
		public String toString() {
			return string;
		}
	}
}


