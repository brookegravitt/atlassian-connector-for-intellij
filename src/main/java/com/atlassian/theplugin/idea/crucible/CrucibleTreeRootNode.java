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

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 12, 2008
 * Time: 12:22:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleTreeRootNode extends DefaultMutableTreeNode {
	static final long serialVersionUID = 0L;

	private ReviewData reviewData;

	CrucibleTreeRootNode(ReviewData infoAdapater) {
		reviewData = infoAdapater;
	}

	CrucibleTreeRootNode() {
		
	}

	public String toString() {
		if (reviewData != null) {
			return reviewData.getProjectKey() + ", " + reviewData.getPermId()
                    + ", " + reviewData.getName();
		} else {
			return "No Review is selected";
		}
	}



	public ReviewData getCrucibleChangeSet() {
		return reviewData;
	}

	public void setCrucibleChangeSet(ReviewData reviewData) {
		this.reviewData = reviewData;
	}
}
