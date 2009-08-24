using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Xml.XPath;

namespace PaZu
{
    public class JiraIssue
    {
        public JiraIssue(string key, string description)
        {
            this.key = key;
            this.summary = description;
        }

        public JiraIssue(XPathNavigator nav)
        {
            nav.MoveToFirstChild();
            do
            {
                switch (nav.Name)
                {
                    case "key":
                        key = nav.Value;
                        break;
                    case "summary":
                        summary = nav.Value;
                        break;
                    default:
                        break;
                }
            } while (nav.MoveToNext());
            if (key == null || summary == null)
            {
                throw new InvalidDataException();
            }
        }

        private string key;
        private string summary;

        public string Key
        {
            get
            {
                return key;
            }
        }

        public string Summary
        {
            get
            {
                return summary;
            }
        }
    }
}
