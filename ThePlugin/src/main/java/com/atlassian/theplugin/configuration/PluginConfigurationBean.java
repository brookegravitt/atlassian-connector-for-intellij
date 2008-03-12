package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.util.Version;

public class PluginConfigurationBean implements PluginConfiguration {
    private BambooConfigurationBean bambooConfiguration = new BambooConfigurationBean();
    private CrucibleConfigurationBean crucibleConfiguration = new CrucibleConfigurationBean();
    private JiraConfigurationBean jiraConfiguration = new JiraConfigurationBean();
	private static final double ID_DISCRIMINATOR = 1e3d;
	private long uid = 0;
	private boolean isAutoUpdateEnabled = true;
	private Version rejectedUpgrade = Version.NULL_VERSION;

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
		this.setUid(cfg.getUid());
		this.setAutoUpdateEnabled(cfg.isAutoUpdateEnabled());
		this.setRejectedUpgrade(cfg.getRejectedUpgrade());
		this.setBambooConfigurationData(new BambooConfigurationBean(cfg.getProductServers(ServerType.BAMBOO_SERVER)));
        this.setCrucibleConfigurationData(new CrucibleConfigurationBean(cfg.getProductServers(ServerType.CRUCIBLE_SERVER)));
        this.setJIRAConfigurationData(new JiraConfigurationBean(cfg.getProductServers(ServerType.JIRA_SERVER)));
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

	public Version getRejectedUpgrade() {
		return rejectedUpgrade;
	}

	public void setRejectedUpgrade(Version rejectedUpgrade) {
		this.rejectedUpgrade = rejectedUpgrade;
	}

	public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PluginConfigurationBean that = (PluginConfigurationBean) o;

		if (uid != that.uid) {
            return false;
        }

		if (isAutoUpdateEnabled != that.isAutoUpdateEnabled) {
            return false;
        }

		if (!rejectedUpgrade.equals(that.rejectedUpgrade)) {
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
	private static final int SHIFT_VAL = 32;
    public int hashCode() {
        int result = 0;
        result = ONE_EFF * result + (bambooConfiguration != null ? bambooConfiguration.hashCode() : 0);
        result = ONE_EFF * result + (crucibleConfiguration != null ? crucibleConfiguration.hashCode() : 0);
        result = ONE_EFF * result + (jiraConfiguration != null ? jiraConfiguration.hashCode() : 0);
		result = ONE_EFF * result + (int) (uid ^ (uid >>> SHIFT_VAL));
		result = ONE_EFF * result + (isAutoUpdateEnabled ? 1 : 0);
		result = ONE_EFF * result + (rejectedUpgrade != null ? rejectedUpgrade.hashCode() : 0);
        return result;
    }
}