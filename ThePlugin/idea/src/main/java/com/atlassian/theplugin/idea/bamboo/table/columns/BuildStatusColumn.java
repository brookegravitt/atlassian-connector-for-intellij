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

import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.atlassian.theplugin.idea.TableColumnInfo;

import javax.swing.*;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Mar 10, 2008
 * Time: 12:24:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class BuildStatusColumn extends TableColumnInfo {
	private static final int COL_ICON_WIDTH = 20;

	public String getColumnName() {
		return "";
	}

	public Object valueOf(Object o) {
		return ((BambooBuildAdapterIdea) o).getBuildIcon();
	}

	public Class getColumnClass() {
		return Icon.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((BambooBuildAdapterIdea) o).getStatus().compareTo(((BambooBuildAdapterIdea) o1).getStatus());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_ICON_WIDTH;
	}

}
