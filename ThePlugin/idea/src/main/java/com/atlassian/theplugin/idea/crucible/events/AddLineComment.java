package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.intellij.openapi.editor.Editor;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 23, 2008
 * Time: 11:48:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class AddLineComment extends CrucibleEvent {
	private ReviewData review;
	private CrucibleFileInfo file;
	private int start;
	private int end;
	private Editor editor;

	public AddLineComment(CrucibleReviewActionListener caller, ReviewData review, CrucibleFileInfo file, Editor editor, int start, int end) {
		super(caller);
		this.review = review;
		this.file = file;
		this.editor = editor;
		this.start = start;
		this.end = end;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.aboutToAddLineComment(review, file, editor, start, end);
	}
}