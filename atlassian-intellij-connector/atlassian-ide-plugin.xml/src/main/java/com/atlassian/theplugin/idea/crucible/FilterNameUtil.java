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

package com.atlassian.theplugin.idea.crucible;


import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to provide new filter names
 * User: mwent
 * Date: 2008-01-31
 * Time: 10:15:24
 */
public final class FilterNameUtil {

    private FilterNameUtil() { }

    /**
	 * Suggest a name to be used as the default for a new regexp context.
	 *
	 * @return
	 */
	public static String suggestNewName(Map existing) {
		return suggestName("Unnamed_{0}", existing);
	}

	public static String suggestName(String template, Map existing) {
		String regexp = MessageFormat.format(template, new Object[]{ "(\\d+)" });
		Pattern pattern = Pattern.compile(regexp);

		long maxIndex = 0;

		for (Iterator iter = existing.keySet().iterator(); iter.hasNext();) {
			String filterData
                    = (String) iter.next();
			Matcher m = pattern.matcher(filterData);
			if (m.matches()) {
				long index = Long.parseLong(m.group(1));
				if (index > maxIndex) {
					maxIndex = index;
				}
			}
		}
		return MessageFormat.format(template, new Object[]{ Long.valueOf(maxIndex + 1) });
	}
}