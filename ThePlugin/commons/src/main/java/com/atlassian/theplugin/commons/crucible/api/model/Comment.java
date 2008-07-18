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
import java.util.Map;

public interface Comment {
	PermId getPermId();

	String getMessage();

	boolean isDraft();

	boolean isDeleted();

	boolean isDefectRaised();

	boolean isDefectApproved();

	boolean isReply();

	String getUser();

	String getDisplayUser();

	Date getCreateDate();

	Map<String, CustomField> getCustomFields();

	STATE getState();

	public enum STATE {

        DRAFT("#FFD415"),
		REVIEW("green"),
		DEFECT_RAISED("red") {
			public String toString() {
				return "DEFECT";
			}
		},
		DEFECT_APPROVED(DEFECT_RAISED.getColorString()) {
			public String toString() {
				return "DEFECT APROVED";
			}
		},
		DELETED("black");

        STATE(String colorString) {
            this.colorString = colorString;
        }

        private String colorString;

        public String getColorString() {
            return colorString;
        }
	}
}
