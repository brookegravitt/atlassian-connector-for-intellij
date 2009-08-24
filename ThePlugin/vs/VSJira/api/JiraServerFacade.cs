using System;
using System.Collections.Generic;
using System.Text;
using PaZu.api.soap;

namespace PaZu.api
{
    public class JiraServerFacade
    {
        private SortedDictionary<string, SoapSession> sessionMap = new SortedDictionary<string, SoapSession>();

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
        }

        public List<JiraProject> getProjects(JiraServer server)
        {
            return getSoapSession(server).getProjects();
        }

        public List<JiraSavedFilter> getSavedFilters(JiraServer server)
        {
            return getSoapSession(server).getSavedFilters();
        }

        public List<JiraIssue> getSavedFilterIssues(JiraServer server, JiraSavedFilter filter)
        {
            RssClient rss = new RssClient(server);
            return rss.getSavedFilterIssues(filter.ID, "issuekey", "ASC", 0, 25);
        }
    }
}
