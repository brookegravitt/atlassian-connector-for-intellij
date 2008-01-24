package com.atlassian.theplugin.bamboo;

import static com.atlassian.theplugin.bamboo.BuildStatus.FAILED;
import com.atlassian.theplugin.idea.BambooStatusIcon;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 3:49:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooStatusListenerImpl implements BambooStatusListener {
    private int counter = 0;

    BambooStatusIcon statusBarIcon;

    public BambooStatusListenerImpl(BambooStatusIcon icon) {
        statusBarIcon = icon;
    }

    private String getSuccessBuildRow(BambooBuild buildInfo) {
        StringBuilder sb = new StringBuilder("<tr><td><a href='");
        sb.append(buildInfo.getPlanUrl());
        sb.append("'>");
        sb.append(buildInfo.getBuildKey());
        sb.append("</a></td><td><a href='");
        sb.append(buildInfo.getBuildUrl());
        sb.append("'>");
        sb.append("build ");
        sb.append(buildInfo.getBuildNumber());
        sb.append("</a>");
        sb.append("</td><td>");
        sb.append("<font color=\"green\">success</font>");
        sb.append("</td></tr>");

        return sb.toString();
    }

    private String getFailedBuildRow(BambooBuild buildInfo) {
        StringBuilder sb = new StringBuilder("<tr><td><a href='");
        sb.append(buildInfo.getPlanUrl());
        sb.append("'>");
        sb.append(buildInfo.getBuildKey());
        sb.append("</a></td><td><a href='");
        sb.append(buildInfo.getBuildUrl());
        sb.append("'>");
        sb.append("build ");
        sb.append(buildInfo.getBuildNumber());
        sb.append("</a>");
        sb.append("</td><td>");
        sb.append("<font color=\"red\">failed</font>");
        sb.append("</td></tr>");

        return sb.toString();
    }

    private String getErrorBuildRow(BambooBuild buildInfo) {
        StringBuilder sb = new StringBuilder("<tr><td><a href='");
        sb.append(buildInfo.getPlanUrl());
        sb.append("'>");
        sb.append(buildInfo.getBuildKey());
        sb.append("</a></td><td></td><td>");
        sb.append("<font color=\"ltgray\">").append(buildInfo.getMessage()).append("</font>");
        sb.append("</td></tr>");

        return sb.toString();
    }

    public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {

        BuildStatus status = BuildStatus.SUCCESS;
        StringBuilder sb = new StringBuilder("<html><body>");

        if (buildStatuses == null || buildStatuses.size() == 0) {
            sb.append("No plans defined.");            
        } else {
            sb.append("<table>");
            for (BambooBuild buildInfo : buildStatuses) {
                switch (buildInfo.getStatus()) {
                    case FAILED:
                        sb.append(getFailedBuildRow(buildInfo));
                        status = FAILED;
                        break;
                    case ERROR:
                        sb.append(getErrorBuildRow(buildInfo));
                        if (status != FAILED) {
                            status = BuildStatus.ERROR;
                        }
                        break;
                    case SUCCESS:
                        sb.append(getSuccessBuildRow(buildInfo));
                        break;
                    default:
                        throw new IllegalStateException("Unexpected build status encountered");
                }
            }
            sb.append("</table>");
        }
        sb.append("</body></html>");
        statusBarIcon.updateBambooStatus(status, sb.toString());
    }
}
