package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.jira.api.JIRAXmlRpcClient;
import com.atlassian.theplugin.jira.api.JIRALoginException;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRARssClient;
import com.atlassian.theplugin.configuration.Server;

import java.util.List;

public class JIRAServerFacadeImpl implements JIRAServerFacade {
    public void testServerConnection(String url, String userName, String password) throws JIRALoginException {
        JIRAXmlRpcClient client = new JIRAXmlRpcClient(url);
        try {
            if (!client.login(userName, password)) {
                throw new JIRALoginException("Bad credentials");
            }
        } catch (JIRAException e) {
            throw new JIRALoginException("Error logging in", e);
        }
    }

    public List getAssignedIssues(Server server, String username) throws JIRAException {
        JIRARssClient rss = new JIRARssClient(server.getUrlString(), server.getUserName(), server.getPasswordString());
        return rss.getAssignedIssues(username);
    }
}
