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

package com.atlassian.theplugin.idea.crucible;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

/**
 * CruciblePatchSubmitCommitSession Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>02/12/2008</pre>
 */
public class CruciblePatchSubmitCommitSessionTest extends TestCase {
	public CruciblePatchSubmitCommitSessionTest(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		super.setUp();
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testFake(){
		assertTrue(true);
	}
	public void xtestGenerateUnifiedPatch() throws Exception {
		Collection<Change> changeSet = new ArrayList<Change>();
		changeSet.add(createChange("modified-1", "modified-1.orig", "modified-1.new"));
		changeSet.add(createChange("added", null, "added.new"));
		changeSet.add(createChange("deleted", "deleted.orig", null));

		StringBuilder expectedPatch = new StringBuilder();
		expectedPatch.append(loadResource("modified-1.diff"));
		expectedPatch.append(loadResource("added.diff"));
		expectedPatch.append(loadResource("deleted.diff"));

		CruciblePatchSubmitCommitSessionWrapper session = new CruciblePatchSubmitCommitSessionWrapper(changeSet);
		String patch = session.generateUnifiedDiff();

		assertEquals("Comparing expected patch to returned one", expectedPatch.toString(), patch);
	}

	public void xtestDiffAtFileStart() {
		Collection<Change> changeSet = new ArrayList<Change>();
		changeSet.add(createChange("modified-start", "modified-start.orig", "modified-start.new"));
		StringBuilder expectedPatch = new StringBuilder();
		expectedPatch.append(loadResource("modified-start.diff"));

		CruciblePatchSubmitCommitSessionWrapper session = new CruciblePatchSubmitCommitSessionWrapper(changeSet);
		String patch = session.generateUnifiedDiff();

		assertEquals("Comparing expected patch to returned one", expectedPatch.toString(), patch);
	}

	public void xtestDiffMixed() {
		Collection<Change> changeSet = new ArrayList<Change>();
		changeSet.add(createChange("modified-mixed", "modified-mixed.orig", "modified-mixed.new"));
		StringBuilder expectedPatch = new StringBuilder();
		expectedPatch.append(loadResource("modified-mixed.diff"));
		CruciblePatchSubmitCommitSessionWrapper session = new CruciblePatchSubmitCommitSessionWrapper(changeSet);
		String patch = session.generateUnifiedDiff();

		assertEquals("Comparing expected patch to returned one", expectedPatch.toString(), patch);
	}

	public void xtestDiffMixed2() {
		Collection<Change> changeSet = new ArrayList<Change>();
		changeSet.add(createChange("modified-mixed", "modified-mixed.orig", "modified-mixed-1.new"));
		StringBuilder expectedPatch = new StringBuilder();
		expectedPatch.append(loadResource("modified-mixed-1.diff"));
		CruciblePatchSubmitCommitSessionWrapper session = new CruciblePatchSubmitCommitSessionWrapper(changeSet);
		String patch = session.generateUnifiedDiff();

		assertEquals("Comparing expected patch to returned one", expectedPatch.toString(), patch);
	}


	private static Change createChange(final String filePath, final String beforeResource, final String afterResource) {
		ContentRevision beforeRevision = beforeResource != null ? new MockRevision(beforeResource, filePath, "1000") : null;
		ContentRevision afterRevision = afterResource != null ? new MockRevision(afterResource, filePath, null) : null;

		return new Change(beforeRevision, afterRevision, null);
	}

	private static String loadResource(String name) {
		InputStream is = CruciblePatchSubmitCommitSessionTest.class.getResourceAsStream("/diff/" + name);
		try {
			return FileUtil.loadTextAndClose(new InputStreamReader(is));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static Test suite() {
		return new TestSuite(CruciblePatchSubmitCommitSessionTest.class);
	}
}


class CruciblePatchSubmitCommitSessionWrapper extends PatchProducer {
	CruciblePatchSubmitCommitSessionWrapper(Collection<Change> changeSet) {
		super(null, changeSet);
	}

	protected String getPath(ContentRevision revision) {
		if (revision == null) {
			return null;
		}
		return ((MockRevision) revision).getPath();
	}
}

class MockRevision implements ContentRevision {

	private final String resourceName;
	private final String version;
	private final String fileName;

	MockRevision(String resourceName, String fileName, String version) {
		this.resourceName = resourceName;
		this.version = version;
		this.fileName = fileName;
	}


	public String getContent() throws VcsException {
		InputStream is = getClass().getResourceAsStream("/diff/" + resourceName);
		try {
			return FileUtil.loadTextAndClose(new InputStreamReader(is));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@NotNull
	public FilePath getFile() {
		throw new UnsupportedOperationException("method getFile not implemented");
	}

	@NotNull
	public VcsRevisionNumber getRevisionNumber() {
		if (version == null) return VcsRevisionNumber.NULL;
		return new VcsRevisionNumber() {
			public String asString() {
				return version;
			}

			public int compareTo(VcsRevisionNumber o) {
				throw new UnsupportedOperationException("method compareTo not implemented");
			}
		};
	}

	public String getPath() {
		return fileName;
	}
}
