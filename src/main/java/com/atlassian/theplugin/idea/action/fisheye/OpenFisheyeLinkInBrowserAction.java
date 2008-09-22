package com.atlassian.theplugin.idea.action.fisheye;

import com.intellij.ide.BrowserUtil;

public class OpenFisheyeLinkInBrowserAction extends FisheyeLinkAction {
	@Override
	protected void performUrlAction(final String url) {
		BrowserUtil.launchBrowser(url);
	}
}