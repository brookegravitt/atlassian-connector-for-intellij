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

package com.atlassian.theplugin.idea.bamboo.table.columns;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;

import javax.swing.*;
import java.util.Comparator;

public class BuildStatusColumn extends TableColumnInfo<BambooBuildAdapterIdea, Icon> {
	private static final int COL_ICON_WIDTH = 20;
	private static final Comparator<BambooBuildAdapterIdea> COMPARATOR = new Comparator<BambooBuildAdapterIdea>() {
		public int compare(BambooBuildAdapterIdea o, BambooBuildAdapterIdea o1) {
			return o.getStatus().compareTo(o1.getStatus());
		}
	};

	@Override
	public String getColumnName() {
		return "";
	}

	@Override
	public Icon valueOf(BambooBuildAdapterIdea o) {
		return o.getBuildIcon();
	}

	@Override
	public Class<Icon> getColumnClass() {
		return Icon.class;
	}

	@Override
	public Comparator<BambooBuildAdapterIdea> getComparator() {
		return COMPARATOR;
	}

	@Override
	public int getPrefferedWidth() {
		return COL_ICON_WIDTH;
	}

}
