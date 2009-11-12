using System;
using System.Collections.Generic;
using System.Text;
using EnvDTE;
using PaZu.api;

namespace PaZu.models
{
    public class CustomFilter
    {
        private readonly JiraServer server;

        private const string ISSUE_NAVIGATOR = "/secure/IssueNavigator.jspa?refreshFilter=false&reset=update&show=View+%3E%3E";
        private const string BROWSER_QUERY_SUFFIX = "&pager/start=-1&tempMax=100";

        public List<JiraProject> Projects { get; private set; }
        public List<JiraNamedEntity> IssueTypes { get; private set; }
        public List<JiraNamedEntity> FixForVersions { get; private set; }
        public List<JiraNamedEntity> AffectsVersions { get; private set; }
        public List<JiraNamedEntity> Components { get; private set; }

        public bool Empty 
        { 
            get
            {
                return Projects.Count + IssueTypes.Count + FixForVersions.Count + AffectsVersions.Count + Components.Count == 0;
            }
        }

        private CustomFilter(JiraServer server)
        {
            this.server = server;

            Projects = new List<JiraProject>();
            IssueTypes = new List<JiraNamedEntity>();
            FixForVersions = new List<JiraNamedEntity>();
            AffectsVersions = new List<JiraNamedEntity>();
            Components = new List<JiraNamedEntity>();
        }

        public static List<CustomFilter> getAll(JiraServer server)
        {
            List<CustomFilter> list = new List<CustomFilter>(1) { new CustomFilter(server) };
            return list;
        }

        public static void clear()
        {
        }

        public string getQuery()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(ISSUE_NAVIGATOR);

            foreach (JiraProject project in Projects)
                sb.Append("&pid=").Append(project.Id);
            foreach (JiraNamedEntity issueType in IssueTypes)
                sb.Append("&type=").Append(issueType.Id);
            foreach (JiraNamedEntity version in AffectsVersions)
                sb.Append("&version=").Append(version.Id);
            foreach (JiraNamedEntity version in FixForVersions)
                sb.Append("&fixfor=").Append(version.Id);
            foreach (JiraNamedEntity comp in Components)
                sb.Append("&component=").Append(comp.Id);

            sb.Append(BROWSER_QUERY_SUFFIX);

            return sb.ToString();
        }

        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();

            if (Empty)
                return "Filter not defined";

            sb.Append("Server URL: ").Append(server.Url);

            if (Projects.Count > 0)
            {
                sb.Append("\nProjects: ");
                foreach (JiraProject project in Projects)
                    sb.Append(project.Key).Append(" ");
            }
            if (IssueTypes.Count > 0)
            {
                sb.Append("\nIssue Types: ");
                foreach (JiraNamedEntity issueType in IssueTypes)
                    sb.Append(issueType.Name).Append(" ");
            }
            if (AffectsVersions.Count > 0)
            {
                sb.Append("\nAffects Versions: ");
                foreach (JiraNamedEntity version in AffectsVersions)
                    sb.Append(version.Name).Append(" ");
            }
            if (FixForVersions.Count > 0)
            {
                sb.Append("\nFix For Versions: ");
                foreach (JiraNamedEntity version in FixForVersions)
                    sb.Append(version.Name).Append(" ");
            }
            if (Components.Count > 0)
            {
                sb.Append("\nComponents: ");
                foreach (JiraNamedEntity comp in Components)
                    sb.Append(comp.Name).Append(" ");
            }
            return sb.ToString();
        }

        public static void load(Globals globals, string solutionName)
        {
        }
    }
}
