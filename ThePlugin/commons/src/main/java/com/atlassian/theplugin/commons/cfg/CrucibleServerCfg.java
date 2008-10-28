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

public class CrucibleServerCfg extends ServerCfg {
	private boolean isFisheyeInstance;
	private static final int HASHCODE_MAGIC = 31;

	public CrucibleServerCfg(final String name, final ServerId serverId) {
        super(true, name, serverId);
	}

	public CrucibleServerCfg(final CrucibleServerCfg other) {
		super(other);
		isFisheyeInstance = other.isFisheyeInstance();
	}

	@Override
	public ServerType getServerType() {
        return ServerType.CRUCIBLE_SERVER;
    }

	@Override
	public boolean equals(final Object o) {
		if (super.equals(o) == false) {
			return false;
		}

		if (this == o) {
			return true;
		}
		if (!(o instanceof CrucibleServerCfg)) {
			return false;
		}

		final CrucibleServerCfg that = (CrucibleServerCfg) o;

		if (isFisheyeInstance != that.isFisheyeInstance) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = HASHCODE_MAGIC * result + (isFisheyeInstance ? 1 : 0);
		return result;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Crucible Server [");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public CrucibleServerCfg getClone() {
		return new CrucibleServerCfg(this);
	}

	public boolean isFisheyeInstance() {
		return isFisheyeInstance;
	}

	public void setFisheyeInstance(final boolean fisheyeInstance) {
		isFisheyeInstance = fisheyeInstance;
	}
}
