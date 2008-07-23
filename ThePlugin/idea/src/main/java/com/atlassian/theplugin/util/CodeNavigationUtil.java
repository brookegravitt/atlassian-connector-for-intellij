package com.atlassian.theplugin.util;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class CodeNavigationUtil {
    @Nullable
    public static PsiFile guessMatchingFile(String pathname, PsiFile[] psifiles, VirtualFile baseDir) {
        for (PsiFile psiFile : psifiles) {
            String relativePath = VfsUtil.getPath(baseDir, psiFile.getVirtualFile(), File.separatorChar);
            if (pathname.endsWith(relativePath) == true) {
                return psiFile;
            }

        }
        return null;
    }
}
