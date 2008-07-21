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