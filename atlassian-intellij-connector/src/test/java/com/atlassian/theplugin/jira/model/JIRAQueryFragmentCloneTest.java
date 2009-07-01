package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.*;
import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * User: pmaruszak
 */
public class JIRAQueryFragmentCloneTest extends TestCase {
	private JIRAActionBean jiraActionBean;
	private JIRAActionFieldBean jiraActionFieldBean;
	private JIRAAssigneeBean jiraAssigneeBean;
	private JIRAComponentBean jiraComponentBean;
	private JIRAFixForVersionBean jiraFixForVersionBean;
	private JIRAIssueTypeBean jiraIssueTypeBean;
	private JIRAPriorityBean jiraPriorityBean;
	private JIRAProjectBean jiraProjectBean;
	private JIRAReporterBean jiraReporterBean;
	private JIRAResolutionBean jiraResolutionBean;
	private JIRAStatusBean jiraStatusBean;
	private JIRAVersionBean jiraVersionBean;


	protected void setUp() throws Exception {
		super.setUp();
		String value;
		String name;
		URL iconUrl;

		Integer i = 0;
		i++;
		name = getFragmentName(i,  JIRAActionBean.class.toString());
		jiraActionBean = new JIRAActionBean(++i, JIRAActionBean.class.toString());


		i++;
		name = getFragmentName(i,  JIRAActionFieldBean.class.toString());
		jiraActionFieldBean = new JIRAActionFieldBean(i.toString(), name);

		i++;
		name = getFragmentName(i, JIRAAssigneeBean.class.toString());
		value = getFragmentValue(i, JIRAAssigneeBean.class.toString());
		jiraAssigneeBean = new JIRAAssigneeBean(i, name, value);

		i++;
		name = getFragmentName(i, JIRAComponentBean.class.toString());
		jiraComponentBean = new JIRAComponentBean(i, name);

		i++;
		name = getFragmentName(i, JIRAFixForVersionBean.class.toString());
		jiraFixForVersionBean = new JIRAFixForVersionBean(i, name, false);


		i++;
		name = getFragmentName(i, JIRAIssueTypeBean.class.toString());
		iconUrl = getFragmentIconUrl(i, JIRAIssueTypeBean.class.toString());
		jiraIssueTypeBean = new JIRAIssueTypeBean(i, name, iconUrl);

		i++;
		name = getFragmentName(i, JIRAPriorityBean.class.toString());
		iconUrl = getFragmentIconUrl(i, JIRAPriorityBean.class.toString());
		jiraPriorityBean = new JIRAPriorityBean(i, 0, name, iconUrl);

		i++;
		name = getFragmentName(i, JIRAProjectBean.class.toString());
		jiraProjectBean = new JIRAProjectBean(i, name);

		i++;
		name = getFragmentName(i, JIRAReporterBean.class.toString());
		value = getFragmentValue(i, JIRAReporterBean.class.toString());
		jiraReporterBean = new JIRAReporterBean(i, name, value);

		i++;
		name = getFragmentName(i, JIRAResolutionBean.class.toString());
		jiraResolutionBean = new JIRAResolutionBean(i, name);

		i++;
		name= getFragmentName(i, JIRAStatusBean.class.toString());
		iconUrl = getFragmentIconUrl(i, JIRAStatusBean.class.toString());
		jiraStatusBean = new JIRAStatusBean(i, name, iconUrl);

		i++;
		name = getFragmentName(i, JIRAVersionBean.class.toString());
		jiraVersionBean = new JIRAVersionBean(i, name, false);



	}

	public void testJIRAActionBeanClone(){
		JIRAActionBean clone = jiraActionBean.getClone();
		assertTrue(clone != jiraActionBean);
		assertEquals(clone.getId(), jiraActionBean.getId());
		assertEquals(clone.getName(), jiraActionBean.getName());
	}

