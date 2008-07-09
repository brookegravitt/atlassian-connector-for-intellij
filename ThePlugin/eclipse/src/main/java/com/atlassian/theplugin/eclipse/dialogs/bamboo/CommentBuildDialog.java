package com.atlassian.theplugin.eclipse.dialogs.bamboo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.atlassian.theplugin.eclipse.view.bamboo.BambooBuildAdapterEclipse;

public class CommentBuildDialog {
	
	private Shell shell = null;
	
	private int returnCode = 0;
	private String returnText = null;

	private String buildPlan = null;
	private Text comment = null;

	private Composite compositeRowButtons;

	public CommentBuildDialog(Shell parent, BambooBuildAdapterEclipse build) {
		shell = new Shell(parent, SWT.BORDER | SWT.CLOSE | SWT.APPLICATION_MODAL);
		shell.setText("Comment Build");
		// place the window in the center of parent
		shell.setLocation(
				parent.getLocation().x + parent.getSize().x/2, 
				parent.getLocation().y + parent.getSize().y/2);
		
		this.buildPlan = build.getBuildKey() + " " + build.getBuildNumber();
		
		initialize();
	}

	private void initialize() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		shell.setLayout(gridLayout);
		
		shell.setSize(320,230);
		createRowUpper();
		createRowBottom();
		createButtons();
	}

	private void createButtons() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		compositeRowButtons = new Composite(shell, SWT.NONE);
		compositeRowButtons.setLayout(gridLayout);
		Button buttonOk = new Button(compositeRowButtons, SWT.NONE);
		buttonOk.setText("Add comment");
		
		buttonOk.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				CommentBuildDialog.this.returnCode = SWT.OK;
				CommentBuildDialog.this.returnText = comment.getText();
				shell.close();
			}
			
		});
		
		shell.setDefaultButton(buttonOk);
		
		Button buttonCancel = new Button(compositeRowButtons, SWT.NONE);
		buttonCancel.setText("Cancel");
		buttonCancel.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				CommentBuildDialog.this.returnCode = SWT.CANCEL;
				shell.close();
			}
			
		});
	}

	private void createRowUpper() {
		Composite compositeRowUpper = null;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		//GridData gridData = new GridData();
		compositeRowUpper = new Composite(shell, SWT.NONE);
		//compositeRowUpper.setLayoutData(gridData);
		compositeRowUpper.setLayout(gridLayout);
		Label text = new Label(compositeRowUpper, SWT.NONE);
		text.setText("Add comment to build " + this.buildPlan);
	}

	private void createRowBottom() {
		Composite compositeRowBottom = null;
		GridData gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.widthHint = 260;
		gridData.heightHint = 100;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		compositeRowBottom = new Composite(shell, SWT.NONE);
		compositeRowBottom.setLayout(gridLayout);
		comment = new Text(compositeRowBottom, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		comment.setLayoutData(gridData);
	}
	
	public void open() {
		shell.open();
		Display display = shell.getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
	}

	public int getReturnCode() {
		return returnCode;
	}

	public String getComment() {
		return returnText;
	}

}
