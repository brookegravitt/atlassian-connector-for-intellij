using System;
using System.Collections.Generic;
using System.Text;
using EnvDTE;
using System.Diagnostics;
using PaZu.api;

namespace PaZu.models
{
    public class JiraServerModel
    {
        private static string SERVER_COUNT = "serverCount";
        private static string SERVER_GUID = "serverGuid_";
        private static string SERVER_NAME = "serverName_";
        private static string SERVER_URL = "serverUrl_";

        private bool changedSinceLoading = false;

        public class ModelException: Exception 
        {
            public ModelException(string message) : base(message)
            {
            }
        }

        private SortedDictionary<Guid, JiraServer> serverMap = new SortedDictionary<Guid, JiraServer>();

        private JiraServerModel()
        {
        }

        private static JiraServerModel instance = new JiraServerModel();

        public static JiraServerModel Instance { get { return instance; } }

        public ICollection<JiraServer> getAllServers()
        {
            lock (serverMap)
            {
                return serverMap.Values;
            }
        }

        public void load(Globals globals)
        {

            if (globals.get_VariableExists(SERVER_COUNT))
            {
                try
                {
                    int count = int.Parse(globals[SERVER_COUNT].ToString());
                    for (int i = 1; i <= count; ++i)
                    {
                        string guidStr = globals[SERVER_GUID + i].ToString();
                        Guid guid = new Guid(guidStr);
                        string sName = globals[SERVER_NAME + guidStr].ToString();
                        string url = globals[SERVER_URL + guidStr].ToString();
                        JiraServer server = new JiraServer(guid, sName, url, null, null);
                        server.UserName = CredentialsVault.Instance.getUserName(server);
                        server.Password = CredentialsVault.Instance.getPassword(server);
                        addServer(server);
                    }
                    changedSinceLoading = false;
                }
                catch (Exception e)
                {
                    Debug.WriteLine(e);
                }
            }
            
        }

        public void save(Globals globals)
        {
            if (!changedSinceLoading)
            {
                return;
            }

            try
            {
                globals[SERVER_COUNT] = serverMap.Values.Count.ToString();
                globals.set_VariablePersists(SERVER_COUNT, true);

                int i = 1;
                foreach (JiraServer s in getAllServers())
                {
                    string var = SERVER_GUID + i.ToString();
                    globals[var] = s.GUID.ToString();
                    globals.set_VariablePersists(var, true);
                    var = SERVER_NAME + s.GUID.ToString();
                    globals[var] = s.Name;
                    globals.set_VariablePersists(var, true);
                    var = SERVER_URL + s.GUID.ToString();
                    globals[var] = s.Url;
                    globals.set_VariablePersists(var, true);
                    CredentialsVault.Instance.saveCredentials(s);
                    ++i;
                }
            }
            catch (Exception e)
            {
                Debug.WriteLine(e);
            }
        }

        public void addServer(JiraServer server)
        {
            lock (serverMap)
            {
                if (serverMap.ContainsKey(server.GUID))
                {
                    throw new ModelException("Server exists");
                }
                serverMap.Add(server.GUID, server);
                changedSinceLoading = true;
            }
        }

        public JiraServer getServer(Guid guid)
        {
            lock (serverMap)
            {
                if (serverMap.ContainsKey(guid))
                {
                    return serverMap[guid];
                }
                return null;
            }
        }

        public void removeServer(Guid guid)
        {
            JiraServer s = getServer(guid);
            if (s != null)
            {
                removeServer(guid, false);
                CredentialsVault.Instance.deleteCredentials(s);
            }
        }

        public void removeServer(Guid guid, bool nothrow)
        {
            lock (serverMap)
            {
                if (serverMap.ContainsKey(guid))
                {
                    serverMap.Remove(guid);
                    changedSinceLoading = true;
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
                changedSinceLoading = true;
            }
        }
    }
}
