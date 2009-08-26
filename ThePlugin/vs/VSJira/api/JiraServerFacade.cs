﻿using System.Collections.Generic;
using PaZu.api.soap;

namespace PaZu.api
{
    public class JiraServerFacade
    {
        private readonly SortedDictionary<string, SoapSession> sessionMap = new SortedDictionary<string, SoapSession>();

        private SoapSession getSoapSession(JiraServer server)
        {
            SoapSession s;
            if (!sessionMap.TryGetValue(server.Url + server.UserName, out s))
            {
                s = new SoapSession(server.Url);
                s.login(server.UserName, server.Password);
                sessionMap.Add(server.Url + server.UserName, s);
            }
            return s;
        }

        public void login(JiraServer server)
        {
            new SoapSession(server.Url).login(server.UserName, server.Password);
        }

        public List<JiraProject> getProjects(JiraServer server)
        {
            return getSoapSession(server).getProjects();
        }

        public List<JiraNamedEntity> getIssueTypes(JiraServer server)
        {
            return getSoapSession(server).getIssueTypes();
        }

        public List<JiraSavedFilter> getSavedFilters(JiraServer server)
        {
            return getSoapSession(server).getSavedFilters();
        }

        public List<JiraIssue> getSavedFilterIssues(JiraServer server, JiraSavedFilter filter, int start, int count)
        {
            RssClient rss = new RssClient(server);
            return rss.getSavedFilterIssues(filter.Id, "issuekey", "ASC", start, count);
        }

        public List<JiraNamedEntity> getPriorities(JiraServer server)
        {
            return getSoapSession(server).getPriorities();
        }

        public List<JiraNamedEntity> getStatuses(JiraServer server)
        {
            return getSoapSession(server).getStatuses();
        }
    }
}
