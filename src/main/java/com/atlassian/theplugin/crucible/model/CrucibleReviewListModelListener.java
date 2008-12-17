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
package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;

/**
 * User: jgorycki
 * Date: Dec 2, 2008
 * Time: 10:48:02 AM
 */
public interface CrucibleReviewListModelListener {
	void reviewAdded(ReviewAdapter review);
	void reviewRemoved(ReviewAdapter review);
	void reviewChanged(ReviewAdapter review);
	void modelChanged();

	void reviewListUpdateStarted();
	void reviewListUpdateFinished();
	void reviewChangedWithoutFiles(ReviewAdapter review);
}
