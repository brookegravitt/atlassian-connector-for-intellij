package com.atlassian.theplugin.crucible.api;

public class ReviewItemDataBean implements ReviewItemData {
	private PermId permId;
	private String repositoryName;
	private String fromPath;
	private String fromRevision;
	private String toPath;
	private String toRevision;

	public ReviewItemDataBean() {
	}

	public PermId getPermId() {
		return permId;
	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public String getFromPath() {
		return fromPath;
	}

	public String getFromRevision() {
		return fromRevision;
	}

	public String getToPath() {
		return toPath;
	}

	public String getToRevision() {
		return toRevision;
	}

	public void setPermId(PermId permId) {
		this.permId = permId;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public void setFromPath(String fromPath) {
		this.fromPath = fromPath;
	}

	public void setFromRevision(String fromRevision) {
		this.fromRevision = fromRevision;
	}

	public void setToPath(String toPath) {
		this.toPath = toPath;
	}

	public void setToRevision(String toRevision) {
		this.toRevision = toRevision;
	}	
}
