/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.ex.Range;
import com.intellij.openapi.vcs.ex.RangesBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;


public final class ChangeViewer {

	public static final Key<Collection<RangeHighlighter>> CRUCIBLE_RANGES = Key.create("crucible_ranges");
//	private static final Key<DocumentListener> DOCUMENT_LISTENER_KEY = Key.create("crucible_document_change_listener");


	private ChangeViewer() {
	}

	public static void highlightChangesInEditor(final Project project, /*Editor editor,*/
			Document referenceDoc, final Document displayDoc, final String fromRevision, final String toRevision) {

		Collection<RangeHighlighter> registeredHighlighters = displayDoc.getUserData(CRUCIBLE_RANGES);

		if (registeredHighlighters == null) {
			displayDoc.addDocumentListener(new DocumentListener() {
				public void beforeDocumentChange(final DocumentEvent event) {
				}

				public void documentChanged(final DocumentEvent event) {
					ApplicationManager.getApplication().invokeLater(new Runnable() {
						public void run() {
							ChangeViewer.removeHighlighters(project, displayDoc);
						}
					});
				}
			});

//			removeHighlighters(project, displayDoc);
			List<Range> ranges = new RangesBuilder(displayDoc, referenceDoc).getRanges();
			Collection<RangeHighlighter> rangeHighlighters = MiscUtil.buildArrayList();
			for (Range range : ranges) {
				if (!range.hasHighlighter()) {
					final RangeHighlighter highligter = getRangeHighligter(project, ranges, range, referenceDoc, displayDoc,
							fromRevision, toRevision);
					rangeHighlighters.add(highligter);
					range.setHighlighter(highligter);
				}
			}
			displayDoc.putUserData(CRUCIBLE_RANGES, rangeHighlighters);
		}
	}

	public static void removeHighlightersInEditors(@NotNull final Project project) {
		for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
            if (!project.equals(editor.getProject())) {
                continue;
            }
			Document document = editor.getDocument();
			VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
			if (virtualFile != null) {
				Collection<RangeHighlighter> ranges = document.getUserData(ChangeViewer.CRUCIBLE_RANGES);
				document.putUserData(ChangeViewer.CRUCIBLE_RANGES, null);
				if (ranges == null) {
					continue;
				}
				for (RangeHighlighter rangeHighlighter : ranges) {
                    // see PL-1566 - attempt at blind fix, so sorry that it looks so ugly
                    //
                    // It looks like MarkupModelImpl's internal bookkeeping gets b0rked sometimes.
                    // Decompiling removeHilighter() method shows something like this:
                    //
                    // public void removeHighlighter(RangeHighlighter rangehighlighter)
                    // {
                    //    boolean flag = d.remove(rangehighlighter);
                    //    a.assertTrue(flag);
                    //    ...
                    // }
                    //
                    // No way to test if "d" contains the hilighter prior to attempting to remove it though,
                    // because the containsHighlighter() method of MarkupModelImpl, which could tell us that,
                    // is not exposed in the MarkupModel interface :(
                    try {
					    document.getMarkupModel(project).removeHighlighter(rangeHighlighter);
                    } catch (Throwable t) {
                        LoggerImpl.getInstance().error(t);
                    }
				}
			}
		}
	}

	private static void removeHighlighters(Project project, Document displayDoc) {
		Collection<RangeHighlighter> ranges = displayDoc.getUserData(CRUCIBLE_RANGES);
		if (ranges == null) {
			return;
		}
		for (RangeHighlighter rangeHighlighter : ranges) {
			displayDoc.getMarkupModel(project).removeHighlighter(rangeHighlighter);
		}
		displayDoc.putUserData(CRUCIBLE_RANGES, null);
	}

	private static synchronized RangeHighlighter getRangeHighligter(final Project project, final List<Range> ranges,
			final Range range, final Document referenceDocument, final Document displayDocument,
			final String fromRevision, final String toRevision) {

		int j = range.getOffset1() < displayDocument.getLineCount() ? displayDocument
				.getLineStartOffset(range.getOffset1()) : displayDocument
				.getTextLength();
		int k = range.getOffset2() < displayDocument.getLineCount() ? displayDocument
				.getLineStartOffset(range.getOffset2()) : displayDocument
				.getTextLength();

		RangeHighlighter rangehighlighter = displayDocument.getMarkupModel(project)
				.addRangeHighlighter(j, k, HighlighterLayer.FIRST - 1, null, HighlighterTargetArea.LINES_IN_RANGE);
		rangehighlighter.setLineMarkerRenderer(
				createRenderer(project, ranges, range, referenceDocument, displayDocument, fromRevision, toRevision));
		rangehighlighter.setEditorFilter(MarkupEditorFilterFactory.createIsNotDiffFilter());
		return rangehighlighter;
	}

	private static LineMarkerRenderer createRenderer(final Project project, final List<Range> ranges, final Range range,
			final Document referenceDocument, final Document displayDocument, final String fromRevision,
			final String toRevision) {
		return new CrucibleDiffGutterRenderer(project, ranges, range, referenceDocument, displayDocument, fromRevision,
				toRevision);
	}
}
