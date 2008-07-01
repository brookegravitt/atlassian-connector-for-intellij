package com.atlassian.theplugin.eclipse.core.bamboo;

public class BambooServer {
	private String id;
	private String label;
	private String username;
	private String password;
	private boolean passwordSaved;
	private String url;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isPasswordSaved() {
		return passwordSaved;
	}
	public void setPasswordSaved(boolean passwordSaved) {
		this.passwordSaved = passwordSaved;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void copyTo(BambooServer server) {
		server.setId(id);
		server.setLabel(label);
		server.setPassword(password);
		server.setPasswordSaved(passwordSaved);
		server.setUrl(url);
	}
}
