using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.Win32;
using System.Diagnostics;
using PaZu.api;

namespace PaZu.models
{
    class CredentialsVault
    {
        private static CredentialsVault instance = new CredentialsVault();

        public static CredentialsVault Instance { get { return instance; } }

        private static string ATL_KEY = "Software\\Atlassian";
        private static string PAZU_KEY = "PaZu";
        private static string USER_NAME = "UserName_";
        private static string USER_PASSWORD = "UserPassword_";

        private CredentialsVault()
        {
        }

        public string getUserName(JiraServer server)
        {
            try
            {
                RegistryKey key = Registry.CurrentUser.OpenSubKey(ATL_KEY + "\\" + PAZU_KEY);
                return (string) key.GetValue(USER_NAME + server.GUID.ToString(), "");
            }
            catch (Exception e)
            {
                Debug.WriteLine(e.Message);
            }
            return "";
        }

        public string getPassword(JiraServer server)
        {
            try
            {
                RegistryKey key = Registry.CurrentUser.OpenSubKey(ATL_KEY + "\\" + PAZU_KEY);
                return (string) key.GetValue(USER_PASSWORD + server.GUID.ToString(), "");
            }
            catch (Exception e)
            {
                Debug.WriteLine(e.Message);
            }
            return "";
        }

        public void saveCredentials(JiraServer server)
        {
            RegistryKey atlKey = Registry.CurrentUser.CreateSubKey(ATL_KEY);
            RegistryKey key = atlKey.CreateSubKey(PAZU_KEY);
            key.SetValue(USER_NAME + server.GUID.ToString(), server.UserName);
            key.SetValue(USER_PASSWORD + server.GUID.ToString(), server.Password);
        }

        public void deleteCredentials(JiraServer server)
        {
            RegistryKey atlKey = Registry.CurrentUser.CreateSubKey(ATL_KEY);
            RegistryKey key = atlKey.CreateSubKey(PAZU_KEY);
            key.DeleteValue(USER_NAME + server.GUID.ToString(), false);
            key.DeleteValue(USER_PASSWORD + server.GUID.ToString(), false);
        }
    }
}
