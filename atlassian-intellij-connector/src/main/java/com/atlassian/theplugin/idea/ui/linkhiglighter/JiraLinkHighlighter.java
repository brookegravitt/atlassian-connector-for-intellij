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
package com.atlassian.theplugin.idea.ui.linkhiglighter;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * User: pmaruszak
 */
public class JiraLinkHighlighter {
	private final Project project;
	private final VirtualFile newFile;
	private final PsiFile psiFile;
	private final Editor editor;
	private final JiraEditorLinkParser jiraEditorLinkParser;
	private final List<JiraURLTextRange> ranges = new ArrayList<JiraURLTextRange>();
	private EditorInputHandler inputEditorInputHandler = null;
	private DocumentAdapter docAdapter = null;
	private boolean isListening = false;

	public JiraLinkHighlighter(@NotNull final Project project, final VirtualFile newFile,
			final PsiFile psiFile,
			final Editor editor, JiraEditorLinkParser jiraEditorLinkParser) {
		this.project = project;


		this.newFile = newFile;
		this.psiFile = psiFile;
		this.editor = editor;
		this.jiraEditorLinkParser = jiraEditorLinkParser;
	}

	public void stopListening() {
		if (isListening) {
			editor.removeEditorMouseListener(inputEditorInputHandler);
			editor.removeEditorMouseMotionListener(inputEditorInputHandler);
			editor.getContentComponent().removeKeyListener(inputEditorInputHandler);
			editor.getDocument().removeDocumentListener(docAdapter);
			isListening = false;
		}
	}

	public void removeAllRanges() {
		for (JiraURLTextRange range : ranges) {
			range.setActive(false);
		}
		highlightLink(ranges);
		ranges.clear();
	}

	public void startListeninig() {
		listenOnDocument();
		listenOnInput();
		isListening = true;
	}

	public void reparseAll() {
		int length = editor.getDocument().getCharsSequence().length();
		parseDocumentRanges(0, length, length);
	}


	public void checkComments() {
		List<JiraURLTextRange> newRanges = new ArrayList<JiraURLTextRange>();
		for (JiraURLTextRange range : ranges) {
			boolean isComment = JiraEditorLinkParser.isComment(psiFile, range.getStartOffset());
			if (isComment != range.isActive()) {
				range.setActive(isComment);
				newRanges.add(range);
			}
		}

		if (!newRanges.isEmpty()) {
			highlightLink(newRanges);
		}

	}

	private void listenOnInput() {
		inputEditorInputHandler = new EditorInputHandler(IdeaHelper.getProjectCfgManager(project), project, editor, psiFile,
				jiraEditorLinkParser);
		editor.getContentComponent().addKeyListener(inputEditorInputHandler);
		editor.addEditorMouseMotionListener(inputEditorInputHandler);
		editor.addEditorMouseListener(inputEditorInputHandler);
	}

	private void listenOnDocument() {
		final Document doc = editor.getDocument();
		docAdapter = new DocumentAdapter() {

			public void beforeDocumentChange(final DocumentEvent event) {
				if (event.getNewLength() < event.getOldLength()) { //deletion
					forgetDocumentRanges(event.getOffset(), event.getOffset() + event.getOldLength());
				}
				super.beforeDocumentChange(event);
			}

			public void documentChanged(final DocumentEvent event) {
				parseDocumentRanges(event.getOffset(), event.getOldLength(), event.getNewLength());
				super.documentChanged(event);
			}
		};

		doc.addDocumentListener(docAdapter);
		//@todo: implement unregistering listener  

	}

