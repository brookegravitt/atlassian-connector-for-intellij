package com.atlassian.theplugin.eclipse.view.bamboo;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooStatusListener;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.eclipse.util.PluginUtil;

public class BambooToolWindowContent implements BambooStatusListener {

	private Collection<BambooBuildAdapterEclipse> buildStatuses = new ArrayList<BambooBuildAdapterEclipse>();
	private Table table;
	private TableViewer tableViewer;
	private final BambooToolWindow viewPart;

	public BambooToolWindowContent(Composite parent, final BambooToolWindow viewPart) {
		
		this.viewPart = viewPart;
		
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | 
		SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		
		tableViewer = new TableViewer(parent, style);
		tableViewer.setContentProvider(new BambooContentProvider());
		tableViewer.setLabelProvider(new BambooLabelProvider());
		
		tableViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						if (event.getSelection().isEmpty()) {
							viewPart.disableBambooBuildActions();
						} else {
							viewPart.enableBambooBuildActions();
						}
						
						//System.out.println(event.toString());
						
					}
				}
			);
		
		table = tableViewer.getTable();
		
		TableColumn tableColumn; 
		
		for (Column column : Column.values()) {
			tableColumn = new TableColumn(table, SWT.LEFT);
			tableColumn.setText(column.columnName());
			tableColumn.setWidth(column.columnWidth());
		}
		
		tableViewer.setInput(buildStatuses);
		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
	}
	
	public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {
		
		Date pollingTime = new Date();
		
		this.buildStatuses.clear();
		
		for (BambooBuild build : buildStatuses) {
			this.buildStatuses.add(new BambooBuildAdapterEclipse(build));
			pollingTime = build.getPollingTime();
		}
		 
		tableViewer.setInput(buildStatuses);
		viewPart.setHeader("Last polling time: " + pollingTime.toString());
		
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
			
			BambooBuildAdapterEclipse build = (BambooBuildAdapterEclipse) element;
			
			Column column = Column.valueOfAlias(table.getColumn(columnIndex).getText());
			
			if (column == Column.BUILD_STATUS) {
				switch (build.getStatus()) {
					case BUILD_SUCCEED:
						return PluginUtil.getImageRegistry().get(BuildStatus.BUILD_SUCCEED.toString());
					case BUILD_FAILED:
						return PluginUtil.getImageRegistry().get(BuildStatus.BUILD_FAILED.toString());
					case UNKNOWN:
						return PluginUtil.getImageRegistry().get(BuildStatus.UNKNOWN.toString());
					default:
						return null;
				}
			}
			
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			BambooBuildAdapterEclipse build = (BambooBuildAdapterEclipse) element;
			
			Column column = Column.valueOfAlias(table.getColumn(columnIndex).getText());
			
			switch (column) {
				
				case BUILD_DATE:
					return build.getBuildTimeFormated();
				case BUILD_NUMBER:
					return build.getBuildNumber();
				case BUILD_KEY:
					return build.getBuildKey();
				case BUILD_STATUS:
					return ""; //build.getStatus().toString();
				case PROJECT_KEY:
					return build.getProjectKey();
				case BUILD_REASON:
					return build.getBuildReason();
				case MESSAGE:
					return build.getMessage();
				case PASSED_TESTS:
					return build.getTestsPassedSummary();
				case SERVER:
					return build.getServerName();
				default:
					return "";
			}
			
		}

	}
	
	/**
	 * That class provides column names and width for bamboo tab table
	 */
	private enum Column {
		BUILD_STATUS ("", 30),
		BUILD_KEY ("Build Plan", 80),
		BUILD_NUMBER ("Build Number", 100),
		PROJECT_KEY ("Project", 100),
		BUILD_DATE ("Build Date", 100),
		PASSED_TESTS ("Passed Tests", 100),
		BUILD_REASON ("Reason", 100),
		SERVER ("Server", 100),
		MESSAGE ("Message", 200);
		
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
	}

}
