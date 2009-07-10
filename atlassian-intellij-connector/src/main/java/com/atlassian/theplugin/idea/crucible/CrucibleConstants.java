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

import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.FolderNode;

/**
 * @author lguminski
 */
public final class CrucibleConstants {


	public static final String CRUCIBLE_AUTH_COLOR = "green";

	public static final String CRUCIBLE_MOD_COLOR = "#FEA02C";
	public static final String CRUCIBLE_MESSAGE_NOT_UNDER_VCS
			= "You can use this action only if VCS is enabled for this project";
	public static final String CRUCIBLE_TITLE_NOT_UNDER_VCS = "Action not available";
	//	private static final String TOOLBAR_ID = "ThePlugin.Crucible.Comment.ToolBar";
	public static final String MENU_PLACE = "menu comments";
	public static final AtlassianTreeNode ROOT = new FolderNode("/", AtlassianClickAction.EMPTY_ACTION);

	private CrucibleConstants() {
        // this is a utility class        
    }
}
