package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 16, 2008
 * Time: 10:34:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class GeneralCommentMessageNode extends GeneralCommentNode {
	static final long serialVersionUID = -1192703287399203269L;
	private String name ;


	GeneralCommentMessageNode(ReviewDataInfoAdapter reviewAdapter, GeneralComment aGeneralComment){
		super(reviewAdapter, aGeneralComment);
	}
}
