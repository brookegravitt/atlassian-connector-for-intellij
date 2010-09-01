package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.intellij.codeInsight.hint.EditorFragmentComponent;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diff.DiffColors;
import com.intellij.openapi.diff.DiffContent;
import com.intellij.openapi.diff.DiffManager;
import com.intellij.openapi.diff.DiffRequest;
import com.intellij.openapi.diff.DocumentContent;
import com.intellij.openapi.diff.FragmentContent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.impl.AtlassianEditorImpl;
import com.intellij.openapi.editor.markup.ActiveGutterRenderer;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vcs.actions.AbstractVcsAction;
import com.intellij.openapi.vcs.actions.VcsContext;
import com.intellij.openapi.vcs.ex.Range;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.HintListener;
import com.intellij.ui.LightweightHint;
import com.intellij.ui.SideBorder2;
import com.intellij.util.ui.UIUtil;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class CrucibleDiffGutterRenderer implements ActiveGutterRenderer {
	private static final int TWO_PIXEL = 2;
	private static final int THREE_PIXEL = 3;
	private static final int FOUR_PIXEL = 4;
	private static final int INSERTED_RANGE = 2;
	private static final int DELETED_RANGE = 3;
	private static final int MODIFIED_RANGE = 1;

	private static final int REFERENCE_TEXT_RANGE = 2;
	private static final int DISPLAY_TEXT_RANGE = 3;

	private final Range range;
	private final Document referenceDocument;
	private final Document displayDocument;
	private final String fromRevision;
	private final String toRevision;
	private final Project project;
	private List<Range> ranges;

	public CrucibleDiffGutterRenderer(Project project, final List<Range> ranges, final Range range,
			final Document referenceDocument, final Document displayDocument, final String fromRevision,
			final String toRevision) {
		this.project = project;
		this.ranges = ranges;
		this.range = range;
		this.referenceDocument = referenceDocument;
		this.displayDocument = displayDocument;
		this.fromRevision = fromRevision;
		this.toRevision = toRevision;
	}

	private TextAttributesKey getDiffColor(Range aRange) {
		switch (aRange.getType()) {
			case INSERTED_RANGE:
				return DiffColors.DIFF_INSERTED;

			case DELETED_RANGE:
				return DiffColors.DIFF_DELETED;

			case MODIFIED_RANGE:
				return DiffColors.DIFF_MODIFIED;
			default:
				return null;
		}
	}

	public void paint(Editor anEditor, Graphics g, Rectangle r) {
		paintGutterFragment(anEditor, g, r, getDiffColor(range));

	}

	private void paintGutterFragment(Editor anEditor, Graphics g, Rectangle r, TextAttributesKey diffAttributeKey) {

		EditorGutterComponentEx gutter = ((EditorEx) anEditor).getGutterComponentEx();
		g.setColor(anEditor.getColorsScheme().getAttributes(diffAttributeKey).getBackgroundColor());
		int endX = gutter.getWhitespaceSeparatorOffset();
		int x = r.x + r.width - TWO_PIXEL;
		int width = endX - x;
		if (r.height > 0) {
			g.fillRect(x, r.y + TWO_PIXEL, width, r.height - FOUR_PIXEL);
			g.setColor(gutter.getFoldingColor(false));
			UIUtil.drawLine(g, x, r.y + TWO_PIXEL, x + width, r.y + TWO_PIXEL);
			UIUtil.drawLine(g, x, r.y + TWO_PIXEL, x, r.y + r.height - THREE_PIXEL);
			UIUtil.drawLine(g, x, r.y + r.height - THREE_PIXEL, x + width, r.y + r.height - THREE_PIXEL);
		} else {
			int[] xPoints = new int[]{x,
					x,
					x + width - 1};
			int[] yPoints = new int[]{r.y - FOUR_PIXEL,
					r.y + FOUR_PIXEL,
					r.y};
			g.fillPolygon(xPoints, yPoints, THREE_PIXEL);

			g.setColor(gutter.getFoldingColor(false));
			g.drawPolygon(xPoints, yPoints, THREE_PIXEL);
		}
	}

	public void doAction(Editor anEditor, MouseEvent e) {
		e.consume();
		JComponent comp = (JComponent) e.getComponent(); // shall be EditorGutterComponent, cast is safe.
		JLayeredPane layeredPane = comp.getRootPane().getLayeredPane();
		Point point = SwingUtilities
				.convertPoint(comp, ((EditorEx) anEditor).getGutterComponentEx().getWidth(), e.getY(), layeredPane);
		showActiveHint(range, anEditor, point);
	}

	public boolean canDoAction(MouseEvent event) {
		return true;
	}

	public Range getLastRange() {
		if (ranges.size() > 0) {
			return ranges.get(ranges.size() - 1);
		}
		return null;
	}

	public Range getNextRange(Range aRange) {
		int j = ranges.indexOf(aRange);
		if (j == ranges.size() - 1) {
			return null;
		} else {
			return ranges.get(j + 1);
		}
	}

	public Range getPrevRange(Range aRange) {
		int j = ranges.indexOf(aRange);
		if (j <= 0) {
			return null;
		} else {
			return ranges.get(j - 1);
		}
	}

	public Range getNextRange(int j) {
		for (Iterator<Range> iterator = ranges.iterator(); iterator.hasNext();) {
			Range myRange = iterator.next();
			if (myRange.getOffset2() >= j) {
				return myRange;
			}
		}

		return null;
	}

	public Range getPrevRange(int j) {
		for (ListIterator<Range> listiterator = ranges.listIterator(ranges.size()); listiterator.hasPrevious();) {
			Range myRange = listiterator.previous();
			if (myRange.getOffset1() <= j) {
				return myRange;
			}
		}

		return null;
	}

	public VirtualFile getReferenceVirtualFile() {
		return FileDocumentManager.getInstance().getFile(referenceDocument);
	}

	public VirtualFile getDisplayVirtualFile() {
		return FileDocumentManager.getInstance().getFile(displayDocument);
	}


	private TextRange getDisplayTextRange(Range aRange) {
		return getTextRange(aRange.getType(), aRange.getOffset1(), aRange.getOffset2(), (byte) DISPLAY_TEXT_RANGE,
				displayDocument);
	}

	private TextRange getReferenceTextRange(Range aRange) {
		return getTextRange(aRange.getType(), aRange.getUOffset1(), aRange.getUOffset2(), (byte) REFERENCE_TEXT_RANGE,
				referenceDocument);
	}

	private String getReferenceVirtualFileName() {
		VirtualFile virtualfile = getReferenceVirtualFile();
		if (virtualfile == null) {
			return "";
		} else {
			return virtualfile.getName();
		}
	}

	private static TextRange getTextRange(byte byte0, int j, int k, byte byte1, Document document) {
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


	public void moveToRange(final Range aRange, final Editor anEditor,
							final Document aDisplayDocument, final boolean showHint) {
		final int firstOffset = aDisplayDocument
				.getLineStartOffset(Math.min(aRange.getOffset1(), aDisplayDocument.getLineCount() - 1));
		anEditor.getCaretModel().moveToOffset(firstOffset);
		anEditor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
		anEditor.getScrollingModel().runActionOnScrollingFinished(new Runnable() {

			public void run() {
				java.awt.Point point = anEditor.visualPositionToXY(anEditor.offsetToVisualPosition(firstOffset));
				JComponent jcomponent = anEditor.getContentComponent();
				javax.swing.JLayeredPane jlayeredpane = jcomponent.getRootPane().getLayeredPane();
				point = SwingUtilities.convertPoint(jcomponent, 0, point.y, jlayeredpane);
				if (showHint) {
					showActiveHint(aRange, anEditor, point);
				}
			}
		});
	}

	private abstract class ShowChangeMarkerAction extends AbstractVcsAction {
		protected Range range;
		protected Editor editor;
		protected Document document;

		protected abstract Range extractRange(ChangeViewer aHighlighter, int i, Editor anEditor);

		public ShowChangeMarkerAction(final Range range, final Editor anEditor, final Document document) {
			this.range = range;

			this.editor = anEditor;
			this.document = document;
		}

		@Override
		protected boolean forceSyncUpdate(AnActionEvent anactionevent) {
			return true;
		}

		private boolean checkPosition(VcsContext vcscontext) {
			return range != null;
		}

		@Override
		protected void update(VcsContext vcscontext, Presentation presentation) {
			presentation.setEnabled(checkPosition(vcscontext));
		}

		@Override
		protected void actionPerformed(VcsContext vcscontext) {
			moveToRange(range, editor, document, true);
		}
	}

	private class ShowPrevChangeMarkerAction extends ShowChangeMarkerAction {

		public ShowPrevChangeMarkerAction(Range range, Editor anEditor, Document document) {
			super(range, anEditor, document);
		}

		@Override
		protected Range extractRange(ChangeViewer aHighlighter, int i, Editor anEditor) {
			return getPrevRange(i);
		}
	}

	private class ShowNextChangeMarkerAction extends ShowChangeMarkerAction {

		public ShowNextChangeMarkerAction(Range range, Editor editor, Document document) {
			super(range, editor, document);
		}

		@Override
		protected Range extractRange(ChangeViewer aHighlighter, int i, Editor anEditor) {
			return getNextRange(i);
		}
	}


	private class ShowDiffAction extends AnAction {
		protected final Range myRange;
		private final Project project;
		private final Document referenceDocument;
		private final Document displayDocument;

		@Override
		public void update(final AnActionEvent e) {
			e.getPresentation().setEnabled(checkModified() || checkDeleted());
		}

		private boolean checkDeleted() {
			return myRange.getType() == DELETED_RANGE;
		}

		private boolean checkModified() {
			return myRange.getType() == MODIFIED_RANGE;
		}

		@Override
		public void actionPerformed(AnActionEvent anactionevent) {
			DiffManager.getInstance().getDiffTool().show(prepareDiffRequest());
		}

		private DiffRequest prepareDiffRequest() {
			return new DiffRequest(project) {

				@Override
				public DiffContent[] getContents() {
					return (new DiffContent[]{
							getDiffContent(referenceDocument, getReferenceTextRange(myRange),
									getReferenceVirtualFile()),
							getDiffContent(displayDocument, getDisplayTextRange(myRange),
									getDisplayVirtualFile())
					});
				}

				@Override
				public String[] getContentTitles() {
					return (new String[]{
							VcsBundle.message("diff.content.title.repository.version", new Object[]{fromRevision}),
							VcsBundle.message("diff.content.title.repository.version", new Object[]{toRevision})
					});
				}

				@Override
				public String getWindowTitle() {
					return VcsBundle.message("dialog.title.diff.for.range", new Object[0]);
				}
			};
		}

		private DiffContent getDiffContent(Document document, TextRange textrange, VirtualFile virtualfile) {
			DocumentContent documentcontent = new DocumentContent(project, document);
			return new FragmentContent(documentcontent, textrange, project, virtualfile);
		}

		public ShowDiffAction(Project project, Document referenceDocument, Document displayDocument, Range range) {
			super(VcsBundle.message("action.name.show.difference"), null, IconLoader.getIcon("/actions/diff.png"));
			this.project = project;
			this.referenceDocument = referenceDocument;
			this.displayDocument = displayDocument;
			myRange = range;
		}
	}


	public void showActiveHint(Range aRange, final Editor anEditor, Point point) {

		DefaultActionGroup group = new DefaultActionGroup();

		final AnAction globalShowNextAction = ActionManager.getInstance().getAction("VcsShowNextChangeMarker");
		final AnAction globalShowPrevAction = ActionManager.getInstance().getAction("VcsShowPrevChangeMarker");

		final ShowPrevChangeMarkerAction localShowPrevAction = new ShowPrevChangeMarkerAction(getPrevRange(aRange),
				anEditor, displayDocument);
		final ShowNextChangeMarkerAction localShowNextAction = new ShowNextChangeMarkerAction(getNextRange(aRange),
				anEditor, displayDocument);

		JComponent editorComponent = anEditor.getComponent();

		localShowNextAction.registerCustomShortcutSet(localShowNextAction.getShortcutSet(), editorComponent);
		localShowPrevAction.registerCustomShortcutSet(localShowPrevAction.getShortcutSet(), editorComponent);

		group.add(localShowPrevAction);
		group.add(localShowNextAction);

		localShowNextAction.copyFrom(globalShowNextAction);
		localShowPrevAction.copyFrom(globalShowPrevAction);

		group.add(new ShowDiffAction(project, referenceDocument, displayDocument, aRange));

		@SuppressWarnings("unchecked")
		final java.util.List<AnAction> actionList = (java.util.List<AnAction>) editorComponent
				.getClientProperty(AnAction.ourClientProperty);

		actionList.remove(globalShowPrevAction);
		actionList.remove(globalShowNextAction);

		JComponent toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.FILEHISTORY_VIEW_TOOLBAR, group, true)
				.getComponent();

		final Color background = IdeaVersionFacade.getInstance().getEditorBackgroundColor((EditorEx) anEditor);
		final Color foreground = anEditor.getColorsScheme().getColor(EditorColors.CARET_COLOR);
		toolbar.setBackground(background);

		toolbar.setBorder(
				new SideBorder2(foreground, foreground, aRange.getType() != Range.INSERTED ? null : foreground, foreground, 1));

		JPanel component = new JPanel(new BorderLayout());
		component.setOpaque(false);

		JPanel toolbarPanel = new JPanel(new BorderLayout());
		toolbarPanel.setOpaque(false);
		toolbarPanel.add(toolbar, BorderLayout.WEST);
		component.add(toolbarPanel, BorderLayout.NORTH);

		if (aRange.getType() != Range.INSERTED) {
            //todo uncomment if IDEa x IS FIXED
			DocumentEx doc = (DocumentEx) referenceDocument;
			AtlassianEditorImpl uEditor = new AtlassianEditorImpl(doc, true, project);
			EditorHighlighter highlighter = HighlighterFactory
					.createHighlighter(project, getReferenceVirtualFileName());
			uEditor.getEditor().setHighlighter(highlighter);

			EditorFragmentComponent editorFragmentComponent =
					EditorFragmentComponent
							.createEditorFragmentComponent(uEditor.getEditor(), aRange.getUOffset1(),
                                    aRange.getUOffset2(), false, false);

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

		IdeaVersionFacade.getInstance().showEditorHints(lightweightHint, anEditor, point);

	}
}
