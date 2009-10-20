package com.atlassian.theplugin.idea.fisheye;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.editor.EditorGutterAction;

import java.awt.*;

public class FisheyeEditorAction implements EditorGutterAction {
	private final String url;

	public FisheyeEditorAction(final String url) {
		this.url = url;
	}

	public void doAction(final int lineNum) {
		if (url != null && url.indexOf("#l") > 0) {
			String newUrl = url.substring(0, url.indexOf("#"));
			newUrl += "#l" + (lineNum + 1);
			BrowserUtil.launchBrowser(newUrl);
		}
	}

	public Cursor getCursor(final int lineNum) {
		return null;
	}
}
