package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldBean;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldValue;
import com.atlassian.theplugin.commons.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

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
	private final Map<String, CustomField> customFields;
	private final JPanel parent;

	public CrucibleReviewMetricsCombos(final Map<String, CustomField> customFields,
			final Collection<CustomFieldDef> metrics, JPanel parent) {
		this.customFields = customFields;

		this.parent = parent;
		if (metrics != null) {
			for (CustomFieldDef metric : metrics) {
				final JLabel label = new JLabel(metric.getLabel());
				final JComboBox combo = new JComboBox();
				combo.setModel(new CustomFieldComboBoxModel(customFields, metric));
				final String metricName = metric.getLabel();
				combos.put(metricName, combo);
				parent.add(label);
				parent.add(combo);
			}
		}
	}

	public void showMetricCombos(boolean visible) {
		for (String key : customFields.keySet()) {
			if (combos.get(key) != null) {
				combos.get(key).setSelectedItem(customFields.get(key).getValue());
				combos.get(key).setVisible(visible);
			}
		}
		parent.setVisible(visible);
	}

	private final class CustomFieldValueWrapper extends CustomFieldValue {
		private CustomFieldValueWrapper(final String name, final Object value) {
			super(name, value);
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	private final class CustomFieldComboBoxModel extends AbstractListModel implements ComboBoxModel {
		private Collection<CustomFieldValueWrapper> data;
		private final Map<String, CustomField> customFields;
		private CustomFieldDef customFieldDef;
		private CustomFieldValueWrapper noneCustomField = new CustomFieldValueWrapper("None", "none");

		private CustomFieldComboBoxModel(Map<String, CustomField> customFields, CustomFieldDef customFieldDef) {
			this.customFields = customFields;
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
					setMetricField(customFieldDef, noneCustomField);
				}
				fireContentsChanged(this, -1, -1);
			}
		}

		private void setMetricField(CustomFieldDef field, CustomFieldValue value) {
			CustomField oldCf = customFields.get(customFieldDef.getName());
			if (oldCf != null) {
				customFields.remove(customFieldDef.getName());
			}
			if (!value.getName().equals(noneCustomField.getName())) {
				CustomFieldBean newField = new CustomFieldBean();
				newField.setConfigVersion(field.getConfigVersion());
				newField.setValue(value.getName());
				customFields.put(field.getName(), newField);
			} else {
				customFields.remove(field.getName());
			}
		}

		public Object getSelectedItem() {
			CustomField v = customFields.get(customFieldDef.getName());
			if (v != null) {
				return new CustomFieldValueWrapper(v.getValue(), v.getValue());
			}
			return noneCustomField;
		}

		@Nullable
		public Object getElementAt(final int index) {
			if (index == 0) {
				return noneCustomField;
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
