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

package com.atlassian.theplugin.idea.util.memoryvfs;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.vfs.DeprecatedVirtualFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * A file system for content that resides only in memory.
 *
 * @author Steve Chaloner
 */
public final class PlainTextMemoryVirtualFileSystem extends DeprecatedVirtualFileSystem implements ApplicationComponent {
    /**
     * The name of the component.
     */
    private static final String COMPONENT_NAME = "PlainText-MemoryFileSystem";

    @NonNls
    public String getProtocol() {
        return Constants.PLAINTEXT_PROTOCOL;
    }

    @Nullable
    public VirtualFile findFileByPath(@NotNull @NonNls String s) {
        return null;
    }

    public void refresh(boolean b) {
    }

    @Nullable
    public VirtualFile refreshAndFindFileByPath(String s) {
        return null;
    }

    protected void deleteFile(Object o, VirtualFile virtualFile) throws IOException {
        throw new UnsupportedOperationException("method deleteFile not implemented");
    }

    protected void moveFile(Object o, VirtualFile virtualFile, VirtualFile virtualFile1) throws IOException {
        throw new UnsupportedOperationException("method moveFile not implemented");
    }

    protected void renameFile(Object o, VirtualFile virtualFile, String s) throws IOException {
        throw new UnsupportedOperationException("method renameFile not implemented");
    }

    protected VirtualFile createChildFile(Object o, VirtualFile virtualFile, String s) throws IOException {
        throw new UnsupportedOperationException("method createChildFile not implemented");
    }

    protected VirtualFile createChildDirectory(Object o, VirtualFile virtualFile, String s) throws IOException {
        throw new UnsupportedOperationException("method createChildDirectory not implemented");
    }

    protected VirtualFile copyFile(Object o, VirtualFile virtualFile, VirtualFile virtualFile1, String s) throws IOException {
        throw new UnsupportedOperationException("method copyFile not implemented");
    }

    private static final PlainTextMemoryVirtualFileSystem INSTANCE = new PlainTextMemoryVirtualFileSystem();

    public static PlainTextMemoryVirtualFileSystem getInstance() {
        return INSTANCE;
    }

    public void disposeComponent() {
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public void initComponent() {
    }
}