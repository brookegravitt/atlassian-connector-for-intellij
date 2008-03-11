package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.idea.bamboo.table.BambooColumnInfo;
import com.atlassian.theplugin.idea.bamboo.table.columns.*;
import com.atlassian.theplugin.idea.bamboo.table.renderer.DateTableCellRenderer;

import javax.swing.table.TableCellRenderer;

public final class BambooTableColumnProvider {
	private BambooTableColumnProvider() {		
	}

	public static BambooColumnInfo[] makeColumnInfo() {
		return new BambooColumnInfo[]{
				new BuildStatusColumn(),
				new BuildKeyColumn(),
				new BuildNumberColumn(),
				new BuildDateColumn(),
				new ProjectKeyColumn(),
				new BuildTestRatioColumn(),
				new BuildReasonColumn(),
				new BuildServerColumn()
		};
	}

	public static TableCellRenderer[] makeRendererInfo() {
		return new TableCellRenderer[]{
				null,
				null,
				null,
				new DateTableCellRenderer(),
				null,
				null,
				null,
				null
		};
	}
}