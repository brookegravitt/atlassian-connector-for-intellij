package com.atlassian.theplugin.bamboo.api;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BambooPlan;
import com.atlassian.theplugin.bamboo.BambooProject;
import com.atlassian.theplugin.bamboo.BuildDetails;

import java.util.List;

public class AutoRenewBambooSession implements BambooSession {
	private final BambooSession delegate;
	private String userName;
	private char[] password;

	public AutoRenewBambooSession(String url) {
		this.delegate = new BambooSessionImpl(url);
	}

	public void addCommentToBuild(String buildKey, String buildNumber, String buildComment) throws BambooException {
		try {
			delegate.addCommentToBuild(buildKey, buildNumber, buildComment);
		} catch (BambooSessionExpiredException e) {
			delegate.login(userName, password);
			delegate.addCommentToBuild(buildKey, buildNumber, buildComment);
		}
	}

	public void executeBuild(String buildKey) throws BambooException {
		try {
			delegate.executeBuild(buildKey);
		} catch (BambooSessionExpiredException e) {
			delegate.login(userName, password);
			delegate.executeBuild(buildKey);
		}
	}

	public void addLabelToBuild(String buildKey, String buildNumber, String buildLabel) throws BambooException {
		try {
			delegate.addLabelToBuild(buildKey, buildNumber, buildLabel);
		} catch (BambooSessionExpiredException e) {
			delegate.login(userName, password);
			delegate.addLabelToBuild(buildKey, buildNumber, buildLabel);
		}
	}

	public BuildDetails getBuildResultDetails(String buildKey, String buildNumber) throws BambooException {
		try {
			return delegate.getBuildResultDetails(buildKey, buildNumber);
		} catch (BambooSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getBuildResultDetails(buildKey, buildNumber);
		}					
	}

	public List<String> getFavouriteUserPlans() throws BambooException {
		try {
			return delegate.getFavouriteUserPlans();
		} catch (BambooSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getFavouriteUserPlans();
		}		
	}

	public BambooBuild getLatestBuildForPlan(String planKey) throws BambooException {
		try {
			return delegate.getLatestBuildForPlan(planKey);
		} catch (BambooSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getLatestBuildForPlan(planKey);
		}				
	}

	public boolean isLoggedIn() {
		return delegate.isLoggedIn();
	}

	public List<BambooPlan> listPlanNames() throws BambooException {
		try {
			return delegate.listPlanNames();
		} catch (BambooSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.listPlanNames();
		}
	}

	public List<BambooProject> listProjectNames() throws BambooException {
		try {
			return delegate.listProjectNames();
		} catch (BambooSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.listProjectNames();
		}
	}

	public void login(String name, char[] aPassword) throws BambooLoginException {
		this.userName = name;
		this.password = new char[aPassword.length];
		System.arraycopy(aPassword, 0, password, 0, aPassword.length);
		delegate.login(name, aPassword);
	}

	public void logout() {
		delegate.logout();
	}

	public int getBamboBuildNumber() throws BambooException {
		try {
			return delegate.getBamboBuildNumber();
		} catch (BambooSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getBamboBuildNumber();
		}
	}
}
