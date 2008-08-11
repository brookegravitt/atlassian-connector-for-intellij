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

package com.atlassian.theplugin.idea.ui.tree;

/**
 * Created by IntelliJ IDEA.
* User: pmaruszak
* Date: Aug 6, 2008
* Time: 11:42:11 AM
* To change this template use File | Settings | File Templates.
*/
public abstract class Filter {
	public abstract boolean isValid(AtlassianTreeNode node);
	

	public static final Filter ALL = new Filter() {
		public boolean isValid(final AtlassianTreeNode node) {
			return true;
		}
	};
}
