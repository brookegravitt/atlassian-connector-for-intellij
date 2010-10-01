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

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ReviewNotificationBean {
	private List<ReviewAdapter> reviews;
	private HashMap<ServerData, Exception> exceptions = new HashMap<ServerData, Exception>();

	public ReviewNotificationBean() {
	}

	public List<ReviewAdapter> getReviews() {
		return reviews;
	}

	public void setReviews(final List<ReviewAdapter> reviews) {
		this.reviews = reviews;
	}

	public Exception getException(ServerData serverData) {
		return exceptions.get(serverData);
	}

	public void addException(final ServerData serverData, final Exception exception) {
		exceptions.put(serverData, exception);
	}

    public Collection<ServerData> getExceptionServers() {
        return exceptions.keySet();
    }
}
