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
package com.atlassian.theplugin.commons.cfg.xstream;

import java.io.IOException;
import java.io.StringWriter;
import java.io.StringReader;
import java.util.List;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
import com.spartez.util.junit3.TestUtil;
import com.spartez.util.junit3.IAction;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.commons.cfg.ProjectConfigurationFactoryTest;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ProjectConfigurationFactory;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfgFactoryException;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.PrivateProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.PrivateServerCfgInfo;
import static com.atlassian.theplugin.commons.cfg.xstream.JDomProjectConfigurationFactory.createPrivateProjectConfiguration;
import com.atlassian.theplugin.commons.SubscribedPlan;

/**
 * JDomProjectConfigurationFactory Tester.
 *
 * @author wseliga
 */
public class JDomProjectConfigurationFactoryTest extends ProjectConfigurationFactoryTest {
	private final BambooServerCfg bamboo1 = new BambooServerCfg("mybamboo1", new ServerId("141d662c-e744-4690-a5f8-6e127c0bc84f"));
	private final BambooServerCfg bamboo2 = new BambooServerCfg("mybamboo2", new ServerId("241d662c-e744-4690-a5f8-6e127c0bc84f"));
	private final CrucibleServerCfg crucible1 = new CrucibleServerCfg("mycrucible1", new ServerId("341d662c-e744-4690-a5f8-6e127c0bc84f"));
	private ProjectConfiguration projectCfg;

	private static final String FAKE_CLASS_NAME = "whateverfakeclasshere";

	private Element element = new Element("test");
	private Element privateElement = new Element("private-root");
	private JDomProjectConfigurationFactory jdomFactory = new JDomProjectConfigurationFactory(
			element, privateElement);
	private static final String EXPECTED_OUTPUT_XML = "expected-output.xml";

	@Override
	protected ProjectConfigurationFactory getProjectConfigurationFactory() {
		return jdomFactory;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		projectCfg = new ProjectConfiguration(MiscUtil.<ServerCfg>buildArrayList(bamboo1));
		bamboo1.setPassword("mycleartextpassword");
		bamboo1.setPasswordStored(true);
	}

	public void testJDomSaveLoadGlobalConfiguration() throws IOException, ServerCfgFactoryException {
		final JDomProjectConfigurationFactory factory = new JDomProjectConfigurationFactory(element, privateElement);
		factory.save(projectCfg);

		assertEquals(1, element.getChildren().size());

		final JDomProjectConfigurationFactory loadFactory = new JDomProjectConfigurationFactory(element, privateElement);
		ProjectConfiguration readCfg = loadFactory.load();
		assertEquals(projectCfg, readCfg);
	}


	public void testHashedPassword() throws ServerCfgFactoryException, IOException {
		final JDomProjectConfigurationFactory factory = new JDomProjectConfigurationFactory(element, privateElement);
		factory.save(projectCfg);

		final StringWriter writer = new StringWriter();

		new XMLOutputter(Format.getPrettyFormat()).output(element, writer);
		assertTrue(writer.toString().indexOf(bamboo1.getName()) != -1);
		// password should be hashed
		assertEquals(-1, writer.toString().indexOf(bamboo1.getPassword()));

		final StringWriter privateWriter = new StringWriter();
		new XMLOutputter(Format.getPrettyFormat()).output(privateElement, privateWriter);
		assertTrue(privateWriter.toString().indexOf(bamboo1.getServerId().getUuid().toString()) != -1);
		// password should be hashed - so it should not be found in resulting xml stream
		assertEquals(-1, privateWriter.toString().indexOf(bamboo1.getPassword()));
	}


	public void testPublicSerialization() throws ServerCfgFactoryException, IOException, JDOMException {
		bamboo1.getSubscribedPlans().add(new SubscribedPlan("MYID"));
		bamboo1.getSubscribedPlans().add(new SubscribedPlan("PLANID2"));
		bamboo1.setUrl("http://mygreaturl");
		bamboo1.setUsername("mytestuser");

		final JDomProjectConfigurationFactory factory = new JDomProjectConfigurationFactory(element, privateElement);
		factory.save(projectCfg);

		StringWriter writer = new StringWriter();

		writeXml(element, writer);
		// password should be hashed
		String expected = StringUtil.slurp(getClass().getResourceAsStream(EXPECTED_OUTPUT_XML));
		assertEquals(expected, writer.toString());

		// and also vice-versa
		Document doc = new SAXBuilder(false).build(getClass().getResourceAsStream(EXPECTED_OUTPUT_XML));
		final JDomProjectConfigurationFactory loadFactory = new JDomProjectConfigurationFactory(
				doc.getRootElement(), privateElement);
		final ProjectConfiguration readCfg = loadFactory.load();
		assertEquals(projectCfg, readCfg);
	}


	public void testPublicOnlyDeSerialization() throws ServerCfgFactoryException, IOException, JDOMException {
		bamboo1.getSubscribedPlans().add(new SubscribedPlan("MYID"));
		bamboo1.getSubscribedPlans().add(new SubscribedPlan("PLANID2"));
		bamboo1.setUrl("http://mygreaturl");
		bamboo1.setUsername("mytestuser");

		final JDomProjectConfigurationFactory factory = new JDomProjectConfigurationFactory(element, privateElement);
		factory.save(projectCfg);

		StringWriter writer = new StringWriter();
		writeXml(element, writer);

		final Document doc = new SAXBuilder(false).build(new StringReader(writer.toString()));

		// load public info only
		final JDomProjectConfigurationFactory loadFactory = new JDomProjectConfigurationFactory(doc.getRootElement(), null);
		final ProjectConfiguration readCfg = loadFactory.load();
		assertEquals(1, readCfg.getServers().size());
		final ServerCfg readServer = readCfg.getServerCfg(bamboo1.getServerId());
		assertEquals("", readServer.getPassword());
		assertEquals("", readServer.getUsername());
		assertEquals(bamboo1.getUrl(), readServer.getUrl());
		assertEquals(bamboo1.getName(), readServer.getName());
	}

