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
package com.atlassian.theplugin.idea.jira.controls;

import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.JIRAActionFieldBean;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import javax.swing.text.EditorKit;
import java.awt.*;

/**
 * @autrhor pmaruszak
 * @date Jul 8, 2010
 */
public class FieldEditorPane extends JScrollPane implements ActionFieldEditor {
    private JIRAActionField field;
    private JEditorPane editorPane;

    public FieldEditorPane(final String contentText, final JIRAActionField field, final boolean enableHtml) {

        this.field = field;
        if (enableHtml) {
            editorPane = new JEditorPane("text/html", contentText);
            EditorKit kit = new ClasspathHTMLEditorKit();
            editorPane.setEditorKit(kit);
            editorPane.setEditorKitForContentType("text/html", kit);
            editorPane.setContentType("text/html");
            editorPane.setText(contentText);
        } else {
            editorPane = new JEditorPane("text/plain", contentText);
        }


        this.setViewportView(editorPane);
        this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    }

    public JIRAActionField getEditedFieldValue() {
        JIRAActionField ret = new JIRAActionFieldBean(field);
        int startIndex = editorPane.getText().indexOf("<body>");
        int endIndex = editorPane.getText().indexOf("</body>");
        ret.addValue(editorPane.getText().substring(startIndex + 6, endIndex));
        return ret;
    }

    public Component getComponent() {
        return this;
    }

    public String getFieldName() {
        return field.getName();
    }
}
