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

import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.util.StringUtil;
import com.thoughtworks.xstream.XStream;
import junit.framework.TestCase;

import java.io.IOException;

public class JDomXStreamUtilTest extends TestCase {

	private static final BambooServerCfg BAMBOO = new BambooServerCfg("mybamboo", new ServerId());

	static {
//		BAMBOO.getSubscribedPlans().add(new SuppressWarnings())
	}

	public void testBambooServerCfg() throws IOException {
		final XStream xstream = JDomXStreamUtil.getProjectJDomXStream();
		final String xml = StringUtil.slurp(getClass().getResourceAsStream("bamboo-with-no-plans.xml"));
		BambooServerCfg bamboo = (BambooServerCfg) xstream.fromXML(xml);
		assertEquals("mybamboo1", bamboo.getName());
		assertEquals(0, bamboo.getSubscribedPlans().size());
	}
}
