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
package com.atlassian.theplugin.idea.config.serverconfig.defaultCredentials;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public enum ConnectionStatus {
    
    NONE(safeGetIcon("/actions/help.png", "/actions/help.png")),
    // stupid idea throws an exception internally anyway if there is no icon. please kill me
    PASSED(safeGetIcon("/icons/icn_plan_passed.gif", "/icons/icn_plan_passed.gif")),
    FAILED(safeGetIcon("/icons/icn_plan_failed.gif", "/icons/icn_plan_failed.gif"));

    private final Icon icon;

    Icon getIcon() {
        return icon;
    }

    ConnectionStatus(Icon icon) {
        this.icon = icon;
    }

    private static Icon safeGetIcon(String icon, String backup) {
        try {
            return IconLoader.getIcon(icon);
        } catch (Throwable t) {
            try {
                return IconLoader.getIcon(backup);
            } catch (Throwable t2) {
                return null;
            }
        }
    }
}

