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

package com.atlassian.connector.intellij.bamboo;

import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.configuration.BambooTooltipOption;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This listener fires crucible tooltip if bamboo build has changes status between SUCCEED and FAILED
 */
public class BambooStatusTooltipListener implements BambooStatusListener {

    private final Map<String, BambooBuildAdapter> prevBuildStatuses = new HashMap<String, BambooBuildAdapter>(0);
    private final BambooStatusDisplay display;
    private final PluginConfiguration pluginConfiguration;
    private BambooPopupInfo popupInfo = new BambooPopupInfo();
    private BambooBuildGroup prevGroup;


    /**
     * @param display             reference to display component
     * @param pluginConfiguration cfg how incoming status changes should be handled
     */
    public BambooStatusTooltipListener(final BambooStatusDisplay display,
                                       final PluginConfiguration pluginConfiguration) {
        this.display = display;
        this.pluginConfiguration = pluginConfiguration;
    }

    public static boolean isBuildStatusFailed(final BambooBuildAdapter currentBuild, final BambooBuildAdapter prevBuild,
                                              BambooTooltipOption option) {
        if (currentBuild.getStatus() == BuildStatus.FAILURE) {
            if (prevBuild != null) {
                if (prevBuild.getStatus() == BuildStatus.SUCCESS
                        || (prevBuild.getStatus() == BuildStatus.FAILURE
                        && currentBuild.isValid() && prevBuild.isValid()
                        && prevBuild.getNumber() != currentBuild.getNumber()
                        && option == BambooTooltipOption.ALL_FAULIRES_AND_FIRST_SUCCESS)) {

                    // build has changed status from SUCCEED to FAILED
                    // or this is new build and still failed
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public void updateBuildStatuses(Collection<BambooBuildAdapter> buildsUpdate,
                                    Collection<Exception> generalExceptions) {
        BuildStatus iconStatus = privateUpdateBuildStatuses(buildsUpdate, prevBuildStatuses, pluginConfiguration, popupInfo);
        BambooBuildGroup updatedGroup = new BambooBuildGroup(buildsUpdate);
        if (updatedGroup.isGroup()) {
            iconStatus = privateUpdateBuildStatuses(updatedGroup.getGroupBuilds(),
                    prevGroup != null ? prevGroup.getBuildStatuses() : null, pluginConfiguration, popupInfo);
            prevGroup = updatedGroup;
        }

        if (popupInfo.getBambooBuilds().size() > 0) {
            //fire tooltip
            if (iconStatus != null) {
                display.updateBambooStatus(iconStatus, popupInfo);
            }
        }
    }

    public static BuildStatus privateUpdateBuildStatuses(Collection<BambooBuildAdapter> buildsUpdate,
                                                         Map<String, BambooBuildAdapter> prevStatuses,
                                                         PluginConfiguration pluginCfg, BambooPopupInfo infos) {
        infos.clear();
        final BambooTooltipOption bambooTooltipOption = pluginCfg != null
                ? pluginCfg.getBambooConfigurationData().getBambooTooltipOption()
                : null;
        if (bambooTooltipOption == BambooTooltipOption.NEVER) {
            return null;
        }
        BuildStatus status = null;
        if (buildsUpdate != null && buildsUpdate.size() > 0) {

            for (BambooBuildAdapter currentBuild : buildsUpdate) {
                if (pluginCfg == null || !pluginCfg.getBambooConfigurationData().isOnlyMyBuilds()
                        || (pluginCfg.getBambooConfigurationData().isOnlyMyBuilds() && currentBuild.isMyBuild())) {

                    // if the build was reported then check it, if not then skip it
                    BambooBuildAdapter prevBuild = prevStatuses != null ? prevStatuses.get(getBuildMapKey(currentBuild)) : null;
                    switch (currentBuild.getStatus()) {
                        case FAILURE:
                            if (isBuildStatusFailed(currentBuild, prevBuild, bambooTooltipOption)) {
                                // build has changed status from SUCCEED to FAILED
                                // or this is new build and still failed
                                status = BuildStatus.FAILURE;
                                // prepare information
                                infos.add(currentBuild);
                            }
                            if (prevStatuses != null) {
                                prevStatuses.put(getBuildMapKey(currentBuild), currentBuild);
                            }
                            break;
                        case UNKNOWN:
                            // no action here
                            break;
                        case SUCCESS:
                            if (prevBuild != null) {
                                if (prevBuild.getStatus() == BuildStatus.FAILURE) {
                                    // build has changed status from FAILED to SUCCEED
                                    if (status == null) {
                                        status = BuildStatus.SUCCESS;
                                    }
                                    // prepare information
                                    infos.add(currentBuild);
                                }
                            } else {
                                status = BuildStatus.SUCCESS;
                                //infos.add(currentBuild);
                            }
                            if (prevStatuses != null) {
                                prevStatuses.put(getBuildMapKey(currentBuild), currentBuild);
                            }
                            break;
                        default:
                            throw new IllegalStateException("Unexpected build status encountered");
                    }
                }
            }
        }
        return status;
    }

    private static String getBuildMapKey(BambooBuildAdapter build) {
        String serverId = "none";
        if (build.getServer() != null) {
            serverId = build.getServer().getServerId().toString();
        }
        return serverId + build.getPlanKey();
    }

    public void resetState() {
        popupInfo.clear();
    }
}
