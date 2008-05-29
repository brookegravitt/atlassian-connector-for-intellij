package com.atlassian.theplugin.jira.api;

public class JIRAActionFieldBean extends AbstractJIRAConstantBean implements JIRAActionField {
	private String fieldId;

	public JIRAActionFieldBean(String fieldId, String name) {
		super(fieldId.hashCode(), name, null);
		this.fieldId = fieldId;
	}

	public String getQueryStringFragment() {
		// todo: I am almost absolutely sure this is wrong. Once we get
		// to actually handling action fields, this will have to be fixed
		return fieldId + "=";
	}
}
