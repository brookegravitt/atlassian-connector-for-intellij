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

package com.atlassian.theplugin.idea.crucible;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ex.Range;
import com.intellij.openapi.vcs.ex.RangesBuilder;

import java.util.List;


public class ChangeViewer {
	public static void highlightChangesInEditor(Project project, Editor editor, Document referenceDoc, Document displayDoc,
			final String fromRevision,
			final String toRevision) {
		if (editor != null) {
			List<Range> ranges;

			ranges = new RangesBuilder(displayDoc, referenceDoc).getRanges();
			for (Range range : ranges) {
				if (!range.hasHighlighter()) {
					range.setHighlighter(
							getRangeHighligter(project, ranges, range, referenceDoc, displayDoc, fromRevision, toRevision));
				}
			}
		}
	}

	private static synchronized RangeHighlighter getRangeHighligter(final Project project, final List<Range> ranges,
			final Range range,
			final Document referenceDocument, final Document displayDocument, final String fromRevision,
			final String toRevision) {
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
