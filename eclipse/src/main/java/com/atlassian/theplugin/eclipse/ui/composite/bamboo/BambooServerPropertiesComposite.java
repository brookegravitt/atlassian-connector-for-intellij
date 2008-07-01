/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.composite.bamboo;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.atlassian.theplugin.eclipse.core.bamboo.BambooServer;
import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.ui.dialog.DefaultDialog;
import com.atlassian.theplugin.eclipse.ui.panel.IPropertiesPanel;
import com.atlassian.theplugin.eclipse.ui.utility.UserInputHistory;
import com.atlassian.theplugin.eclipse.ui.verifier.AbstractVerifier;
import com.atlassian.theplugin.eclipse.ui.verifier.CompositeVerifier;
import com.atlassian.theplugin.eclipse.ui.verifier.IValidationManager;

/**
 * Repository properties editor panel
 * 
 * @author Alexander Gurov
 */
public class BambooServerPropertiesComposite extends Composite implements IPropertiesPanel {
    protected static final String URL_HISTORY_NAME = "bambooServerURL";
    
	protected Text repositoryLabel;
	protected Combo url;
	protected CompositeVerifier urlVerifier;
	protected UserInputHistory urlHistory;
	protected Button browse;
	protected Button useLocationButton;
	protected Button newLabelButton;
	//protected CredentialsComposite credentialsComposite;
	
	protected BambooServer repositoryLocation;
	protected String rootUrl;
	
	protected IValidationManager validationManager;
	
	protected BambooServer credentialsInput;

	public BambooServerPropertiesComposite(Composite parent, int style, IValidationManager validationManager) {
		super(parent, style);

		this.validationManager = validationManager;
	}
	
