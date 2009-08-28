using System;
using System.Collections.Generic;
using System.IO;
using System.Xml.XPath;

namespace PaZu.api
{
    public class JiraIssue
    {
        public const int UNKNOWN = -1;

        public class Comment
        {
            public string Body { get; internal set; }
            public string Created { get; internal set; }
            public string Author { get; internal set; }
        }

        private readonly List<Comment> comments = new List<Comment>();

        public JiraIssue(JiraServer server, XPathNavigator nav)
        {
            Server = server;

            nav.MoveToFirstChild();
            do
            {
                switch (nav.Name)
                {
                    case "key":
                        Key = nav.Value;
                        Id = getAttributeSafely(nav, "id", UNKNOWN);
                        ProjectKey = Key.Substring(0, Key.LastIndexOf('-'));
                        break;
                    case "summary":
                        Summary = nav.Value;
                        break;
                    case "status":
                        Status = nav.Value;
                        StatusIconUrl = getAttributeSafely(nav, "iconUrl", null);
                        StatusId = getAttributeSafely(nav, "id", UNKNOWN);
                        break;
                    case "priority":
                        Priority = nav.Value;
                        PriorityIconUrl = getAttributeSafely(nav, "iconUrl", null);
                        PriorityId = getAttributeSafely(nav, "id", UNKNOWN);
                        break;
                    case "description":
                        Description = nav.Value;
                        break;
                    case "type":
                        IssueType = nav.Value;
                        IssueTypeIconUrl = getAttributeSafely(nav, "iconUrl", null);
                        IssueTypeId = getAttributeSafely(nav, "id", UNKNOWN);
                        break;
                    case "assignee":
                        Assignee = nav.Value;
                        break;
                    case "reporter":
                        Reporter = nav.Value;
                        break;
                    case "created":
                        CreationDate = nav.Value;
                        break;
                    case "updated":
                        UpdateDate = nav.Value;
                        break;
                    case "resolution":
                        Resolution = nav.Value;
                        break;
                    case "timeestimate":
                        RemainingEstimate = nav.Value;
                        RemainingEstimateInSeconds = getAttributeSafely(nav, "seconds", UNKNOWN);
                        break;
                    case "timeoriginalestimate":
                        OriginalEstimate = nav.Value;
                        OriginalEstimateInSeconds = getAttributeSafely(nav, "seconds", UNKNOWN);
                        break;
                    case "timespent":
                        TimeSpent = nav.Value;
                        TimeSpentInSeconds = getAttributeSafely(nav, "seconds", UNKNOWN);
                        break;
                    case "comments":
                        createComments(nav);
                        break;
                    default:
                        break;
                }
            } while (nav.MoveToNext());
            if (Key == null || Summary == null)
            {
                throw new InvalidDataException();
            }
        }

        private void createComments(XPathNavigator nav)
        {
            XPathExpression expr = nav.Compile("comment");
            XPathNodeIterator it = nav.Select(expr);

            if (!nav.MoveToFirstChild()) return;
            while (it.MoveNext())
            {
                Comment c = new Comment
                                {
                                    Body = it.Current.Value,
                                    Author = getAttributeSafely(it.Current, "author", "Unknown"),
                                    Created = getAttributeSafely(it.Current, "created", "Unknown")
                                };
                comments.Add(c);
            }
            nav.MoveToParent();
        }

        public JiraServer Server { get; private set; }

        public string IssueType { get; private set; }

        public int IssueTypeId { get; private set; }

        public string IssueTypeIconUrl { get; private set; }

        public string Description { get; private set; }

        public int Id { get; private set; }

        public string Key { get; private set; }

        public string Summary { get; private set; }

        public string Status { get; private set; }

        public int StatusId { get; private set; }

        public string StatusIconUrl { get; private set; }

        public string Priority { get; private set; }

        public string PriorityIconUrl { get; private set; }

        public int PriorityId { get; set; }

        public string Resolution { get; private set; }

        public string Reporter { get; private set; }

        public string Assignee { get; private set; }

        public string CreationDate { get; private set; }

        public string UpdateDate { get; private set; }

        public string ProjectKey { get; private set; }

        public string OriginalEstimate { get; private set; }

        public int OriginalEstimateInSeconds { get; private set; }

        public string RemainingEstimate { get; set; }

        public int RemainingEstimateInSeconds { get; set; }

        public string TimeSpent { get; set; }

        public int TimeSpentInSeconds { get; set; }

        public List<Comment> Comments { get { return comments; } }

        private static string getAttributeSafely(XPathNavigator nav, string name, string defaultValue)
        {
            if (nav.HasAttributes && nav.MoveToFirstAttribute())
            {
                do
                {
                    if (!nav.Name.Equals(name)) continue;
                    string val = nav.Value;
                    nav.MoveToParent();
                    return val;
                } while (nav.MoveToNextAttribute());
                nav.MoveToParent();
            }
            return defaultValue;
        }

        private static int getAttributeSafely(XPathNavigator nav, string name, int defaultValue)
        {
            if (nav.HasAttributes && nav.MoveToFirstAttribute())
            {
                do
                {
                    if (!nav.Name.Equals(name)) continue;
                    int val = nav.ValueAsInt;
                    nav.MoveToParent();
                    return val;
                } while (nav.MoveToNextAttribute());
                nav.MoveToParent();
            }
            return defaultValue;
        }
    }
}
