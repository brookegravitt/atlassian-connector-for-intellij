package com.atlassian.connector.intellij.crucible.content;

import com.atlassian.theplugin.commons.VersionedVirtualFile;
import org.apache.commons.lang.StringUtils;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Dec 30, 2009
 * Time: 3:08:17 PM
 * To change this template use File | Settings | File Templates.
 */
public final class ContentUtil {
    private ContentUtil() {
    }

    public static String getKey(VersionedVirtualFile virtualFile) {
        if (StringUtils.isBlank(virtualFile.getRevision()) && StringUtils.isBlank(virtualFile.getUrl())) {
            return "";
        }
        return virtualFile.getRevision() + ":" + virtualFile.getUrl();
    }

}
