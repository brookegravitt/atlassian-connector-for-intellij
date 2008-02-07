package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.idea.config.serverconfig.model.ServerType;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 4:16:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class PluginConfigurationBean implements PluginConfiguration {
	private boolean pluginEnabled = true;
	private BambooConfigurationBean bambooConfiguration = new BambooConfigurationBean();
	private CrucibleConfigurationBean crucibleConfiguration = new CrucibleConfigurationBean();

	public PluginConfigurationBean() {
	}

	public PluginConfigurationBean(PluginConfiguration cfg) {
		this.setPluginEnabled(cfg.isPluginEnabled());
		this.setBambooConfigurationData(new BambooConfigurationBean(cfg.getProductServers(ServerType.BAMBOO_SERVER)));
		this.setCrucibleConfigurationData(new CrucibleConfigurationBean(cfg.getProductServers(ServerType.CRUCIBLE_SERVER)));
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

	public boolean isPluginEnabled() {
		return pluginEnabled;
	}

	public void setPluginEnabled(boolean value) {
		pluginEnabled = value;
	}

    /**
	 * Implemnentation for the interface.
	 * <p/>
	 * Do not mistake for #getBambooConfigurationData()
	 */
    public ProductServerConfiguration getProductServers(ServerType serverType) {
        switch (serverType) {
            case BAMBOO_SERVER:
                return bambooConfiguration;
            case CRUCIBLE_SERVER:
                return crucibleConfiguration;
        }
        return null;
    }

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

        return true;
	}

	public int hashCode() {
		return bambooConfiguration.hashCode();
	}
}