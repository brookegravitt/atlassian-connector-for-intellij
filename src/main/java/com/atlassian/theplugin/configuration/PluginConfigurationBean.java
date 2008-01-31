package com.atlassian.theplugin.configuration;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 4:16:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class PluginConfigurationBean implements PluginConfiguration, Cloneable {
	private BambooConfigurationBean bambooConfiguration = new BambooConfigurationBean();


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
	 * Implemnentation for the interface.
	 * <p/>
	 * Do not mistake for #getBambooConfigurationData()
	 */
	public BambooConfiguration getBambooConfiguration() {
		return bambooConfiguration;
	}

	public Object clone() throws CloneNotSupportedException {
		PluginConfigurationBean cfgBean = new PluginConfigurationBean();
		cfgBean.setBambooConfigurationData((BambooConfigurationBean)this.getBambooConfigurationData().clone());
		return cfgBean;	
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

		return true;
	}

	public int hashCode() {
		return bambooConfiguration.hashCode();
	}
}