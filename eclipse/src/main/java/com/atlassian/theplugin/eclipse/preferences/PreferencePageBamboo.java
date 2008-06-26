/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.eclipse.preferences;


import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import com.atlassian.theplugin.commons.configuration.BambooTooltipOption;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class PreferencePageBamboo
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public PreferencePageBamboo() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		//setDescription("Bamboo Configuration");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		
		addField(new RadioGroupFieldEditor(PreferenceConstants.BAMBOO_POPUP, "Show popup:", 1, 
			new String[][] { 
				{BambooTooltipOption.ALL_FAULIRES_AND_FIRST_SUCCESS.toString(), BambooTooltipOption.ALL_FAULIRES_AND_FIRST_SUCCESS.name() }, 
				{BambooTooltipOption.FIRST_FAILURE_AND_FIRST_SUCCESS.toString(), BambooTooltipOption.FIRST_FAILURE_AND_FIRST_SUCCESS.name() },
				{BambooTooltipOption.NEVER.toString(), BambooTooltipOption.NEVER.name()}, 
		}, getFieldEditorParent()));
		
		MyIntegerFieldEditor pollingTime = new MyIntegerFieldEditor(PreferenceConstants.BAMBOO_POLLING_TIME, "Polling Time [min]:", getFieldEditorParent(), 3);
		pollingTime.setValidRange(1, 999);
		addField(pollingTime);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}