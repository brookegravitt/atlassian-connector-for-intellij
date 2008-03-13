package com.atlassian.theplugin;

/**
 * Represents server types
 */
public enum ServerType {
	BAMBOO_SERVER {
		public String toString() {
			return "Bamboo Servers";
		}
	},
	CRUCIBLE_SERVER {
		public String toString() {
			return "Crucible Servers";
		}
	},
    JIRA_SERVER {
		public String toString() {
			return "JIRA Servers";
		}
	}
}
