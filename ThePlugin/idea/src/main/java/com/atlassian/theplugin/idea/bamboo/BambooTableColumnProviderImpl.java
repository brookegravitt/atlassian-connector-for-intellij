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

package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.bamboo.table.columns.BuildDateColumn;
import com.atlassian.theplugin.idea.bamboo.table.columns.BuildErrorMessageColumn;
import com.atlassian.theplugin.idea.bamboo.table.columns.BuildKeyColumn;
import com.atlassian.theplugin.idea.bamboo.table.columns.BuildNumberColumn;
import com.atlassian.theplugin.idea.bamboo.table.columns.BuildReasonColumn;
import com.atlassian.theplugin.idea.bamboo.table.columns.BuildServerColumn;
import com.atlassian.theplugin.idea.bamboo.table.columns.BuildStatusColumn;
import com.atlassian.theplugin.idea.bamboo.table.columns.BuildTestRatioColumn;
import com.atlassian.theplugin.idea.bamboo.table.columns.ProjectKeyColumn;
import com.atlassian.theplugin.idea.bamboo.table.renderer.BackgroundAwareBambooRenderer;
import com.atlassian.theplugin.idea.bamboo.table.renderer.BuildNumberCellRenderer;
import com.atlassian.theplugin.idea.bamboo.table.renderer.BuildTestRatioCellRenderer;
import com.atlassian.theplugin.idea.bamboo.table.renderer.DateTableCellRenderer;
import com.atlassian.theplugin.idea.bamboo.table.renderer.RightJustifyCellRenderer;
import com.atlassian.theplugin.idea.ui.TableColumnProvider;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

public class BambooTableColumnProviderImpl implements TableColumnProvider {
	public BambooTableColumnProviderImpl() {		
	}

	public TableColumnInfo[] makeColumnInfo() {
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

	public TableCellRenderer[] makeRendererInfo() {
		return new TableCellRenderer[]{
				new BackgroundAwareBambooRenderer() {
					{
						setHorizontalAlignment(JLabel.CENTER);
					}

					@Override
					public void setValue(Object value) {
						setIcon((value instanceof Icon) ? (Icon) value : null);
					}
				},
				new BackgroundAwareBambooRenderer(),
				new BuildNumberCellRenderer(),
				new DateTableCellRenderer(),
				new RightJustifyCellRenderer(),
				new BuildTestRatioCellRenderer(),
				new BackgroundAwareBambooRenderer(),
				new BackgroundAwareBambooRenderer(),
				new BackgroundAwareBambooRenderer()
		};
	}
}