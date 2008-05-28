package com.atlassian.theplugin.eclipse.view.bamboo;


import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooStatusListener;

public class BambooToolWindowContent implements BambooStatusListener {

	private Collection<BambooBuildAdapter> buildStatuses = new ArrayList<BambooBuildAdapter>();
	private Table table;
	private TableViewer tableViewer;

	public BambooToolWindowContent(Composite parent) {
		
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | 
		SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		
		tableViewer = new TableViewer(parent, style);
		tableViewer.setContentProvider(new BambooContentProvider());
		tableViewer.setLabelProvider(new BambooLabelProvider());
		
		table = tableViewer.getTable();
		
		TableColumn tableColumn; 
		
		for (Column column : Column.values()) {
			tableColumn = new TableColumn(table, SWT.LEFT);
			tableColumn.setText(column.columnName());
			tableColumn.setWidth(100);
		}
		
		tableViewer.setInput(buildStatuses);
		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		//table.update();
		//table.pack(true);
		
	}
	
	public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {
		
		for (BambooBuild build : buildStatuses) {
			this.buildStatuses.add(new BambooBuildAdapter(build));
		}
		 
		tableViewer.setInput(buildStatuses);
		
	}

	public void resetState() {
	}

	private class BambooContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return buildStatuses.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
	}
	
	private class BambooLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			BambooBuildAdapter build = (BambooBuildAdapter) element;
			
			Column column = Column.valueOfAlias(table.getColumn(columnIndex).getText());
			
			switch (column) {
				
				case BUILD_DATE:
					return build.getBuildTime();
				case BUILD_NUMBER:
					return build.getBuildNumber();
				case BUILD_KEY:
					return build.getBuildKey();
				case BUILD_STATUS:
					return build.getStatus();
				case PROJECT_KEY:
					return build.getProjectKey();
				case BUILD_REASON:
					return build.getBuildReason();
				case MESSAGE:
					return build.getMessage();
				case PASSED_TESTS:
					return build.getTestsPassedSummary();
				case SERVER:
					return build.getServerConfigName();
				default:
					return "";
			}
			
		}

	}
	
	private enum Column {
		BUILD_STATUS ("Build Status"),
		BUILD_KEY ("Build Plan"),
		BUILD_NUMBER ("Build Number"),
		BUILD_DATE ("Build Date"),
		PROJECT_KEY ("Project"),
		PASSED_TESTS ("Passed Tests"),
		BUILD_REASON ("Reason"),
		SERVER ("Server"),
		MESSAGE ("Message");
		
		private String columnName;

		Column(String columnName) {
			this.columnName = columnName;
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
		
	}

}
