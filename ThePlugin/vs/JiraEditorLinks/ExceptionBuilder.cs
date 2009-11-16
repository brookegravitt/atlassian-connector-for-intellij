using System;
using System.Globalization;

namespace Atlassian.JiraEditorLinks
{
    internal static class ExceptionBuilder
    {
        /// <summary>
        /// This exception is thrown when a switch statement has no useful behavior
        /// for the default case and raising an error is an appropriate action.
        /// </summary>
        /// <param name="value">The value used in the switch statement.</param>
        public static NotImplementedException UnhandledCaseLabel(object value)
        {
            string message = String.Format(CultureInfo.CurrentCulture, "Unhandled case label", value);
            return new NotImplementedException(message);
        }
    }
}