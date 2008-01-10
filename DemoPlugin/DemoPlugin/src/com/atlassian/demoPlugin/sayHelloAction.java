package com.atlassian.demoPlugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-01-10
 * Time: 17:05:26
 * To change this template use File | Settings | File Templates.
 */
public class sayHelloAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {

        Editor editor = (Editor) e.getDataContext().getData(DataKeys.EDITOR.getName()  );
        int offset = editor.getCaretModel().getOffset();
        CharSequence editorText = editor.getDocument().getCharsSequence();

        String word = getWordAtCursor(editorText, offset);

        if (word != null) {
            Messages.showMessageDialog(word, "some titl", Messages.getInformationIcon());
        } else {
            Messages.showMessageDialog("gowno wybrales", "some titl", Messages.getInformationIcon());
        }
    }

       private String getWordAtCursor(CharSequence editorText, int cursorOffset) {

        if (editorText.length() == 0 || cursorOffset >= editorText.length()) {
            return null;
        }

        if (cursorOffset > 0 && !Character.isJavaIdentifierPart(editorText.charAt(cursorOffset)) &&
                Character.isJavaIdentifierPart(editorText.charAt(cursorOffset - 1))) {
            cursorOffset--;
        }

        if (Character.isJavaIdentifierPart(editorText.charAt(cursorOffset))) {
            int start = cursorOffset;
            int end = cursorOffset;

            while(start > 0 && Character.isJavaIdentifierPart(editorText.charAt(start - 1))) {
                 start--;
            }

            while (end < editorText.length() && Character.isJavaIdentifierPart(editorText.charAt(end))) {
                end++;
            }

            return editorText.subSequence(start, end).toString();
        }

        return null;
    }
}
