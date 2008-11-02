package com.spartez.mazio;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issueoperation.AbstractPluggableIssueOperation;
import com.atlassian.jira.plugin.issueoperation.IssueOperationModuleDescriptor;
import com.atlassian.jira.plugin.issueoperation.PluggableIssueOperation;
import com.atlassian.jira.security.JiraAuthenticationContext;
import webwork.action.ServletActionContext;

import java.util.HashMap;
import java.util.Map;

public class MazioJira extends AbstractPluggableIssueOperation implements PluggableIssueOperation
{
	public boolean showOperation(Issue issue) {
		return true;
	}

	private JiraAuthenticationContext authenticationContext;

	public MazioJira(JiraAuthenticationContext _authenticationContext) {
			authenticationContext = _authenticationContext;
	}

	public void init(IssueOperationModuleDescriptor issueOperationModuleDescriptor) {
		super.init(issueOperationModuleDescriptor);
	}

	public String getHtml(Issue issue) {
		return getBullet() + descriptor.getHtml("view", getVelocityParams(issue));
	}

	private Map getVelocityParams(Issue issue) {
		Map velocityParams = new HashMap();
		velocityParams.put("user", authenticationContext.getUser().getName());
		String url = ServletActionContext.getRequest().getRequestURL().toString();
		url = url.substring(0, url.lastIndexOf("/browse"));
		String key = issue.getKey();
		velocityParams.put("serverUrl", url);
		velocityParams.put("issueKey", key);
		return velocityParams;
	}
}