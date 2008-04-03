/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 13/03/2004
 * Time: 23:19:19
 */
package com.atlassian.theplugin.jira.api;

import com.atlassian.theplugin.util.PluginUtil;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

public class JIRAXmlRpcClient {

	private String serverUrl;
	private String token;
	private boolean loggedIn;
	private String userName;
	private String password;

	public JIRAXmlRpcClient(String url) {
		this.serverUrl = url;
	}

	public JIRAXmlRpcClient(String url, String userName, String password) {
		this.serverUrl = url;
		this.userName = userName;
		this.password = password;
	}

	public XmlRpcClient getClient() throws JIRAException {
		try {
			return new XmlRpcClient(serverUrl + "/rpc/xmlrpc");
		} catch (MalformedURLException e) {
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public String getToken() {
		return token;
	}

	public boolean login() throws JIRAException {
		return login(userName, password);
	}

	public boolean login(String lUserName, String lPassword) throws JIRAException {
		try {
			XmlRpcClient client = getClient();
			Vector params = new Vector();
			params.add(lUserName);
			params.add(lPassword);
			token = (String) client.execute("jira1.login", params);

			loggedIn = token != null && token.length() > 0;
		} catch (Throwable e) { // ugly exceptions get thrown here - catch 'em all.
			throw new JIRAException("RPC not supported or remote error: " + e.getMessage(), e);
		}

		return loggedIn;
	}

	public List<JIRAIssue> getIssuesFromSavedFilter(JIRAQueryFragment query) throws JIRAException {
		if (!loggedIn) {
			login();
		}

		XmlRpcClient client = getClient();
		Vector params = new Vector();
		params.add(token);
		params.add(query.getQueryStringFragment());

		List retrieved = null;
		try {
			retrieved = (List) client.execute("jira1.getIssuesFromFilter", params);
			List result = new ArrayList<JIRAProject>(retrieved.size());
			for (Iterator iterator = retrieved.iterator(); iterator.hasNext();) {
				result.add(new JIRAIssueBean(serverUrl, (Map) iterator.next()));
			}
			return result;
		} catch (XmlRpcException e) {
			throw new JIRAException("RPC not supported or remote error: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new JIRAException("RPC not supported or remote error: " + e.getMessage(), e);
		}
	}


	public List<JIRAProject> getProjects() throws JIRAException {
		List projVector = getListFromRPCMethod("jira1.getProjects");
		List result = new ArrayList<JIRAProject>(projVector.size());
		for (Iterator iterator = projVector.iterator(); iterator.hasNext();) {
			result.add(new JIRAProjectBean((Map) iterator.next()));
		}
		return result;
	}

	private List getListFromRPCMethod(String rpcCommand) throws JIRAException {
		if (!loggedIn) {
			login();
		}

		XmlRpcClient client = getClient();
		Vector params = new Vector();
		params.add(token);

		try {
			return (List) client.execute(rpcCommand, params);
		} catch (Exception e) {
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAConstant> getStatuses() throws JIRAException {
		List retrieved = getListFromRPCMethod("jira1.getStatuses");
		List result = new ArrayList<JIRAConstant>(retrieved.size());
		for (Iterator iterator = retrieved.iterator(); iterator.hasNext();) {
			result.add(new JIRAStatusBean((Map) iterator.next()));
		}
		return result;
	}

	public List<JIRAConstant> getIssueTypes() throws JIRAException {
		List retrieved = getListFromRPCMethod("jira1.getIssueTypes");
		List result = new ArrayList<JIRAConstant>(retrieved.size());
		for (Iterator iterator = retrieved.iterator(); iterator.hasNext();) {
			result.add(new JIRAIssueTypeBean((Map) iterator.next()));
		}
		return result;
	}

	public List<JIRAQueryFragment> getComponents(String projectKey) throws JIRAException {
		if (!loggedIn) {
			login();
		}

		XmlRpcClient client = getClient();
		Vector params = new Vector();
		params.add(token);
		params.add(projectKey);

		PluginUtil.getLogger().info("Getting components for project: " + projectKey);

		try {
			List retrieved = (List) client.execute("jira1.getComponents", params);
			List result = new ArrayList<JIRAConstant>(retrieved.size());
			for (Iterator iterator = retrieved.iterator(); iterator.hasNext();) {
				result.add(new JIRAComponentBean((Map) iterator.next()));
			}
			return result;
		} catch (XmlRpcException e) {
			throw new JIRAException(e.getMessage(), e);
		} catch (IOException e) {
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAQueryFragment> getVersions(String projectKey) throws JIRAException {
		if (!loggedIn) {
			login();
		}

		XmlRpcClient client = getClient();
		Vector params = new Vector();
		params.add(token);
		params.add(projectKey);

		PluginUtil.getLogger().info("Getting project versions: " + token + " | " + projectKey);

		try {
			List retrieved = (List) client.execute("jira1.getVersions", params);
			List result = new ArrayList<JIRAConstant>(retrieved.size());
			for (Iterator iterator = retrieved.iterator(); iterator.hasNext();) {
				result.add(new JIRAVersionBean((Map) iterator.next()));
			}
			return result;
		} catch (XmlRpcException e) {
			throw new JIRAException(e.getMessage(), e);
		} catch (IOException e) {
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAConstant> getSavedFilters() throws JIRAException {
		List retrieved = getListFromRPCMethod("jira1.getSavedFilters");
		List result = new ArrayList<JIRAConstant>(retrieved.size());
		for (Iterator iterator = retrieved.iterator(); iterator.hasNext();) {
			result.add(new JIRASavedFilterBean((Map) iterator.next()));
		}
		return result;


	}

	public List getResolutions() throws JIRAException {
		List retrieved = getListFromRPCMethod("jira1.getResolutions");
		List result = new ArrayList<JIRAConstant>(retrieved.size());
		for (Iterator iterator = retrieved.iterator(); iterator.hasNext();) {
			result.add(new JIRAResolutionBean((Map) iterator.next()));
		}
		return result;
	}

	public List<JIRAConstant> getPriorities() throws JIRAException {
		List retrieved = getListFromRPCMethod("jira1.getPriorities");
		List result = new ArrayList<JIRAConstant>(retrieved.size());
		for (Iterator iterator = retrieved.iterator(); iterator.hasNext();) {
			result.add(new JIRAPriorityBean((Map) iterator.next()));
		}
		return result;
	}

	public List<JIRAConstant> getIssueTypesForProject(String projectKey) throws JIRAException {
		if (!loggedIn) {
			login();
		}

		XmlRpcClient client = getClient();
		Vector params = new Vector();
		params.add(token);
		params.add(projectKey);

		PluginUtil.getLogger().info("Getting issue types for project: " + token + " | " + projectKey);

		List issueTypes = null;
		try {
			issueTypes = (List) client.execute("jira1.getIssueTypesForProject", params);
			List<JIRAConstant> result = new ArrayList<JIRAConstant>(issueTypes.size());
			for (Iterator iterator = issueTypes.iterator(); iterator.hasNext();) {
				result.add(new JIRAIssueTypeBean((Map) iterator.next()));
			}
			return result;
		} catch (XmlRpcException e) {
			throw new JIRAException(e.getMessage(), e);
		} catch (IOException e) {
			throw new JIRAException(e.getMessage(), e);
		}
	}


	public void addIssueComment(String issueKey, String comment) throws JIRAException {
		if (!loggedIn) {
			login();
		}

		try {
			XmlRpcClient client = getClient();
			Vector params = new Vector();
			params.add(token);
			params.add(issueKey);
			params.add(comment);

			client.execute("jira1.addComment", params);
		} catch (XmlRpcException e) {
			throw new JIRAException(e.getMessage(), e);
		} catch (IOException e) {
			throw new JIRAException(e.getMessage(), e);
		}

	}

	public JIRAIssue createIssue(JIRAIssue issue) throws JIRAException {

		if (!loggedIn) {
			login();
		}

		try {
			XmlRpcClient client = getClient();
			Vector params = new Vector();
			params.add(token);

			Hashtable issueMap = new Hashtable();
			issueMap.put("project", issue.getProjectKey());
			issueMap.put("type", String.valueOf(issue.getTypeConstant().getId()));
			issueMap.put("summary", issue.getSummary());

			if (issue.getDescription() != null) {
				issueMap.put("description", issue.getDescription());
			}
			if (issue.getAssignee() != null) {
				issueMap.put("assignee", issue.getAssignee());
			}

/*
            if (affectsVersionId != null)
            {
                Vector affectsVersions = new Vector();
                Hashtable affectsVersion = new Hashtable();
                affectsVersion.put("id", affectsVersionId);
                affectsVersions.add(affectsVersion);
                issueMap.put("affectsVersions", affectsVersions);
            }

            if (fixForVersionId != null)
            {
                Vector fixForVersions = new Vector();
                Hashtable fixForVersion = new Hashtable();
                fixForVersion.put("id", fixForVersionId);
                fixForVersions.add(fixForVersion);
                issueMap.put("fixVersions", fixForVersions);
            }
*/
			params.add(issueMap);

			Map result = (Hashtable) client.execute("jira1.createIssue", params);

			if (result != null) {
				return new JIRAIssueBean(serverUrl, result);
			} else {
				return null;
			}
		} catch (XmlRpcException e) {
			throw new JIRAException(e.getMessage(), e);
		} catch (IOException e) {
			throw new JIRAException(e.getMessage(), e);
		}
	}

	// methods below here might work, but I haven't tested them - commented out for now!
	// when commenting in, please be sure to add tests

	/*

		public List getSavedFilters() throws JIRAException
		{
			return getListFromRPCMethod("jira1.getSavedFilters");
		}

		public List getResolutions() throws JIRAException
		{
			return getListFromRPCMethod("jira1.getResolutions");
		}

		public List getPriorities() throws JIRAException
		{
			return getListFromRPCMethod("jira1.getPriorities");
		}


		public List getUnreleasedVersions(String projectKey) throws JIRAException
		{
			List allVersions = getVersions(projectKey);
			List unReleasedVersions = new ArrayList();
			for (Object allVersion : allVersions)
			{
				Hashtable hashtable = (Hashtable) allVersion;
				if (!(Boolean.valueOf((String) hashtable.get("released"))))
				{
					unReleasedVersions.add(hashtable);
				}
			}
			return unReleasedVersions;
		}

		public Hashtable getIssueHashtable(String issueKey) throws JIRAException
		{
			if (!loggedIn)
			{
				login();
			}

			try
			{
				XmlRpcClient client = getClient();
				Vector params = new Vector();
				params.add(token);
				params.add(issueKey);

				Hashtable issue = (Hashtable) client.execute("jira1.getIssue", params);
				return issue;
			}
			catch (XmlRpcException e)
			{
				throw new JIRAException(e.getMessage(), e);
			}
			catch (IOException e)
			{
				throw new JIRAException(e.getMessage(), e);
			}
		}



		public Vector getIssuesFromTextSearch(String searchTerms) throws JIRAException
		{

			if (!loggedIn)
			{
				login();
			}

			try
			{
				XmlRpcClient client = getClient();
				Vector params = new Vector();
				params.add(token);
				params.add(searchTerms);

				return (Vector) client.execute("jira1.getIssuesFromTextSearch", params);
			}
			catch (JIRAException e)
			{
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			catch (IOException e)
			{
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			catch (XmlRpcException e)
			{
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}

			return null;

		}


		public void progressWorkflowAction(String key, int workFlowId, Vector fields) throws JIRAException
		{
			if (!loggedIn)
			{
				login();
			}

			try
			{
				XmlRpcClient client = getClient();
				Vector params = new Vector();
				params.add(token);
				params.add(key);
				params.add(workFlowId);
				params.add(fields);

				client.execute("jira1.progressWorkflowAction", params);
			}
			catch (IOException e)
			{
				throw new JIRAException(e.getMessage(), e);
			}
			catch (XmlRpcException e)
			{
				throw new JIRAException(e.getMessage(), e);
			}
		}
		*/

	/*
		public JiraItem getIssue(String issueKey) throws JIRAException {
			try {
				Hashtable issue = getIssueHashtable(issueKey);
				MailDateFormat format = new MailDateFormat();

				JiraItem item = new JiraItem(server);
				item.setKey((String) issue.get("key"));
				item.setEnvironment((String) issue.get("environment"));
				item.setDescription((String) issue.get("description"));
				item.setCreated(format.parse((String) issue.get("created")));
				item.setUpdated(format.parse((String) issue.get("updated")));
				item.setDueDate(format.parse((String) issue.get("dueDate")));


				return item;
			} catch (ParseException e) {
				throw new JIRAException(e.getMessage(), e);
			}

		}
		*/

}