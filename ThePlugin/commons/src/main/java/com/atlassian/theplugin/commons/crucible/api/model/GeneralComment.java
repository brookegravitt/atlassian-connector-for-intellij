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

package com.atlassian.theplugin.commons.crucible.api.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface GeneralComment {
	PermId getPermId();

	String getMessage();

	boolean isDraft();

	boolean isDeleted();

	boolean isDefectRaised();

	boolean isDefectApproved();

	String getUser();

	String getDisplayUser();

	Date getCreateDate();

	List<GeneralComment> getReplies();

	Map<String, CustomField> getCustomFields();

	STATE getState();

	public enum STATE {
		DRAFT {
			public String GetColorString() {
				return "#FFD415";
			}},
		REVIEW {
			public String GetColorString() {
				return "green";
			}},
		DEFECT_RAISED {
			public String GetColorString() {
				return "#FFD415";
			}
			public String toString() {
				return "DEFECT";
			}
		},
		DEFECT_APPROVED {
			public String GetColorString() {
				return DEFECT_RAISED.GetColorString();
			}

			public String toString() {
				return "DEFECT APROVED";
			}
		},
		DELETED {
			public String GetColorString() {
				return "black";
			}
		};

		public abstract String GetColorString();
	}
}
