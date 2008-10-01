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

import java.util.Comparator;
import java.util.Date;
import java.util.Calendar;


public class BuildDateColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 120;

	public String getColumnName() {
		return "Build Date";
	}

	public Object valueOf(Object o) {
		BambooBuildAdapterIdea bbai = (BambooBuildAdapterIdea) o;
		bbai.getServer();
		Date d = bbai.getBuildTime();
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.HOUR_OF_DAY, bbai.getServer().getTimezoneOffset());
		return c.getTime();
	}

	public Class getColumnClass() {
		return Date.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				if (((BambooBuildAdapterIdea) o).getBuildTime() != null
						&& ((BambooBuildAdapterIdea) o1).getBuildTime() != null) {
					return ((BambooBuildAdapterIdea) o).getBuildTime()
							.compareTo(((BambooBuildAdapterIdea) o1).getBuildTime());
				} else {
					return 0;
				}

			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}


}