package com.atlassian.theplugin.eclipse.bamboo;

import java.util.Collection;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.commons.bamboo.BambooStatusListener;

public class BambooTabListener implements BambooStatusListener {

	private BambooStatusDisplay display;

	public BambooTabListener(BambooStatusDisplay bambooTabDisplay) {
		display = bambooTabDisplay;
	}

	public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {
		
//		if (buildStatuses == null || buildStatuses.size() == 0) {
//			sb.append("No plans defined.");
//			status = BuildStatus.UNKNOWN;
//		} else {
//			List<BambooBuild> sortedStatuses = new ArrayList<BambooBuild>(buildStatuses);
//			Collections.sort(sortedStatuses, new Comparator<BambooBuild>() {
//				public int compare(BambooBuild b1, BambooBuild b2) {
//					return b1.getServerUrl().compareTo(b2.getServerUrl());
//				}
//			});
//
//			sb.append("<table width=\"100%\">");
//			String lastServer = null;
//
//			for (BambooBuild buildInfo : buildStatuses) {
//				if (!buildInfo.getServerUrl().equals(lastServer)) {
//					Server server = getServerFromUrl(buildInfo.getServerUrl());
//					if (server == null) { // PL-122 lguminski immuning to a situation when getServerFromUrl returns null 
//						continue;
//					}
//					if (lastServer != null) {
//						sb.append("<tr><td colspan=3>&nbsp;</td></tr>");
//					}
//					sb.append("<tr><td colspan=3>");
//					sb.append(
//							"<table width=100% cellpadding=0 cellspacing=0>"
//									+ "<tr>"
////                          + "<td width=1%><a href='"
//// 							+ server.getUrlString()
//// 							+ "'><img src=/icons/bamboo-blue-32.png height=32 width=32 border=0></a></td>"
//									+ "<td width=100%><b><a href='"
//									+ server.getUrlString()
//									+ "'>"
//									+ server.getName()
//									+ "</a></b><br>"
//									+ "<font style=\"font-size: 10pt;\" color=#999999>LAST UPDATE: "
//									+ TIME_DF.format(buildInfo.getPollingTime()) + "</font></td>"
//									+ "<td width=1% nowrap align=right valign=bottom style=\"font-size:10pt ;\">"
//									+ "</td></tr></table>");
//					sb.append("</td></tr>");
//				}
//				if (buildInfo.getEnabled()) {
//					switch (buildInfo.getStatus()) {
//						case BUILD_FAILED:
//							sb.append(getFailedBuildRow(buildInfo));
//							status = BuildStatus.BUILD_FAILED;
//							break;
//						case UNKNOWN:
//							sb.append(getErrorBuildRow(buildInfo));
////							if (status != BUILD_FAILED && status != BuildStatus.BUILD_SUCCEED) {
////								status = BuildStatus.UNKNOWN;
////							}
//							break;
//						case BUILD_SUCCEED:
//							sb.append(getSuccessBuildRow(buildInfo));
//							if (status != BuildStatus.BUILD_FAILED) {
//								status = BuildStatus.BUILD_SUCCEED;
//							}
//							break;
//						default:
//							throw new IllegalStateException("Unexpected build status encountered");
//					}
//				} else {
//					sb.append(getDisabledBuildRow(buildInfo));
//				}
//				lastServer = buildInfo.getServerUrl();
//			}
//			sb.append("</table>");
//		}
//		sb.append("</body></html>");
//		display.updateBambooStatus(status, sb.toString());
		
	}

	public void resetState() {
		// TODO Auto-generated method stub
		
	}

}
