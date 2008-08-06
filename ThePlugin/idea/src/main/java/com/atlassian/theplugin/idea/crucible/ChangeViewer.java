package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.intellij.codeInsight.hint.EditorFragmentComponent;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diff.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vcs.actions.AbstractVcsAction;
import com.intellij.openapi.vcs.actions.VcsContext;
import com.intellij.openapi.vcs.ex.Range;
import com.intellij.openapi.vcs.ex.RangesBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.HintListener;
import com.intellij.ui.LightweightHint;
import com.intellij.ui.SideBorder2;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Jul 30, 2008
 * Time: 1:57:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChangeViewer {
    private static final Color VERSIONED_COMMENT_BACKGROUND_COLOR = Color.LIGHT_GRAY;
    private static final Color VERSIONED_COMMENT_STRIP_MARK_COLOR = Color.ORANGE;

    private Project project;
    private Editor editor;
    private Document referenceDoc;
    private Document displayDoc;
    private List<Range> ranges;
    private int highlighterCount = 0;


    public ChangeViewer(Project project, Editor editor, Document referenceDoc, Document displayDoc) {
        this.project = project;
        this.editor = editor;
        this.referenceDoc = referenceDoc;
        this.displayDoc = displayDoc;
    }

    public Project getProject() {
        return project;
    }

    private List<Range> getRanges() {
        return this.ranges;
    }

    public void highlightChangesInEditor() {
        if (editor != null) {
            ranges = new RangesBuilder(displayDoc, referenceDoc).getRanges();
            for (Range range : ranges) {
                if (!range.hasHighlighter()) {
                    range.setHighlighter(getRangeHighligter(range));
                }
            }
        }
    }

    public void highlightCommentsInEditor(List<VersionedComment> fileVersionedComments) {
        if (editor != null) {
            TextAttributes textAttributes = new TextAttributes();
            textAttributes.setBackgroundColor(VERSIONED_COMMENT_BACKGROUND_COLOR);
            for (VersionedComment comment : fileVersionedComments) {
                for (int i = comment.getToStartLine() - 1; i < comment.getToEndLine(); i++) {
                    RangeHighlighter rh = editor.getDocument().getMarkupModel(project).addLineHighlighter(
                            i, HighlighterLayer.SELECTION, textAttributes);

                    rh.setErrorStripeTooltip(comment.getAuthor().getDisplayName() + ":" + comment.getMessage());
                    rh.setErrorStripeMarkColor(VERSIONED_COMMENT_STRIP_MARK_COLOR);
                }
            }
        }
    }

    private synchronized RangeHighlighter getRangeHighligter(Range range) {
        int j = range.getOffset1() < displayDoc.getLineCount() ? displayDoc.getLineStartOffset(range.getOffset1()) : displayDoc
                .getTextLength();
        int k = range.getOffset2() < displayDoc.getLineCount() ? displayDoc.getLineStartOffset(range.getOffset2()) : displayDoc
                .getTextLength();

        RangeHighlighter rangehighlighter = displayDoc.getMarkupModel(project)
                .addRangeHighlighter(j, k, HighlighterLayer.FIRST - 1, null, HighlighterTargetArea.LINES_IN_RANGE);
        highlighterCount++;
        rangehighlighter.setLineMarkerRenderer(createRenderer(range));
        rangehighlighter.setEditorFilter(MarkupEditorFilterFactory.createIsNotDiffFilter());
        return rangehighlighter;
    }

    private LineMarkerRenderer createRenderer(final Range range) {
        return new ActiveGutterRenderer() {

            private TextAttributesKey getDiffColor(Range range) {
                switch (range.getType()) {
                    case 2:
                        return DiffColors.DIFF_INSERTED;

                    case 3:
                        return DiffColors.DIFF_DELETED;

                    case 1:
                        return DiffColors.DIFF_MODIFIED;
					default:
						return null;
				}
            }

            public void paint(Editor editor, Graphics g, Rectangle r) {
                paintGutterFragment(editor, g, r, getDiffColor(range));

            }

            private void paintGutterFragment(Editor editor, Graphics g, Rectangle r, TextAttributesKey diffAttributeKey) {

                EditorGutterComponentEx gutter = ((EditorEx) editor).getGutterComponentEx();
                g.setColor(editor.getColorsScheme().getAttributes(diffAttributeKey).getBackgroundColor());
                int endX = gutter.getWhitespaceSeparatorOffset();
                int x = r.x + r.width - 2;
                int width = endX - x;
                if (r.height > 0) {
                    g.fillRect(x, r.y + 2, width, r.height - 4);
                    g.setColor(gutter.getFoldingColor(false));
                    UIUtil.drawLine(g, x, r.y + 2, x + width, r.y + 2);
                    UIUtil.drawLine(g, x, r.y + 2, x, r.y + r.height - 3);
                    UIUtil.drawLine(g, x, r.y + r.height - 3, x + width, r.y + r.height - 3);
                } else {
                    int[] xPoints = new int[]{x,
                            x,
                            x + width - 1};
                    int[] yPoints = new int[]{r.y - 4,
                            r.y + 4,
                            r.y};
                    g.fillPolygon(xPoints, yPoints, 3);

                    g.setColor(gutter.getFoldingColor(false));
                    g.drawPolygon(xPoints, yPoints, 3);
                }
            }

            public void doAction(Editor editor, MouseEvent e) {
                e.consume();
                JComponent comp = (JComponent) e.getComponent(); // shall be EditorGutterComponent, cast is safe.
                JLayeredPane layeredPane = comp.getRootPane().getLayeredPane();
                Point point = SwingUtilities
                        .convertPoint(comp, ((EditorEx) editor).getGutterComponentEx().getWidth(), e.getY(), layeredPane);
                showActiveHint(range, editor, point);
            }
        };
    }

    private Range getNextRange(Range range) {
        int j = ranges.indexOf(range);
        if (j == ranges.size() - 1) {
            return null;
        } else {
            return ranges.get(j + 1);
        }
    }

    private Range getPrevRange(Range range) {
        int j = ranges.indexOf(range);
        if (j == 0) {
            return null;
        } else {
            return ranges.get(j - 1);
        }
    }

    public Range getNextRange(int j) {
        for (Iterator iterator = getRanges().iterator(); iterator.hasNext();) {
            Range range = (Range) iterator.next();
            if (range.getOffset2() >= j) {
                return range;
            }
        }

        return null;
    }

    public Range getPrevRange(int j) {
        for (ListIterator listiterator = getRanges().listIterator(getRanges().size()); listiterator.hasPrevious();) {
            Range range = (Range) listiterator.previous();
            if (range.getOffset1() <= j) {
                return range;
            }
        }

        return null;
    }

    public void moveToRange(final Range range, final Editor editor) {
        final int firstOffset = getDisplayDocument()
                .getLineStartOffset(Math.min(range.getOffset1(), getDisplayDocument().getLineCount() - 1));
        editor.getCaretModel().moveToOffset(firstOffset);
        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
        editor.getScrollingModel().runActionOnScrollingFinished(new Runnable() {

            public void run() {
                java.awt.Point point = editor.visualPositionToXY(editor.offsetToVisualPosition(firstOffset));
                JComponent jcomponent = editor.getContentComponent();
                javax.swing.JLayeredPane jlayeredpane = jcomponent.getRootPane().getLayeredPane();
                point = SwingUtilities.convertPoint(jcomponent, 0, point.y, jlayeredpane);
                showActiveHint(range, editor, point);
            }
        });
    }


    private String getReferenceVirtualFileName() {
        VirtualFile virtualfile = getReferenceVirtualFile();
        if (virtualfile == null) {
            return "";
        } else {
            return virtualfile.getName();
        }
    }

    public Document getDisplayDocument() {
        return displayDoc;
    }

    public Document getReferenceDocument() {
        return referenceDoc;
    }

    public VirtualFile getReferenceVirtualFile() {
        return FileDocumentManager.getInstance().getFile(getReferenceDocument());
    }

    public VirtualFile getDisplayVirtualFile() {
        return FileDocumentManager.getInstance().getFile(getDisplayDocument());
    }

    private TextRange getDisplayTextRange(Range range) {
        return getTextRange(range.getType(), range.getOffset1(), range.getOffset2(), (byte) 3, getDisplayDocument());
    }

    private TextRange getReferenceTextRange(Range range) {
        return getTextRange(range.getType(), range.getUOffset1(), range.getUOffset2(), (byte) 2, getReferenceDocument());
    }


    private TextRange getTextRange(byte byte0, int j, int k, byte byte1, Document document) {
        if (byte0 == byte1) {
            int l;
            if (j == 0) {
                l = 0;
            } else {
                l = document.getLineEndOffset(j - 1);
            }
            return new TextRange(l, l);
        }
        int i1 = document.getLineStartOffset(j);
        int j1 = document.getLineEndOffset(k - 1);
        if (i1 > 0) {
            i1--;
            j1--;
        }
        return new TextRange(i1, j1);
    }


    public void showActiveHint(Range range, final Editor anEditor, Point point) {

        DefaultActionGroup group = new DefaultActionGroup();

        final AnAction globalShowNextAction = ActionManager.getInstance().getAction("VcsShowNextChangeMarker");
        final AnAction globalShowPrevAction = ActionManager.getInstance().getAction("VcsShowPrevChangeMarker");

        final ShowPrevChangeMarkerAction localShowPrevAction = new ShowPrevChangeMarkerAction(getPrevRange(range), this,
                anEditor);
        final ShowNextChangeMarkerAction localShowNextAction = new ShowNextChangeMarkerAction(getNextRange(range), this,
                anEditor);

        JComponent editorComponent = anEditor.getComponent();

        localShowNextAction.registerCustomShortcutSet(localShowNextAction.getShortcutSet(), editorComponent);
        localShowPrevAction.registerCustomShortcutSet(localShowPrevAction.getShortcutSet(), editorComponent);

        group.add(localShowPrevAction);
        group.add(localShowNextAction);

        localShowNextAction.copyFrom(globalShowNextAction);
        localShowPrevAction.copyFrom(globalShowPrevAction);

        group.add(new ShowDiffAction(this, range));

        final List<AnAction> actionList = (List<AnAction>) editorComponent.getClientProperty(AnAction.ourClientProperty);

        actionList.remove(globalShowPrevAction);
        actionList.remove(globalShowNextAction);

        JComponent toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.FILEHISTORY_VIEW_TOOLBAR, group, true)
                .getComponent();

        final Color background = ((EditorEx) anEditor).getBackroundColor();
        final Color foreground = anEditor.getColorsScheme().getColor(EditorColors.CARET_COLOR);
        toolbar.setBackground(background);

        toolbar.setBorder(
                new SideBorder2(foreground, foreground, range.getType() != Range.INSERTED ? null : foreground, foreground, 1));

        JPanel component = new JPanel(new BorderLayout());
        component.setOpaque(false);

        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setOpaque(false);
        toolbarPanel.add(toolbar, BorderLayout.WEST);
        component.add(toolbarPanel, BorderLayout.NORTH);

        if (range.getType() != Range.INSERTED) {
            DocumentEx doc = (DocumentEx) referenceDoc;
            EditorImpl uEditor = new EditorImpl(doc, true, project);
            EditorHighlighter highlighter = HighlighterFactory
                    .createHighlighter(project, getReferenceVirtualFileName());
            uEditor.setHighlighter(highlighter);

            EditorFragmentComponent editorFragmentComponent =
                    EditorFragmentComponent
                            .createEditorFragmentComponent(uEditor, range.getUOffset1(), range.getUOffset2(), false, false);

            component.add(editorFragmentComponent, BorderLayout.CENTER);
        }

        LightweightHint lightweightHint = new LightweightHint(component);
        lightweightHint.addHintListener(new HintListener() {
            public void hintHidden(EventObject event) {
                actionList.remove(localShowPrevAction);
                actionList.remove(localShowNextAction);
                actionList.add(globalShowPrevAction);
                actionList.add(globalShowNextAction);
            }
        });

        HintManager.getInstance()
                .showEditorHint(lightweightHint, anEditor, point, HintManager.HIDE_BY_ANY_KEY | HintManager.HIDE_BY_TEXT_CHANGE
						| HintManager.HIDE_BY_OTHER_HINT | HintManager.HIDE_BY_SCROLLING,
                        -1, false);
    }

    private abstract class ShowChangeMarkerAction extends AbstractVcsAction {
        protected Range range;
        protected ChangeViewer highlighter;
        protected Editor editor;

        protected abstract Range extractRange(ChangeViewer highlighter, int i, Editor editor);

        public ShowChangeMarkerAction(final Range range, final ChangeViewer highlighter, final Editor editor) {
            this.range = range;
            this.highlighter = highlighter;
            this.editor = editor;
        }

        protected boolean forceSyncUpdate(AnActionEvent anactionevent) {
            return true;
        }

        private boolean checkPosition(VcsContext vcscontext) {
            return range != null;
        }

        protected void update(VcsContext vcscontext, Presentation presentation) {
            presentation.setEnabled(checkPosition(vcscontext));
        }

        protected void actionPerformed(VcsContext vcscontext) {
            highlighter.moveToRange(range, editor);
        }
    }

    private class ShowPrevChangeMarkerAction extends ShowChangeMarkerAction {

        public ShowPrevChangeMarkerAction(Range range, ChangeViewer highlighter, Editor editor) {
            super(range, highlighter, editor);
        }

        protected Range extractRange(ChangeViewer highlighter, int i, Editor editor) {
            return highlighter.getPrevRange(i);
        }
    }

    private class ShowNextChangeMarkerAction extends ShowChangeMarkerAction {

        public ShowNextChangeMarkerAction(Range range, ChangeViewer highlighter, Editor editor) {
            super(range, highlighter, editor);
        }

        protected Range extractRange(ChangeViewer highlighter, int i, Editor editor) {
            return highlighter.getNextRange(i);
        }
    }

    private class ShowDiffAction extends AnAction {
        protected final ChangeViewer myHighlighter;
        protected final Range myRange;

        public void update(final AnActionEvent e) {
            e.getPresentation().setEnabled(checkModified() || checkDeleted());

        }

        private boolean checkDeleted() {
            return myRange.getType() == 3;
        }

        private boolean checkModified() {
            return myRange.getType() == 1;
        }

        public void actionPerformed(AnActionEvent anactionevent) {
            DiffManager.getInstance().getDiffTool().show(prepareDiffRequest());
        }

        private DiffRequest prepareDiffRequest() {
            return new DiffRequest(myHighlighter.getProject()) {

                public DiffContent[] getContents() {
                    return (new DiffContent[]{
                            getDiffContent(myHighlighter.getReferenceDocument(), myHighlighter.getReferenceTextRange(myRange),
									myHighlighter.getReferenceVirtualFile()),
                            getDiffContent(myHighlighter.getDisplayDocument(), myHighlighter.getDisplayTextRange(myRange),
									myHighlighter.getDisplayVirtualFile())
                    });
                }

                public String[] getContentTitles() {
                    return (new String[]{
                            VcsBundle.message("diff.content.title.up.to.date", new Object[0]),
                            VcsBundle.message("diff.content.title.current.range", new Object[0])
                    });
                }

                public String getWindowTitle() {
                    return VcsBundle.message("dialog.title.diff.for.range", new Object[0]);
                }
            };
        }

        private DiffContent getDiffContent(Document document, TextRange textrange, VirtualFile virtualfile) {
            DocumentContent documentcontent = new DocumentContent(project, document);
            return new FragmentContent(documentcontent, textrange, myHighlighter.getProject(), virtualfile);
        }

        public ShowDiffAction(ChangeViewer highlighter, Range range) {
            super(VcsBundle.message("action.name.show.difference"), null, IconLoader.getIcon("/actions/diff.png"));
            myHighlighter = highlighter;
            myRange = range;
        }
    }
}
