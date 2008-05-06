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

package com.atlassian.theplugin.commons.configuration;

import com.atlassian.theplugin.commons.SubscribedPlan;

public class SubscribedPlanBean implements SubscribedPlan {
	private String planId;

	public SubscribedPlanBean() {
	}

	public SubscribedPlanBean(SubscribedPlan cfg) {
		this.setPlanId(cfg.getPlanId());
	}

    public SubscribedPlanBean(String planId) {
        this.planId = planId;
    }

    public String getPlanId() {
		return planId;
	}

	public void setPlanId(String planId) {
		this.planId = planId;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		SubscribedPlanBean that = (SubscribedPlanBean) o;

		if (!planId.equals(that.planId)) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		return planId.hashCode();
	}
}
