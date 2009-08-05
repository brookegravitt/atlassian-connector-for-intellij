package com.atlassian.theplugin.commons.jira;

import com.atlassian.theplugin.commons.jira.api.JIRAActionField;
import com.atlassian.theplugin.commons.jira.api.JIRAActionFieldBean;
import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import com.atlassian.theplugin.commons.jira.api.fields.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
		DUE_DATE,
		TIMETRACKING,
		VERSIONS,
		FIX_VERSIONS,
		RESOLUTION,
		PRIORITY,
		COMPONENTS,
		SECURITY
	}

	private static final class WidgetTypeAndFieldFiller {
		private final int sortOrder;
		private FieldFiller filler;
		private WidgetType widgetType;

		private WidgetTypeAndFieldFiller(WidgetType widgetType, int sortOrder, FieldFiller filler) {
			this.sortOrder = sortOrder;
			this.filler = filler;
			this.widgetType = widgetType;
		}

		public FieldFiller getFiller() {
			return filler;
		}

		public WidgetType getWidgetType() {
			return widgetType;
		}

		public int getSortOrder() {
			return sortOrder;
		}
	}

	private static Map<String, WidgetTypeAndFieldFiller> typeMap = new HashMap<String, WidgetTypeAndFieldFiller>();
	private static CustomFieldFiller customFieldFiller = new CustomFieldFiller();

	static {
		int i = 0;

		typeMap.put("summary", new WidgetTypeAndFieldFiller(WidgetType.SUMMARY, ++i, new SummaryFiller()));
		typeMap.put("resolution", new WidgetTypeAndFieldFiller(WidgetType.RESOLUTION, ++i, new ResolutionFiller()));
		typeMap.put("issuetype", new WidgetTypeAndFieldFiller(WidgetType.ISSUE_TYPE, ++i, new IssueTypeFiller()));
		typeMap.put("priority", new WidgetTypeAndFieldFiller(WidgetType.PRIORITY, ++i, new PriorityFiller()));
		typeMap.put("duedate", new WidgetTypeAndFieldFiller(WidgetType.DUE_DATE, ++i, new DueDateFiller()));
		typeMap.put("components", new WidgetTypeAndFieldFiller(WidgetType.COMPONENTS, ++i, new ComponentsFiller()));
		typeMap.put("versions", new WidgetTypeAndFieldFiller(WidgetType.VERSIONS, ++i, new AffectsVersionsFiller()));
		typeMap.put("fixVersions", new WidgetTypeAndFieldFiller(WidgetType.FIX_VERSIONS, ++i, new FixVersionsFiller()));
		typeMap.put("assignee", new WidgetTypeAndFieldFiller(WidgetType.ASSIGNEE, ++i, new AssigneeFiller()));
		typeMap.put("reporter", new WidgetTypeAndFieldFiller(WidgetType.REPORTER, ++i, new ReporterFiller()));
		typeMap.put("environment", new WidgetTypeAndFieldFiller(WidgetType.ENVIRONMENT, ++i, new EnvironmentFiller()));
		typeMap.put("description", new WidgetTypeAndFieldFiller(WidgetType.DESCRIPTION, ++i, new DescriptionFiller()));
		typeMap.put("timetracking", new WidgetTypeAndFieldFiller(WidgetType.TIMETRACKING, ++i, new TimeTrackingFiller()));
		typeMap.put("security", new WidgetTypeAndFieldFiller(WidgetType.SECURITY, ++i, new SecurityFiller()));
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

	/**
	 * @param issue  must be detailed issue
	 * @param fields fields to pre-fill
	 * @return list of fields with original values from the server
	 */
	public static List<JIRAActionField> fillFieldValues(JIRAIssue issue, List<JIRAActionField> fields) {
		List<JIRAActionField> result = new ArrayList<JIRAActionField>();

		for (JIRAActionField field : fields) {
			JIRAActionField filledField = fillField(issue, field);
			if (filledField != null) {
				result.add(filledField);
			}
		}

		addTimeFields(issue, result);

		return result;
	}

	private static void addTimeFields(JIRAIssue issue, List<JIRAActionField> result) {
		String originalEstimate = issue.getOriginalEstimateInSeconds();
		String remainingEstimate = issue.getRemainingEstimateInSeconds();
		String timeSpent = issue.getTimeSpentInSeconds();

		if (originalEstimate != null) {
			JIRAActionField originalEstimateField = new JIRAActionFieldBean("timeoriginalestimate", "Original Estimate");
			originalEstimateField.addValue(originalEstimate);
			result.add(originalEstimateField);
		}
		if (remainingEstimate != null) {
			JIRAActionField remainingEstimateField = new JIRAActionFieldBean("timeestimate", "Remaining Estimate");
			remainingEstimateField.addValue(remainingEstimate);
			result.add(remainingEstimateField);
		}
		if (timeSpent != null) {
			JIRAActionField timeSpentField = new JIRAActionFieldBean("timespent", "Time Spent");
			timeSpentField.addValue(timeSpent);
			result.add(timeSpentField);
		}
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

	public static Collection<JIRAActionField> sortFieldList(List<JIRAActionField> fieldList) {

		int customFieldOffset = typeMap.size();
		Map<Integer, JIRAActionField> sorted = new TreeMap<Integer, JIRAActionField>();
		for (JIRAActionField field : fieldList) {
			if (typeMap.containsKey(field.getFieldId())) {
				sorted.put(typeMap.get(field.getFieldId()).getSortOrder(), field);
			} else {
				sorted.put(customFieldOffset++, field);
			}
		}

		return sorted.values();
	}
}
