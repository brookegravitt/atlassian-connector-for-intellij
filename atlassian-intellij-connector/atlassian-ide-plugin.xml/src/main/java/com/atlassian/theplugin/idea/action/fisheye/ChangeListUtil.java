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
package com.atlassian.theplugin.idea.action.fisheye;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ContentRevision;

import java.util.ArrayList;
import java.util.List;

public final class ChangeListUtil {
	private ChangeListUtil() {
	}

	public static String getRevision(AnActionEvent event) {
		final ChangeList[] changes = DataKeys.CHANGE_LISTS.getData(event.getDataContext());
		if (changes != null && changes.length == 1) {
			for (Change change : changes[0].getChanges()) {
				if (change.getAfterRevision() == null) {
					continue;
				}
				final ContentRevision contentRevision = change.getAfterRevision();
				if (contentRevision == null) {
					return null;
				}
				return contentRevision.getRevisionNumber().asString();
			}
		}
		return null;
	}

	public static List<String> getRevisions(AnActionEvent event) {
		final ChangeList[] changes = DataKeys.CHANGE_LISTS.getData(event.getDataContext());
		if (changes != null) {
			List<String> revisions = new ArrayList<String>();
			for (Change change : changes[0].getChanges()) {
				if (change.getAfterRevision() == null) {
					continue;
				}
				final ContentRevision contentRevision = change.getAfterRevision();
				if (contentRevision == null) {
					return null;
				}
				revisions.add(contentRevision.getRevisionNumber().asString());
			}
			return revisions;
		}
		return null;
	}

	public static Change getChangeItem(AnActionEvent event) {
		final Change[] changes = DataKeys.CHANGES.getData(event.getDataContext());
		if (changes != null && changes.length == 1) {
			for (Change change : changes) {
				return change;
			}
		}
		return null;
	}
}
