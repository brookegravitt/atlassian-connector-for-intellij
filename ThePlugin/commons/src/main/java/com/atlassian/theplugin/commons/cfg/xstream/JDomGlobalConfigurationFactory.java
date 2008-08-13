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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.JDomReader;
import com.thoughtworks.xstream.io.xml.JDomWriter;
import com.atlassian.theplugin.commons.cfg.GlobalConfigurationFactory;
import com.atlassian.theplugin.commons.cfg.GlobalConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerCfgFactoryException;
import org.jdom.Element;

public class JDomGlobalConfigurationFactory implements GlobalConfigurationFactory {
	private final Element element;

	public JDomGlobalConfigurationFactory(final Element element) {
		this.element = element;
	}

	public void save(final GlobalConfiguration globalConfiguration)  {
		final JDomWriter writer = new JDomWriter(element);
		final XStream xStream = JDomXStreamUtil.getProjectJDomXStream();
		xStream.marshal(globalConfiguration, writer);
	}

	public GlobalConfiguration load() throws ServerCfgFactoryException {
		final int childCount = element.getChildren().size();
		if (childCount != 1) {
			throw new ServerCfgFactoryException("Cannot travers JDom tree. Exactly one child node expected, but found ["
					+ childCount + "]");
		}
		final JDomReader reader = new JDomReader((Element) element.getChildren().get(0));
		final XStream xStream = JDomXStreamUtil.getProjectJDomXStream();
		return (GlobalConfiguration) xStream.unmarshal(reader);

	}
}
