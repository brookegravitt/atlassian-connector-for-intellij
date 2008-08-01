package com.atlassian.theplugin.idea.action.tree.file;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.DataProvider;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 31, 2008
 * Time: 8:35:51 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class TreeAction extends AnAction {

	@Override
	public void update(final AnActionEvent e) {
		AtlassianTreeWithToolbar tree = identifyTreeWithAllPossibleMeans(e);
		if (tree != null) {
			updateTreeAction(e, tree);
		}
	}

	public void actionPerformed(final AnActionEvent e) {
		AtlassianTreeWithToolbar tree = identifyTreeWithAllPossibleMeans(e);
		if (tree != null) {
			executeTreeAction(tree);
		}
	}

	private AtlassianTreeWithToolbar identifyTreeWithAllPossibleMeans(final AnActionEvent e) {
		AtlassianTreeWithToolbar tree = findTreeM1(e);
		if (tree == null) {
			tree = findTreeM2(e);
		}
		if (tree == null) {
			tree = findTreeM3(e);
		}
		return tree;
	}

	private AtlassianTreeWithToolbar findTreeM1(final AnActionEvent e) {
		return (AtlassianTreeWithToolbar) e.getDataContext().getData(Constants.FILE_TREE);
	}

	private AtlassianTreeWithToolbar findTreeM2(final AnActionEvent e) {
		AtlassianTreeWithToolbar tree = null;
		Component component = DataKeys.CONTEXT_COMPONENT.getData(e.getDataContext());
		if (component != null) {
			Container parent = component.getParent();
			while (parent != null) {
				if (parent instanceof DataProvider) {
					DataProvider o = (DataProvider) parent;
					tree = (AtlassianTreeWithToolbar) o.getData(Constants.FILE_TREE);
					if (tree != null) {
						break;
					}
				}
				parent = parent.getParent();
			}
		}
		return tree;
	}

	private AtlassianTreeWithToolbar findTreeM3(final AnActionEvent e) {
		Component component = DataKeys.CONTEXT_COMPONENT.getData(e.getDataContext());
		Container parent = null;
		if (component != null) {
			parent = component.getParent();
			while (parent != null) {
				if (parent instanceof AtlassianTreeWithToolbar) {
					break;
				}
				parent = parent.getParent();
			}
		}
		if (parent == null) {
			return null;
		}
		return (AtlassianTreeWithToolbar) parent;
	}

	protected abstract void executeTreeAction(AtlassianTreeWithToolbar tree);

	protected abstract void updateTreeAction(final AnActionEvent e, final AtlassianTreeWithToolbar tree);

}
