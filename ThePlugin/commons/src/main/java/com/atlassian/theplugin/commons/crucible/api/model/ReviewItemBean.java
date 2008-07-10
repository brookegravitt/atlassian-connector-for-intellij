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

package com.atlassian.theplugin.commons.crucible.api.model;

public class ReviewItemBean implements ReviewItem {
	private PermId permId;
	private String repositoryName;
	private String fromPath;
	private String fromRevision;
	private String toPath;
	private String toRevision;

	public ReviewItemBean() {
	}

	public PermId getPermId() {
		return permId;
	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public String getFromPath() {
		return fromPath;
	}

	public String getFromRevision() {
		return fromRevision;
	}

	public String getToPath() {
		return toPath;
	}

	public String getToRevision() {
		return toRevision;
	}

	public void setPermId(PermId permId) {
		this.permId = permId;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public void setFromPath(String fromPath) {
		this.fromPath = fromPath;
	}

	public void setFromRevision(String fromRevision) {
		this.fromRevision = fromRevision;
	}

	public void setToPath(String toPath) {
		this.toPath = toPath;
	}

	public void setToRevision(String toRevision) {
		this.toRevision = toRevision;
	}

	public String toString() {
		String from = stripPath(getFromPath());
		String to = stripPath(getToPath());

		if (from.length() > 0 && to.length() > 0) {
			return from + " (mod)";
		} else if (from.length() > 0) {
			return from + " (new)";
		} else {
			if (to.length() > 0) {
				return to + " (del)";
			} else {
				return "unknown";
			}
		}
	}

	private static String stripPath(String path) {
		int lastIndex;
		lastIndex = path.lastIndexOf("/");
		if (lastIndex != -1) {
			path = path.substring(lastIndex + 1);
		}
		return path;
	}

}
