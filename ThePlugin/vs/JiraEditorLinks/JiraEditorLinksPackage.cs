// VsPkg.cs : Implementation of JiraEditorLinks
//

using System;
using System.Diagnostics;
using System.Diagnostics.CodeAnalysis;
using System.Globalization;
using System.Reflection;
using System.Runtime.InteropServices;
using System.ComponentModel.Design;
using Atlassian.JiraEditorLinks.EventSinks;
using Atlassian.JiraEditorLinks.Markers;
using Microsoft.VisualStudio;
using Microsoft.VisualStudio.TextManager.Interop;
using Microsoft.Win32;
using Microsoft.VisualStudio.Shell.Interop;
using Microsoft.VisualStudio.OLE.Interop;
using Microsoft.VisualStudio.Shell;

namespace Atlassian.JiraEditorLinks
{
    /// <summary>
    /// This is the class that implements the package exposed by this assembly.
    ///
    /// The minimum requirement for a class to be considered a valid package for Visual Studio
    /// is to implement the IVsPackage interface and register itself with the shell.
    /// This package uses the helper classes defined inside the Managed Package Framework (MPF)
    /// to do it: it derives from the Package class that provides the implementation of the 
    /// IVsPackage interface and uses the registration attributes defined in the framework to 
    /// register itself and its components with the shell.
    /// </summary>
    // This attribute tells the registration utility (regpkg.exe) that this class needs
    // to be registered as package.
    [PackageRegistration(UseManagedResourcesOnly = true)]
    // A Visual Studio component can be registered under different regitry roots; for instance
    // when you debug your package you want to register it in the experimental hive. This
    // attribute specifies the registry root to use if no one is provided to regpkg.exe with
    // the /root switch.
    [DefaultRegistryRoot("Software\\Microsoft\\VisualStudio\\9.0")]
    // This attribute is used to register the informations needed to show the this package
    // in the Help/About dialog of Visual Studio.
    [InstalledProductRegistration(false, "#110", "#112", "1.0", IconResourceID = 400)]
    // In order be loaded inside Visual Studio in a machine that has not the VS SDK installed, 
    // package needs to have a valid load key (it can be requested at 
    // http://msdn.microsoft.com/vstudio/extend/). This attributes tells the shell that this 
    // package has a load key embedded in its resources.
    [ProvideLoadKey(MINIMUM_VISUAL_STUDIO_EDITION, PRODUCT_VERSION, PRODUCT_NAME, COMPANY, 104)]
    // This attribute is needed to let the shell know that this package exposes some menus.
    [ProvideMenuResource(1000, 1)]
    [Guid(GuidList.GUID_JIRA_LINKS_PKG_STRING)]
    public sealed class JiraEditorLinksPackage : Package
    {

        public const string MINIMUM_VISUAL_STUDIO_EDITION = "Standard";
        public const string PRODUCT_NAME = "JIRA Editor Links";
        public const string PRODUCT_VERSION = "1.0";
        public const string DESCRIPTION = "Blah";
        public const string COMPANY = "Atlassian";


        /// <summary>
        /// Default constructor of the package.
        /// Inside this method you can place any initialization code that does not require 
        /// any Visual Studio service because at this point the package object is created but 
        /// not sited yet inside Visual Studio environment. The place to do all the other 
        /// initialization is the Initialize method.
        /// </summary>
        public JiraEditorLinksPackage()
        {
            Trace.WriteLine(string.Format(CultureInfo.CurrentCulture, "Entering constructor for: {0}", this.ToString()));
        }


        public new object GetService(Type serviceType)
        {
            return base.GetService(serviceType);
        }

        /////////////////////////////////////////////////////////////////////////////
        // Overriden Package Implementation
        #region Package Members

        private uint solutionEventCookie;
        private IConnectionPoint tmConnectionPoint;
        private uint tmConnectionCookie;
        private uint rdtEventCookie;

        /// <summary>
        /// Initialization of the package; this method is called right after the package is sited, so this is the place
        /// where you can put all the initilaization code that rely on services provided by VisualStudio.
        /// </summary>
        protected override void Initialize()
        {
//            Trace.WriteLine (string.Format(CultureInfo.CurrentCulture, "Entering Initialize() of: {0}", this.ToString()));
//            base.Initialize();
//
//            // Add our command handlers for menu (commands must exist in the .vsct file)
//            OleMenuCommandService mcs = GetService(typeof(IMenuCommandService)) as OleMenuCommandService;
//            if ( null != mcs )
//            {
//                // Create the command for the menu item.
//                CommandID menuCommandID = new CommandID(GuidList.guidJiraEditorLinksCmdSet, (int)PkgCmdIDList.cmdidMyCommand);
//                MenuCommand menuItem = new MenuCommand(MenuItemCallback, menuCommandID );
//                mcs.AddCommand( menuItem );
//            }



            JiraLinkMarkerTypeProvider markerTypeProvider = new JiraLinkMarkerTypeProvider();
            ((IServiceContainer)this).AddService(markerTypeProvider.GetType(), markerTypeProvider, true);

            base.Initialize();

            // Now it's time to initialize our copies of the marker IDs. We need them to be
            // able to create marker instances.
            JiraLinkMarkerTypeProvider.InitializeMarkerIds(this);

            // Advise event sinks. We need to know when a solution is opened and closed
            // (SolutionEventSink), when a document is opened and closed (TextManagerEventSink),
            // and when a document is saved (RunningDocTableEventSink).
            SolutionEventSink solutionEventSink = new SolutionEventSink();
            TextManagerEventSink textManagerEventSink = new TextManagerEventSink();
            RunningDocTableEventSink runningDocTableEventSink = new RunningDocTableEventSink();

            IVsSolution solution = (IVsSolution)GetService(typeof(SVsSolution));
            ErrorHandler.ThrowOnFailure(solution.AdviseSolutionEvents(solutionEventSink, out solutionEventCookie));

            IConnectionPointContainer textManager = (IConnectionPointContainer)GetService(typeof(SVsTextManager));
            Guid interfaceGuid = typeof(IVsTextManagerEvents).GUID;
            textManager.FindConnectionPoint(ref interfaceGuid, out tmConnectionPoint);
            tmConnectionPoint.Advise(textManagerEventSink, out tmConnectionCookie);

            IVsRunningDocumentTable rdt = (IVsRunningDocumentTable)GetService(typeof(SVsRunningDocumentTable));
            ErrorHandler.ThrowOnFailure(rdt.AdviseRunningDocTableEvents(runningDocTableEventSink, out rdtEventCookie));

            // Since we register custom text markers we have to ensure the font and color
            // cache is up-to-date.
            ValidateFontAndColorCacheManagerIsUpToDate();


        }

