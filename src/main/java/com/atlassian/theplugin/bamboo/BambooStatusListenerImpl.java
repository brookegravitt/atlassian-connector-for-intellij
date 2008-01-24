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

    public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {

        BuildStatus status = BuildStatus.SUCCESS;
        StringBuilder sb = new StringBuilder("<html><body>");

        if (buildStatuses == null || buildStatuses.size() == 0) {
            sb.append("No plans defined.");            
        } else {
            sb.append("<table>");
            for (BambooBuild buildInfo : buildStatuses) {
                sb.append("<tr><td>");
                sb.append(buildInfo.getBuildKey());
                sb.append("</td><td>");
                switch (buildInfo.getStatus()) {
                    case FAILED:
                        sb.append("<font color=\"red\">build failed</font>");
                        status = FAILED;
                        break;
                    case ERROR:
                        sb.append("<font color=\"ltgray\">").append(buildInfo.getMessage()).append("</font>");
                        if (status != FAILED) {
                            status = BuildStatus.ERROR;
                        }
                        break;
                    case SUCCESS:
                        sb.append("<font color=\"green\">success</font>");
                        break;
                    default:
                        throw new IllegalStateException("Unexpected build status encountered");
                }
                sb.append("</td></tr>");
            }
            sb.append("</table>");
        }
        sb.append("</body></html>");
        statusBarIcon.updateBambooStatus(status, sb.toString());
    }
}
