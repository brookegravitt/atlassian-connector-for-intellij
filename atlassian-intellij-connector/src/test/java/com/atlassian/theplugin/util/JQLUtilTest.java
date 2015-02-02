package com.atlassian.theplugin.util;

import junit.framework.TestCase;

/**
 * Created by klopacinski on 2015-01-30.
 */
public class JQLUtilTest extends TestCase {

    public void testEscapeJQL() {
        String escaped = JQLUtil.escapeReservedJQLKeyword("CHAR");
        assertEquals("\"CHAR\"", escaped);
    }

    public void testEscapeNoJQL() {
        String escaped = JQLUtil.escapeReservedJQLKeyword("PSI");
        assertEquals("PSI", escaped);
    }
}