	private void writeXml(final Element rootElement, final StringWriter writer) throws IOException {
		final Format prettyFormat = Format.getPrettyFormat();
		prettyFormat.setLineSeparator("\n");
		new XMLOutputter(prettyFormat).output(rootElement, writer);
	}

	public void testPrivateSerialization() throws ServerCfgFactoryException, IOException, JDOMException {
		bamboo1.setUsername("mytestuser");
		bamboo1.setPassword("mypassword1");
		bamboo1.setPasswordStored(true);
		bamboo2.setUsername("mytestuser2");
		bamboo2.setPassword("mypassword2");
		bamboo2.setPasswordStored(false);
		crucible1.setUsername("xyz");
		crucible1.setPassword("passwordxyz");
		crucible1.setPasswordStored(true);
		projectCfg.getServers().add(bamboo2);
		projectCfg.getServers().add(crucible1);

		final JDomProjectConfigurationFactory factory = new JDomProjectConfigurationFactory(element, privateElement);
		factory.save(projectCfg);

		StringWriter writer = new StringWriter();
		writeXml(privateElement, writer);

		final String expected = StringUtil.slurp(getClass().getResourceAsStream("expected-private-output.xml"));
		assertEquals(writer.toString(), expected);
		//System.out.println(writer.toString());

		StringReader reader = new StringReader(writer.toString());

		// and also vice-versa
		Document doc = new SAXBuilder(false).build(reader);
		final JDomProjectConfigurationFactory loadFactory = new JDomProjectConfigurationFactory(element, doc.getRootElement());
		final PrivateProjectConfiguration readCfg = loadFactory.load(doc.getRootElement(), PrivateProjectConfiguration.class);
		TestUtil.assertHasOnlyElements(readCfg.getPrivateServerCfgInfos(), createPrivateProjectConfiguration(bamboo1),
				createPrivateProjectConfiguration(bamboo2), createPrivateProjectConfiguration(crucible1));
	}

	public void testCreatePrivateProjectConfiguration() {
		bamboo1.setUsername("mytestuser");
		bamboo1.setPassword("mypassword1");
		bamboo1.setPasswordStored(true);
		bamboo2.setUsername("mytestuser2");
		bamboo2.setPassword("mypassword2");
		bamboo2.setPasswordStored(false);
		final PrivateServerCfgInfo privateCfg = createPrivateProjectConfiguration(bamboo1);
		assertEquals(bamboo1.getUsername(), privateCfg.getUsername());
		assertEquals(bamboo1.getPassword(), privateCfg.getPassword());
		assertEquals(bamboo1.getServerId(), privateCfg.getServerId());

		final PrivateServerCfgInfo privateCfg2 = createPrivateProjectConfiguration(bamboo2);
		assertEquals(bamboo2.getUsername(), privateCfg2.getUsername());
		assertEquals(null, privateCfg2.getPassword());
		assertEquals(bamboo2.getServerId(), privateCfg2.getServerId());
	}


	public void testInvalidJDomElement() {
		final JDomProjectConfigurationFactory factory = new JDomProjectConfigurationFactory(new Element("element"), privateElement);
		TestUtil.assertThrows(ServerCfgFactoryException.class, new IAction() {

			public void run() throws Throwable {
				factory.load();
			}
		});

		add(element, new Element(FAKE_CLASS_NAME));
		final JDomProjectConfigurationFactory factory2 = new JDomProjectConfigurationFactory(element, privateElement);
		TestUtil.assertThrows(ServerCfgFactoryException.class, new IAction() {

			public void run() throws Throwable {
				factory2.load();
			}
		});
	}

	private void add(final Element parent, final Element child) {
		@SuppressWarnings("unchecked")
		final List<Element> children = parent.getChildren();
		children.add(child);
	}

	public void testInvalidClass() throws ServerCfgFactoryException {
		// just let us forge a simple DOM which instead of ProjectConfiguration contains just ServerId
		final Element serverId = new Element(ServerId.class.getName());
		serverId.setText(new ServerId().getUuid().toString());
		add(element, serverId);
		final JDomProjectConfigurationFactory factory2 = new JDomProjectConfigurationFactory(element, privateElement);
		TestUtil.assertThrowsAndMsgContainsRe(ServerCfgFactoryException.class, 
				"Cannot load ProjectConfiguration:.*" + ServerId.class.getName(),
				new IAction() {

			public void run() throws Throwable {
				factory2.load();
			}
		});

	}

	public void testNullDomElement() {
		TestUtil.assertThrows(NullPointerException.class, new IAction() {
			public void run() throws Throwable {
				new JDomProjectConfigurationFactory(null, privateElement);
			}
		});
		TestUtil.assertThrows(NullPointerException.class, new IAction() {
			public void run() throws Throwable {
				new JDomProjectConfigurationFactory(null, null);
			}
		});
	}

	public void testNullConfiguration() {
		TestUtil.assertThrows(NullPointerException.class, new IAction() {
			public void run() throws Throwable {
				jdomFactory.save(null);
			}
		});
	}


}
