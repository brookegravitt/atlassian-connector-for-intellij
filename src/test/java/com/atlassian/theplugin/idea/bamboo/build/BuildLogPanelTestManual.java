package com.atlassian.theplugin.idea.bamboo.build;

import com.intellij.testFramework.IdeaTestCase;

/**
 * Add *public* here if you want to run it manually :)
 */
class BuildLogPanelTestManual extends IdeaTestCase {
	// use this test to find out execution time of filter (regexp)
	public void testJavaFilterTime() {
		String line = "build/           02-Feb-2009 01:21:58	2009-02-02 01:21:58,128 WARN [HtmlUnit Managed Thread #253: "
				+ "window.setInterval] [htmlunit.javascript.host.Stylesheet] error CSS error: null [110:108] "
				+ "Error in expression. Invalid token \"=\". Was expecting one of: <S>, <COMMA>, \"/\", <PLUS>, \"-\", <HASH>"
				+ ", <STRING>, \")\", <URI>, \"inherit\", <EMS>, <EXS>, <LENGTH_PX>, <LENGTH_CM>, <LENGTH_MM>, <LENGTH_IN>, "
				+ "<LENGTH_PT>, <LENGTH_PC>, <ANGLE_DEG>, <ANGLE_RAD>, <ANGLE_GRAD>, <TIME_MS>, <TIME_S>, <FREQ_HZ>, "
				+ "<FREQ_KHZ>, <DIMENSION>, <PERCENTAGE>, <NUMBER>, <FUNCTION>, <IDENT>.";
//		String line = "build\t03-Feb-2009 02:13:55\t\tat com.atlassian.confluence.search.v2.lucene.
// TestLuceneSearcherImpl.testSearchThrowsException(TestLuceneSearcherImpl.java:74)";
//		String line = "no more tokens - could not parse error message: C:\\Users\\Jacek\\.bamboo-home
// \\xml-data\\build-dir\\EX-COMPILATION\\src\\main\\java\\com\\atlassian\\example\\Kolo.java:7: ';' expected";
		BuildLogPanel.JavaFileFilter filter = new BuildLogPanel.JavaFileFilter(getProject());

		long start = System.currentTimeMillis();
		filter.applyFilter(line, line.length() - 1);
		long stop = System.currentTimeMillis(); // stop timing
		System.out.println((stop - start) + "\t" + line); // print execution time
	}


}
