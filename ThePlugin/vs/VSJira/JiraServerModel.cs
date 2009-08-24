using System;
using System.Collections.Generic;
using System.Text;
using EnvDTE;
using System.Diagnostics;
using Microsoft.Win32;

namespace PaZu
{
    public class JiraServerModel
    {
        private static string SERVER_COUNT = "serverCount";
        private static string SERVER_GUID = "serverGuid_";
        private static string SERVER_NAME = "serverName_";
        private static string USER_NAME = "UserName_";
        private static string USER_PASSWORD = "UserPassword_";
        private static string SERVER_URL = "serverUrl_";
        private static string PAZU_KEY = "\\Software\\Atlassian\\PaZu";

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
            return serverMap.Values;
        }

        public void load(Globals globals)
        {

            if (globals.get_VariableExists(SERVER_COUNT))
            {
                try
                {
                    RegistryKey key = Registry.CurrentUser.OpenSubKey(PAZU_KEY);

                    int count = int.Parse(globals[SERVER_COUNT].ToString());
                    for (int i = 1; i <= count; ++i)
                    {
                        Guid guid = new Guid(globals[SERVER_GUID + i].ToString());
                        string sName = globals[SERVER_NAME + i].ToString();
                        string url = globals[SERVER_URL + i].ToString();
                        string uName = key.GetValue(USER_NAME + i).ToString();
                        string pwd = key.GetValue(USER_PASSWORD + i).ToString();
                        addServer(new JiraServer(guid, sName, url, uName, pwd));
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

            globals[SERVER_COUNT] = serverMap.Values.Count.ToString();
            globals.set_VariablePersists(SERVER_COUNT, true);
            RegistryKey key = Registry.CurrentUser.CreateSubKey(PAZU_KEY);

            int i = 1;
            foreach (JiraServer s in getAllServers())
            {
                try
                {
                    string var = SERVER_GUID + i.ToString();
                    globals[var] = s.GUID.ToString();
                    globals.set_VariablePersists(var, true);
                    var = SERVER_NAME + i.ToString();
                    globals[var] = s.Name;
                    globals.set_VariablePersists(var, true);
                    var = SERVER_URL + i.ToString();
                    globals[var] = s.Url;
                    globals.set_VariablePersists(var, true);
                    key.SetValue(USER_NAME + i.ToString(), s.UserName);
                    key.SetValue(USER_PASSWORD + i.ToString(), s.Password);
                }
                catch (Exception e)
                {
                    Debug.WriteLine(e);
                }

                ++i;
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
            removeServer(guid, false);
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
