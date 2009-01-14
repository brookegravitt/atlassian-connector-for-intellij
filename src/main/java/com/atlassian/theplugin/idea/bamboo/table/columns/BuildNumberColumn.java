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

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Mar 10, 2008
 * Time: 12:24:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class BuildNumberColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 70;

	@Override
	public String getColumnName() {
		return "Build No";
	}

	@Override
	public Object valueOf(Object o) {
		if (((BambooBuildAdapterIdea) o).getBuildNumber().length() > 0) {
			return Integer.valueOf(((BambooBuildAdapterIdea) o).getBuildNumber());
		} else {
			return 0;
		}
	}

	@Override
	public Class getColumnClass() {
		return Integer.class;
	}

	@Override
	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return Integer.parseInt(((BambooBuildAdapterIdea) o).getBuildNumber())
						- Integer.parseInt(((BambooBuildAdapterIdea) o1).getBuildNumber());
			}
		};
	}

	@Override
	public int getPrefferedWidth() {
		return COL_WIDTH;
	}
}