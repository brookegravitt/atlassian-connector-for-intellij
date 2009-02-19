package com.atlassian.theplugin.idea.action.fisheye;

import com.atlassian.theplugin.idea.fisheye.FisheyeAnnotationProvider;
import com.atlassian.theplugin.idea.fisheye.FisheyeEditorAction;
import com.intellij.openapi.editor.Editor;

public class AnnotateAction extends FisheyeLinkAction {

	protected void performUrlAction(final String url, final Editor editor) {
		editor.getGutter().registerTextAnnotation(new FisheyeAnnotationProvider(url), new FisheyeEditorAction(url));

	}
}
