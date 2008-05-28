/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.ui.TableColumnProvider;
import com.atlassian.theplugin.idea.crucible.table.column.*;

import javax.swing.table.TableCellRenderer;

public final class CrucibleTableColumnProviderImpl implements TableColumnProvider {
	public CrucibleTableColumnProviderImpl() {
	}

	public TableColumnInfo[] makeColumnInfo() {
		return new TableColumnInfo[]{
				new ReviewKeyColumn(),
				new ReviewSummaryColumn(),
				new ReviewAuthorColumn(),
				new ReviewStateColumn(),
				new ReviewReviewersColumn()
		};
	}

	public TableCellRenderer[] makeRendererInfo() {
		return new TableCellRenderer[]{
				null,
				null,
				null,
				null,
				null
		};
	}
}