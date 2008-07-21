package com.atlassian.theplugin.idea.util.memoryvfs;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.DeprecatedVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A memory-based file.
 *
 * @author Steve Chaloner
 */
public class PlainTextMemoryVirtualFile extends DeprecatedVirtualFile {
    /**
     * The name of the file.
     */
    private final String name;
    private final String nameWithoutExtension;

    /**
     * The content of the file.
     */
    private String content;

    /**
     * Immutability flag
     */
    private boolean writable = false;

    public PlainTextMemoryVirtualFile(@NotNull String name) {
        this.name = name;
        nameWithoutExtension = FileUtil.getNameWithoutExtension(name);
    }

    /**
     * Initialises a new instance of this class.
     *
     * @param name    the name of the file
     * @param content the content of the file.  This is mutually exclusive with
     *                <code>isDirectory</code>.
     */
    public PlainTextMemoryVirtualFile(@NotNull String name, String content) {
        this.name = name;
        nameWithoutExtension = FileUtil.getNameWithoutExtension(name);
        this.content = content;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @NonNls
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public VirtualFileSystem getFileSystem() {
        return PlainTextMemoryVirtualFileSystem.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    public String getPath() {
        VirtualFile myParent = getParent();
        return myParent == null ? name : myParent.getPath() + '/' + name;
    }

    @NotNull
    public FileType getFileType() {
        return StdFileTypes.PLAIN_TEXT;
    }

    /**
     * Sets the writable status of the file.
     *
     * @param writable true if the file is writable
     */
    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWritable() {
        return writable;
    }

    public boolean isDirectory() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValid() {
        return true;
    }

    @Nullable
    public VirtualFile getParent() {
        return null;
    }

    public VirtualFile[] getChildren() {
        throw new UnsupportedOperationException("method getChidren");
    }

    public Icon getIcon() {
        return IconLoader.getIcon("/icons/tab_bamboo.png");
    }


    /**
     * {@inheritDoc}
     */
    public OutputStream getOutputStream(Object object, long l, long l1) throws IOException {
        return new ByteArrayOutputStream();
    }


    /**
     * {@inheritDoc}
     */
    public byte[] contentsToByteArray() throws IOException {
        return content.getBytes();
    }

    /**
     * {@inheritDoc}
     */
    public long getTimeStamp() {
        return 0L;
    }

    /**
     * {@inheritDoc}
     */
    public long getLength() {
        return content.getBytes().length;
    }

    /**
     * {@inheritDoc}
     */
    public void refresh(boolean b,
                        boolean b1,
                        Runnable runnable) {
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content.getBytes());
    }

    /**
     * Sets the content of the file.
     *
     * @param content the content
     */
    public void setContent(@NotNull String content) {
        this.content = content;
    }

    /**
     * Gets the content of the file.
     *
     * @return the content of the file
     */
    @NotNull
    public String getContent() {
        return content;
    }

    /**
     * {@inheritDoc}
     */
    public long getModificationStamp() {
        return 0L;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public String getUrl() {
        return Constants.PLAINTEXT_SCHEMA + getPath();
    }

    @NonNls
    public String toString() {
        return nameWithoutExtension;
    }
}