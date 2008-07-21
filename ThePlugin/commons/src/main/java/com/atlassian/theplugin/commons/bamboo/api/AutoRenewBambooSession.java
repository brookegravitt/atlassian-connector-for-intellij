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

package com.atlassian.theplugin.commons.bamboo.api;

import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooProject;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;

import java.util.List;

public class AutoRenewBambooSession implements BambooSession {
	private final BambooSession delegate;
	private String userName;
	private char[] password;

	public AutoRenewBambooSession(String url) throws RemoteApiException {
		this.delegate = new BambooSessionImpl(url);
	}

	public void addCommentToBuild(String buildKey, String buildNumber, String buildComment) throws RemoteApiException {
		try {
			delegate.addCommentToBuild(buildKey, buildNumber, buildComment);
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			delegate.addCommentToBuild(buildKey, buildNumber, buildComment);
		}
	}

	public void executeBuild(String buildKey) throws RemoteApiException {
		try {
			delegate.executeBuild(buildKey);
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			delegate.executeBuild(buildKey);
		}
	}

	public void addLabelToBuild(String buildKey, String buildNumber, String buildLabel) throws RemoteApiException {
		try {
			delegate.addLabelToBuild(buildKey, buildNumber, buildLabel);
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			delegate.addLabelToBuild(buildKey, buildNumber, buildLabel);
		}
	}

	public BuildDetails getBuildResultDetails(String buildKey, String buildNumber) throws RemoteApiException {
		try {
			return delegate.getBuildResultDetails(buildKey, buildNumber);
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getBuildResultDetails(buildKey, buildNumber);
		}					
	}

	public List<String> getFavouriteUserPlans() throws RemoteApiException {
		try {
			return delegate.getFavouriteUserPlans();
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getFavouriteUserPlans();
		}		
	}

	public BambooBuild getLatestBuildForPlan(String planKey) throws RemoteApiException {
		try {
			return delegate.getLatestBuildForPlan(planKey);
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getLatestBuildForPlan(planKey);
		}				
	}

	public boolean isLoggedIn() {
		return delegate.isLoggedIn();
	}

    public byte[] getBuildLogs(String buildKey, String buildNumber) throws RemoteApiException {
        try {
            return delegate.getBuildLogs(buildKey, buildNumber);
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getBuildLogs(buildKey, buildNumber);
        }
    }

    public List<BambooPlan> listPlanNames() throws RemoteApiException {
		try {
			return delegate.listPlanNames();
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.listPlanNames();
		}
	}

	public List<BambooProject> listProjectNames() throws RemoteApiException {
		try {
			return delegate.listProjectNames();
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.listProjectNames();
		}
	}

	public void login(String name, char[] aPassword) throws RemoteApiLoginException {
		this.userName = name;
		this.password = new char[aPassword.length];
		System.arraycopy(aPassword, 0, password, 0, aPassword.length);
		delegate.login(name, aPassword);
	}

	public void logout() {
		delegate.logout();
	}

	public int getBamboBuildNumber() throws RemoteApiException {
		try {
			return delegate.getBamboBuildNumber();
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getBamboBuildNumber();
		}
	}
}
