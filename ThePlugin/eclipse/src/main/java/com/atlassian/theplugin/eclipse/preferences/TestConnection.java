package com.atlassian.theplugin.eclipse.preferences;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.progress.WorkbenchJob;

import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.eclipse.EclipseActionScheduler;
import com.atlassian.theplugin.eclipse.util.PluginUtil;
import com.sun.corba.se.impl.orb.ParserTable.TestBadServerIdHandler;

public class TestConnection extends FieldEditor {


	private PreferencePageServers parentPreferencePage;

	public TestConnection(Composite parent,	PreferencePageServers preferencePageServers) {
		createControl(parent);
		
		this.parentPreferencePage = preferencePageServers;
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		
		Label label = getLabelControl(parent);
		GridData gdLabel = new GridData();
		gdLabel.verticalAlignment = GridData.BEGINNING;
		label.setLayoutData(gdLabel);

		Button testConnectionButton = getTestConnectionButton(parent); 
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns - 1;
		gd.horizontalAlignment = GridData.END;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = GridData.FILL;
		testConnectionButton.setLayoutData(gd);
	}

	private Button getTestConnectionButton(Composite parent) {
		
		Button button = new Button(parent, SWT.PUSH);
		button.setText("Test Connection");
		button.setToolTipText("Test connection using form values");
		
		button.addMouseListener(new TestConnectionListener());
		
		return button;
	}

	@Override
	protected void doLoad() {
	}

	@Override
	protected void doLoadDefault() {
	}

	@Override
	protected void doStore() {
	}

	@Override
	public int getNumberOfControls() {
		return 1;
	}
	
	private class TestConnectionListener extends MouseAdapter {

			@Override
		public void mouseUp(MouseEvent e) {
			super.mouseUp(e);

			final String url = parentPreferencePage.getBambooUrl();
			final String user = parentPreferencePage.getUserName();
			final String password = parentPreferencePage.getPassword();
			
//			try {
//				Activator.getDefault().getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
//
//					public void run(IProgressMonitor monitor)
//							throws InvocationTargetException, InterruptedException {
//						
//						Thread.sleep(5000);
//					}
//					
//
//				});
//			} catch (InvocationTargetException e1) {
//				e1.printStackTrace();
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
			
//			try {
//				new ProgressMonitorDialog(Activator.getDefault().getShell()).run(true, true, new IRunnableWithProgress() {
//
//					public void run(IProgressMonitor monitor)
//							throws InvocationTargetException, InterruptedException {
//						
//						monitor.beginTask("aaa", IProgressMonitor.UNKNOWN);
//						Thread.sleep(5000);
//						monitor.done();
//					}
//					
//				});
//			} catch (InvocationTargetException e1) {
//				e1.printStackTrace();
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
			

			
			Job job = new Job("Atlassian test connection") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					
					BambooServerFacade bambooFacade = BambooServerFacadeImpl
							.getInstance(PluginUtil.getLogger());

					int icon;
					String message = "";
					String title = "";

					try {
						bambooFacade.testServerConnection(url, user, password);
						icon = SWT.ICON_INFORMATION;
						message = "Connected successfully";
						title = "Connection OK";

					} catch (final RemoteApiException ex) {
						icon = SWT.ICON_ERROR;
						message = ex.getMessage();
						title = "Connection Error";
					}

					final String messageFinal = message;
					final String titleFinal = title;
					final int iconFinal = icon;

					EclipseActionScheduler.getInstance().invokeLater(
							new Runnable() {
								public void run() {
									MessageBox dialog = new MessageBox(
											Activator.getDefault().getShell(),
											SWT.OK | iconFinal);
									
									dialog.setMessage(messageFinal);
									dialog.setText(titleFinal);
									dialog.open();
								}
							});
					
					return Status.OK_STATUS;
				}
			};

			job.setUser(true);
			job.schedule();
		}
	}


}
