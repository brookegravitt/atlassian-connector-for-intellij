package com.atlassian.theplugin.bamboo.api;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BambooPlan;
import com.atlassian.theplugin.bamboo.BambooProject;
import com.atlassian.theplugin.bamboo.BuildDetails;
import com.atlassian.theplugin.rest.RestException;
import com.atlassian.theplugin.rest.RestLoginException;
import com.atlassian.theplugin.rest.RestSessionExpiredException;

import java.util.List;

public class AutoRenewBambooSession implements BambooSession {
	private final BambooSession delegate;
	private String userName;
	private char[] password;

	public AutoRenewBambooSession(String url) throws RestException {
		this.delegate = new BambooSessionImpl(url);
	}

	public void addCommentToBuild(String buildKey, String buildNumber, String buildComment) throws RestException {
		try {
			delegate.addCommentToBuild(buildKey, buildNumber, buildComment);
		} catch (RestSessionExpiredException e) {
			delegate.login(userName, password);
			delegate.addCommentToBuild(buildKey, buildNumber, buildComment);
		}
	}

	public void executeBuild(String buildKey) throws RestException {
		try {
			delegate.executeBuild(buildKey);
		} catch (RestSessionExpiredException e) {
			delegate.login(userName, password);
			delegate.executeBuild(buildKey);
		}
	}

	public void addLabelToBuild(String buildKey, String buildNumber, String buildLabel) throws RestException {
		try {
			delegate.addLabelToBuild(buildKey, buildNumber, buildLabel);
		} catch (RestSessionExpiredException e) {
			delegate.login(userName, password);
			delegate.addLabelToBuild(buildKey, buildNumber, buildLabel);
		}
	}

	public BuildDetails getBuildResultDetails(String buildKey, String buildNumber) throws RestException {
		try {
			return delegate.getBuildResultDetails(buildKey, buildNumber);
		} catch (RestSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getBuildResultDetails(buildKey, buildNumber);
		}					
	}

	public List<String> getFavouriteUserPlans() throws RestException {
		try {
			return delegate.getFavouriteUserPlans();
		} catch (RestSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getFavouriteUserPlans();
		}		
	}

	public BambooBuild getLatestBuildForPlan(String planKey) throws RestException {
		try {
			return delegate.getLatestBuildForPlan(planKey);
		} catch (RestSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getLatestBuildForPlan(planKey);
		}				
	}

	public boolean isLoggedIn() {
		return delegate.isLoggedIn();
	}

	public List<BambooPlan> listPlanNames() throws RestException {
		try {
			return delegate.listPlanNames();
		} catch (RestSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.listPlanNames();
		}
	}

	public List<BambooProject> listProjectNames() throws RestException {
		try {
			return delegate.listProjectNames();
		} catch (RestSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.listProjectNames();
		}
	}

	public void login(String name, char[] aPassword) throws RestLoginException {
		this.userName = name;
		this.password = new char[aPassword.length];
		System.arraycopy(aPassword, 0, password, 0, aPassword.length);
		delegate.login(name, aPassword);
	}

	public void logout() {
		delegate.logout();
	}

	public int getBamboBuildNumber() throws RestException {
		try {
			return delegate.getBamboBuildNumber();
		} catch (RestSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getBamboBuildNumber();
		}
	}
}
