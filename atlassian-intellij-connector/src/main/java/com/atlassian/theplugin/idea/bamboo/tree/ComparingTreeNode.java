package com.atlassian.theplugin.idea.bamboo.tree;

/**
 * User: kalamon
 * Date: 18.03.13
 * Time: 15:00
 */
public interface ComparingTreeNode {
    boolean isMe(Object child);
}
