package com.atlassian.theplugin.idea.fisheye;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.TextAnnotationGutterProvider;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorFontType;

import java.awt.Color;
import java.util.List;

public class FisheyeAnnotationProvider implements TextAnnotationGutterProvider {
	private final String url;

	public FisheyeAnnotationProvider(final String url) {
		this.url = url;
	}

	public String getLineText(final int line, final Editor editor) {
		return Integer.toString(line + 1);
	}

	public String getToolTip(final int line, final Editor editor) {
		return null;
	}

	public EditorFontType getStyle(final int line, final Editor editor) {
		return EditorFontType.ITALIC;
	}

	public List<AnAction> getPopupActions(final Editor editor) {
		return null;
	}

	public void gutterClosed() {
	}

    public ColorKey getColor(int i, Editor editor) {
        return null;
    }

    public Color getBgColor(int i, Editor editor) {
        return null;
    }

    public List<AnAction> getPopupActions(int i, Editor editor) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
