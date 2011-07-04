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

package com.atlassian.theplugin.idea.config.serverconfig.util;


import com.atlassian.theplugin.commons.cfg.ServerCfg;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to provide new server names
 * User: mwent
 * Date: 2008-01-31
 * Time: 10:15:24
 */
public final class ServerNameUtil {

    private ServerNameUtil() { }
    
    /**
	 * Suggest a name to be used as the default for a new regexp context.
	 *
	 * @return
	 */
	public static String suggestNewName(Collection<ServerCfg> existing) {
		return suggestName("Unnamed_{0}", existing);
	}

	/**
	 * Suggest a name to be used as the default for a copy of the specified
	 * regexp context.
	 *
	 * @param server
	 * @return
	 */
	public static String suggestCopyName(ServerCfg server, Collection<ServerCfg> existing) {
		return suggestName("Copy_{0}_of_" + server.getName(), existing);
	}

	public static String suggestName(String template, Collection<ServerCfg> existing) {
		String regexp = MessageFormat.format(template, "(\\d+)");
		Pattern pattern = Pattern.compile(regexp);

		long maxIndex = 0;

        for (ServerCfg server : existing) {
            Matcher m = pattern.matcher(server.getName());
            if (m.matches()) {
                long index = Long.parseLong(m.group(1));
                if (index > maxIndex) {
                    maxIndex = index;
                }
            }
        }
		return MessageFormat.format(template, maxIndex + 1L);
	}	
}
