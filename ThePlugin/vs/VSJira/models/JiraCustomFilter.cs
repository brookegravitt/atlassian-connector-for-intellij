using System;
using System.Collections.Generic;
using System.Text;
using EnvDTE;
using PaZu.api;
using PaZu.util;

namespace PaZu.models
{
    public class JiraCustomFilter
    {
        private readonly JiraServer server;

        private const string ISSUE_NAVIGATOR = "/secure/IssueNavigator.jspa?refreshFilter=false&reset=update&show=View+%3E%3E";
        private const string BROWSER_QUERY_SUFFIX = "&pager/start=-1&tempMax=100";

        public List<JiraProject> Projects { get; private set; }
        public List<JiraNamedEntity> IssueTypes { get; private set; }
        public List<JiraNamedEntity> FixForVersions { get; private set; }
        public List<JiraNamedEntity> AffectsVersions { get; private set; }
        public List<JiraNamedEntity> Components { get; private set; }

        private static Dictionary<Guid, JiraCustomFilter> filters = new Dictionary<Guid, JiraCustomFilter>();

        public bool Empty 
        { 
            get
            {
                return Projects.Count + IssueTypes.Count + FixForVersions.Count + AffectsVersions.Count + Components.Count == 0;
            }
        }

        private JiraCustomFilter(JiraServer server)
        {
            this.server = server;

            Projects = new List<JiraProject>();
            IssueTypes = new List<JiraNamedEntity>();
            FixForVersions = new List<JiraNamedEntity>();
            AffectsVersions = new List<JiraNamedEntity>();
            Components = new List<JiraNamedEntity>();
        }

        public static List<JiraCustomFilter> getAll(JiraServer server)
        {
            List<JiraCustomFilter> list = new List<JiraCustomFilter>(1);
            if (!filters.ContainsKey(server.GUID))
                filters[server.GUID] = new JiraCustomFilter(server);
            list.Add(filters[server.GUID]);
            return list;
        }

        public static void clear()
        {
            filters.Clear();
        }

        public string getBrowserQueryString()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(ISSUE_NAVIGATOR).Append("&");

            sb.Append(getQueryParameters());

            sb.Append(BROWSER_QUERY_SUFFIX);

            return sb.ToString();
        }

        public string getFilterQueryString()
        {
            StringBuilder sb = new StringBuilder();

            sb.Append(getQueryParameters());

            return sb.ToString();
        }

        private string getQueryParameters()
        {
            StringBuilder sb = new StringBuilder();
            int first = 0;
            foreach (JiraProject project in Projects)
                sb.Append(first++ == 0 ? "" : "&").Append("pid=").Append(project.Id);
            foreach (JiraNamedEntity issueType in IssueTypes)
                sb.Append(first++ == 0 ? "" : "&").Append("type=").Append(issueType.Id);
            foreach (JiraNamedEntity version in AffectsVersions)
                sb.Append(first++ == 0 ? "" : "&").Append("version=").Append(version.Id);
            foreach (JiraNamedEntity version in FixForVersions)
                sb.Append(first++ == 0 ? "" : "&").Append("fixfor=").Append(version.Id);
            foreach (JiraNamedEntity comp in Components)
                sb.Append(first++ == 0 ? "" : "&").Append("component=").Append(comp.Id);

            return sb.ToString();
        }

        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();

            if (Empty)
                return "Filter not defined\n\nRight-click to define the filter";

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
            sb.Append("\n\nRight-click to edit filter definition");

            return sb.ToString();
        }

        public static void load(Globals globals, string solutionName)
        {
//            string key = ParameterSerializer.getKeyFromSolutionName(solutionName);
//            if (ParameterSerializer.loadParameter(globals, "jiraCustomFilterCount", null) != null)
        }

        public static void save(Globals globals, string solutionName)
        {
            string key = ParameterSerializer.getKeyFromSolutionName(solutionName);
            ParameterSerializer.storeParameter(globals, key + "_jiraCustomFilterCount", filters.Count);
            int i = 0;
            foreach (var filter in filters)
            {
                ParameterSerializer.storeParameter(globals, key + "_jiraCustormFilterGuid_" + i, filter.Key.ToString());
                JiraCustomFilter f = filter.Value;

                ParameterSerializer.storeParameter(globals, key + "jiraCustomFilterProjectCount_" + filter.Key, f.Projects.Count);
                int k = 0;
                foreach (JiraProject project in f.Projects)
                {
                    ParameterSerializer.storeParameter(globals, key + "jiraCustomFilterProjectId_" + k, project.Id);
                    ParameterSerializer.storeParameter(globals, key + "jiraCustomFilterProjectKey_" + k, project.Key);
                    ++k;
                }

                ++i;
            }
        }
    }
}
