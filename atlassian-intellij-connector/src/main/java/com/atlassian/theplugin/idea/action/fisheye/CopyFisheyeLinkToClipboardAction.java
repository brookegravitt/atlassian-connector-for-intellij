package com.atlassian.theplugin.idea.action.fisheye;

import com.intellij.openapi.ide.CopyPasteManager;

import java.awt.datatransfer.StringSelection;

public class CopyFisheyeLinkToClipboardAction extends FisheyeLinkAction {
	@Override
	protected  void performUrlAction(final String url) {
		CopyPasteManager.getInstance().setContents(new StringSelection(url));
	}
}