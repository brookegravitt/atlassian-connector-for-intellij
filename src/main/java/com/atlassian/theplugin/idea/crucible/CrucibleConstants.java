package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.idea.ui.UserTableContext;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 23, 2008
 * Time: 9:15:51 PM
 * To change this template use File | Settings | File Templates.
 */
public final class CrucibleConstants {


	public static final String CRUCIBLE_AUTH_COLOR = "green";

	public static final String CRUCIBLE_MOD_COLOR = "#FEA02C";

    private CrucibleConstants() {
        // this is a utility class        
    }

    public enum CrucibleTableState {
		REVIEW_ADAPTER,
		REVIEW_ITEM,
		VERSIONED_COMMENTS,
		SELECTED_VERSIONED_COMMENT;

		public Object getValue(UserTableContext context) {
			return context.getProperty(name());
		}

		public void setValue(UserTableContext context, Object value) {
			context.setProperty(name(), value);
		}
	}

}
