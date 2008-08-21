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
package com.atlassian.theplugin.commons.cfg;

public class PrivateServerCfgInfo {
	private final ServerId serverId;
	private final boolean isEnabled;
	private final String username;
	private final String password;
	private static final int HASHCODE_MAGIC = 31;

	public PrivateServerCfgInfo(final ServerId serverId, final boolean enabled, final String username, final String password) {
		this.serverId = serverId;
		isEnabled = enabled;
		this.username = username;
		this.password = password;
	}

	public ServerId getServerId() {
		return serverId;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof PrivateServerCfgInfo)) {
			return false;
		}

		final PrivateServerCfgInfo that = (PrivateServerCfgInfo) o;

		if (password != null ? !password.equals(that.password) : that.password != null) {
			return false;
		}
		if (serverId != null ? !serverId.equals(that.serverId) : that.serverId != null) {
			return false;
		}
		if (username != null ? !username.equals(that.username) : that.username != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = (serverId != null ? serverId.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (username != null ? username.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (password != null ? password.hashCode() : 0);
		return result;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

}
