package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.util.PicoUtil;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.AreaInstance;
import com.intellij.openapi.extensions.AreaListener;
import com.intellij.openapi.extensions.AreaPicoContainer;
import com.intellij.openapi.extensions.Extensions;

public class PerProjectPicoInit implements AreaListener {

	public void areaCreated(String areaClass, AreaInstance areaInstance) {
		if (PluginManager.AREA_IDEA_PROJECT.equals(areaClass)) {
			AreaPicoContainer apc = Extensions.getArea(areaInstance).getPicoContainer();
			PicoUtil.populateProjectPicoContainer(apc);
		}
	}

	public void areaDisposing(String areaClass, AreaInstance areaInstance) {
	}
}
