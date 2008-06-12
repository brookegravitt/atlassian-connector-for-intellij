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
		return generalComment.getMessage().substring(10) + "(" + generalComment.getUser() + ")";
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

