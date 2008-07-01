/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.atlassian.theplugin.commons.SubscribedPlan;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooPlanData;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.configuration.ServerBean;
import com.atlassian.theplugin.commons.configuration.SubscribedPlanBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.eclipse.EclipseActionScheduler;
import com.atlassian.theplugin.eclipse.util.PluginUtil;

/**
 * A field editor for a string type preference.
 * <p>
 * This class may be used as is, or subclassed as required.
 * </p>
 */
public class MyTableFieldEditor extends FieldEditor {

	/**
     * Cached valid state.
     */
    private boolean isValid;

    /**
     * Old text value.
     */
    private String oldValue;

     /**
     * The error message, or <code>null</code> if none.
     */
    private String errorMessage;

	private Table table;

	private Collection<BambooPlan> serverPlans;

	private Set<SubscribedPlan> subscribedPlans;

    /**
     * Creates a new string field editor 
     */
    protected MyTableFieldEditor() {
    }

    /**
     * Creates a string field editor of unlimited width.
     * Use the method <code>setTextLimit</code> to limit the text.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
    public MyTableFieldEditor(String name, String labelText, Composite parent) {
    	init(name, labelText);
        isValid = false;
        errorMessage = JFaceResources
                .getString("StringFieldEditor.errorMessage");//$NON-NLS-1$
        createControl(parent);
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void adjustForNumColumns(int numColumns) {
        GridData gd = (GridData) table.getLayoutData();
        gd.horizontalSpan = numColumns - 1;
        // We only grab excess space if we have to
        // If another field editor has more columns then
        // we assume it is setting the width.
        gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
    }

    /**
     * Checks whether the text input field contains a valid value or not.
     *
     * @return <code>true</code> if the field value is valid,
     *   and <code>false</code> if invalid
     */
    protected boolean checkState() {
    	return true;
    }

    /**
     * Hook for subclasses to do specific state checks.
     * <p>
     * The default implementation of this framework method does
     * nothing and returns <code>true</code>.  Subclasses should 
     * override this method to specific state checks.
     * </p>
     *
     * @return <code>true</code> if the field value is valid,
     *   and <code>false</code> if invalid
     */
    protected boolean doCheckState() {
        return true;
    }

    /**
     * Fills this field editor's basic controls into the given parent.
     * <p>
     * The string field implementation of this <code>FieldEditor</code>
     * framework method contributes the text field. Subclasses may override
     * but must call <code>super.doFillIntoGrid</code>.
     * </p>
     */
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        
    	Label label = getLabelControl(parent);
    	GridData gdLabel = new GridData();
    	gdLabel.verticalAlignment = GridData.BEGINNING;
    	label.setLayoutData(gdLabel);
        
        table = getTableControl(parent);

        GridData gd = new GridData();
        gd.horizontalSpan = numColumns - 1;
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.heightHint = convertVerticalDLUsToPixels(table, 90);
        gd.verticalAlignment = GridData.FILL;

