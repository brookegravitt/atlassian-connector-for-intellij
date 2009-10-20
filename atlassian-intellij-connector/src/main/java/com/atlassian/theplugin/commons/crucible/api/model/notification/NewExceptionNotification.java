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

package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import org.jetbrains.annotations.NotNull;


public class NewExceptionNotification implements CrucibleNotification {
	private static final int HASHCODE_MAGIC = 31;

	private Exception exception;
	private ServerData server;

	public NewExceptionNotification(final Exception exception, final ServerData server) {
		this.exception = exception;
		this.server = server;
	}

	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.EXCEPTION_RAISED;
	}

	public PermId getId() {
		return null;
	}

	@NotNull
	public String getItemUrl() {
		return "";
	}

	public String getPresentationMessage() {
		return "Crucible communication exception: " + exception.getMessage();
	}

	public Exception getException() {
		return exception;
	}

	public void setException(final Exception exception) {
		this.exception = exception;
	}

	public ServerData getServer() {
		return server;
	}

	public void setServer(final ServerData server) {
		this.server = server;
	}

	@Override
	public int hashCode() {
		int result = exception != null ? exception.hashCode() : 0;
		result = HASHCODE_MAGIC * result + (server != null ? server.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;

		}

		final NewExceptionNotification that = (NewExceptionNotification) o;

		if (exception.getMessage() == null) {
			return that.exception.getMessage() == null;
		}

		if (!exception.getMessage().equals(that.exception.getMessage())) {
			return false;
		}

		if (server != null ? !server.equals(that.server) : that.server != null) {
			return false;
		}

		return true;
	}


}

