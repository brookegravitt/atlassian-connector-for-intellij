package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BambooStatusListener;
import com.atlassian.theplugin.bamboo.BuildStatus;
import static com.atlassian.theplugin.bamboo.BuildStatus.BUILD_FAILED;
import com.intellij.openapi.wm.StatusBar;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This listener fires notification tooltip if bamboo build has changes status form SUCCEED to FAILED
 */
public class BambooStatusListenerImpl implements BambooStatusListener {

	//private BambooStatusDisplay bambooDisplay;
	private StatusBar projectStatusBar;

	private Map buildPrevStatus = new HashMap<String, BuildStatus>(0);
	private static final Color BACKGROUND_COLOR = new Color(255, 214, 214);

	/**
	 *
	 * @param statusBar reference to status bar needed to call fireNotificationPopup method
	 */
	public BambooStatusListenerImpl(StatusBar statusBar /*BambooStatusDisplay display*/) {
		//bambooDisplay = display;
		projectStatusBar = statusBar;
	}

	public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {
		String tooltipContent = new String();

		BuildStatus status = BuildStatus.BUILD_SUCCEED;

		if (buildStatuses != null && buildStatuses.size() > 0) {
			for (BambooBuild buildInfo : buildStatuses) {
				switch (buildInfo.getStatus()) {
					case BUILD_FAILED:
						if (buildPrevStatus.containsKey(buildInfo.getBuildKey())) {
							if (buildPrevStatus.get(buildInfo.getBuildKey()) == BuildStatus.BUILD_SUCCEED) {
								// build has changes status from SUCCEED to FAILED
								status = BUILD_FAILED;
								// prepare information
								tooltipContent +=
										"Build "
										+ buildInfo.getBuildKey()
										+ "-"
										+ buildInfo.getBuildNumber()
										+ " failed.\n";
							}
							buildPrevStatus.remove(buildInfo.getBuildKey());
						}

						buildPrevStatus.put(buildInfo.getBuildKey(), buildInfo.getStatus());

						break;
					case UNKNOWN:
						// no action here
						break;
					case BUILD_SUCCEED:

						if (buildPrevStatus.containsKey(buildInfo.getBuildKey())) {
							buildPrevStatus.remove(buildInfo.getBuildKey());
						}
						buildPrevStatus.put(buildInfo.getBuildKey(), buildInfo.getStatus());

						// prepare information
						//tooltipContent +=
						// 		"Build "
						// 		+ buildInfo.getBuildKey()
						// 		+ "/"
						// 		+ buildInfo.getBuildName()
						// 		+ "/"
						// 		+ buildInfo.getBuildNumber()
						// 		+ " succeed.\n";

						break;
					default:
						throw new IllegalStateException("Unexpected build status encountered");
				}
			}
		}

		if (status == BuildStatus.BUILD_FAILED) {
			// fire notification popup
			projectStatusBar.fireNotificationPopup(new JLabel(tooltipContent), BACKGROUND_COLOR);
		}

		//bambooDisplay.updateBambooStatus(status, "");
	}
}
