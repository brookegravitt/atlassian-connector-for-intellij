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
	public static final String CRUCIBLE_MESSAGE_NOT_UNDER_VCS
			= "You can use this action only if VCS is enabled for this project";
	public static final String CRUCIBLE_TITLE_NOT_UNDER_VCS = "Action not available";

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
