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
public class BuildTestRatioColumn extends TableColumnInfo {
	private static final int COL_TEST_RATIO_WIDTH = 100;

	@Override
	public String getColumnName() {
		return "Failed Tests";
	}

	@Override
	public Object valueOf(Object o) {
		return ((BambooBuildAdapterIdea) o).getTestsPassedSummary();
	}

	@Override
	public Class getColumnClass() {
		return String.class;
	}

	@Override
	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				double oTests = getTestRatio(o);
				double o1Tests = getTestRatio(o1);
				return Double.compare(oTests, o1Tests);
			}
		};
	}

	private double getTestRatio(Object o) {
		double oTests = 0;
		if (((BambooBuildAdapterIdea) o).getTestsNumber() > 0) {
			oTests = ((double) ((BambooBuildAdapterIdea) o).getTestsPassed())
					/ ((double) ((BambooBuildAdapterIdea) o).getTestsNumber());
		}
		return oTests;
	}

	@Override
	public int getPrefferedWidth() {
		return COL_TEST_RATIO_WIDTH;
	}
}