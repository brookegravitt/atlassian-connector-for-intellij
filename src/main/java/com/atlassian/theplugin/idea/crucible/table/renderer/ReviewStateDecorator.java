package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.commons.crucible.api.model.State;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-07-14
 * Time: 14:03:40
 * To change this template use File | Settings | File Templates.
 */
public class ReviewStateDecorator {
	private String text;

	public ReviewStateDecorator(String value, State state) {
		text = value;

		switch (state) {
			case CLOSED:
				text = "<html><body><span style=\"color: #999999; text-decoration: line-through;\">"
						+ text
						+ "</span></body></html>";
				break;
			default:
		}
	}

	public String toString() {
		return text;
	}
}
