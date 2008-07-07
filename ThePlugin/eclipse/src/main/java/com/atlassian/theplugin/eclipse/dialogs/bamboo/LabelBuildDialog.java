package com.atlassian.theplugin.eclipse.dialogs.bamboo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LabelBuildDialog {
	
	private Shell shell = null;
	
	private int returnCode = 0;
	private String returnText = null;

	private Label label = null;
	private Label label1 = null;
	private Label label2 = null;
	private Label label3 = null;
	private Label label4 = null;
	private Text text = null;

	private Composite compositeRowButtons;

	public LabelBuildDialog(Shell parent) {
		shell = new Shell(parent, SWT.BORDER | SWT.CLOSE);
		shell.setText("Label Build");
		initialize();
	}

	private void initialize() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		shell.setLayout(gridLayout);
		shell.setSize(new Point(383, 135));
		createRowUpper();
		createRowBottom();
		createButtons();
	}

	private void createButtons() {
		GridData gridData1 = new GridData();
		gridData1.horizontalSpan = 4;
		gridData1.widthHint = 200;
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 5;
		compositeRowButtons = new Composite(shell, SWT.NONE);
		compositeRowButtons.setLayout(gridLayout2);
		Button buttonOk = new Button(compositeRowButtons, SWT.NONE);
		buttonOk.setText("OK");
		buttonOk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				LabelBuildDialog.this.returnCode = SWT.OK;
				LabelBuildDialog.this.returnText = text.getText();
				shell.close();
			}
		});
		
		Button buttonCancel = new Button(compositeRowButtons, SWT.NONE);
		buttonCancel.setText("Cancel");
		buttonCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				LabelBuildDialog.this.returnCode = SWT.CANCEL;
				shell.close();
			}
		});
		
		
	}

	/**
	 * This method initializes composite	
	 *
	 */
	private void createRowUpper() {
		Composite compositeRowUpper = null;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 4;
		GridData gridData = new GridData();
		compositeRowUpper = new Composite(shell, SWT.NONE);
		compositeRowUpper.setLayoutData(gridData);
		compositeRowUpper.setLayout(gridLayout1);
		label = new Label(compositeRowUpper, SWT.NONE);
		label.setText("Label");
		label2 = new Label(compositeRowUpper, SWT.NONE);
		label2.setText("Label");
		label3 = new Label(compositeRowUpper, SWT.NONE);
		label3.setText("Label");
		label4 = new Label(compositeRowUpper, SWT.NONE);
		label4.setText("Label");
	}

	/**
	 * This method initializes composite1	
	 *
	 */
	private void createRowBottom() {
		Composite compositeRowBottom = null;
		GridData gridData1 = new GridData();
		gridData1.horizontalSpan = 4;
		gridData1.widthHint = 200;
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 5;
		compositeRowBottom = new Composite(shell, SWT.NONE);
		compositeRowBottom.setLayout(gridLayout2);
		label1 = new Label(compositeRowBottom, SWT.NONE);
		label1.setText("Label");
		text = new Text(compositeRowBottom, SWT.BORDER);
		text.setLayoutData(gridData1);
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

	public String getLabel() {
		return returnText;
	}

}
