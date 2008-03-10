package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.idea.bamboo.table.columns.*;
import com.intellij.util.ui.ColumnInfo;

public class BambooTableColumnProvider {
	private BambooTableColumnProvider() {		
	}

	public static ColumnInfo[] makeColumnInfo() {
		return new ColumnInfo[]{
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
}