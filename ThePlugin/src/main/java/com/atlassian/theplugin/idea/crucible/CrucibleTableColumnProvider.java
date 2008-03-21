package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.crucible.table.column.*;

import javax.swing.table.TableCellRenderer;

public final class CrucibleTableColumnProvider {
	private CrucibleTableColumnProvider() {
	}

	public static TableColumnInfo[] makeColumnInfo() {
		return new TableColumnInfo[]{
				new ReviewKeyColumn(),
				new ReviewSummaryColumn(),
				new ReviewAuthorColumn(),
				new ReviewStateColumn(),
				new ReviewReviewersColumn()
		};
	}

	public static TableCellRenderer[] makeRendererInfo() {
		return new TableCellRenderer[]{
				null,
				null,
				null,
				null,
				null
		};
	}
}