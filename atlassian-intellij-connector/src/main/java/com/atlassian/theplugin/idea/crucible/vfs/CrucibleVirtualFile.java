package com.atlassian.theplugin.idea.crucible.vfs;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.DeprecatedVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A memory-based file. Borrowed from IntelliJad (http://code.google.com/p/intellijad/)
 *
 * @author Steve Chaloner, hacked and fubared by kalamon to display files retrieved from CRU
 */
public class CrucibleVirtualFile extends DeprecatedVirtualFile {
	/**
	 * The name of the file.
	 */
	private final CrucibleFileInfo info;

	private final String nameWithoutExtension;

	/**
	 * The content of the file.
	 */
	private byte[] content;

	/**
	 * A flag to indicate if this file represents a directory.
	 */
	private final boolean isDirectory;

	/**
	 * The children of this file, if the file is a directory.
	 */
	private final Map<String, CrucibleVirtualFile> children = new HashMap<String, CrucibleVirtualFile>();

	/**
	 * The parent of this file.  If this file is at the root of the file
	 * system, it will not have a parent.
	 */
	@Nullable
	private VirtualFile parent;

	/**
	 * Immutability flag
	 */
	private boolean writable = true;

	public CrucibleVirtualFile(CrucibleFileInfo info, byte[] content) {
		this(info, content, false);
	}

	public CrucibleVirtualFile(CrucibleFileInfo info) {
		this(info, null, false);
	}

	private CrucibleVirtualFile(CrucibleFileInfo info, byte[] content, boolean isDirectory) {
		this.info = info;
		nameWithoutExtension = FileUtil.getNameWithoutExtension(info.getFileDescriptor().getName());
		this.content = content;
		this.isDirectory = isDirectory;
	}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	@NonNls
	public String getName() {
		return info.getFileDescriptor().getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	public VirtualFileSystem getFileSystem() {
		return CrucibleVirtualFileSystem.getInstance();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPath() {
		VirtualFile myParent = getParent();
        String name = info != null ? info.getFileDescriptor().getName() : "root";
        return myParent == null ? name : myParent.getPath() + '/' + name;
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

	/**
	 * {@inheritDoc}
	 */
	public boolean isDirectory() {
		return isDirectory;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isValid() {
		return true;
	}

	/**
	 * Sets the parent of this file.
	 *
	 * @param parent the parent
	 */
	public void setParent(@Nullable VirtualFile parent) {
		this.parent = parent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	public VirtualFile getParent() {
		return parent;
	}

	/**
	 * Add the given file to the child list of this directory.
	 *
	 * @param file the file to add to the list of children
	 * @throws IllegalStateException if this file is not a directory
	 */
	public void addChild(CrucibleVirtualFile file) throws IllegalStateException {
		if (isDirectory) {
			file.setParent(this);
			children.put(file.getName(), file);
		} else {
			throw new IllegalStateException("files can only be added to a directory");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public VirtualFile[] getChildren() {
		return children.values().toArray(new VirtualFile[children.size()]);
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
		return content;
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
		return content.length;
	}

	/**
	 * {@inheritDoc}
	 */
	public void refresh(boolean b, boolean b1, Runnable runnable) {
	}

	/**
	 * {@inheritDoc}
	 */
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(content);
	}

	/**
	 * Sets the content of the file.
	 *
	 * @param content the content
	 */
	public void setContent(@NotNull byte[] content) {
		this.content = content;
	}

	/**
	 * Gets the file from this directory's children.
	 *
	 * @param filename the name of the child to retrieve
	 * @return the file, or null if it cannot be found
	 */
	@Nullable
	public CrucibleVirtualFile getChild(String filename) {
		return children.get(filename);
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
		return Constants.CRUCIBLE_BOGUS_SCHEMA + getPath();
	}

	/**
	 * Deletes the specified file.
	 *
	 * @param file the file to delete
	 */
	public void deleteChild(CrucibleVirtualFile file) {
		children.remove(file.getName());
	}

	@NonNls
	public String toString() {
		return nameWithoutExtension;
	}
}
