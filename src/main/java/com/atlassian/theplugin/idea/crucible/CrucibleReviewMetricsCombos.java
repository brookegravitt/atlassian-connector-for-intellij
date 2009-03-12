package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldBean;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldValue;
import com.atlassian.theplugin.commons.util.MiscUtil;

import javax.swing.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: jgorycki
 * Date: Mar 11, 2009
 * Time: 3:23:20 PM
 */
public class CrucibleReviewMetricsCombos {

	private Map<String, JComboBox> combos = new HashMap<String, JComboBox>();

	private final CommentBean comment;
	private final JPanel parent;

	public CrucibleReviewMetricsCombos(CommentBean comment,
			Collection<CustomFieldDef> metrics, JPanel parent) {
		this.comment = comment;
		this.parent = parent;
		for (CustomFieldDef metric : metrics) {
			final JLabel label = new JLabel(metric.getLabel());
			final JComboBox combo = new JComboBox();
			combo.setModel(new CustomFieldComboBoxModel(comment, metric));
			final String metricName = metric.getLabel();
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

	private class CustomFieldValueWrapper extends CustomFieldValue {
		private CustomFieldValueWrapper(final String name, final Object value) {
			super(name, value);
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	private class CustomFieldComboBoxModel extends AbstractListModel implements ComboBoxModel {
		private Collection<CustomFieldValueWrapper> data;
		private final Comment comment;
		private CustomFieldDef customFieldDef;
		private CustomFieldValueWrapper NONE = new CustomFieldValueWrapper("None", "none");

		private CustomFieldComboBoxModel(Comment comment, CustomFieldDef customFieldDef) {
			this.comment = comment;
			this.customFieldDef = customFieldDef;
		}

		private Collection<CustomFieldValueWrapper> getCustomFields() {
			if (data == null) {
				data = MiscUtil.buildArrayList();
				for (CustomFieldValue value : customFieldDef.getValues()) {
					data.add(new CustomFieldValueWrapper(value.getName(), value.getValue()));
				}
			}
			return data;
		}

		public void setSelectedItem(final Object anItem) {
			final Object selectedItem = getSelectedItem();
			if (selectedItem != null && !selectedItem.equals(anItem) || selectedItem == null && anItem != null) {
				if (anItem != null) {
					CustomFieldValue item = (CustomFieldValue) anItem;
					setMetricField(customFieldDef, item);
				} else {
					setMetricField(customFieldDef, NONE);
				}
				fireContentsChanged(this, -1, -1);
			}
		}

		private void setMetricField(CustomFieldDef field, CustomFieldValue value) {
			if (comment == null) {
				return;
			}
			CustomField oldCf = comment.getCustomFields().get(customFieldDef.getName());
			if (oldCf != null) {
				comment.getCustomFields().remove(customFieldDef.getName());
			}
			if (!value.getName().equals(NONE.getName())) {
				CustomFieldBean newField = new CustomFieldBean();
				newField.setConfigVersion(field.getConfigVersion());
				newField.setValue(value.getName());
				comment.getCustomFields().put(field.getName(), newField);
			}
		}

		public Object getSelectedItem() {
			CustomField v = comment.getCustomFields().get(customFieldDef.getName());
			if (v != null) {
				return new CustomFieldValueWrapper(v.getValue(), v.getValue());
			}
			return NONE;
		}

		public Object getElementAt(final int index) {
			if (index == 0) {
				return NONE;
			}
			int i = 1;
			for (CustomFieldValue value : getCustomFields()) {
				if (i == index) {
					return value;
				}
				i++;
			}
			return null;
		}

		public int getSize() {
			return getCustomFields().size() + 1;
		}

	}
}
