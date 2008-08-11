package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.idea.crucible.tree.ModelProvider;
import com.atlassian.theplugin.idea.ui.tree.FilteredModelProvider;
import com.intellij.util.Icons;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Aug 8, 2008
 * Time: 12:15:25 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class CrucibleFilteredModelProvider extends FilteredModelProvider<CrucibleFilteredModelProvider.FILTER> {
	protected CrucibleFilteredModelProvider(ModelProvider provider, final FILTER initialFiltering) {
		super(provider, initialFiltering);
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
		;
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
