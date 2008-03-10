package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.intellij.ide.BrowserUtil;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.util.ui.ListTableModel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BambooTableModel extends ListTableModel {

	private List<BambooBuildAdapter> builds = Collections.EMPTY_LIST;
    private static final int NUM_COLUMNS = 3;

    public void setItems(Collection<BambooBuild> builds) {
		for (BambooBuild build : builds) {
			this.builds.add(new BambooBuildAdapter(build));
		}
    }

    public int getRowCount() {
        return builds.size();
    }

    public int getColumnCount() {
        return NUM_COLUMNS;
    }

    public Class<?> getColumnClass(int col) {
        if (col == 0) {
            return Icon.class;
        } else if (col == 1) {
            return JLabel.class;
        }

        return String.class;
    }

    public Object getValueAt(int row, int col) {
        final BambooBuildAdapter build = builds.get(row);

        switch (col) {
            case 0:
				return build.getBuildIcon();
            case 1:
                HyperlinkLabel hyperlinkLabel = new HyperlinkLabel(build.getBuildResultUrl());
                hyperlinkLabel.addHyperlinkListener(new HyperlinkListener() {
                    public void hyperlinkUpdate(HyperlinkEvent event) {
                        BrowserUtil.launchBrowser(build.getBuildResultUrl());
                    }
                });
                return hyperlinkLabel;
            case 2:
                return build.getProjectKey();
            default:
                return "";
        }
    }

    public String getColumnName(int col) {
        switch (col) {
            case 0:
                return "Status";
            case 1:
                return "Build";
            case 2:
                return "Project";
            default:
                return "";
        }
    }

    public BambooBuildAdapter getBuildAtRow(int row) {
        return builds.get(row);
    }
}