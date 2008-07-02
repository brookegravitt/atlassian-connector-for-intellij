package com.atlassian.theplugin.idea.jira.editor.vfs;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.DeprecatedVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
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
import java.util.HashMap;
import java.util.Map;

/**
* A memory-based file.
*
* @author Steve Chaloner
*/
public class MemoryVirtualFile extends DeprecatedVirtualFile
{
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
    * A flag to indicate if this file represents a directory.
    */
   private final boolean isDirectory;

   /**
    * The children of this file, if the file is a directory.
    */
   private final Map<String, MemoryVirtualFile> children = new HashMap<String, MemoryVirtualFile>();

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

   /**
    * Initialises a new instance of this class.
    *
    * @param name the name of the file
    * @param content the content of the file
    */
   public MemoryVirtualFile(@NotNull String name,
                            String content)
   {
       this(name,
            content,
            false);
   }

   /**
    * Initialises a new instance of this class.
    *
    * @param name the name of the file
    */
   public MemoryVirtualFile(@NotNull String name)
   {
       this(name,
            null,
            true);
   }

   /**
    * Initialises a new instance of this class.
    *
    * @param name the name of the file
    * @param content the content of the file.  This is mutually exclusive with
    * <code>isDirectory</code>.
    * @param isDirectory true iff this file is a directory.  This is mutually exclusive
    * with <code>content<code>.
    */
   private MemoryVirtualFile(@NotNull String name,
                             String content,
                             boolean isDirectory)
   {
       this.name = name;
       nameWithoutExtension = FileUtil.getNameWithoutExtension(name);
       this.content = content;
       this.isDirectory = isDirectory;
   }

   /** {@inheritDoc} */
   @NotNull
   @NonNls
   public String getName()
   {
       return name;
   }

   /** {@inheritDoc} */
   @NotNull
   public VirtualFileSystem getFileSystem()
   {
       return MemoryVirtualFileSystem.getInstance();
   }

   /** {@inheritDoc} */
   public String getPath()
   {
       VirtualFile parent = getParent();
       return parent == null ? name : parent.getPath() + '/' + name;
   }

   /**
    * Sets the writable status of the file.
    *
    * @param writable true if the file is writable
    */
   public void setWritable(boolean writable)
   {
       this.writable = writable;
   }

   /** {@inheritDoc} */
   public boolean isWritable()
   {
       return writable;
   }

   /** {@inheritDoc} */
   public boolean isDirectory()
   {
       return isDirectory;
   }

   /** {@inheritDoc} */
   public boolean isValid()
   {
       return true;
   }

	public Icon getIcon() {
		return IconLoader.getIcon("/icons/jira-blue-16.png");
	}

   /**
    * Sets the parent of this file.
    *
    * @param parent the parent
    */
   public void setParent(@Nullable VirtualFile parent)
   {
       this.parent = parent;
   }

   /** {@inheritDoc} */
   @Nullable
   public VirtualFile getParent()
   {
       return parent;
   }

   /**
    * Add the given file to the child list of this directory.
    *
    * @param file the file to add to the list of children
    * @throws IllegalStateException if this file is not a directory
    */
   public void addChild(MemoryVirtualFile file) throws IllegalStateException
   {
       if (isDirectory)
       {
           file.setParent(this);
           children.put(file.getName(),
                        file);
       }
       else
       {
           throw new IllegalStateException("files can only be added to a directory");
       }
   }

   /** {@inheritDoc} */
   public VirtualFile[] getChildren()
   {
       return children.values().toArray(new VirtualFile[children.size()]);
   }

   /** {@inheritDoc} */
   public OutputStream getOutputStream(Object object,
                                       long l,
                                       long l1) throws IOException
   {
       return new ByteArrayOutputStream();
   }


   /** {@inheritDoc} */
   public byte[] contentsToByteArray() throws IOException
   {
       return content.getBytes();
   }

   /** {@inheritDoc} */
   public long getTimeStamp()
   {
       return 0L;
   }

   /** {@inheritDoc} */
   public long getLength()
   {
       return content.getBytes().length;
   }

   /** {@inheritDoc} */
   public void refresh(boolean b,
                       boolean b1,
                       Runnable runnable)
   {
   }

   /** {@inheritDoc} */
   public InputStream getInputStream() throws IOException
   {
       return new ByteArrayInputStream(content.getBytes());
   }

   /**
    * Sets the content of the file.
    *
    * @param content the content
    */
   public void setContent(@NotNull String content)
   {
       this.content = content;
   }

   /**
    * Gets the content of the file.
    *
    * @return the content of the file
    */
   @NotNull
   public String getContent()
   {
       return content;
   }

   /**
    * Gets the file from this directory's children.
    *
    * @param name the name of the child to retrieve
    * @return the file, or null if it cannot be found
    */
   @Nullable
   public MemoryVirtualFile getChild(String name)
   {
       return children.get(name);
   }

   /** {@inheritDoc} */
   public long getModificationStamp()
   {
       return 0L;
   }

   /** {@inheritDoc} */
   @NotNull
   public String getUrl() {
       return Constants.JIRAISSUE_SCHEMA + getPath();
   }

   /**
    * Deletes the specified file.
    *
    * @param file the file to delete
    */
   public void deleteChild(MemoryVirtualFile file)
   {
       children.remove(file.getName());
   }

   @NonNls
   public String toString()
   {
       return nameWithoutExtension;
   }
}
