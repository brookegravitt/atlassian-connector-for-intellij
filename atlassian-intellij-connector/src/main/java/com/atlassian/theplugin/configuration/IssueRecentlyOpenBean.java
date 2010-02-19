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
package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;

/**
 * @author Jacek Jaroczynski
 */
public class IssueRecentlyOpenBean {

	protected  ServerIdImpl serverId;
	protected  String issueKey;
    protected  String issueUrl;

	public IssueRecentlyOpenBean() {
	}


	public IssueRecentlyOpenBean(final ServerId serverId, final String issueKey, String issueUrl) {

        if (serverId instanceof ServerId) {
			this.serverId = (ServerIdImpl) serverId;
		}
        
		this.issueKey = issueKey;
        this.issueUrl = issueUrl;
	}

	public ServerIdImpl getServerId() {
		return serverId;
	}

	public void setServerId(final ServerIdImpl serverId) {
		this.serverId = serverId;
	}

	public String getIssueKey() {
		return issueKey;
	}

	public void setIssueKey(final String issueKey) {
		this.issueKey = issueKey;
    }

    public String getIssueUrl() {
        return issueUrl;
    }

    public void setIssueUrl(String issueUrl) {
        this.issueUrl = issueUrl;
    }

    public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final IssueRecentlyOpenBean that = (IssueRecentlyOpenBean) o;

		if (issueKey != null ? !issueKey.equals(that.issueKey) : that.issueKey != null) {
			return false;
		}
		if (serverId != null ? !serverId.equals(that.serverId) : that.serverId != null) {
			return false;
		}

         if (issueUrl != null ? !issueUrl.equals(that.issueUrl) : that.issueUrl != null) {
			return false;
		}

		return true;
	}



	public int hashCode() {
		int result;
		result = (serverId != null ? serverId.hashCode() : 0);
		result = 31 * result + (issueKey != null ? issueKey.hashCode() : 0);
        result = 31 * result + (issueUrl != null ? issueUrl.hashCode() : 0);
		return result;
	}
}