        table.setLayoutData(gd);
    }


	/* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doLoad() {
    	
    	this.subscribedPlans = 
    		subscribedPlansString2Collection(getPreferenceStore().getString(getPreferenceName()));
    	
    	loadBuildPlans();
    	
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doLoadDefault() {
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doStore() {
        getPreferenceStore().setValue(getPreferenceName(), getStringValue());
    }

    /**
     * Returns the error message that will be displayed when and if 
     * an error occurs.
     *
     * @return the error message, or <code>null</code> if none
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    public int getNumberOfControls() {
        return 2;
    }

    /**
     * Returns the field editor's value.
     *
     * @return the current value
     */
    private String getStringValue() {
        if (table != null) {
        	
        	StringBuffer ret = new StringBuffer();
        	
        	for (TableItem item : table.getItems()) {
        		if (item.getChecked()) {
        			ret.append(item.getText(2));
        			ret.append(" ");
        		}
        	}
        	
			return ret.toString();
		}
        
        return getPreferenceStore().getString(getPreferenceName());
    }

    /**
     * Returns this field editor's text control.
     *
     * @return the text control, or <code>null</code> if no
     * text field is created yet
     */
    protected Table getTableControl() {
        return table;
    }
    
    private Table getTableControl(Composite parent) {
		if (table == null) {

			int style = SWT.CHECK | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL 
							| SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

			table = new Table(parent, style);
			
			TableColumn tableColumn;
			
			for (int i = 0 ; i < Column.values().length ; ++i) {
				Column column = Column.values()[i];
				
				tableColumn = new TableColumn(table, SWT.LEFT);
				tableColumn.setText(column.columnName());
				tableColumn.setWidth(column.columnWidth());
				tableColumn.setMoveable(false);
				tableColumn.setResizable(false);
				
			}

			table.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					table = null;
				}
			});
			
		} else {
			checkParent(table, parent);
		}

		return table;
	}

    /*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
    public boolean isValid() {
        return isValid;
    }

    /*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
    protected void refreshValidState() {
        isValid = checkState();
    }

    /**
     * Sets the error message that will be displayed when and if 
     * an error occurs.
     *
     * @param message the error message
     */
    public void setErrorMessage(String message) {
        errorMessage = message;
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    public void setFocus() {
        if (table != null) {
            table.setFocus();
        }
    }

//    /**
//     * Sets this field editor's value.
//     *
//     * @param value the new value, or <code>null</code> meaning the empty string
//     */
//    public void setStringValue(String value) {
//        if (table != null) {
//            if (value != null) {
//				
//            	
//            	
//            	
//			}
//            //oldValue = textField.getText();
//            if (!oldValue.equals(value)) {
//            //    textField.setText(value);
//                valueChanged();
//            }
//        }
//    }

    /**
     * Shows the error message set via <code>setErrorMessage</code>.
     */
    public void showErrorMessage() {
        showErrorMessage(errorMessage);
    }

    /**
     * Informs this field editor's listener, if it has one, about a change
     * to the value (<code>VALUE</code> property) provided that the old and
     * new values are different.
     * <p>
     * This hook is <em>not</em> called when the text is initialized 
     * (or reset to the default value) from the preference store.
     * </p>
     */
    protected void valueChanged() {
        setPresentsDefaultValue(false);
        boolean oldState = isValid;
        refreshValidState();

        if (isValid != oldState) {
			fireStateChanged(IS_VALID, oldState, isValid);
		}

        String newValue = ""; //textField.getText();
        if (!newValue.equals(oldValue)) {
            fireValueChanged(VALUE, oldValue, newValue);
            oldValue = newValue;
        }
    }

    /*
     * @see FieldEditor.setEnabled(boolean,Composite).
     */
    public void setEnabled(boolean enabled, Composite parent) {
        super.setEnabled(enabled, parent);
        getTableControl(parent).setEnabled(enabled);
    }

    /**
     * Sets plans (fills the whole list). 
     * @param allPlans
     */
	private void setPlans(Collection<BambooPlan> allPlans) {
		this.serverPlans = allPlans;
		fillTable();
	}
	
	private void fillTable() {
		table.clearAll();
		
		TableItem item;
		
		// add server plans to the list
		for (BambooPlan plan : serverPlans) {
			item = new TableItem(table, SWT.NONE);
			
			// check the plan if was subscribed
			if (subscribedPlans.contains(new SubscribedPlanBean(plan.getPlanKey()))) {
				item.setChecked(true);
			}
			
			if (plan.isFavourite()) {
				item.setImage(1, PluginUtil.getImageRegistry().get(PluginUtil.ICON_FAVOURITE_ON));
			} else {
				item.setImage(1, PluginUtil.getImageRegistry().get(PluginUtil.ICON_FAVOURITE_OFF));
			}
			item.setText(2, plan.getPlanKey());
		}
		
		// add to the list subscribed plans which do not exist on the server (with grey icon)  
		for (SubscribedPlan plan : subscribedPlans) {
			if (! serverPlans.contains(new BambooPlanData(plan.getPlanId()))) {
				item = new TableItem(table, SWT.NONE);
				item.setChecked(true);
				item.setImage(1, PluginUtil.getImageRegistry().get(PluginUtil.ICON_BAMBOO_UNKNOWN));
				item.setText(2, plan.getPlanId());
				//item.setGrayed(true);
				
			}
		}
		

	}

	private Set<SubscribedPlan> subscribedPlansString2Collection(String subscribedPlans) {
		Set<SubscribedPlan> plansList = new HashSet<SubscribedPlan>();
		
		String[] plansArray = subscribedPlans.split(" ");
		
		for (String plan : plansArray) {
			if (plan != null && plan.length() > 0) {
				SubscribedPlanBean subscribedPlan = new SubscribedPlanBean(plan);
				plansList.add(subscribedPlan);
			}
		}
		
		return plansList;
	}

	private enum Column {
		WATCHED ("Watched", 25),
		FAVOURITE ("Favourite", 20),
		PLAN_KEY ("Plan Key", 150);

		private String columnName;
		private int columnWidth;

		Column(String columnName, int columnWidth) {
			this.columnName = columnName;
			this.columnWidth = columnWidth;
		}
		
		public static Column valueOfAlias(String text) {
			for (Column column : Column.values()) {
				if (column.columnName().equals(text)) {
					return column;
				}
			}
			return null;
		}

		public String columnName() {
			return columnName;
		}

		public int columnWidth() {
			return columnWidth;
		}
		
		public static String[] getNames() {
			ArrayList<String> list = new ArrayList<String>(Column.values().length);
			for (Column column : Column.values()) {
				list.add(column.name());
			}
			return list.toArray(new String[0]);
		}
	}


	private void loadBuildPlans() {
		
		//TODO add refresh button to retrieve plan keys manually (e.g. in case server has changed)
		
		Job planListJob = new Job("Atlassian Bamboo plans") {

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
					
					final Collection<BambooPlan> finalPlans = plans;
				
					EclipseActionScheduler.getInstance().invokeLater(new Runnable() {
						public void run() {
							MyTableFieldEditor.this.setPlans(finalPlans);
						}
					});
				}
	
				return Status.OK_STATUS;
			}
		};
		
		planListJob.schedule();
	}
}
	


