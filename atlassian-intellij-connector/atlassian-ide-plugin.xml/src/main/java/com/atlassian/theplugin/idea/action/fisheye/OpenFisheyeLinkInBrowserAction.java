package com.atlassian.theplugin.idea.action.fisheye;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.editor.Editor;

public class OpenFisheyeLinkInBrowserAction extends FisheyeLinkAction {
	@Override
	protected void performUrlAction(final String url, final Editor editor) {
		BrowserUtil.launchBrowser(url);
	}
}