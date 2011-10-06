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
package com.atlassian.theplugin.idea.config.serverconfig.defaultCredentials;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

/**
 * User: pmaruszak
 */
class ServerDataExt {
		private ServerData serverData;
		private ServerType serverType;
		private ConnectionStatus status = ConnectionStatus.NONE;
		private String errorMessage;


		public ServerDataExt(final ServerData serverData, final ServerType serverTpe) {
			this.serverData = serverData;
			this.serverType = serverTpe;
		}

		public void setStatus(final ConnectionStatus status) {
			this.status = status;
		}

		public void setErrorMessage(final String errorMessage) {
			this.status = ConnectionStatus.FAILED;
			this.errorMessage = errorMessage;
		}


	public ServerType getServerType() {
		return serverType;
	}

	public ServerData getServerData() {
		return serverData;
	}

    public ConnectionStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			if (serverData == null) {
				return false;
			}

			final ServerDataExt that = (ServerDataExt) o;

			return serverData.equals(((ServerDataExt) o).serverData);
		}

		public int hashCode() {
			return serverData.hashCode();
		}
	}

