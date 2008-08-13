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

import org.jdom.Element;

import com.thoughtworks.xstream.io.xml.JDomWriter;
import com.thoughtworks.xstream.io.xml.JDomReader;
import com.thoughtworks.xstream.XStream;
import com.atlassian.theplugin.commons.cfg.ProjectConfigurationFactory;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerCfgFactoryException;
import com.atlassian.theplugin.commons.cfg.PrivateProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.PrivateServerCfgInfo;

public class JDomProjectConfigurationFactory implements ProjectConfigurationFactory {

	private final Element publicElement;
	private final Element privateElement;

	public JDomProjectConfigurationFactory(final Element element, final Element privateElement) {
		if (element == null) {
			throw new NullPointerException(Element.class.getSimpleName() + " cannot be null");
		}
		this.publicElement = element;
		this.privateElement = privateElement;
	}

	public ProjectConfiguration load() throws ServerCfgFactoryException {
		final int childCount = publicElement.getChildren().size();
		if (childCount != 1) {
			throw new ServerCfgFactoryException("Cannot travers JDom tree. Exactly one child node expected, but found ["
					+ childCount + "]");
		}
		final JDomReader reader = new JDomReader((Element) publicElement.getChildren().get(0));
		final XStream xStream = JDomXStreamUtil.getProjectJDomXStream();
		try {
			final ProjectConfiguration res = (ProjectConfiguration) xStream.unmarshal(reader);
			if (privateElement == null) {
				return res;
			}
			final PrivateProjectConfiguration ppc = load(privateElement, PrivateProjectConfiguration.class);
			return merge(res, ppc);
		} catch (Exception e) {
			throw new ServerCfgFactoryException("Cannot load " + ProjectConfiguration.class.getSimpleName() + ": "
					+ e.getMessage(), e);
		}
	}

	private ProjectConfiguration merge(final ProjectConfiguration projectConfiguration,
			final PrivateProjectConfiguration privateProjectConfiguration) {
		for (PrivateServerCfgInfo psci : privateProjectConfiguration.getPrivateServerCfgInfos()) {
			final ServerCfg serverCfg = projectConfiguration.getServerCfg(psci.getServerId());
			if (serverCfg != null) {
				serverCfg.setUsername(psci.getUsername());
				final String password = psci.getPassword();
				if (password != null) {
					serverCfg.setPassword(password);
				}
			}
		}
		return projectConfiguration;
	}

//	PrivateProjectConfiguration loadPrivateProjectConfiguration() throws ServerCfgFactoryException {
//		final JDomReader reader = new JDomReader(privateElement);
//		final XStream xStream = JDomXStreamUtil.getProjectJDomXStream();
//		try {
//			return (PrivateProjectConfiguration) xStream.unmarshal(reader);
//		} catch (Exception e) {
//			throw new ServerCfgFactoryException("Cannot load " + PrivateProjectConfiguration.class.getSimpleName() + ": "
//					+ e.getMessage(), e);
//		}
//	}

	<T> T load(final Element rootElement, Class<T> clazz) throws ServerCfgFactoryException {
		final int childCount = rootElement.getChildren().size();
		if (childCount != 1) {
			throw new ServerCfgFactoryException("Cannot travers JDom tree. Exactly one child node expected, but found ["
					+ childCount + "]");
		}
		final JDomReader reader = new JDomReader((Element) rootElement.getChildren().get(0));
		final XStream xStream = JDomXStreamUtil.getProjectJDomXStream();
		try {
			return clazz.cast(xStream.unmarshal(reader));
		} catch (Exception e) {
			throw new ServerCfgFactoryException("Cannot load " + clazz.getSimpleName() + ": "
					+ e.getMessage(), e);
		}
	}


	public void save(final ProjectConfiguration projectConfiguration) {
		if (projectConfiguration == null) {
			throw new NullPointerException(ProjectConfiguration.class.getSimpleName() + " cannot be null");
		}
		final JDomWriter writer = new JDomWriter(publicElement);
		final XStream xStream = JDomXStreamUtil.getProjectJDomXStream();
		xStream.marshal(projectConfiguration, writer);

		final PrivateProjectConfiguration privateCfg = getPrivateProjectConfiguration(projectConfiguration);
		save(privateCfg, privateElement);
	}

	void save(final Object object, final Element rootElement) {
		if (object == null) {
			throw new NullPointerException("Serialized object cannot be null");
		}
		final JDomWriter writer = new JDomWriter(rootElement);
		final XStream xStream = JDomXStreamUtil.getProjectJDomXStream();
		xStream.marshal(object, writer);

	}

//	public void save(final PrivateProjectConfiguration privateProjectConfiguration) {
//		if (privateProjectConfiguration == null) {
//			throw new NullPointerException(PrivateProjectConfiguration.class.getSimpleName() + " cannot be null");
//		}
//		final JDomWriter writer = new JDomWriter(privateElement);
//		final XStream xStream = JDomXStreamUtil.getProjectJDomXStream();
//		xStream.marshal(privateProjectConfiguration, writer);
//
//	}


	PrivateProjectConfiguration getPrivateProjectConfiguration(final ProjectConfiguration projectConfiguration) {
		final PrivateProjectConfiguration res = new PrivateProjectConfiguration();
		for (ServerCfg serverCfg : projectConfiguration.getServers()) {
			res.add(createPrivateProjectConfiguration(serverCfg));
		}
		return res;
	}

	static PrivateServerCfgInfo createPrivateProjectConfiguration(final ServerCfg serverCfg) {
		return new PrivateServerCfgInfo(serverCfg.getServerId(), serverCfg.getUsername(),
				serverCfg.isPasswordStored() ? serverCfg.getPassword() : null);
	}

}
