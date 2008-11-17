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


import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 10, 2008
 * Time: 4:26:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommentNode  extends DefaultMutableTreeNode {
	private GeneralComment generalComment;
    // TODO why its 10???                                    
    private static final int BEGIN_INDEX = 10;

    public CommentNode(GeneralComment aGeneralComment) {
		this.generalComment = aGeneralComment;
	}

   // public abstract ServerType getServerType();

	public GeneralComment getGeneralComment() {
		return generalComment;
	}

	public void setGeneralComment(GeneralComment generalComment) {
		this.generalComment = generalComment;
	}

	public String toString() {
		return generalComment.getMessage().substring(BEGIN_INDEX) + "(" + generalComment.getAuthor() + ")";
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CommentNode that = (CommentNode) o;

		if (!generalComment.equals(that.generalComment)) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		return generalComment.hashCode();
	}
}

