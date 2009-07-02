package thirdparty.javaworld;

import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

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
