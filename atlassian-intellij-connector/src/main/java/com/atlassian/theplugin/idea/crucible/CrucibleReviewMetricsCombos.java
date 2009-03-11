package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * User: jgorycki
 * Date: Mar 11, 2009
 * Time: 3:23:20 PM
 */
public class CrucibleReviewMetricsCombos {

	private Map<String, JComboBox> combos = new HashMap<String, JComboBox>();
	private final ReviewAdapter review;
	private final CommentBean comment;
	private final JPanel parent;

	public CrucibleReviewMetricsCombos(ReviewAdapter review, CommentBean comment,
									   Collection<CustomFieldDef> metrics, JPanel parent) {
		this.review = review;
		this.comment = comment;
		this.parent = parent;
		for (CustomFieldDef metric : metrics) {
			final JLabel label = new JLabel(metric.getLabel());
			final JComboBox combo = new JComboBox();
			final String metricName = metric.getLabel();
			combo.addItem("Select " + metricName);
			for (CustomFieldValue value : metric.getValues()) {
				combo.addItem(value.getName());
			}
			combo.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					setMetricField(combo, metricName);
				}
			});
			combos.put(metricName, combo);
			parent.add(label);
			parent.add(combo);
		}
	}

	public void showMetricCombos(boolean visible) {
		for (String key : comment.getCustomFields().keySet()) {
			if (combos.get(key) != null) {
				combos.get(key).setSelectedItem(comment.getCustomFields().get(key).getValue());
				combos.get(key).setVisible(visible);
			}
		}
		parent.setVisible(visible);
	}

	private void setMetricField(JComboBox combo, String field) {
		if (comment == null) {
			return;
		}
		CustomField oldCf = comment.getCustomFields().get(field);
		if (oldCf != null) {
			comment.getCustomFields().remove(oldCf);
		}
		if (combo.getSelectedIndex() > 0) {
			CustomFieldBean cf = getCustomFieldFromCombo(combo);
			comment.getCustomFields().put(field, cf);
		}
	}

	private CustomFieldBean getCustomFieldFromCombo(JComboBox combo) {
		CustomFieldBean cf = new CustomFieldBean();
		cf.setConfigVersion(review.getMetricsVersion());
		cf.setValue((String) combo.getSelectedItem());
		return cf;
	}

//	public Collection<CustomField> getCustomFields() {
//		List<CustomField> fields = new ArrayList<CustomField>();
//		for (JComboBox combo : combos.values()) {
//			if (combo.getSelectedIndex() > 0) {
//				CustomFieldBean cf = getCustomFieldFromCombo(combo);
//				fields.add(cf);
//			}
//		}
//		return fields;
//	}
}
