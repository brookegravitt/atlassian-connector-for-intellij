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

import com.atlassian.theplugin.commons.crucible.api.PredefinedFilter;

import java.util.List;
import java.util.Arrays;

public class CrucibleConfigurationBean extends AbstractServerConfigurationBean {
	private CrucibleTooltipOption crucibleTooltipOption;
    private int pollTime = 1;
    private Boolean[] filters = new Boolean[PredefinedFilter.values().length];

    public CrucibleConfigurationBean() {
		super();
        Arrays.fill(filters, false);
    }

	public CrucibleConfigurationBean(ProductServerConfiguration cfg) {
		super(cfg);
        Arrays.fill(filters, false);
        if (cfg instanceof CrucibleConfigurationBean) {
			this.crucibleTooltipOption = ((CrucibleConfigurationBean) cfg).getCrucibleTooltipOption();
			this.pollTime = ((CrucibleConfigurationBean) cfg).getPollTime();
            for (int i = 0; i < ((CrucibleConfigurationBean) cfg).getFilters().length; ++i) {
                this.filters[i] = ((CrucibleConfigurationBean) cfg).getFilters()[i];                 
            }
        }
    }

	public int getPollTime() {
		return pollTime;
	}

	public void setPollTime(int pollTime) {
		this.pollTime = pollTime;
	}

	public CrucibleTooltipOption getCrucibleTooltipOption() {
		return crucibleTooltipOption;
	}

	public void setCrucibleTooltipOption(CrucibleTooltipOption crucibleTooltipOption) {
		this.crucibleTooltipOption = crucibleTooltipOption;
	}

    public Boolean[] getFilters() {
        return filters;
    }

    public void setFilters(Boolean[] filters) {
        this.filters = filters;
    }    
}