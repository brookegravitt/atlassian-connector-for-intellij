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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;
import java.util.Arrays;

public class ScrollableTwoColumnPanel extends JScrollPane {
	private static final int VAL_COL = 4;

	public static class Entry {
		public final String label;
		public final String value;

		public Entry(final String label,  final String value) {
			this.value = value;
			this.label = label;
		}
	}

	private final JPanel panel = new JPanel();

	public ScrollableTwoColumnPanel() {
		super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		setViewportView(panel);
		getViewport().setBackground(panel.getBackground());

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				panel.setPreferredSize(null);
				panel.setPreferredSize(new Dimension(e.getComponent().getWidth(), panel.getPreferredSize().height));
			}
		});

		setWheelScrollingEnabled(true);
	}

	public void updateContent(@Nullable Collection<Entry> entries) {
		panel.removeAll();
		if (entries == null || entries.size() == 0) {
			panel.add(new JLabel("no filter defined"));
			return;
		}

		FormLayout layout = new FormLayout("4dlu, right:p, 4dlu, left:d, 4dlu");
		PanelBuilder builder = new PanelBuilder(layout, panel);

		int row = 1;
		CellConstraints cc = new CellConstraints();
		for (Entry entry : entries) {
			builder.appendRow("2dlu");
			builder.appendRow("p");
			cc.xy(2, row * 2).vAlign = CellConstraints.TOP;
			builder.addLabel(entry.label + ":", cc);
			cc.xy(VAL_COL, row * 2).vAlign = CellConstraints.TOP;
			builder.addLabel("<html>" + entry.value, cc);
			row++;
		}
		// this two line (simulating JScrollPane resize)
		// are needed to more or less workaround the problem of revalidating whole scrollpane when more or fewer
		// rows could have been added. Without them you may end up with JScrollPane not completely showing its viewport
		panel.setPreferredSize(null);
		panel.setPreferredSize(new Dimension(getWidth(), panel.getPreferredSize().height));
	}


	public static void main(String[] args) {
		final ScrollableTwoColumnPanel panel = new ScrollableTwoColumnPanel();
		Collection<Entry> entires = Arrays.asList(new Entry("some label", "very long value of the first lable - left"),
				new Entry("short l", "another very long value for the second lable - THE END"));
		panel.updateContent(entires);
		SwingAppRunner.run(panel);
	}
}
