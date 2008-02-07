package com.atlassian.theplugin;

/**
 * Represents server types
 * User: mwent
 * Date: 2008-01-29
 * Time: 09:33:42
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
	}
}
