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
    }
}
