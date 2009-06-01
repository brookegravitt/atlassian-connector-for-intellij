package com.atlassian.theplugin.idea.ui;

import com.atlassian.theplugin.idea.Constants;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.UIUtil;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class InformationDialogWithCheckBox extends DialogWrapper {
    private Icon infoIcon = IconLoader.getIcon("/actions/help.png");
    private JPanel panel1;
    private JCheckBox doNotShow;
    private JEditorPane description;

    public InformationDialogWithCheckBox(Project project, final String title, final String description) {
        super(project, false);
        init();
        setTitle(title);
        this.description.setBackground(UIUtil.getLabelBackground());
        this.description.setContentType("text/html");
        this.description.setEditorKit(new ClasspathHTMLEditorKit());
        this.description.setText("<html>" + Constants.BODY_WITH_STYLE + description + "</html>");
//        panel1.revalidate();
//        panel1.setOpaque(true);
//        setModal(true);

    }

    @Override
    protected Action[] createActions() {
        return new Action[]{getOKAction()};
    }

    protected JComponent createCenterPanel() {
        return panel1;
    }

    public boolean isDoNotShowChecked() {
        return doNotShow.isSelected();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

}
