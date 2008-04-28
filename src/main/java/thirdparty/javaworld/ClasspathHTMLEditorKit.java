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

package thirdparty.javaworld;

import javax.swing.text.html.*;
import javax.swing.text.*;

/**
 * A simple extension to HTMLEditorKit which allows for loading of images absolutely off the classpath.
 *
 * To load an image, simply specify <img src="/image/foo.gif"> where the image is anywhere on the classpath in /image.
 *
 * @see ClasspathImageView
 */
public class ClasspathHTMLEditorKit extends HTMLEditorKit {
	static final long serialVersionUID = -6514354184056679038L;

	public ViewFactory getViewFactory() {
		return new HTMLFactoryX();
	}

	public static class HTMLFactoryX extends HTMLFactory {

		public View create(Element elem) {
			Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
			if (o instanceof HTML.Tag) {
				HTML.Tag kind = (HTML.Tag) o;
				if (kind == HTML.Tag.IMG) {
					return new ClasspathImageView(elem);
				}
			}
			return super.create(elem);
		}
	}
}