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

import com.atlassian.theplugin.commons.cfg.PrivateProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.PrivateServerCfgInfo;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ProjectConfigurationFactory;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfgFactoryException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.JDomReader;
import com.thoughtworks.xstream.io.xml.JDomWriter;
import org.jdom.Element;

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
		ProjectConfiguration res = load(publicElement, ProjectConfiguration.class);
		final PrivateProjectConfiguration ppc = (privateElement != null) 
				? load(privateElement, PrivateProjectConfiguration.class)
				: new PrivateProjectConfiguration();
		return merge(res, ppc);
	}


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
		} catch (ClassCastException e) {
			throw new ServerCfgFactoryException("Cannot load " + clazz.getSimpleName() + " due to ClassCastException: "
					+ e.getMessage(), e);
		} catch (Exception e) {
			throw new ServerCfgFactoryException("Cannot load " + clazz.getSimpleName() + ": "
					+ e.getMessage(), e);
		}
	}


	private ProjectConfiguration merge(final ProjectConfiguration projectConfiguration,
			final PrivateProjectConfiguration privateProjectConfiguration) {
		for (ServerCfg serverCfg : projectConfiguration.getServers()) {
			PrivateServerCfgInfo psci = privateProjectConfiguration.getPrivateServerCfgInfo(serverCfg.getServerId());
			if (psci != null) {
				serverCfg.setUsername(psci.getUsername());
				serverCfg.setEnabled(psci.isEnabled());
				final String password = psci.getPassword();
				if (password != null) {
					serverCfg.setPassword(password);
					serverCfg.setPasswordStored(true);
				} else {
					serverCfg.setPasswordStored(false);
				}
			} else {
				serverCfg.setPasswordStored(false);
				serverCfg.setEnabled(true); // new servers (for which there was no private info yet) are enabled by default
			}
		}
		return projectConfiguration;
	}

	public void save(final ProjectConfiguration projectConfiguration) {
		save(projectConfiguration, publicElement);
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

	PrivateProjectConfiguration getPrivateProjectConfiguration(final ProjectConfiguration projectConfiguration) {
		final PrivateProjectConfiguration res = new PrivateProjectConfiguration();
		for (ServerCfg serverCfg : projectConfiguration.getServers()) {
			res.add(createPrivateProjectConfiguration(serverCfg));
		}
		return res;
	}

	static PrivateServerCfgInfo createPrivateProjectConfiguration(final ServerCfg serverCfg) {
		return new PrivateServerCfgInfo(serverCfg.getServerId(), serverCfg.isEnabled(), serverCfg.getUsername(),
				serverCfg.isPasswordStored() ? serverCfg.getPassword() : null);
	}

}
