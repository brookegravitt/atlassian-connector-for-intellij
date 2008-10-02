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
package com.atlassian.theplugin.commons.util;

import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;

import java.io.IOException;

/**
 * @author Jacek Jaroczynski
 */
public abstract class XmlUtil {
	private XmlUtil() {
	}

	public static void printXml(Document request) {
		XMLOutputter o = new XMLOutputter(Format.getPrettyFormat());
		try {
			o.output(request, System.out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