	private void parseDocumentRanges(final int offset, final int oldLength, final int newLength) {
		List<JiraURLTextRange> newRanges = jiraEditorLinkParser.getJiraURLTextRange(editor, getStartLineOffset(editor, offset),
				getEndLineOffset(editor, offset + newLength));

		List<JiraURLTextRange> rangesToForget = new ArrayList<JiraURLTextRange>();
		List<JiraURLTextRange> intersectingRanges = getIntersectingRanges(offset, oldLength);

		if (newLength != oldLength) {
			//shift ranges
			for (JiraURLTextRange range : ranges) {
				if (range.getStartOffset() >= offset) {
					range.shift(newLength - oldLength);
				}
			}
		}

		for (JiraURLTextRange irange : intersectingRanges) {
			boolean oldInNew = false;
			for (JiraURLTextRange newRange : newRanges) {
				if (irange.equals(newRange)) {
					oldInNew = true;
					break;
				}
			}

			if (!oldInNew) {
				rangesToForget.add(irange);
			}
		}

		if (!rangesToForget.isEmpty()) {
			for (JiraURLTextRange range : rangesToForget) {
				range.setActive(false);
				ranges.remove(range);
			}
			highlightLink(rangesToForget);
		}

		if (!newRanges.isEmpty()) {
			List<JiraURLTextRange> rangesToRemember = new ArrayList<JiraURLTextRange>();
			for (JiraURLTextRange range : newRanges) {
				if (!ranges.contains(range)) {
					rangesToRemember.add(range);
				}
			}

			if (!rangesToRemember.isEmpty()) {
				ranges.addAll(rangesToRemember);
				highlightLink(ranges);

			}
		}

	}

	private List<JiraURLTextRange> getIntersectingRanges(final int offset, final int oldLength) {
		List<JiraURLTextRange> intersecting = new ArrayList<JiraURLTextRange>();
		for (JiraURLTextRange range : ranges) {
			if (Math.max(range.getStartOffset(), offset) <= Math.min(range.getEndOffset(), offset + oldLength)) {
				intersecting.add(range);
			}
		}

		return intersecting;
	}

	public static int getEndLineOffset(final Editor editor, final int o) {
		final int textLength = editor.getDocument().getTextLength();
		int offset = Math.max(Math.min(o, Math.max(0, textLength)), Math.min(0, textLength));
		int lineCount = editor.getDocument().getLineCount();
		int lineNumber = 0;

		if (offset < 0) {
			lineNumber = 0;
		} else if (offset < textLength) {
			lineNumber = editor.getDocument().getLineNumber(offset);
		} else {
			lineNumber = lineCount - 1;
		}

		if (lineNumber >= 0 && lineNumber < lineCount) {
			return editor.getDocument().getLineEndOffset(lineNumber);
		} else {
			return 0;
		}
	}

	private void highlightLink(final List<JiraURLTextRange> rangesList) {
		for (RangeHighlighter h : editor.getMarkupModel().getAllHighlighters()) {
			JiraURLTextRange jiraRange = JiraURLTextRange.getFrom(h);
			if (jiraRange != null && rangesList.contains(jiraRange) && !jiraRange.isActive()) {
				editor.getMarkupModel().removeHighlighter(h);

			}
		}

		for (JiraURLTextRange urlTextRange : rangesList) {
			if (urlTextRange.isActive()) {
				urlTextRange.addLinkHighlighter(editor);
			}
		}
	}

	public static int getStartLineOffset(final Editor editor, final int o) {
		final int textLength = editor.getDocument().getTextLength();
		int offset = Math.max(Math.min(o, Math.max(0, textLength)), Math.min(0, textLength));
		int lineCount = editor.getDocument().getLineCount();
		int lineNumber = 0;

		if (offset < 0) {
			lineNumber = 0;
		} else if (offset < textLength) {
			lineNumber = editor.getDocument().getLineNumber(offset);
		} else {
			lineNumber = lineCount - 1;
		}

		if (lineNumber >= 0 && lineNumber < lineCount) {
			return editor.getDocument().getLineStartOffset(lineNumber);
		} else {
			return 0;
		}
	}

	private void forgetDocumentRanges(final int start, final int end) {
		List<JiraURLTextRange> forgetRangesList = new ArrayList<JiraURLTextRange>();
		for (JiraURLTextRange urlRange : ranges) {
			if (Math.max(start, urlRange.getStartOffset()) < Math.min(end, urlRange.getEndOffset())) {
				forgetRangesList.add(urlRange);
				urlRange.setActive(false);
			}

			if (!forgetRangesList.isEmpty()) {
				highlightLink(forgetRangesList);
			}

		}

		if (!forgetRangesList.isEmpty()) {
			ranges.removeAll(forgetRangesList);
		}

	}
}
