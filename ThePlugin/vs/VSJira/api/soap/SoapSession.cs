using System;
using System.Collections.Generic;
using PaZu.JIRA;

namespace PaZu.api.soap
{
    public class SoapSession
    {
        private readonly string url;
        private string token;
        private readonly JiraSoapServiceService service = new JiraSoapServiceService();

        public SoapSession(string u)
        {
            url = u + "/rpc/soap/jirasoapservice-v2";
            service.Url = url;
        }

        public void login(string userName, string password)
        {
            try
            {
                token = service.login(userName, password);
            }
            catch (Exception e)
            {
                throw new LoginException(e);
            }
        }

        public List<JiraProject> getProjects()
        {
            RemoteProject[] pTable = service.getProjectsNoSchemes(token);
            List<JiraProject> list = new List<JiraProject>();
            foreach (RemoteProject p in pTable)
            {
                list.Add(new JiraProject(int.Parse(p.id), p.key, p.name));
            }
            return list;
        }

        public List<JiraSavedFilter> getSavedFilters()
        {
            RemoteFilter[] fTable = service.getSavedFilters(token);
            List<JiraSavedFilter> list = new List<JiraSavedFilter>();
            foreach (RemoteFilter f in fTable)
            {
                list.Add(new JiraSavedFilter(int.Parse(f.id), f.name));
            }
            return list;
        }


        public void addComment(JiraIssue issue, string comment)
        {
            service.addComment(token, issue.Key, new RemoteComment {body = comment});
        }

        public class LoginException : Exception
        {
            public LoginException(Exception e) : base("Login failed", e)
            {
            }
        }

        public List<JiraNamedEntity> getIssueTypes()
        {
            return createEntityList(service.getIssueTypes(token));
        }

        public List<JiraNamedEntity> getPriorities()
        {
            return createEntityList(service.getPriorities(token));
        }

        public List<JiraNamedEntity> getStatuses()
        {
            return createEntityList(service.getStatuses(token));
        }

        private static List<JiraNamedEntity> createEntityList(IEnumerable<AbstractRemoteConstant> vals)
        {
            List<JiraNamedEntity> list = new List<JiraNamedEntity>();
            foreach (AbstractRemoteConstant val in vals)
            {
                list.Add(new JiraNamedEntity(int.Parse(val.id), val.name, val.icon));
            }
            return list;
            
        }
    }
}
