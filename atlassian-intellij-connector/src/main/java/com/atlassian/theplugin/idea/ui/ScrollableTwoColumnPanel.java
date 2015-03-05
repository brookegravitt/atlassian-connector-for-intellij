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
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.Collection;

public class ScrollableTwoColumnPanel extends JScrollPane {
	private static final int VAL_COL = 4;

	public static class Entry {
		private final String label;
        private boolean error;

        private final String value;

        public Entry(String label, String value) {
            this(label, value, false);
        }

        public Entry(final String label,  final String value, boolean error) {
			this.value = value;
			this.label = label;
            this.error = error;
        }

		public String getLabel() {
			return label;
		}

		public String getValue() {
			return value;
		}

        public boolean isError() {
            return error;
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
			panel.setLayout(new BorderLayout());
			final JLabel label = new JLabel("No Custom Filter Defined", JLabel.CENTER);
			panel.setPreferredSize(label.getPreferredSize());
			panel.add(label);
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
			builder.addLabel(entry.getLabel() + ":", cc);
			cc.xy(VAL_COL, row * 2).vAlign = CellConstraints.TOP;
            if (entry.isError()) {
                builder.addLabel("<html>" + "<font color=\"red\">" + entry.getValue(), cc);    
            } else {
			    builder.addLabel("<html>" + entry.getValue(), cc);
            }
			row++;
		}
		// this lines (simulating JScrollPane resize)
		// are needed to more or less workaround the problem of revalidating whole scrollpane when more or fewer
		// rows could have been added. Without them you may end up with JScrollPane not completely showing its viewport
		panel.setPreferredSize(null);
		panel.setPreferredSize(new Dimension(getWidth(), panel.getPreferredSize().height));
		panel.validate();
		// it was needed here as sometimes panel was left with some old rubbish
		panel.repaint();
	}


	public static void main(String[] args) {
		final ScrollableTwoColumnPanel testPanel = new ScrollableTwoColumnPanel();
		Collection<Entry> entires = Arrays.asList(new Entry("some label", "very long value of the first lable - left"),
				new Entry("short l", "another very long value for the second lable - THE END"));
		testPanel.updateContent(entires);
		SwingAppRunner.run(testPanel);
	}

	// ------------------------------------------------------------------------
	// just for playing with Swing - it was quite hard to figure out how to deal with that. And current solution is still
	// far from perfect

	private static final class MyNewPanel extends JPanel {
		private static final int VAL_COLUMN = 4;

		private MyNewPanel() {
			setBorder(new BevelBorder(BevelBorder.RAISED, Color.RED, Color.CYAN));
			FormLayout layout = new FormLayout("12dlu, right:p, 14dlu, left:d, 12dlu");
			PanelBuilder builder = new PanelBuilder(layout, this);
			CellConstraints cc = new CellConstraints();
			builder.appendRow("p");
			cc.xy(2, 1).vAlign = CellConstraints.TOP;
			builder.addLabel("My Label:", cc);
			final Component wrapLabel = new JLabel("<html>Very long text which should be wraaaaaaped wrap!");
//			final JTextArea wrapLabel = new JTextArea("Very long text which should be wraaaaaaped wrap!");
//			wrapLabel.setWrapStyleWord(false);
//			wrapLabel.setLineWrap(true);
			builder.add(wrapLabel, cc.xy(VAL_COLUMN, 1));
			builder.appendRow("p");
			cc.xy(2, 2).vAlign = CellConstraints.TOP;
			builder.addLabel("My Label:", cc);
			final Component wrapLabel2 = new JLabel("<html>Very long text fds kdsfkl kdsfh kl ldsfjlkjdfs"
					+ " ljdfs lksjdjfkldsjfklsd lfjwhich should be wraaaaaaped wrap!");
			builder.add(wrapLabel2, cc.xy(VAL_COLUMN, 2));
			add(wrapLabel);
		}
	}

//	private static class MyOuterPanel extends JPanel {
//		private MyOuterPanel() {
//			super(new BorderLayout());
//			add(new MyNewPanel());
//		}
//	}



	// just for testing bloody Swing layouts with JScrollPane
	public static void main2(String[] args) {
		//SwingAppRunner.run(new MyNewPanel());
//		SwingAppRunner.run(new JScrollPane(new MyNewPanel()));
//		SwingAppRunner.run(new JScrollnew MyOuterPanel());
		final MyNewPanel panel = new MyNewPanel();
		final JScrollPane jScrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				panel.setPreferredSize(null);
				panel.setPreferredSize(new Dimension(e.getComponent().getWidth(), panel.getPreferredSize().height));
			}
		});
		SwingAppRunner.run(jScrollPane);
	}

}
