package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.ServerType;

public class PluginConfigurationBean implements PluginConfiguration {
    private boolean pluginEnabled = true;
    private BambooConfigurationBean bambooConfiguration = new BambooConfigurationBean();
    private CrucibleConfigurationBean crucibleConfiguration = new CrucibleConfigurationBean();
    private JIRAConfigurationBean jiraConfiguration = new JIRAConfigurationBean();
	private static final double ID_DISCRIMINATOR = 1e3d;
	private long uid = 0;
	private boolean isAutoUpdateEnabled = true;

	public PluginConfigurationBean() {
    }

    public PluginConfigurationBean(PluginConfiguration cfg) {
        this.setPluginEnabled(cfg.isPluginEnabled());
        this.setBambooConfigurationData(new BambooConfigurationBean(cfg.getProductServers(ServerType.BAMBOO_SERVER)));
        this.setCrucibleConfigurationData(new CrucibleConfigurationBean(cfg.getProductServers(ServerType.CRUCIBLE_SERVER)));
        this.setJIRAConfigurationData(new JIRAConfigurationBean(cfg.getProductServers(ServerType.JIRA_SERVER)));
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
    public JIRAConfigurationBean getJIRAConfigurationData() {
        return jiraConfiguration;
    }

    /**
     * For storage purposes.
     * <p/>
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    public void setJIRAConfigurationData(JIRAConfigurationBean newConfiguration) {
        jiraConfiguration = newConfiguration;
    }

    public boolean isPluginEnabled() {
        return pluginEnabled;
    }

    public void setPluginEnabled(boolean value) {
        pluginEnabled = value;
    }

    /**
     * Implementation for the interface.
     * <p/>
     * Do not mistake for #getBambooConfigurationData()
     */
    public ProductServerConfiguration getProductServers(ServerType serverType) {
        switch (serverType) {
            case BAMBOO_SERVER:
                return bambooConfiguration;
            case CRUCIBLE_SERVER:
                return crucibleConfiguration;
            case JIRA_SERVER:
                return jiraConfiguration;
            default:
                return null;
        }
    }

	public long getUid() {
		if (uid == 0)  {
			uid = System.currentTimeMillis() + (long) (Math.random() * ID_DISCRIMINATOR);
		}
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public boolean isAutoUpdateEnabled() {
		return isAutoUpdateEnabled;
	}

	public void setAutoUpdateEnabled(boolean autoUpdateEnabled) {
		isAutoUpdateEnabled = autoUpdateEnabled;
	}

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PluginConfigurationBean that = (PluginConfigurationBean) o;

        if (pluginEnabled != that.pluginEnabled) {
            return false;
        }

		if (uid != that.uid) {
            return false;
        }

		if (isAutoUpdateEnabled != that.isAutoUpdateEnabled) {
            return false;
        }


		if (!bambooConfiguration.equals(that.bambooConfiguration)) {
            return false;
        }

        if (!crucibleConfiguration.equals(that.crucibleConfiguration)) {
            return false;
        }

        return jiraConfiguration.equals(that.jiraConfiguration);
    }

    private static final int ONE_EFF = 31;
    public int hashCode() {
        int result;
        result = (pluginEnabled ? 1 : 0);
        result = ONE_EFF * result + (bambooConfiguration != null ? bambooConfiguration.hashCode() : 0);
        result = ONE_EFF * result + (crucibleConfiguration != null ? crucibleConfiguration.hashCode() : 0);
        result = ONE_EFF * result + (jiraConfiguration != null ? jiraConfiguration.hashCode() : 0);
        return result;
    }
}