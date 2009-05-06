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

import com.atlassian.theplugin.commons.crucible.api.model.User;

public class UserListItem {
	private User user;
	private boolean selected;
	private static final int HASH_NUMBER = 31;

	public UserListItem(User user, boolean selected) {
		this.user = user;
		this.selected = selected;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final UserListItem that = (UserListItem) o;

		if (user != null ? !user.equals(that.user) : that.user != null) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		if (user != null) {
			return user.getDisplayName();
		}
		return "User null";
	}

	@Override
	public int hashCode() {
		int result;
		result = (user != null ? user.hashCode() : 0);
		result = HASH_NUMBER * result + (selected ? 1 : 0);
		return result;
	}
}
