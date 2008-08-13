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
package com.atlassian.theplugin.commons.cfg;

import com.atlassian.theplugin.commons.util.MiscUtil;

import java.util.Collection;

public class GlobalConfiguration {
	private Collection<ServerCfg> globalServers = MiscUtil.buildArrayList();
	private GeneralCfg generalCfg = new GeneralCfg();
	private BambooCfg bambooCfg = new BambooCfg();
	private CrucibleCfg crucibleCfg = new CrucibleCfg();
	private static final int HASHCODE_MAGIC = 31;

	public Collection<ServerCfg> getGlobalServers() {
		return globalServers;
	}

	public void setGlobalServers(final Collection<ServerCfg> globalServers) {
		if (globalServers == null) {
			throw new NullPointerException();
		}
		this.globalServers = globalServers;
	}

	public GeneralCfg getGeneralCfg() {
		return generalCfg;
	}

	public void setGeneralCfg(final GeneralCfg generalCfg) {
		this.generalCfg = generalCfg;
	}

	public CrucibleCfg getCrucibleCfg() {
		return crucibleCfg;
	}

	public void setCrucibleCfg(final CrucibleCfg crucibleCfg) {
		this.crucibleCfg = crucibleCfg;
	}


	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof GlobalConfiguration)) {
			return false;
		}

		final GlobalConfiguration that = (GlobalConfiguration) o;

//		if (!crucibleCfg.equals(that.crucibleCfg)) {
//			return false;
//		}
//		if (!generalCfg.equals(that.generalCfg)) {
//			return false;
//		}
		if (!globalServers.equals(that.globalServers)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = globalServers.hashCode();
		result = HASHCODE_MAGIC * result + generalCfg.hashCode();
		result = HASHCODE_MAGIC * result + crucibleCfg.hashCode();
		return result;
	}

	public BambooCfg getBambooCfg() {
		return bambooCfg;
	}

	public void setBambooCfg(final BambooCfg bambooCfg) {
		this.bambooCfg = bambooCfg;
	}
}
