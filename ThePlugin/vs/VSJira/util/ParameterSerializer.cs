using EnvDTE;

namespace PaZu.util
{
    public class ParameterSerializer
    {

        public static string getKeyFromSolutionName(string solutionName)
        {
            string res = solutionName;
            if (res.Contains("\\") && !res.EndsWith("\\"))
            {
                res = res.Substring(res.LastIndexOf("\\") + 1);
            }
            res = res.Replace(".", "_dot_");
            return res;
        }

        public static void storeParameter(Globals globals, string name, int value)
        {
            storeParameter(globals, name, value.ToString());
        }

        public static void storeParameter(Globals globals, string name, string value)
        {
            globals[name] = value;
            globals.set_VariablePersists(value, true);
        }

        public static string loadParameter(Globals globals, string name, string defaultValue)
        {
            return !globals.get_VariableExists(name) ? defaultValue : globals[name].ToString();
        }
    }
}
