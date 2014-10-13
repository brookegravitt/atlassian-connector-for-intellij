package com.atlassian.theplugin.jira.model;

import com.atlassian.connector.commons.jira.beans.JIRAProjectBean;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by klopacinski on 2014-10-10.
 */
public class JIRACustomFilterTest {
    @Test
    public void testGetJqlWithNormalProjectName() {
        JiraCustomFilter filter = new JiraCustomFilter();
        ImmutableList<JIRAQueryFragment> fragments = ImmutableList.of((JIRAQueryFragment)new JIRAProjectBean(1, "PSI", "PSI"));

        filter.setQueryFragments(fragments);

        String jql = filter.getJql();

        Assert.assertEquals("(project = PSI)", jql);
    }

    @Test
    public void testGetJqlWithReservedProjectName() {
        JiraCustomFilter filter = new JiraCustomFilter();
        ImmutableList<JIRAQueryFragment> fragments = ImmutableList.of((JIRAQueryFragment)new JIRAProjectBean(1, "ABORT", "ABORT"));

        filter.setQueryFragments(fragments);

        String jql = filter.getJql();

        Assert.assertEquals("(project = \"ABORT\")", jql);
    }
}
