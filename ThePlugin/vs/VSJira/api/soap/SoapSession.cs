using System;
using System.Collections.Generic;
using System.Text;
using PaZu.JIRA;

namespace PaZu.api.soap
{
    public class SoapSession
    {
        private string url;
        private string token;
        private JiraSoapServiceService service = new JiraSoapServiceService();

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
                list.Add(new JiraProject(p.id, p.key, p.name));
            }
            return list;
        }

        public List<JiraSavedFilter> getSavedFilters()
        {
            RemoteFilter[] fTable = service.getSavedFilters(token);
            List<JiraSavedFilter> list = new List<JiraSavedFilter>();
            foreach (RemoteFilter f in fTable)
            {
                list.Add(new JiraSavedFilter(f.id, f.name));
            }
            return list;
        }

        public class LoginException : Exception
        {
            public LoginException(Exception e) : base("Login failed", e)
            {
            }
        }
    }
}
