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

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewData;

/**
 * Created by IntelliJ IDEA.
* User: lguminski
* Date: Jun 17, 2008
* Time: 8:41:47 PM
* To change this template use File | Settings | File Templates.
*/
public class ShowReviewEvent extends CrucibleEvent {
	private ReviewData reviewInfo;


	public ShowReviewEvent(CrucibleReviewActionListener caller, ReviewData reviewInfo) {
		super(caller);
		this.reviewInfo = reviewInfo;		
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.showReview(reviewInfo);
	}
}
