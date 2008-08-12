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

package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.PermId;


public class NewExceptionNotification implements CrucibleNotification {
	private Exception exception;



	public NewExceptionNotification(final Exception exception) {
		this.exception = exception;
	}

	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.EXCEPTION_RAISED;
	}

	public PermId getId() {
		return null;
	}

	public String getItemUrl() {
		return "";
	}

	public String getPresentationMessage() {
		return "Crucible communication exception: " + exception.getMessage();
	}

	public boolean equals(final Object o) {
	if (this == o) {
		return true;
	}
	if (o == null || getClass() != o.getClass()) {
		return false;
	}

	final NewExceptionNotification that = (NewExceptionNotification) o;

	if (!exception.getMessage().equals(that.exception.getMessage())) {
		return false;
	}

	return true;
}

	public int hashCode() {
		return exception.hashCode();
	}
}
