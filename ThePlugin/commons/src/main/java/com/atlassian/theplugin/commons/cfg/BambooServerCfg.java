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

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.SubscribedPlan;
import com.atlassian.theplugin.commons.util.MiscUtil;

import java.util.Collection;

public class BambooServerCfg extends ServerCfg {

	private static final int HASHCODE_MAGIC = 31;

	private boolean isUseFavourites;
	private boolean isBamboo2;
	private Collection<SubscribedPlan> plans = MiscUtil.buildArrayList();

	private int timezoneOffset;

	public BambooServerCfg(final String name, final ServerId serverId) {
		super(true, name, serverId);
	}

	public BambooServerCfg(final boolean enabled, final String name, final ServerId serverId) {
		super(enabled, name, serverId);
	}

	public BambooServerCfg(final BambooServerCfg other) {
		super(other);
		isUseFavourites = other.isUseFavourites();
		isBamboo2 = other.isBamboo2();
		// shallow copy of SubscribedPlan objects is enough as they are immutable
		plans = MiscUtil.buildArrayList(other.getPlans());
		timezoneOffset = other.timezoneOffset;
	}

	@Override
	public ServerType getServerType() {
		return ServerType.BAMBOO_SERVER;
	}

	public boolean isUseFavourites() {
		return isUseFavourites;
	}

	public Collection<SubscribedPlan> getSubscribedPlans() {
		return plans;
	}

	public void clearSubscribedPlans() {
		plans.clear();
	}

	public void setUseFavourites(final boolean useFavourites) {
		isUseFavourites = useFavourites;
	}

	public int getTimezoneOffset() {
		return timezoneOffset;
	}

	public void setTimezoneOffset(int timezoneOffset) {
		this.timezoneOffset = timezoneOffset;
	}

	@Override
	public boolean equals(final Object o) {
		if (super.equals(o) == false) {
			return false;
		}
		
		if (this == o) {
			return true;
		}
		if (!(o instanceof BambooServerCfg)) {
			return false;
		}

		final BambooServerCfg that = (BambooServerCfg) o;

		if (isUseFavourites != that.isUseFavourites) {
			return false;
		}

		if (plans.equals(that.plans) == false) {
			return false;
		}

		if (timezoneOffset != that.timezoneOffset) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return (isUseFavourites ? 1 : 0) + HASHCODE_MAGIC * super.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Bamboo Server [");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}

	public void setIsBamboo2(final boolean b) {
		isBamboo2 = b;
	}

	public boolean isBamboo2() {
		return isBamboo2;
	}

	public Collection<SubscribedPlan> getPlans() {
		return plans;
	}

	public void setPlans(final Collection<SubscribedPlan> plans) {
		this.plans = plans;
	}

	@Override
	public BambooServerCfg getClone() {
		return new BambooServerCfg(this);
	}

	// this method is used by XStream - do not remove!!!
	@Override
	protected Object readResolve() {
		super.readResolve();
		if (plans == null) {
			plans = MiscUtil.buildArrayList();
		}
		return this;
	}

	public PrivateServerCfgInfo createPrivateProjectConfiguration() {
		return new PrivateBambooServerCfgInfo(getServerId(), isEnabled(), getUsername(),
				isPasswordStored() ? getPassword() : null, getTimezoneOffset());
	}

	public void mergePrivateConfiguration(PrivateServerCfgInfo psci) {
		super.mergePrivateConfiguration(psci);
		if (psci != null) {
			try {
				setTimezoneOffset(((PrivateBambooServerCfgInfo) psci).getTimezoneOffset());
			} catch (ClassCastException e) {
				// Whisky Tango Foxtrot?
			}
		}
	}

}
