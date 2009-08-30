using PaZu.api;

namespace PaZu.models
{
    public interface JiraIssueListModelListener
    {
        void modelChanged();
        void issueChanged(JiraIssue issue);
    }
}
