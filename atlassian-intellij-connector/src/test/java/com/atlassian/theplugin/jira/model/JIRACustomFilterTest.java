package com.atlassian.theplugin.jira.model;

import com.atlassian.connector.commons.jira.beans.JIRAProjectBean;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.google.common.collect.ImmutableList;
import junit.framework.TestCase;


/**
 * Created by klopacinski on 2014-10-10.
 */
public class JIRACustomFilterTest extends TestCase {

    public void testGetJqlWithNormalProjectName() {
        JiraCustomFilter filter = new JiraCustomFilter();
        ImmutableList<JIRAQueryFragment> fragments = ImmutableList.of((JIRAQueryFragment)new JIRAProjectBean(1, "PSI", "PSI"));

        filter.setQueryFragments(fragments);

        String jql = filter.getJql();

        assertEquals("(project = PSI)", jql);
    }

    public void testGetJqlWithReservedProjectName() {
        JiraCustomFilter filter = new JiraCustomFilter();
        ImmutableList<JIRAQueryFragment> fragments = ImmutableList.of((JIRAQueryFragment)new JIRAProjectBean(1, "ABORT", "ABORT"));

        filter.setQueryFragments(fragments);

        String jql = filter.getJql();

        assertEquals("(project = \"ABORT\")", jql);
    }
}
