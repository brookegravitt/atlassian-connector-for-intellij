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

package com.atlassian.theplugin.util;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class CodeNavigationUtil {

    private CodeNavigationUtil() {
        // this is utility class
    }

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
