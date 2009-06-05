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

import static com.atlassian.theplugin.util.CodeNavigationUtil.guessMatchingFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightIdeaTestCase;
import org.easymock.EasyMock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CodeNavigationUtilIdeaTest extends LightIdeaTestCase {

	private final String changedFile
			= "/PL/trunk/ThePlugin/commons/src/main/java/com/atlassian/theplugin/commons/CrucibleAction.java";
	private final String TEST_FILE_2 = "/PL/trunk/ThePlugin/commons/src/main/java/com/atlassian/theplugin/commons/Action2.java";
	private final String TEST_FILE
			= "/PL/trunk/ThePlugin/commons/src/test/java/com/atlassian/theplugin/commons/CrucibleAction.java";
	private final String PARENT_FILE = "/PL/trunk/ThePlugin/commons/src/main/java/com/atlassian/theplugin/commons";

	public void testGuessMatchingFile() {
		String vf1s
				= "/home/wseliga/lab/pazu-test/idea/src/main/java/com/atlassian/theplugin/idea/crucible/table/CommentNode.java";
		String vf2s
				= "/home/wseliga/lab/pazu-test/idea/src/main/java/com/atlassian/theplugin/common/crucible/table/CommentNode.java";
		String vf3s = "/home/wseliga/lab/pazu-test/idea/src/main/java/com/atlassian/theplugin/common/crucible/CommentNode.java";
		String vf4s = "/home/wseliga/lab/pazu-test/idea/src/main/java/com/atlassian/theplugin/common/crucible/CommentNode.xml";
		final String myExistingFileInProject
				= "PL/trunk/ThePlugin/idea/src/main/java/com/atlassian/theplugin/common/crucible/CommentNode.java";
		MockVirtualFile baseDir = new MockVirtualFile("/home/wseliga/lab/pazu-test", true);

		String[] wrongOnes = {
				"PL/trunk/ThePlugin/idea/src/main/java/com/atlassian/theplugin/commons/CrucibleAction.java",
				"PL/trunk/ThePlugin/idea/src/main/java/com/atlassian/theplugin/common/crucible/CommentNode.java1",
				"PL/trunk/ThePlugin/idea/src/main/java/com/atlassian/theplugin/common/crucible/CommentNode1.java",
				//"PL/trunk/ThePlugin/idea/src/main/java/com/atlassian/theplugin/common/CommentNode.java",
				//"PL/trunk/ThePlugin/idea1/src/main/java/com/atlassian/theplugin/common/crucible/CommentNode.java",
				//"com/atlassian/theplugin/common/crucible/CommentNode.java",
				"",
		};

		PsiFile mock1 = EasyMock.createMock(PsiFile.class);
		PsiFile mock2 = EasyMock.createMock(PsiFile.class);
		PsiFile mock3 = EasyMock.createMock(PsiFile.class);
		PsiFile mock4 = EasyMock.createMock(PsiFile.class);

		EasyMock.expect(mock1.getVirtualFile()).andReturn(new MockVirtualFile(vf1s)).anyTimes();
		EasyMock.expect(mock2.getVirtualFile()).andReturn(new MockVirtualFile(vf2s)).anyTimes();
		EasyMock.expect(mock3.getVirtualFile()).andReturn(new MockVirtualFile(vf3s)).anyTimes();
		EasyMock.expect(mock4.getVirtualFile()).andReturn(new MockVirtualFile(vf4s)).anyTimes();
		EasyMock.replay(mock1, mock2, mock3, mock4);

		final PsiFile[] psiFiles = new PsiFile[]{mock1, mock2, mock3};
		for (String wrongOne : wrongOnes) {
			assertNull(guessMatchingFile(wrongOne, psiFiles, baseDir));
		}
		assertSame(mock3, guessMatchingFile(myExistingFileInProject, psiFiles, baseDir));

		assertNull(guessMatchingFile(myExistingFileInProject, new PsiFile[0], baseDir));

		assertSame(mock3, guessMatchingFile(myExistingFileInProject, new PsiFile[]{mock2, mock4, mock1, mock3}, baseDir));
	}

	public void testGuessMatchingFileMoreTricky() {
		final String vf1s = "/home/wseliga/lab/ide-connector/pom.xml";
		final String vf2s = "/home/wseliga/lab/ide-connector/commons/pom.xml";
		final String vf3s = "/home/wseliga/lab/ide-connector/idea/pom.xml";
		final String vf4s = "/home/wseliga/lab/ide-connector/idea/idea/pom.xml";

		final String myExistingFileInProject = "PL/trunk/ThePlugin/commons/pom.xml";
		MockVirtualFile baseDir = new MockVirtualFile("/home/wseliga/lab/ide-connector", true);

		final PsiFile mock1 = EasyMock.createMock(PsiFile.class);
		final PsiFile mock2 = EasyMock.createMock(PsiFile.class);
		final PsiFile mock3 = EasyMock.createMock(PsiFile.class);
		final PsiFile mock4 = EasyMock.createMock(PsiFile.class);

		EasyMock.expect(mock1.getVirtualFile()).andReturn(new MockVirtualFile(vf1s)).anyTimes();
		EasyMock.expect(mock2.getVirtualFile()).andReturn(new MockVirtualFile(vf2s)).anyTimes();
		EasyMock.expect(mock3.getVirtualFile()).andReturn(new MockVirtualFile(vf3s)).anyTimes();
		EasyMock.expect(mock4.getVirtualFile()).andReturn(new MockVirtualFile(vf4s)).anyTimes();

		EasyMock.replay(mock1, mock2, mock3, mock4);
		final PsiFile[] psiFiles = new PsiFile[]{mock1, mock2, mock3};
		assertSame(mock2, guessMatchingFile(myExistingFileInProject, psiFiles, baseDir));

		final PsiFile[] psiFiles2 = new PsiFile[]{mock1, mock2, mock3, mock4};

		assertSame(mock3, guessMatchingFile("PL/trunk/ThePlugin/idea/pom.xml", psiFiles2, baseDir));
		assertSame(mock4, guessMatchingFile("PL/trunk/idea/idea/pom.xml", psiFiles2, baseDir));
	}


	public void testSanity() {
		MockVirtualFile mvf1 = new MockVirtualFile(changedFile);
		MockVirtualFile mvf2 = new MockVirtualFile(new String(changedFile));
		MockVirtualFile mvf3 = new MockVirtualFile(TEST_FILE);
		MockVirtualFile mvf4 = new MockVirtualFile(TEST_FILE_2);

		MockVirtualFile mvfParent = new MockVirtualFile(PARENT_FILE, true);

		assertEquals(mvf1, mvf1);
		assertEquals(mvf1, mvf2);
		assertFalse(mvf1.equals(mvf3));
		assertFalse(mvf3.equals(mvf1));
		assertEquals(mvfParent, mvf1.getParent());
		assertEquals(mvfParent, mvf2.getParent());
		assertEquals(mvf2.getParent(), mvfParent);
		assertFalse(mvfParent.equals(mvf3.getParent()));
		assertEquals(mvf1.getFileSystem(), mvf3.getFileSystem());
		assertFalse(mvf4.equals(mvf1));
		assertEquals(mvf1.getParent(), mvf4.getParent());

	}


	public void testGetMatchingFiles() {

		String file1 = "file1";
		String file2 = "src/main/file1";
		String file3 = "file3";

		PsiFile psiMock1 = mock(PsiFile.class);
		PsiFile psiMock2 = mock(PsiFile.class);
		PsiFile psiMock3 = mock(PsiFile.class);

		PsiFile[] psiFiles = {psiMock1, psiMock2, psiMock3};

		when(psiMock1.getVirtualFile()).thenReturn(new MockVirtualFile(file1));
		when(psiMock2.getVirtualFile()).thenReturn(new MockVirtualFile(file2));
		when(psiMock3.getVirtualFile()).thenReturn(new MockVirtualFile(file3));

		// "file1" should be found twice
		assertTrue(CodeNavigationUtil.getMatchingFiles(file1, psiFiles).contains(psiMock1));
		assertTrue(CodeNavigationUtil.getMatchingFiles(file1, psiFiles).contains(psiMock2));
		assertEquals(2, CodeNavigationUtil.getMatchingFiles(file1, psiFiles).size());

		// "src/main/file1" should be found once
		assertTrue(CodeNavigationUtil.getMatchingFiles(file2, psiFiles).contains(psiMock2));
		assertEquals(1, CodeNavigationUtil.getMatchingFiles(file2, psiFiles).size());

		// "file3" should be found once
		assertTrue(CodeNavigationUtil.getMatchingFiles(file3, psiFiles).contains(psiMock3));
		assertEquals(1, CodeNavigationUtil.getMatchingFiles(file3, psiFiles).size());

		// "main/file1" should be found once
		assertEquals(1, CodeNavigationUtil.getMatchingFiles("main/file1", psiFiles).size());

		// "dir/src/main/file1" should not be found
		assertEquals(0, CodeNavigationUtil.getMatchingFiles("dir/" + file2, psiFiles).size());

		// "file" should not be found
		assertEquals(0, CodeNavigationUtil.getMatchingFiles("file", psiFiles).size());

		// "/file" should not be found
		assertEquals(0, CodeNavigationUtil.getMatchingFiles("file", psiFiles).size());

		// non existing file should not be found
		assertEquals(0, CodeNavigationUtil.getMatchingFiles("NonExistingFile", psiFiles).size());
	}
}
