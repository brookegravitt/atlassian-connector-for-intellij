package com.atlassian.theplugin.commons.jira.api.rss;

import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.commons.rss.JIRARssClient;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;

/**
 * @author pmaruszak
 * @date Sep 25, 2009
 */
public class IntelliJJiraRssClient extends JIRARssClient {
    private final JiraServerData serverData;


    public IntelliJJiraRssClient(JiraServerData serverData, HttpSessionCallback httpSessionCallback)
            throws RemoteApiMalformedUrlException {
        super(serverData.toHttpConnectionCfg(), httpSessionCallback);
        this.serverData = serverData;
    }


    public JiraServerData getServerData() {
        return serverData;
    }
}
