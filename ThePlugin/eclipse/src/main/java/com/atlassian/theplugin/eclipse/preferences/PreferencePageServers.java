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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.configuration.ServerBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.eclipse.EclipseActionScheduler;
import com.atlassian.theplugin.eclipse.util.PluginUtil;

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

public class PreferencePageServers
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public PreferencePageServers() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Bamboo Configuration");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new StringFieldEditor(PreferenceConstants.BAMBOO_NAME, "Server Name:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.BAMBOO_URL, "Server Url:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.BAMBOO_USER_NAME, "User Name:", getFieldEditorParent()));
		addField(new PasswordFieldEditor(PreferenceConstants.BAMBOO_USER_PASSWORD, "Password:", getFieldEditorParent()));
		
		StringFieldEditor builds = new StringFieldEditor(PreferenceConstants.BAMBOO_BUILDS, "Builds:", getFieldEditorParent());
		builds.getLabelControl(getFieldEditorParent()).setToolTipText("Space separated build names");
		addField(builds);
		
		final MyTableFieldEditor myTableFieldEditor = new MyTableFieldEditor("BUILDSXXX", "Builds:", getFieldEditorParent());
		addField(myTableFieldEditor);
//		Activator.getDefault().getPluginPreferences().addPropertyChangeListener(new IPropertyChangeListener() {
//			public void propertyChange(PropertyChangeEvent event) {
//				if (event.getProperty().equals(PreferenceConstants.BAMBOO_URL)
//			}
//		});
		
		// TODO add refresh button to retrieve plan keys manually (e.g. in case server has changed)
		Job planListJob = new Job("Atlassian Bamboo") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				BambooServerFacade bambooFacade = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger());
				
				Collection<ServerBean> servers = Activator.getDefault().getPluginConfiguration().getBambooConfigurationData().getServersData();
				Iterator<ServerBean> iterator = servers.iterator();
				
				// we take only first server right now
				if (iterator.hasNext()) {
					Collection<BambooPlan> plans = new ArrayList<BambooPlan>(0);
					try {
						plans = bambooFacade.getPlanList(iterator.next());
					} catch (ServerPasswordNotProvidedException e1) {
						e1.printStackTrace();
					} catch (RemoteApiException e1) {
						e1.printStackTrace();
					}
				
					StringBuffer plansString = new StringBuffer();
					for (BambooPlan plan : plans) {
						plansString.append(plan.getPlanKey());
						plansString.append(" ");
					}
					//System.out.println(plansString.toString());
					// myTableFieldEditor.setStringValue(plans.toString());
					
					final Collection<BambooPlan> allPlans = plans;
					
					EclipseActionScheduler.getInstance().invokeLater(new Runnable() {
						public void run() {
							myTableFieldEditor.setPlans(allPlans);
						}
					});
					
				}
				
			
				return Status.OK_STATUS;
			}
		};
		
		planListJob.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}