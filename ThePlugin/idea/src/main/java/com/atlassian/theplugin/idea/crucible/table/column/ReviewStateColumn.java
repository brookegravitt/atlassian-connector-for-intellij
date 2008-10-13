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

package com.atlassian.theplugin.idea.crucible.table.column;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.idea.TableColumnInfo;

import javax.swing.*;
import java.util.Comparator;


public class ReviewStateColumn extends TableColumnInfo {
	private static final int COL_WIDTH = new JLabel("Summarize").getPreferredSize().width;

	public String getColumnName() {
		return "State";
	}

	public Object valueOf(Object o) {
		return o;
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((Review) o).getState().value()
						.compareTo(((Review) o1).getState().value());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}


}