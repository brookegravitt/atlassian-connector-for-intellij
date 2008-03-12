package com.atlassian.theplugin.idea.bamboo.table.columns;

import com.atlassian.theplugin.bamboo.BuildStatus;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.idea.bamboo.table.BambooColumnInfo;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Mar 10, 2008
 * Time: 12:24:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class BuildTestRatioColumn extends BambooColumnInfo {
	private static final int COL_TEST_RATIO_WIDTH = 100;

	public String getColumnName() {
		return "Passed tests";
	}

	public Object valueOf(Object o) {
		if (((BambooBuildAdapter) o).getStatus() == BuildStatus.UNKNOWN) {
			return "-/-";
		} else {
			return ((BambooBuildAdapter) o).getTestsPassed() + "/" + ((BambooBuildAdapter) o).getTestsNumber();
		}
	}

	public Class getColumnClass() {
		return String.class;
	}

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
		if (((BambooBuildAdapter) o).getTestsNumber() > 0) {
			oTests = ((double)((BambooBuildAdapter) o).getTestsPassed()) / ((double) ((BambooBuildAdapter) o).getTestsNumber());
		}
		return oTests;
	}

	public int getPrefferedWidth() {
		return COL_TEST_RATIO_WIDTH;
	}
}