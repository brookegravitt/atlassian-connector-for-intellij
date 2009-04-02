package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.jira.api.JIRAActionField;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * User: jgorycki
 * Date: Apr 2, 2009
 * Time: 3:11:21 PM
 */
public final class JiraActionFieldType {

	public enum WidgetType {
		UNSUPPORTED,
		USER,
		ISSUE_TYPE,
		TEXT_FIELD,
		TEXT_AREA,
		CALENDAR,
		TIME_SPENT,
		VERSIONS,
		RESOLUTION,
		PRIORITY,
		COMPONENTS
	}

	private static Map<String, WidgetType> typeMap = new HashMap<String, WidgetType>();

	static {
		typeMap.put("resolution", WidgetType.RESOLUTION);
		typeMap.put("fixVersions", WidgetType.VERSIONS);
		typeMap.put("versions", WidgetType.VERSIONS);
		typeMap.put("components", WidgetType.COMPONENTS);
		typeMap.put("description", WidgetType.TEXT_AREA);
		typeMap.put("duedate", WidgetType.CALENDAR);
		typeMap.put("environment", WidgetType.TEXT_AREA);
		typeMap.put("issuetype", WidgetType.ISSUE_TYPE);
		typeMap.put("priority", WidgetType.PRIORITY);
		typeMap.put("reporter", WidgetType.USER);
		typeMap.put("summary", WidgetType.TEXT_FIELD);
		typeMap.put("timetracking", WidgetType.TIME_SPENT);
		typeMap.put("assignee", WidgetType.USER);
	}

	private JiraActionFieldType() {}

	public static WidgetType getFiledTypeForFieldId(@NotNull JIRAActionField field) {
		return getFiledTypeForFieldId(field.getFieldId());
	}

	public static WidgetType getFiledTypeForFieldId(String fieldId) {
		if (typeMap.containsKey(fieldId)) {
			return typeMap.get(fieldId);
		}
		return WidgetType.UNSUPPORTED;
	}
}