	public void initialize() {
		GridLayout layout = null;
		GridData data = null;
		
		layout = new GridLayout();
		layout.marginHeight = 7;
		layout.verticalSpacing = 3;
		this.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		this.setLayoutData(data);
		
		Composite rootURLGroup = new Composite(this, SWT.NULL);
		layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		rootURLGroup.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		rootURLGroup.setLayoutData(data);
		
		Label description = new Label(rootURLGroup, SWT.NULL);
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		description.setLayoutData(data);
		description.setText(Activator.getDefault().getResource("RepositoryPropertiesComposite.URL"));
		
		this.urlHistory = new UserInputHistory(BambooServerPropertiesComposite.URL_HISTORY_NAME);
		
		this.url = new Combo(rootURLGroup, SWT.DROP_DOWN);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.url.setLayoutData(data);
		this.url.setVisibleItemCount(this.urlHistory.getDepth());
		this.url.setItems(this.urlHistory.getHistory());
		this.urlVerifier = new CompositeVerifier() {
			public boolean verify(Control input) {
				boolean retVal = super.verify(input);
				BambooServerPropertiesComposite.this.browse.setEnabled(retVal);
				return retVal;
			}
		};
		this.defineUrlVerifier(null);
		this.validationManager.attachTo(this.url, this.urlVerifier);
		
		this.browse = new Button (rootURLGroup, SWT.PUSH);
		this.browse.setText(Activator.getDefault().getResource("Button.Browse"));
		data = new GridData(GridData.HORIZONTAL_ALIGN_END);	
		data.widthHint = DefaultDialog.computeButtonWidth(this.browse);
		this.browse.setLayoutData(data);
		/*
		this.browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			    SVNRemoteStorage storage = SVNRemoteStorage.instance();
				IRepositoryLocation location = storage.newRepositoryLocation();
				location.setUrl(RepositoryPropertiesComposite.this.url.getText());
				location.setLabel(RepositoryPropertiesComposite.this.url.getText());
				location.setPassword(RepositoryPropertiesComposite.this.credentialsComposite.getPassword().getText());
				location.setUsername(RepositoryPropertiesComposite.this.credentialsComposite.getUsername().getText());
				location.setPasswordSaved(RepositoryPropertiesComposite.this.credentialsComposite.getSavePassword().getSelection());
				
				RepositoryBrowsingPanel panel = new RepositoryBrowsingPanel(Activator.getDefault().getResource("RepositoryPropertiesComposite.SelectNewURL"), location, Revision.HEAD);
				panel.setAutoexpandFirstLevel(true);
				DefaultDialog browser = new DefaultDialog(RepositoryPropertiesComposite.this.getShell(), panel);
				if (browser.open() == 0) {
					if (panel.getSelectedResource() != null) {
						String newUrl = panel.getSelectedResource().getUrl();
						RepositoryPropertiesComposite.this.url.setText(newUrl);
					}
				    RepositoryPropertiesComposite.this.credentialsComposite.getUsername().setText(location.getUsername());
				    RepositoryPropertiesComposite.this.credentialsComposite.getPassword().setText(location.getPassword());
				    RepositoryPropertiesComposite.this.credentialsComposite.getSavePassword().setSelection(location.isPasswordSaved());
				}
			}
		});*/
		
		Group labelGroup = new Group(this, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		layout = new GridLayout();
		labelGroup.setLayout(layout);
		labelGroup.setLayoutData(data);
		labelGroup.setText(Activator.getDefault().getResource("RepositoryPropertiesComposite.Label"));
		
		this.useLocationButton = new Button(labelGroup, SWT.RADIO);
		this.useLocationButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		/*
		this.useLocationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			    RepositoryPropertiesComposite.this.validationManager.validateContent();
				Button button = (Button)e.widget;
				RepositoryPropertiesComposite.this.repositoryLabel.setEnabled(!button.getSelection());
				if (!button.getSelection()) {
					RepositoryPropertiesComposite.this.repositoryLabel.selectAll();
				}
				else {
					RepositoryPropertiesComposite.this.repositoryLabel.setText("");
				}
			}
		});*/
		this.useLocationButton.setText(Activator.getDefault().getResource("RepositoryPropertiesComposite.UseURL"));
		
		this.newLabelButton = new Button(labelGroup, SWT.RADIO);
		this.newLabelButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.newLabelButton.setText(Activator.getDefault().getResource("RepositoryPropertiesComposite.UseCustom")); 
		
		this.repositoryLabel = new Text(labelGroup, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.repositoryLabel.setLayoutData(data);
		/*
		this.validationManager.attachTo(this.repositoryLabel, new AbstractVerifierProxy(
				new NonEmptyFieldVerifier(Activator.getDefault().getResource("RepositoryPropertiesComposite.UseCustom.Verifier"))) {
			protected boolean isVerificationEnabled(Control input) {
				return RepositoryPropertiesComposite.this.newLabelButton.getSelection();
			}			
		});*/
		this.repositoryLabel.setEnabled(false);
		
		//this.credentialsComposite = new CredentialsComposite(this, SWT.NONE);
		//this.credentialsComposite.initialize();
		
		this.url.setFocus();
		
		this.resetChanges();
	}
	
	public void setRepositoryLocation(BambooServer location, String rootUrl) {
		this.credentialsInput = this.repositoryLocation = location;
		this.rootUrl = rootUrl;
	}
	
	public BambooServer getRepositoryLocation() {
		return this.repositoryLocation;
	}
	
	public String getLocationUrl() {
		return this.url.getText();
	}
	
	public void setCredentialsInput(BambooServer location) {
		this.credentialsInput = location;
	}
	
	public void defineUrlVerifier(AbstractVerifier verifier) {
		/*
		String name = Activator.getDefault().getResource("RepositoryPropertiesComposite.URL.Verifier");
		this.urlVerifier.removeAll();
		this.urlVerifier.add(new URLVerifier(name));
		this.urlVerifier.add(new AbsolutePathVerifier(name));
		if (this.rootUrl != null) {
			this.urlVerifier.add(new AbstractFormattedVerifier(name) {
				protected Boolean relatedProjects;
				
				protected String getErrorMessageImpl(Control input) {
					return null;
				}
				protected String getWarningMessageImpl(Control input) {
					if (this.relatedProjects == null) {
						FindRelatedProjectsOperation op = new FindRelatedProjectsOperation(RepositoryPropertiesComposite.this.repositoryLocation);
						UIMonitorUtil.doTaskBusyDefault(op);
						this.relatedProjects = op.getResources() == null || op.getResources().length == 0 ? Boolean.FALSE : Boolean.TRUE;
					}
					if (this.relatedProjects == Boolean.FALSE) {
						return null;
					}
					String newUrl = this.getText(input);
					newUrl = SVNUtility.normalizeURL(newUrl);
					try {
						newUrl = SVNUtility.decodeURL(newUrl);
					}
					catch (Exception ex) {
						// is not encoded URL
					}
					if (!new Path(RepositoryPropertiesComposite.this.rootUrl).isPrefixOf(new Path(newUrl))) {
						return Activator.getDefault().getResource("RepositoryPropertiesComposite.URL.Verifier.Warning");
					}
					return null;
				}
			});
		}

		if (verifier != null) {
			this.urlVerifier.add(verifier);
		}*/
	}
	
	public void saveChanges() {
		/*if (this.useLocationButton.getSelection()) {
			this.repositoryLocation.setLabel(this.url.getText());
		}
		else {
			this.repositoryLocation.setLabel(this.repositoryLabel.getText());
		}
		String newUrl = this.url.getText();
		this.urlHistory.addLine(newUrl);
		this.repositoryLocation.setUrl(newUrl);

		this.credentialsComposite.getUserHistory().addLine(this.credentialsComposite.userName.getText());
		
		this.credentialsInput.setUsername(this.credentialsComposite.getUsername().getText());
		this.credentialsInput.setPassword(this.credentialsComposite.getPassword().getText());
		this.credentialsInput.setPasswordSaved(this.credentialsComposite.getSavePassword().getSelection());
		*/
	}
	
	public void resetChanges() {
		/*String url = this.repositoryLocation.getUrlAsIs();
		url = url == null ? "" : url;
		if (this.repositoryLocation.getLabel() == null || 
			this.repositoryLocation.getLabel().equalsIgnoreCase(this.repositoryLocation.getUrlAsIs()) ||
			this.repositoryLocation.getLabel().equalsIgnoreCase(this.repositoryLocation.getUrl())) {
			this.repositoryLabel.setText(url);
			this.useLocationButton.setSelection(true);
			this.newLabelButton.setSelection(false);
		}
		else {
			this.repositoryLabel.setText(this.repositoryLocation.getLabel());
			this.useLocationButton.setSelection(false);
			this.newLabelButton.setSelection(true);
		}
		RepositoryPropertiesComposite.this.repositoryLabel.setEnabled(!this.useLocationButton.getSelection());
		this.url.setText(url);

		String username = this.credentialsInput.getUsername();
		this.credentialsComposite.getUsername().setText(username == null ? "" : username);
		String password = this.credentialsInput.getPassword();
		this.credentialsComposite.getPassword().setText(password == null ? "" : password);
		
		this.credentialsComposite.getSavePassword().setSelection(this.credentialsInput.isPasswordSaved());
		*/
	}
	
	public void cancelChanges() {
		
	}
	
}
