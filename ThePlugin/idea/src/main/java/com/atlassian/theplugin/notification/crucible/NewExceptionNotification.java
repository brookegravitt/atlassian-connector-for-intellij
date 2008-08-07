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
}
