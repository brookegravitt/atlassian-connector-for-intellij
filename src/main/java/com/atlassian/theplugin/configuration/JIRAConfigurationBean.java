package com.atlassian.theplugin.configuration;

/**
 * Bean storing information about Bamboo servers etc.<p>
 * The class serves both as a configuration provider for plugin logic and Bean for persistence.
 */
public class JIRAConfigurationBean extends AbstractConfigurationBean
{
	public JIRAConfigurationBean() {
        super();
    }

	public JIRAConfigurationBean(ProductServerConfiguration cfg) {
		super(cfg);
	}
}