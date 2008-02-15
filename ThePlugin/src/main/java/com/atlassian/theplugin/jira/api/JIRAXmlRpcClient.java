/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 13/03/2004
 * Time: 23:19:19
 */
package com.atlassian.theplugin.jira.api;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.xmlrpc.XmlRpcClient;

import java.net.MalformedURLException;
import java.util.*;

public class JIRAXmlRpcClient
{
    private static final Logger LOGGER = Logger.getInstance(JIRAXmlRpcClient.class.getName());

    private String serverUrl;
    private String token;
    private boolean loggedIn;

    public JIRAXmlRpcClient(String url)
    {
        this.serverUrl = url;
    }

    public XmlRpcClient getClient() throws JIRAException
    {
        try
        {
            return new XmlRpcClient(serverUrl + "/rpc/xmlrpc");
        }
        catch (MalformedURLException e)
        {
            throw new JIRAException(e.getMessage());
        }
    }

    public String getToken()
    {
        return token;
    }

    public boolean login(String username, String password) throws JIRAException
    {
        try
        {
            XmlRpcClient client = getClient();
            Vector params = new Vector();
            params.add(username);
            params.add(password);
            token = (String) client.execute("jira1.login", params);

            loggedIn = token != null && token.length() > 0;
        }
        catch (Exception e)
        {
            throw new JIRAException("RPC Not Supported: " + e.getMessage());
        }
        
        return loggedIn;
    }

    // methods below here might work, but I haven't tested them - commented out for now!
    // when commenting in, please be sure to add tests

    /*
    public List getProjects() throws JIRAException
    {
        return getListFromRPCMethod("jira1.getProjects");
    }

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

    public List getStatuses() throws JIRAException
    {
        return getListFromRPCMethod("jira1.getStatuses");
    }

    public List getIssueTypes() throws JIRAException
    {
        return getListFromRPCMethod("jira1.getIssueTypes");
    }

    public List getComponents(String projectKey) throws JIRAException
    {
        if (!loggedIn)
        {
            login();
        }

        XmlRpcClient client = getClient();
        Vector params = new Vector();
        params.add(token);
        params.add(projectKey);

        LOGGER.info("Getting components for project: " + projectKey);

        try
        {
            List projects = (List) client.execute("jira1.getComponents", params);
            return projects;
        }
        catch (XmlRpcException e)
        {
            throw new JIRAException(e.getMessage());
        }
        catch (IOException e)
        {
            throw new JIRAException(e.getMessage());
        }

    }

    public List getVersions(String projectKey) throws JIRAException
    {
        if (!loggedIn)
        {
            login();
        }

        XmlRpcClient client = getClient();
        Vector params = new Vector();
        params.add(token);
        params.add(projectKey);

        LOGGER.info("Getting project versions: " + token + " | " + projectKey);

        try
        {
            List versions = (List) client.execute("jira1.getVersions", params);
            return versions;
        }
        catch (Exception e)
        {
            throw new JIRAException(e.getMessage());
        }
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

    public Hashtable createIssue(String project, String summary, String environemt,
                                 String description, String type, String priority,
                                 String affectsVersionId, String fixForVersionId
    ) throws JIRAException
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

            Hashtable issue = new Hashtable();
            issue.put("project", project);
            issue.put("summary", summary);
            issue.put("environment", environemt);
            issue.put("description", description);
            issue.put("type", type);
            issue.put("priority", priority);

            if (affectsVersionId != null)
            {
                Vector affectsVersions = new Vector();
                Hashtable affectsVersion = new Hashtable();
                affectsVersion.put("id", affectsVersionId);
                affectsVersions.add(affectsVersion);
                issue.put("affectsVersions", affectsVersions);
            }

            if (fixForVersionId != null)
            {
                Vector fixForVersions = new Vector();
                Hashtable fixForVersion = new Hashtable();
                fixForVersion.put("id", fixForVersionId);
                fixForVersions.add(fixForVersion);
                issue.put("fixVersions", fixForVersions);
            }

            params.add(issue);

            return (Hashtable) client.execute("jira1.createIssue", params);
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

    public void addIssueComment(String issueKey, String comment) throws JIRAException
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
            params.add(comment);

            client.execute("jira1.addComment", params);
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

    private List getListFromRPCMethod(String rpcCommand) throws JIRAException
    {
        if (!loggedIn)
        {
            login();
        }

        XmlRpcClient client = getClient();
        Vector params = new Vector();
        params.add(token);

        LOGGER.info("Getting saved filters for " + rpcCommand);

        try
        {
            List filters = (List) client.execute(rpcCommand, params);
            return filters;
        }
        catch (Exception e)
        {
            throw new JIRAException(e.getMessage());
        }
    }

    /*

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