package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 16, 2008
 * Time: 11:03:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class VersionedCommentNode extends DefaultMutableTreeNode {
    private VersionedComment versionedComment;

    VersionedCommentNode(VersionedComment versionedComment) {
        this.versionedComment = versionedComment;
    }

    public VersionedComment getVersionedComment() {
        return versionedComment;
    }

    public void setVersionedComment(VersionedComment versionedComment) {
        this.versionedComment = versionedComment;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VersionedCommentNode that = (VersionedCommentNode) o;

        if (!versionedComment.equals(that.versionedComment)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return versionedComment.hashCode();
    }
}
