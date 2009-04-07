package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.jira.api.JIRAActionField;
import com.atlassian.theplugin.jira.api.JIRAActionFieldBean;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.fields.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jgorycki
 * Date: Apr 2, 2009
 * Time: 3:11:21 PM
 */
public final class JiraActionFieldType {

	public enum WidgetType {
		UNSUPPORTED,
		ASSIGNEE,
		REPORTER,
		ISSUE_TYPE,
		SUMMARY,
		DESCRIPTION,
		ENVIRONMENT,
		CALENDAR,
		TIME_SPENT,
		VERSIONS,
		FIX_VERSIONS,
		RESOLUTION,
		PRIORITY,
		COMPONENTS
	}

	private static final class WidgetTypeAndFieldFiller {
		private FieldFiller filler;
		private WidgetType widgetType;

		private WidgetTypeAndFieldFiller(WidgetType widgetType, FieldFiller filler) {
			this.filler = filler;
			this.widgetType = widgetType;
		}

		public FieldFiller getFiller() {
			return filler;
		}

		public WidgetType getWidgetType() {
			return widgetType;
		}
	}

	private static Map<String, WidgetTypeAndFieldFiller> typeMap = new HashMap<String, WidgetTypeAndFieldFiller>();
	private static CustomFieldFiller customFieldFiller = new CustomFieldFiller();

	static {
		typeMap.put("resolution", new WidgetTypeAndFieldFiller(WidgetType.RESOLUTION, new ResolutionFiller()));
		typeMap.put("fixVersions", new WidgetTypeAndFieldFiller(WidgetType.FIX_VERSIONS, new FixVersionsFiller()));
		typeMap.put("versions", new WidgetTypeAndFieldFiller(WidgetType.VERSIONS, new AffectsVersionsFiller()));
		typeMap.put("components", new WidgetTypeAndFieldFiller(WidgetType.COMPONENTS, new ComponentsFiller()));
		typeMap.put("description", new WidgetTypeAndFieldFiller(WidgetType.DESCRIPTION, new DescriptionFiller()));
		typeMap.put("duedate", new WidgetTypeAndFieldFiller(WidgetType.CALENDAR, new DueDateFiller()));
		typeMap.put("environment", new WidgetTypeAndFieldFiller(WidgetType.ENVIRONMENT, new EnvironmentFiller()));
		typeMap.put("issuetype", new WidgetTypeAndFieldFiller(WidgetType.ISSUE_TYPE, new IssueTypeFiller()));
		typeMap.put("priority", new WidgetTypeAndFieldFiller(WidgetType.PRIORITY, new PriorityFiller()));
		typeMap.put("reporter", new WidgetTypeAndFieldFiller(WidgetType.REPORTER, new ReporterFiller()));
		typeMap.put("summary", new WidgetTypeAndFieldFiller(WidgetType.SUMMARY, new SummaryFiller()));
		typeMap.put("timetracking", new WidgetTypeAndFieldFiller(WidgetType.TIME_SPENT, new TimeTrackingFiller()));
		typeMap.put("assignee", new WidgetTypeAndFieldFiller(WidgetType.ASSIGNEE, new AssigneeFiller()));
	}

	private JiraActionFieldType() {

	}

	public static WidgetType getFiledTypeForFieldId(@NotNull JIRAActionField field) {
		return getFiledTypeForFieldId(field.getFieldId());
	}

	public static WidgetType getFiledTypeForFieldId(String fieldId) {
		if (typeMap.containsKey(fieldId)) {
			return typeMap.get(fieldId).getWidgetType();
		}
		return WidgetType.UNSUPPORTED;
	}

	public static List<JIRAActionField> fillFieldValues(JIRAServerFacade facade, JIRAIssue issue,
			List<JIRAActionField> fields) throws JIRAException {
		List<JIRAActionField> result = new ArrayList<JIRAActionField>();

		// we should already have detailed issue here
//		JIRAIssue detailedIssue = facade.getIssueDetails(issue.getServer(), issue);

		for (JIRAActionField field : fields) {
			JIRAActionField filledField = fillField(issue, field);
			result.add(filledField);
		}

		return result;
	}

	private static JIRAActionField fillField(JIRAIssue issue, final JIRAActionField field) {
		WidgetTypeAndFieldFiller widgetTypeAndFieldFiller = typeMap.get(field.getFieldId());
		JIRAActionFieldBean result = new JIRAActionFieldBean(field);
		if (widgetTypeAndFieldFiller != null) {
			result.setValues(widgetTypeAndFieldFiller.getFiller().getFieldValues(field.getFieldId(), issue));
		} else {
			result.setValues(customFieldFiller.getFieldValues(field.getFieldId(), issue));
		}
		return result;
	}
}
