using Microsoft.VisualStudio;
using Microsoft.VisualStudio.Shell.Interop;

namespace Atlassian.JiraEditorLinks.EventSinks
{
    public sealed class RunningDocTableEventSink : IVsRunningDocTableEvents
    {
        #region IVsRunningDocTableEvents Members

        public int OnAfterFirstDocumentLock(uint docCookie, uint dwRdtLockType,
                                            uint dwReadLocksRemaining, uint dwEditLocksRemaining)
        {
            return VSConstants.S_OK;
        }

        public int OnBeforeLastDocumentUnlock(uint docCookie, uint dwRdtLockType,
                                              uint dwReadLocksRemaining, uint dwEditLocksRemaining)
        {
            return VSConstants.S_OK;
        }

        public int OnAfterSave(uint docCookie)
        {
            // As the document has been saved the JiraEditorLinkManager needs the
            // opportunity to save the current state of our text markers.
            JiraEditorLinkManager.OnDocumentSaved(docCookie);

            return VSConstants.S_OK;
        }

        public int OnAfterAttributeChange(uint docCookie, uint grfAttribs)
        {
            return VSConstants.S_OK;
        }

        public int OnBeforeDocumentWindowShow(uint docCookie, int fFirstShow, IVsWindowFrame pFrame)
        {
            return VSConstants.S_OK;
        }

        public int OnAfterDocumentWindowHide(uint docCookie, IVsWindowFrame pFrame)
        {
            return VSConstants.S_OK;
        }

        #endregion
    }
}