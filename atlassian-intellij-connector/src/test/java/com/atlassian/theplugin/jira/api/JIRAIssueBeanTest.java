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

package com.atlassian.theplugin.jira.api;

import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

public class JIRAIssueBeanTest extends TestCase {
	private JIRAIssueBean issue;
	private Document doc;

	protected void setUp() throws Exception {
		doc = new SAXBuilder().build(this.getClass().getResourceAsStream("/jira/api/single-issue.xml"));
		ServerData server = new ServerData("name", new ServerId(), "", "", "http://jira.com");
		issue = new JIRAIssueBean(server, doc.getRootElement());
	}

	public void testFromXml() throws Exception {
		assertEquals("NullPointerException on wrong URL to Bamboo server", issue.getSummary());
		assertEquals("PL-94", issue.getKey());
		assertEquals("http://jira.com", issue.getServerUrl());
		assertEquals("http://jira.com/browse/PL", issue.getProjectUrl());
		assertEquals("http://jira.com/browse/PL-94", issue.getIssueUrl());
	}

	public void testEquals() {
		JIRAIssueBean modyfiedIssue = new JIRAIssueBean(issue);
		assertEquals(issue, modyfiedIssue);

		modyfiedIssue.setSummary(issue.getSummary() + "modyfied");
		assertEquals(issue, modyfiedIssue);

		modyfiedIssue.setDescription(issue.getDescription() + "modyfied");
		assertEquals(issue, modyfiedIssue);

		modyfiedIssue.setAssigneeId("jjaroczynski");
		assertEquals(issue, modyfiedIssue);

		modyfiedIssue.setReporterId("jjaroczynski");
		assertEquals(issue, modyfiedIssue);
	}

	public void testNotEquals() {
		JIRAIssueBean modyfiedIssue;

		modyfiedIssue = new JIRAIssueBean(issue);
		assertEquals(issue, modyfiedIssue);
		modyfiedIssue.setKey(issue.getKey() + "modyfied");
		assertFalse(issue.equals(modyfiedIssue));

		modyfiedIssue = new JIRAIssueBean(issue.getServer(), doc.getRootElement());
		assertEquals(issue, modyfiedIssue);
		modyfiedIssue = new JIRAIssueBean(new ServerData(issue.getServer().getName() + "modyfied", new ServerId(), "", "", ""),
				doc.getRootElement());
		assertFalse(issue.equals(modyfiedIssue));

	}
}
