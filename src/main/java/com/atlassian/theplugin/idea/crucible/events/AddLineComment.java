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

package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.CrucibleReviewActionListener;
import com.intellij.openapi.editor.Editor;

/**
 * @author lguminski
 */
public class AddLineComment extends CrucibleEvent {
	private ReviewAdapter review;
	private CrucibleFileInfo file;
	private int start;
	private int end;
	private Editor editor;

	public AddLineComment(CrucibleReviewActionListener caller,
                          ReviewAdapter review,
                          CrucibleFileInfo file,
                          Editor editor,
                          int start,
                          int end) {
		super(caller);
		this.review = review;
		this.file = file;
		this.editor = editor;
		this.start = start;
		this.end = end;
	}

	@Override
	protected void notify(CrucibleReviewActionListener listener) {
		listener.aboutToAddLineComment(review, file, editor, start, end);
	}
}