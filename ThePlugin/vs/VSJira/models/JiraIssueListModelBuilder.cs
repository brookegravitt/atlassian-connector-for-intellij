using System;
using System.Collections.Generic;
using PaZu.api;

namespace PaZu.models
{
    public class JiraIssueListModelBuilder
    {
        private readonly JiraServerFacade facade;

        public JiraIssueListModelBuilder(JiraServerFacade facade)
        {
            this.facade = facade;
        }

        public void rebuildModelWithSavedFilter(JiraIssueListModel model, JiraServer server, JiraSavedFilter filter)
        {
            lock (this)
            {
                List<JiraIssue> issues = facade.getSavedFilterIssues(server, filter, 0, 25);
                model.clear(false);
                model.addIssues(issues);
            }
        }

        public void updateModelWithSavedFilter(JiraIssueListModel model, JiraServer server, JiraSavedFilter filter)
        {
            lock (this)
            {
                List<JiraIssue> issues = facade.getSavedFilterIssues(server, filter, model.Issues.Count, 25);
                model.addIssues(issues);
            }
        }

        public void rebuildModelWithRecentlyViewedIssues(JiraIssueListModel model)
        {
            lock (this)
            {
                ICollection<RecentlyViewedIssue> issues = RecentlyViewedIssuesModel.Instance.Issues;
                ICollection<JiraServer> servers = JiraServerModel.Instance.getAllServers();

                List<JiraIssue> list = new List<JiraIssue>(issues.Count);
                foreach (RecentlyViewedIssue issue in issues)
                {
                    JiraServer server = findServer(issue.ServerGuid, servers);
                    if (server != null)
                        list.Add(facade.getIssue(server, issue.IssueKey));
                }

                model.clear(false);
                model.addIssues(list);
            }
        }

        private static JiraServer findServer(Guid guid, IEnumerable<JiraServer> servers)
        {
            foreach (JiraServer server in servers)
            {
                if (server.GUID.Equals(guid))
                    return server;
            }
            return null;
        }
    }
}