        protected override void Dispose(bool disposing)
        {
            // Remove solution event notifications.
            IVsSolution solution = (IVsSolution)GetService(typeof(SVsSolution));
            try
            {
                solution.UnadviseSolutionEvents(solutionEventCookie);
            }
            catch
            {
            }

            // Remove text manager event notifications.
            if (tmConnectionPoint != null)
            {
                tmConnectionPoint.Unadvise(tmConnectionCookie);
                tmConnectionPoint = null;
            }

            // Remove running document table (RDT) event notifications.
            // Ignore any errors that might occur since we're shutting down.
            IVsRunningDocumentTable rdt = (IVsRunningDocumentTable)GetService(typeof(SVsRunningDocumentTable));
            try
            {
                rdt.UnadviseRunningDocTableEvents(rdtEventCookie);
            }
            catch (Exception)
            {
            }

            // Forward call to the base class.
            base.Dispose(disposing);
        }

        [SuppressMessage("Microsoft.Design", "CA1031:DoNotCatchGeneralExceptionTypes")]
        private void ValidateFontAndColorCacheManagerIsUpToDate()
        {
            // The Fonts and Colors cache has to be refreshed after each marker change
            // (during development) and of course after install/uninstall.

            // To identify when we have to refresh the cache we store a custom value
            // in the Visual Studio registry hive. Downsides of this approach:
            //
            //     1. The cache is not refreshed until the package is loaded for the
            //        first time. That means that our text markers don't show up in
            //        the Fonts and Colors settings after install. They will only
            //        show up after the first solution has been opened.
            //
            //     2. Since this code is part of the package we can't execute it
            //        after uninstall. That means that the user will see our markers
            //        in the Fonts and Colors settings dialog even after he has
            //        uninstalled the package. That is very ugly!

            IVsFontAndColorCacheManager cacheManager = (IVsFontAndColorCacheManager)GetService(typeof(SVsFontAndColorCacheManager));
            if (cacheManager == null)
                return;

            // We need to know whether we already refreshed the fonts and colors
            // cache to reflect the text markers we registered. We have to do this
            // on a per-user basis.
            bool alreadyInitialized = false;

            try
            {
                const string registryValueName = "InstalledVersion";
                string expectedVersion = Assembly.GetExecutingAssembly().GetName().Version.ToString();

                using (RegistryKey rootKey = UserRegistryRoot)
                using (RegistryKey ourKey = rootKey.CreateSubKey(PRODUCT_NAME))
                {
                    object registryValue = ourKey.GetValue(registryValueName);
                    string initializedVersion = Convert.ToString(registryValue, CultureInfo.InvariantCulture);

                    alreadyInitialized = (initializedVersion == expectedVersion);

                    ourKey.SetValue(registryValueName, expectedVersion, RegistryValueKind.String);
                }
            }
            catch
            {
                // Ignore any errors since it's not a big deal if we can't read
                // this setting. We just always refresh the cache in that case.
            }

            // Actually refresh the Fonts and Colors cache now if we detected we have
            // to do so.
            if (alreadyInitialized) return;

            ErrorHandler.ThrowOnFailure(cacheManager.ClearAllCaches());
            Guid categoryGuid = Guid.Empty;
            ErrorHandler.ThrowOnFailure(cacheManager.RefreshCache(ref categoryGuid));
            categoryGuid = GuidList.GuidFontsAndColorsTextEditor;
            ErrorHandler.ThrowOnFailure(cacheManager.RefreshCache(ref categoryGuid));
        }

        #endregion

        /// <summary>
        /// This function is the callback used to execute a command when the a menu item is clicked.
        /// See the Initialize method to see how the menu item is associated to this function using
        /// the OleMenuCommandService service and the MenuCommand class.
        /// </summary>
//        private void MenuItemCallback(object sender, EventArgs e)
//        {
//            // Show a Message Box to prove we were here
//            IVsUIShell uiShell = (IVsUIShell)GetService(typeof(SVsUIShell));
//            Guid clsid = Guid.Empty;
//            int result;
//            Microsoft.VisualStudio.ErrorHandler.ThrowOnFailure(uiShell.ShowMessageBox(
//                       0,
//                       ref clsid,
//                       "JIRA Editor Links",
//                       string.Format(CultureInfo.CurrentCulture, "Inside {0}.MenuItemCallback()", this.ToString()),
//                       string.Empty,
//                       0,
//                       OLEMSGBUTTON.OLEMSGBUTTON_OK,
//                       OLEMSGDEFBUTTON.OLEMSGDEFBUTTON_FIRST,
//                       OLEMSGICON.OLEMSGICON_INFO,
//                       0,        // false
//                       out result));
//        }

    }
}