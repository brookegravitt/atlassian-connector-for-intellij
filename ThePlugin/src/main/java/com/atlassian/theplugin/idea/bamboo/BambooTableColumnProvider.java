package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.bamboo.table.columns.*;
import com.atlassian.theplugin.idea.bamboo.table.renderer.BuildNumberCellRenderer;
import com.atlassian.theplugin.idea.bamboo.table.renderer.DateTableCellRenderer;

import javax.swing.table.TableCellRenderer;

public final class BambooTableColumnProvider {
	private BambooTableColumnProvider() {		
	}

	public static TableColumnInfo[] makeColumnInfo() {
		return new TableColumnInfo[]{
				new BuildStatusColumn(),
				new BuildKeyColumn(),
				new BuildNumberColumn(),
				new BuildDateColumn(),
				new ProjectKeyColumn(),
				new BuildTestRatioColumn(),
				new BuildReasonColumn(),
				new BuildServerColumn(),
				new BuildErrorMessageColumn()
		};
	}

	public static TableCellRenderer[] makeRendererInfo() {
		return new TableCellRenderer[]{
				null,
				null,
				new BuildNumberCellRenderer(),
				new DateTableCellRenderer(),
				null,
				null,
				null,
				null,
				null
		};
	}
}