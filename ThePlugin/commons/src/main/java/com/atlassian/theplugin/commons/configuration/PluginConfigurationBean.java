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

package com.atlassian.theplugin.commons.configuration;

import com.atlassian.theplugin.commons.util.HttpConfigurableAdapter;

public class PluginConfigurationBean implements PluginConfiguration {
    private BambooConfigurationBean bambooConfiguration = new BambooConfigurationBean();

	private CrucibleConfigurationBean crucibleConfiguration = new CrucibleConfigurationBean();
	private JiraConfigurationBean jiraConfiguration = new JiraConfigurationBean();
	private GeneralConfigurationBean generalConfigurationBean = new GeneralConfigurationBean();
	private HttpConfigurableAdapter httpConfigurableAdapter;

	/**
	 * Default constructor.
	 */
	public PluginConfigurationBean() {
    }

	/**
	 * Copying constructor.<p>
	 * Makes a deep copy of provided configuration.
	 * @param cfg configuration to be deep copied.
	 */
	public PluginConfigurationBean(PluginConfiguration cfg) {
		setConfiguration(cfg);
    }

	/**
	 * Deep copies provided configuration.
	 * @param cfg configuration to be copied to current configuration object.
	 */
	public void setConfiguration(PluginConfiguration cfg) {

		this.setGeneralConfigurationData(new GeneralConfigurationBean(cfg.getGeneralConfigurationData()));
		this.setBambooConfigurationData(new BambooConfigurationBean(cfg.getBambooConfigurationData()));
		this.setCrucibleConfigurationData(new CrucibleConfigurationBean(cfg.getCrucibleConfigurationData()));
		this.setJIRAConfigurationData(new JiraConfigurationBean(cfg.getJIRAConfigurationData()));
		this.transientSetHttpConfigurable(cfg.transientGetHttpConfigurable());
	}

	/**
     * For storage purposes.
     * <p/>
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    public BambooConfigurationBean getBambooConfigurationData() {
        return bambooConfiguration;
    }

	/**
     * For storage purposes.
     * <p/>
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    public void setBambooConfigurationData(BambooConfigurationBean newConfiguration) {
        bambooConfiguration = newConfiguration;
    }

	/**
     * For storage purposes.
     * <p/>
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    public CrucibleConfigurationBean getCrucibleConfigurationData() {
        return crucibleConfiguration;
    }

	/**
     * For storage purposes.
     * <p/>
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    public void setCrucibleConfigurationData(CrucibleConfigurationBean newConfiguration) {
        crucibleConfiguration = newConfiguration;
    }

	/**
     * For storage purposes.
     * <p/>
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    public JiraConfigurationBean getJIRAConfigurationData() {
        return jiraConfiguration;
    }

	/**
     * For storage purposes.
     * <p/>
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    public void setJIRAConfigurationData(JiraConfigurationBean newConfiguration) {
        jiraConfiguration = newConfiguration;
    }

	public GeneralConfigurationBean getGeneralConfigurationData() {
		return generalConfigurationBean;
	}

	public void setGeneralConfigurationData(GeneralConfigurationBean aGeneralConfigurationBean) {
		this.generalConfigurationBean = aGeneralConfigurationBean;
	}

	public void transientSetHttpConfigurable(HttpConfigurableAdapter aHttpConfigurableAdapter) {
		this.httpConfigurableAdapter = aHttpConfigurableAdapter;
	}

	public HttpConfigurableAdapter transientGetHttpConfigurable() {
		return httpConfigurableAdapter;
	}

	/**
     * Implementation for the interface.
     * <p/>
     * Do not mistake for #getBambooConfigurationData()
     */
//    public ProductServerConfiguration getProductServers(ServerType serverType) {
//        switch (serverType) {
//            case BAMBOO_SERVER:
//                return bambooConfiguration;
//            case CRUCIBLE_SERVER:
//                return crucibleConfiguration;
//            case JIRA_SERVER:
//                return jiraConfiguration;
//            default:
//                return null;
//        }
//    }


//	public boolean isAnyServerDefined() {
//		for (ServerType serverType : ServerType.values()) {
//			if (!getProductServers(serverType).transientGetServers().isEmpty()) {
//				return true;
//			}
//		}
//		return false;
////		return bambooConfiguration.isAnyServer() || crucibleConfiguration.isAnyServer() ||
////			jiraConfiguration.isAnyServer();
//	}


//	public boolean isServerPresent(Server server) {
//		for (ServerType serverType : ServerType.values()) {
//			if (getProductServers(serverType).transientGetServer(server) != null) {
//				return true;
//			}
//		}
//		return false;
//	}


	public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PluginConfigurationBean that = (PluginConfigurationBean) o;


		if (!bambooConfiguration.equals(that.bambooConfiguration)) {
            return false;
        }

        if (!crucibleConfiguration.equals(that.crucibleConfiguration)) {
            return false;
        }

		if (!generalConfigurationBean.equals(that.generalConfigurationBean)) {
			return false;
		}

		return jiraConfiguration.equals(that.jiraConfiguration);
    }

    private static final int ONE_EFF = 31;
    public int hashCode() {
        int result = 0;
        result = ONE_EFF * result + (bambooConfiguration != null ? bambooConfiguration.hashCode() : 0);
        result = ONE_EFF * result + (crucibleConfiguration != null ? crucibleConfiguration.hashCode() : 0);
        result = ONE_EFF * result + (jiraConfiguration != null ? jiraConfiguration.hashCode() : 0);
        result = ONE_EFF * result + (generalConfigurationBean != null ? generalConfigurationBean.hashCode() : 0);

        return result;
    }
}