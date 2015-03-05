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
package com.atlassian.theplugin.idea.ui;

import com.atlassian.theplugin.commons.util.MiscUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class MultiLineUtil {
	private static final int MINIMUM_WIDTH = 10;
	private static final int LINE_SEPARATION_PX = 4;

	private MultiLineUtil() {
	}


	public static Collection<LineBreakMeasurer> prepareLineBreakMeasurer(String text, Font font, FontRenderContext frc) {

		final String[] lines = text.split("\n");
		Collection<LineBreakMeasurer> res = MiscUtil.buildArrayList(lines.length);
		Map<AttributedCharacterIterator.Attribute, Object> map =
		Collections.<AttributedCharacterIterator.Attribute, Object>singletonMap(TextAttribute.FONT, font);

		for (String line : lines) {
			if (line.length() > 0) {
				final AttributedString attStr = new AttributedString(line, map);
				final LineBreakMeasurer measurer = new LineBreakMeasurer(attStr.getIterator(), frc);
				res.add(measurer);
			} else {
				res.add(null);
			}

		}
		return res;
	}

	public static int getHeight(String s, int width, Font font, FontRenderContext frc) {
		if (width < MINIMUM_WIDTH) {
			width = MINIMUM_WIDTH; // below 0 nextLayout() below throws java.lang.ArrayIndexOutOfBoundsException: -1
		}
		final Collection<LineBreakMeasurer> measurers = prepareLineBreakMeasurer(s, font, frc);
		int height = 0;
		for (LineBreakMeasurer measurer : measurers) {
			if (measurer == null) {
				TextLayout layout = new TextLayout("Ap", font, frc);
				height += (layout.getAscent());
				height += layout.getDescent() + layout.getLeading();
				height += LINE_SEPARATION_PX;
			} else {
				TextLayout layout;
				while (null != (layout = measurer.nextLayout(width))) {
					height += (layout.getAscent());
					height += layout.getDescent() + layout.getLeading();
					height += LINE_SEPARATION_PX;
				}
			}

		}
		return height;
	}

	@SuppressWarnings({"UnusedDeclaration"})
	public static int getCurrentWidth(JTree tree, int row) {
		// this 100 is lead offset from the left side of JTree and should be calculated depending on
		// current nesting on currently rendered row
		// however I have no idea how to calculate it.
		//CHECKSTYLE:MAGIC:OFF
		return tree.getSize().width - 100;
		//CHECKSTYLE:MAGIC:ON
	}

}
