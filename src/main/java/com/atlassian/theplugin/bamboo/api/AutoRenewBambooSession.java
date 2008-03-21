package com.atlassian.theplugin.bamboo.api;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BambooPlan;
import com.atlassian.theplugin.bamboo.BambooProject;
import com.atlassian.theplugin.bamboo.BuildDetails;
import com.atlassian.theplugin.api.RemoteApiException;
import com.atlassian.theplugin.api.RemoteApiLoginException;
import com.atlassian.theplugin.api.RemoteApiSessionExpiredException;

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
