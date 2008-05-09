package com.atlassian.theplugin.commons.configuration;

public enum CrucibleTooltipOption {
	UNREAD_REVIEWS {
		public String toString() {
			return "Unread reviews exist";
		}
	},

	NEVER {
		public String toString() {
			return "Never";
		}
	}
}
