using Microsoft.VisualStudio;
using Microsoft.VisualStudio.OLE.Interop;
using Microsoft.VisualStudio.TextManager.Interop;

namespace Atlassian.JiraEditorLinks.EventSinks
{
    public sealed class TextBufferDataEventSink : IVsTextBufferDataEvents
    {
        public IVsTextLines TextLines { get; set; }

        public IConnectionPoint ConnectionPoint { get; set; }

        public uint Cookie { get; set; }

        #region IVsTextBufferDataEvents Members

        public void OnFileChanged(uint grfChange, uint dwFileAttrs)
        {
        }

        public int OnLoadCompleted(int fReload)
        {
            // The load procedure completed. Now we can safely notify the
            // JiraEditorLinkManager about it and so we don't need to listen to these
            // events any more.
            ConnectionPoint.Unadvise(Cookie);
            JiraEditorLinkManager.OnDocumentOpened(TextLines);

            return VSConstants.S_OK;
        }

        #endregion
    }
}