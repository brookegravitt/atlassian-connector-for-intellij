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

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 10, 2008
 * Time: 3:08:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReviewItemDataNode extends DefaultMutableTreeNode {
	static final long serialVersionUID = -1192703287399203290L;

	private CrucibleFileInfo file;

	public ReviewItemDataNode(CrucibleFileInfo aReviewItemData) {
		this.file = aReviewItemData;
	}

	public CrucibleFileInfo getFile() {
		return file;
	}

	public void setFile(CrucibleFileInfo file) {
		this.file = file;
	}

	public String toString() {
		return file.toString();
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ReviewItemDataNode that = (ReviewItemDataNode) o;

		if (file.equals(that.file)) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		return file.hashCode();
	}
}

