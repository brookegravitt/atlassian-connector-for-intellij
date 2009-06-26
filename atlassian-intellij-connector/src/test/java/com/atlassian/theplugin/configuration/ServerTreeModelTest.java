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
package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.idea.config.serverconfig.model.*;
import junit.framework.TestCase;

/**
 * User: pmaruszak
 */
public class ServerTreeModelTest extends TestCase {
	private ServerTreeModel stm;
	private RootNode root;
	private JiraServerCfg jiraServerCfg;
	private FishEyeServerCfg fiahEyeServerCfg;
	private CrucibleServerCfg crucibleServerCfg;
	private BambooServerCfg bambooServerCfg;

	protected void setUp() throws Exception {
		super.setUp();

		root = new RootNode();
		stm = new ServerTreeModel(root);
		jiraServerCfg = new JiraServerCfg("jiraServer", new ServerIdImpl());
		fiahEyeServerCfg = new FishEyeServerCfg("fishEyeServer", new ServerIdImpl());
		crucibleServerCfg = new CrucibleServerCfg("crucibleServer", new ServerIdImpl());
		bambooServerCfg = new BambooServerCfg("bambooServer", new ServerIdImpl());
	}

	public void testDefaultValue() {
		assertEquals(stm.getChildCount(root), ServerType.values().length);
		for (int i = 0; i < ServerType.values().length; i++) {

			assertTrue(stm.getChild(root, i) instanceof ServerTypeNode);
			ServerTypeNode type = (ServerTypeNode) stm.getChild(root, i);
			assertEquals(type.getChildCount(), 1);
			ServerInfoNode info = (ServerInfoNode) stm.getChild(type, 0);

			assertEquals(info.getServerType(), ServerType.values()[i]);
		}

	}

	public void testAddJiraServer() {
		tryTestServer(jiraServerCfg, ServerType.JIRA_SERVER);
	}

	public void testAddFishEyeServer() {
		tryTestServer(fiahEyeServerCfg, ServerType.FISHEYE_SERVER);
	}

	public void testAddCrucibleServer() {
		tryTestServer(crucibleServerCfg, ServerType.CRUCIBLE_SERVER);
	}

	public void testAddBambooServer() {
		tryTestServer(bambooServerCfg, ServerType.BAMBOO_SERVER);
	}


	private void tryTestServer(ServerCfg serverCfg, ServerType serverType) {

		ServerNode serverNode = ServerNodeFactory.getServerNode(serverCfg);
		ServerTypeNode typeNode = stm.getServerTypeNode(serverType);

		assertTrue(typeNode != null);

		stm.insertNodeInto(serverNode, typeNode, serverNode.getChildCount());
		assertEquals(typeNode.getChildCount(), 1);
		assertEquals(typeNode.getChildAt(0), serverNode);
	}

	public void testRemoveJiraServer() {
		tryTestServer(jiraServerCfg, ServerType.JIRA_SERVER);
		tryTestRemoveServer(ServerType.JIRA_SERVER);
	}

	public void testRemoveCrucibleServer() {
		tryTestServer(crucibleServerCfg, ServerType.CRUCIBLE_SERVER);
		tryTestRemoveServer(ServerType.CRUCIBLE_SERVER);

	}

	public void testRemoveFishEyeServer() {
		tryTestServer(fiahEyeServerCfg, ServerType.FISHEYE_SERVER);
		tryTestRemoveServer(ServerType.FISHEYE_SERVER);

	}

	public void testRemoveBambooServer() {
		tryTestServer(bambooServerCfg, ServerType.BAMBOO_SERVER);
		tryTestRemoveServer(ServerType.BAMBOO_SERVER);

	}

	private void tryTestRemoveServer(ServerType serverType) {
		ServerTypeNode typeNode = stm.getServerTypeNode(serverType);
		typeNode.remove(0);
		stm.nodeStructureChanged(typeNode);
		assertEquals(typeNode.getChildCount(), 1);
		assertTrue(typeNode.getChildAt(0) instanceof ServerInfoNode);
		assertEquals(((ServerInfoNode) typeNode.getChildAt(0)).getServerType(), serverType);
	}


}
