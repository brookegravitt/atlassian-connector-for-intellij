package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * User: pmaruszak
 */
public class JIRAServerTreeNode extends DefaultMutableTreeNode {	
	private JiraServerCfg jiraServer;

	JIRAServerTreeNode(JiraServerCfg jiraServer){
		this.jiraServer = jiraServer;
	}


	public String getNodeName(){
		return jiraServer.getName();
	}

}
