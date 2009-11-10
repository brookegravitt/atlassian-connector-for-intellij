using System;
using System.Collections.Generic;
using EnvDTE;
using PaZu.api;

namespace PaZu.models
{
    public class CustomFilter
    {
        private readonly JiraServer server;

        private CustomFilter(JiraServer server)
        {
            this.server = server;
        }

        public static List<CustomFilter> getAll(JiraServer server)
        {
            List<CustomFilter> list = new List<CustomFilter>(1) {new CustomFilter(server)};
            return list;
        }

        public static void clear()
        {
        }

        public override string ToString()
        {
            return "server: " + server.Url;
        }

        public static void load(Globals globals, string solutionName)
        {
        }
    }
}
