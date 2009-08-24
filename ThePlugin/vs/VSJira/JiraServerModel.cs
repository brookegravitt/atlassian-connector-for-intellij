using System;
using System.Collections.Generic;
using System.Text;

namespace PaZu
{
    public class JiraServerModel
    {
        public class ModelException: Exception 
        {
            public ModelException(string message) : base(message)
            {
            }
        }

        private SortedDictionary<string, JiraServer> serverMap = new SortedDictionary<string, JiraServer>();

        private JiraServerModel()
        {
        }

        private static JiraServerModel instance = new JiraServerModel();

        public static JiraServerModel Instance { get { return instance; } }

        public ICollection<JiraServer> getAllServers()
        {
            return serverMap.Values;
        }

        public void addServer(JiraServer server)
        {
            lock (serverMap)
            {
                if (serverMap.ContainsKey(server.Name))
                {
                    throw new ModelException("Server exists");
                }
                serverMap.Add(server.Name, server);
            }
        }

        public JiraServer getServer(string name)
        {
            lock (serverMap)
            {
                if (serverMap.ContainsKey(name))
                {
                    return serverMap[name];
                }
                return null;
            }
        }

        public void removeServer(string name)
        {
            removeServer(name, false);
        }

        public void removeServer(string name, bool nothrow)
        {
            lock (serverMap)
            {
                if (serverMap.ContainsKey(name))
                {
                    serverMap.Remove(name);
                }
                else if (!nothrow)
                {
                    throw new ModelException("No such server");
                }
            }
        }

        public void clear()
        {
            lock (serverMap)
            {
                serverMap.Clear();
            }
        }
    }
}
