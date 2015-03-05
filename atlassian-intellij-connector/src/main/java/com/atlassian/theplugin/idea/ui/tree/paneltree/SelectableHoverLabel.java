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
package com.atlassian.theplugin.idea.ui.tree.paneltree;

import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jacek Jaroczynski
 */
public class SelectableHoverLabel extends SelectableLabel {
	public SelectableHoverLabel(final boolean selected, final boolean hover, final boolean enabled, final String text,
			final Icon icon, final int alignment, final int iconHeight) {

		super(selected, enabled, text, icon, alignment, iconHeight);

		setSelected(selected, hover, enabled);
	}

	public void setSelected(boolean selected, final boolean hover, final boolean enabled) {
		setBackground(getBgColor(selected, hover));
		setForeground(getFgColor(selected, enabled));
	}

	public static Color getBgColor(final boolean selected, final boolean hover) {
		Color color = UIUtil.getTreeTextBackground();

		if (selected) {
			color = UIUtil.getTreeSelectionBackground();
		} else if (hover) {
			color = Color.LIGHT_GRAY;
		}

		return color;
	}

	public static Color getFgColor(boolean selected, boolean enabled) {
		Color fgColor = UIUtil.getTreeTextForeground();

		if (selected) {
			fgColor = UIUtil.getTreeSelectionForeground();
			fgColor = enabled ? fgColor : UIUtil.getInactiveTextColor();
		}

		return fgColor;
	}
}
