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

import com.atlassian.connector.intellij.crucible.CrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.util.PluginUtil;
import java.util.HashSet;
import java.util.Set;

public class CrucibleJoinReviewWorker implements Runnable {
	private final ReviewAdapter reviewInfo;

	public CrucibleJoinReviewWorker(ReviewAdapter reviewInfo) {
		this.reviewInfo = reviewInfo;
	}

	public void run() {
		CrucibleServerFacade facade = IntelliJCrucibleServerFacade.getInstance();
		Set<String> reviewers = new HashSet<String>();
		reviewers.add(reviewInfo.getServerData().getUsername());
		try {
			facade.addReviewers(reviewInfo.getServerData(), reviewInfo.getPermId(), reviewers);
		} catch (RemoteApiException e) {
			PluginUtil.getLogger().error("Error joining review: " + e.getMessage(), e);
		} catch (ServerPasswordNotProvidedException e) {
			PluginUtil.getLogger().error("Error joining review: " + e.getMessage(), e);
		}
	}
}