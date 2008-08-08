package com.atlassian.theplugin.idea.crucible.tree;

import com.intellij.util.Icons;
import com.atlassian.theplugin.idea.ui.tree.Filter;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Aug 6, 2008
 * Time: 11:25:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class FilterableTreeWithToolbar extends AtlassianTreeWithToolbar {

	public FilterableTreeWithToolbar(String toolbar, final ModelProvider modelProvider) {
		super(toolbar, modelProvider);
	}

	public FilterableTreeWithToolbar(final String toolbar) {
		super(toolbar);
	}

	public void changeFilter() {
		setFilter(getModelProvider().getNextState(getModelProvider().getFilter()));

	}

	public void setFilter(final Filter filter) {
		getModelProvider().setFilter(filter);
		setModel(getModelProvider().getModel(getState()));
	}

	public enum FILTER {
		FILES_WITH_COMMENTS_ONLY(Icons.ABSTRACT_CLASS_ICON, "Files with comments only") {
			@Override
			public FILTER getNextState() {
				return FILES_ALL;
			}},

		FILES_ALL(Icons.ANONYMOUS_CLASS_ICON, "All files from review") {
			@Override
			public FILTER getNextState() {
				return FILES_WITH_COMMENTS_ONLY;
			}},

		LAST_KNOWN(Icons.ANONYMOUS_CLASS_ICON, "Last known filter") {
			@Override
			public FILTER getNextState() {
				return LAST_KNOWN;
			}};

		private Icon icon;
		private String string;

		FILTER(final Icon icon, final String string) {
			this.icon = icon;
			this.string = string;
		}

		public abstract FILTER getNextState();

		public Icon getIcon() {
			return icon;
		}

		@Override
		public String toString() {
			return string;
		}

	}
}
