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
package com.atlassian.theplugin.commons.cfg;

import com.thoughtworks.xstream.XStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

public class GlobalConfigurationFactoryImpl implements GlobalConfigurationFactory {

	private final String globalSettingsFileName;
	private final String projectSettingsFileName;
	private static final String PROJECT_SPECIFIC_SERVERS_DATA = "project specific servers data";
	private static final String GLOBAL_SERVERS_DATA = "global servers data";

	public GlobalConfigurationFactoryImpl(final String globalSettingsFileName, final String projectSettingsFileName) {
		if (globalSettingsFileName == null || projectSettingsFileName == null) {
			throw new NullPointerException();
		}
		this.globalSettingsFileName = globalSettingsFileName;
		this.projectSettingsFileName = projectSettingsFileName;
	}

	private OutputStream getProjectSettingsOutputStream() throws FileNotFoundException {
		return new BufferedOutputStream(new FileOutputStream(getProjectSettingsFileName()));
	}

	private InputStream getProjectSettingsInputStream() throws FileNotFoundException {
		return new BufferedInputStream(new FileInputStream(getProjectSettingsFileName()));
	}



	private OutputStream getGlobalSettingsOutputStream() throws FileNotFoundException {
		return new BufferedOutputStream(new FileOutputStream(getGlobalSettingsFileName()));
	}

	private String getGlobalSettingsFileName() {
		return globalSettingsFileName;
	}

	private String getProjectSettingsFileName() {
		return projectSettingsFileName;
	}


	private InputStream getGlobalSettingsInputStream() throws FileNotFoundException {
		return new BufferedInputStream(new FileInputStream(getGlobalSettingsFileName()));
	}


	public ProjectConfiguration loadProjectConfiguration() throws ServerCfgFactoryException {
		return new ProjectConfiguration(loadProjectSpecificServers());
	}


	public GlobalConfiguration load() throws ServerCfgFactoryException {
		GlobalConfiguration res = new GlobalConfiguration();
		res.setGlobalServers(loadGlobalServers());
//		res.setProjectConfiguration(load());
		return res;
	}

	public Collection<ServerCfg> loadGlobalServers() throws ServerCfgFactoryException {
		return loadServers(new InputStreamProvider() {
			public InputStream provide() throws IOException {
				return getGlobalSettingsInputStream();
			}

			public String getDescription() {
				return GLOBAL_SERVERS_DATA;
			}
		});
	}


	public Collection<ServerCfg> loadServers(InputStreamProvider inputStreamProvider) throws ServerCfgFactoryException {
		XStream xStream = prepareXStream();
		try {
			@SuppressWarnings("unchecked")
			final Collection<ServerCfg> servers = (Collection<ServerCfg>) xStream.fromXML(inputStreamProvider.provide());
			return servers;
		} catch (IOException e) {
			throw new ServerCfgFactoryException("Cannot load global servers data", e);
		} catch (Exception e) {
			throw new ServerCfgFactoryException("Cannot load global servers data", e);
		}
	}
	


	public Collection<ServerCfg> loadProjectSpecificServers() throws ServerCfgFactoryException {
		return loadServers(new InputStreamProvider() {
			public String getDescription() {
				return PROJECT_SPECIFIC_SERVERS_DATA;
			}

			public InputStream provide() throws IOException {
				return getProjectSettingsInputStream();
			}
		});
	}



	public void saveGlobalServers(final Collection<ServerCfg> servers) throws ServerCfgFactoryException {
		saveServers(servers, new OutputStreamProvider() {

			public OutputStream provide() throws IOException {
				return getGlobalSettingsOutputStream();
			}

			public String getDescription() {
				return GLOBAL_SERVERS_DATA;
			}
		});
	}

	private interface OutputStreamProvider {
		OutputStream provide() throws IOException;
		String getDescription();
	}

	private void saveServers(final Collection<ServerCfg> servers, OutputStreamProvider streamProvider)
		throws ServerCfgFactoryException {
		OutputStream out = null;
		try {
			out = streamProvider.provide();
			saveServers(servers, out);
		} catch (IOException e) {
			throw new ServerCfgFactoryException("Cannot save " + streamProvider.getDescription(), e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					//noinspection ThrowFromFinallyBlock
					throw new ServerCfgFactoryException("Error closing " + streamProvider.getDescription() + " stream", e);
				}
			}
		}
	}


	private void saveServers(final Collection<ServerCfg> servers, final OutputStream out) throws ServerCfgFactoryException {
		if (servers == null) {
			throw new NullPointerException("Servers cannot be null");
		}
		XStream xStream = prepareXStream();
		xStream.toXML(servers, out);
	}

	private XStream prepareXStream() {
		XStream xStream = new XStream();
		xStream.alias("bamboo", BambooServerCfg.class);
		xStream.alias("crucible", CrucibleServerCfg.class);
		return xStream;
	}

	public void saveProjectSpecificServers(final Collection<ServerCfg> servers) throws ServerCfgFactoryException {
		saveServers(servers, new OutputStreamProvider() {

			public OutputStream provide() throws IOException {
				return getProjectSettingsOutputStream();
			}

			public String getDescription() {
				return PROJECT_SPECIFIC_SERVERS_DATA;
			}
		});
	}

	public void save(final GlobalConfiguration globalConfiguration) throws ServerCfgFactoryException {
		saveGlobalServers(globalConfiguration.getGlobalServers());
//		saveProjectSpecificServers(globalConfiguration.getProjectConfiguration().getServers());
	}

	private interface InputStreamProvider {
		InputStream provide() throws IOException;
		String getDescription();
	}


}
