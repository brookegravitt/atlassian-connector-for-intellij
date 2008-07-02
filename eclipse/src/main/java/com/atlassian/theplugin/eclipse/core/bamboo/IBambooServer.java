package com.atlassian.theplugin.eclipse.core.bamboo;

public interface IBambooServer {

	public abstract String getId();

	public abstract String getLabel();

	public abstract void setLabel(String label);

	public abstract String getUsername();

	public abstract void setUsername(String username);

	public abstract String getPassword();

	public abstract void setPassword(String password);

	public abstract boolean isPasswordSaved();

	public abstract void setPasswordSaved(boolean passwordSaved);

	public abstract String getUrl();

	public abstract void setUrl(String url);

}