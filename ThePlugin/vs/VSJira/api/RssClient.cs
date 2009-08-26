using System;
using System.Collections.Generic;
using System.Text;
using System.Web;
using System.Net;
using System.IO;
using System.Diagnostics;
using System.Xml.XPath;

namespace PaZu.api
{
    class RssClient
    {
        private readonly JiraServer server;
        private readonly string baseUrl;
        private readonly string userName;
        private readonly string password;

        public RssClient(JiraServer server)
            : this(server.Url, server.UserName, server.Password)
        {
            this.server = server;
        }

        private RssClient(string url, string userName, string password)
        {
            baseUrl = url;
            this.userName = userName;
            this.password = password;
        }

        public List<JiraIssue> getSavedFilterIssues(
            int filterId,
            string sortBy,
            string sortOrder,
            int start,
            int max)
        {
            StringBuilder url = new StringBuilder(baseUrl + "/sr/jira.issueviews:searchrequest-xml/");
            url.Append(filterId).Append("/SearchRequest-").Append(filterId).Append(".xml");
            url.Append("?sorter/field=" + sortBy);
            url.Append("&sorter/order=" + sortOrder);
            url.Append("&pager/start=" + start);
            url.Append("&tempMax=" + max);

            url.Append(appendAuthentication());

            try
            {
                HttpWebRequest req = (HttpWebRequest)WebRequest.Create(url.ToString());
                req.Timeout = 5000;
                req.ReadWriteTimeout = 20000;
                HttpWebResponse resp = (HttpWebResponse)req.GetResponse();
                Stream s = resp.GetResponseStream();
                return createIssueList(s);
            }
            catch (Exception e)
            {
                Debug.WriteLine(e.Message);
                throw;
            }
        }

        private string appendAuthentication()
        {
            if (userName != null)
            {
                return "&os_username=" + HttpUtility.UrlEncode(userName)
                        + "&os_password=" + HttpUtility.UrlEncode(password);
            }
            return "";
        }

        private List<JiraIssue> createIssueList(Stream s)
        {
            XPathDocument doc = new XPathDocument(s);
            XPathNavigator nav = doc.CreateNavigator();
            XPathExpression expr = nav.Compile("/rss/channel/item");
            XPathNodeIterator it = nav.Select(expr);

            List<JiraIssue> list = new List<JiraIssue>();
            while (it.MoveNext())
            {
                list.Add(new JiraIssue(server, it.Current));
            }

            return list;
        }
    }
}
