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

import com.atlassian.theplugin.commons.crucible.api.model.Action;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.intellij.openapi.project.Project;

public class CrucibleChangeStateWorker {
	private ReviewAdapter review;
	private Action action;
	private Project project;

	public CrucibleChangeStateWorker(Project project, ReviewAdapter reviewInfo, Action action) {
		this.review = reviewInfo;
		this.action = action;
		this.project = project;
	}

	public void run() {

		final CrucibleChangeReviewStateForm changeReviewStateForm =
				new CrucibleChangeReviewStateForm(project, review, action);

		changeReviewStateForm.showDialog();
	}
}