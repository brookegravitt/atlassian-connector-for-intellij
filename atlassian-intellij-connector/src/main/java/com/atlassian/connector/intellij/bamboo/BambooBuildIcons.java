package com.atlassian.connector.intellij.bamboo;

import com.intellij.openapi.util.IconLoader;
import javax.swing.Icon;

public final class BambooBuildIcons {
	private BambooBuildIcons() {
		
	}
	public static final Icon ICON_RED = IconLoader.getIcon("/icons/icn_plan_failed.gif");
	public static final Icon ICON_GREEN = IconLoader.getIcon("/icons/icn_plan_passed.gif");
	public static final Icon ICON_GREY = IconLoader.getIcon("/icons/icn_plan_disabled.gif");

	public static final Icon ICON_IS_IN_QUEUE = IconLoader.getIcon("/icons/cup.png");
	public static final Icon[] ICON_IS_BUILDING = {
			IconLoader.getIcon("/icons/icn_building_1.gif"),
			IconLoader.getIcon("/icons/icn_building_2.gif"),
			IconLoader.getIcon("/icons/icn_building_3.gif"),
			IconLoader.getIcon("/icons/icn_building_4.gif"),
			IconLoader.getIcon("/icons/icn_building_5.gif"),
			IconLoader.getIcon("/icons/icn_building_6.gif"),
			IconLoader.getIcon("/icons/icn_building_7.gif"),
			IconLoader.getIcon("/icons/icn_building_8.gif")};

	public static final Icon ICON_MY_BUILD_RED = IconLoader.getIcon("/actions/lightning.png");
	public static final Icon ICON_MY_BUILD_GREEN = IconLoader.getIcon("/icons/lightning_green.png");

}