	public void testJIRAActionFieldBean(){
		JIRAActionFieldBean clone = jiraActionFieldBean.getClone();
		assertTrue(clone != jiraActionFieldBean);
		assertEquals(clone.getId(), jiraActionFieldBean.getId());
		assertEquals(clone.getName(), jiraActionFieldBean.getName());

	}
	public void testJIRAAssigneeBean(){
		JIRAAssigneeBean clone = jiraAssigneeBean.getClone();
		assertTrue(clone != jiraAssigneeBean);
		assertEquals(clone.getId(), jiraAssigneeBean.getId());
		assertEquals(clone.getName(), jiraAssigneeBean.getName());
		assertEquals(clone.getValue(), jiraAssigneeBean.getValue());
	}

	public void testJIRAComponentBean(){
		JIRAComponentBean clone = jiraComponentBean.getClone();
		assertTrue(clone != jiraComponentBean);
		assertEquals(clone.getId(), jiraComponentBean.getId());
		assertEquals(clone.getName(), jiraComponentBean.getName());
	}

	public void testJIRAFixForVersionBean(){
		JIRAFixForVersionBean clone = jiraFixForVersionBean.getClone();
		assertTrue(clone != jiraFixForVersionBean);
		assertEquals(clone.getId(), jiraFixForVersionBean.getId());
		assertEquals(clone.getName(), jiraFixForVersionBean.getName());

	}

	public void testJIRAIssueTypeBean(){
		JIRAIssueTypeBean clone = jiraIssueTypeBean.getClone();
		assertTrue(clone != jiraIssueTypeBean);
		assertEquals(clone.getId(), jiraIssueTypeBean.getId());
		assertEquals(clone.getName(), jiraIssueTypeBean.getName());
		assertEquals(clone.getIconUrl(), jiraIssueTypeBean.getIconUrl());

	}


	public void testJIRAPriorityBean(){
		JIRAPriorityBean clone = jiraPriorityBean.getClone();
		assertTrue(clone != jiraPriorityBean);
		assertEquals(clone.getId(), jiraPriorityBean.getId());
		assertEquals(clone.getName(), jiraPriorityBean.getName());
		assertEquals(clone.getIconUrl(), jiraPriorityBean.getIconUrl());
	}

	public void testJIRAProjectBean(){
		JIRAProjectBean clone = jiraProjectBean.getClone();
		assertTrue(clone != jiraProjectBean);
		assertEquals(clone.getId(), jiraProjectBean.getId());
		assertEquals(clone.getName(), jiraProjectBean.getName());
	}

	public void testJIRAReporterBean(){
		JIRAReporterBean clone = jiraReporterBean.getClone();
		assertTrue(clone != jiraReporterBean);
		assertEquals(clone.getId(), jiraReporterBean.getId());
		assertEquals(clone.getName(), jiraReporterBean.getName());
	}

	public void testJIRAResolutionBean(){
		JIRAResolutionBean clone = jiraResolutionBean.getClone();
		assertTrue(clone != jiraResolutionBean);
		assertEquals(clone.getId(), jiraResolutionBean.getId());
		assertEquals(clone.getName(), jiraResolutionBean.getName());
	}

	public void testJIRAStatusBean(){
		JIRAStatusBean clone = jiraStatusBean.getClone();
		assertTrue(clone != jiraStatusBean);
		assertEquals(clone.getId(), jiraStatusBean.getId());
		assertEquals(clone.getName(), jiraStatusBean.getName());
		assertEquals(clone.getIconUrl(), jiraStatusBean.getIconUrl());
	}

	public void testJIRAVersionBean(){
		JIRAVersionBean clone = jiraVersionBean.getClone();
		assertTrue(clone != jiraVersionBean);
		assertEquals(clone.getId(), jiraVersionBean.getId());
		assertEquals(clone.getName(), jiraVersionBean.getName());
	}

	private String getFragmentName(int i, String clazz){
		return clazz + i;
	}

	private String getFragmentValue(int i, String clazz){
		return "value" + clazz + i;
	}

	private URL getFragmentIconUrl(int i, String clazz){
		URL url = null;
		try {
			url = new URL("iconUrl" + clazz + i);
		} catch (MalformedURLException e) {
            // boo!
		}
		return url;
	}


}
