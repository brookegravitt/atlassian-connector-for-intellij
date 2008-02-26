package com.atlassian.theplugin.configuration;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-25
 * Time: 11:50:29
 * To change this template use File | Settings | File Templates.
 */
public enum BambooTooltipOption {
	ALL_FAULIRES_AND_FIRST_SUCCESS {
		public String toString() {
			return "All build failures and first build success";
		}
	},

	FIRST_FAILURE_AND_FIRST_SUCCESS {
		public String toString() {
			return "First build failure and first build success";
		}
	},

	NEVER {
		public String toString() {
			return "Never";
		}
	}
}
