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

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
* User: wseliga
* Date: Jul 21, 2008
* Time: 5:33:34 PM
* To change this template use File | Settings | File Templates.
*/
public class MockVirtualFile extends VirtualFile {

    private File file;
    private boolean isDirectory;

    private static VirtualFileSystem vfs = new VirtualFileSystem() {

        @NonNls
        public String getProtocol() {
            throw new UnsupportedOperationException("not implemented for unit test");
        }

        @Nullable
        public VirtualFile findFileByPath(@NotNull @NonNls String path) {
            throw new UnsupportedOperationException("not implemented for unit test");
        }

        public void refresh(boolean asynchronous) {
            throw new UnsupportedOperationException("not implemented for unit test");
        }

        @Nullable
        public VirtualFile refreshAndFindFileByPath(String path) {
            throw new UnsupportedOperationException("not implemented for unit test");
        }

        public void addVirtualFileListener(VirtualFileListener listener) {
            throw new UnsupportedOperationException("not implemented for unit test");
        }

        public void removeVirtualFileListener(VirtualFileListener listener) {
            throw new UnsupportedOperationException("not implemented for unit test");
        }

        protected void deleteFile(Object requestor, VirtualFile vFile) throws IOException {
            throw new UnsupportedOperationException("not implemented for unit test");
        }

        protected void moveFile(Object requestor, VirtualFile vFile, VirtualFile newParent) throws IOException {
            throw new UnsupportedOperationException("not implemented for unit test");
        }

        protected void renameFile(Object requestor, VirtualFile vFile, String newName) throws IOException {
            throw new UnsupportedOperationException("not implemented for unit test");
        }

        protected VirtualFile createChildFile(Object requestor, VirtualFile vDir, String fileName) throws IOException {
            throw new UnsupportedOperationException("not implemented for unit test");
        }

        protected VirtualFile createChildDirectory(Object requestor, VirtualFile vDir, String dirName) throws IOException {
            throw new UnsupportedOperationException("not implemented for unit test");
        }

        protected VirtualFile copyFile(Object requestor, VirtualFile virtualFile, VirtualFile newParent, String copyName) throws IOException {
            throw new UnsupportedOperationException("not implemented for unit test");
        }

        public boolean isReadOnly() {
            return true;
        }
    };

    public MockVirtualFile(String path, boolean isDirectory) {
        this.isDirectory = isDirectory;
        file = new File(path);
    }

    public MockVirtualFile(String path) {
        this(path, false);
    }


    @NotNull
    @NonNls
    public String getName() {
        return file.getName();
    }

    @NotNull
    public VirtualFileSystem getFileSystem() {
        return vfs;
    }

    public String getPath() {
        return file.getAbsolutePath();
    }

    @NotNull
    public String getUrl() {
		return "file://" + file.getAbsolutePath();
    }

    public boolean isWritable() {
        throw new UnsupportedOperationException("not implemented for unit test");
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isValid() {
        return true;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MockVirtualFile that = (MockVirtualFile) o;

        if (isDirectory != that.isDirectory) {
            return false;
        }
        if (!file.equals(that.file)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = file.hashCode();
        result = 31 * result + (isDirectory ? 1 : 0);
        return result;
    }

    @Nullable
    public VirtualFile getParent() {
        String parent = file.getParent();
        if (parent == null) {
            return null;
        }
        return new MockVirtualFile(parent, true);
    }

    public VirtualFile[] getChildren() {
        return new VirtualFile[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
        throw new UnsupportedOperationException("not implemented for unit test");
    }

    public byte[] contentsToByteArray() throws IOException {
        return new byte[0];
    }

    public long getTimeStamp() {
        return 0;
    }

    public long getLength() {
        return 0;
    }

    public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {
        throw new UnsupportedOperationException("not implemented for unit test");
    }

    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException("not implemented for unit test");
    }
}
