package com.atlassian.theplugin.util;

import static com.atlassian.theplugin.util.CodeNavigationUtil.guessMatchingFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightIdeaTestCase;
import org.easymock.EasyMock;

public class CodeNavigationUtilIdeaTest extends LightIdeaTestCase {

    public static final String PROJECT_BASE_DIR = "/home/wseliga/lab/pazu-test";

    private final String changedFile = "/PL/trunk/ThePlugin/commons/src/main/java/com/atlassian/theplugin/commons/Action.java";
    private final String TEST_FILE_2 = "/PL/trunk/ThePlugin/commons/src/main/java/com/atlassian/theplugin/commons/Action2.java";
    private final String TEST_FILE = "/PL/trunk/ThePlugin/commons/src/test/java/com/atlassian/theplugin/commons/Action.java";
    private final String PARENT_FILE = "/PL/trunk/ThePlugin/commons/src/main/java/com/atlassian/theplugin/commons";

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

    public void testGuessMatchingFile() {
        String vf1s = "/home/wseliga/lab/pazu-test/idea/src/main/java/com/atlassian/theplugin/idea/crucible/table/CommentNode.java";
        String vf2s = "/home/wseliga/lab/pazu-test/idea/src/main/java/com/atlassian/theplugin/common/crucible/table/CommentNode.java";
        String vf3s = "/home/wseliga/lab/pazu-test/idea/src/main/java/com/atlassian/theplugin/common/crucible/CommentNode.java";
        String vf4s = "/home/wseliga/lab/pazu-test/idea/src/main/java/com/atlassian/theplugin/common/crucible/CommentNode.xml";
        final String myExistingFileInProject = "PL/trunk/ThePlugin/idea/src/main/java/com/atlassian/theplugin/common/crucible/CommentNode.java";
        MockVirtualFile baseDir = new MockVirtualFile("/home/wseliga/lab/pazu-test", true);

        String[] wrongOnes = {
                "PL/trunk/ThePlugin/idea/src/main/java/com/atlassian/theplugin/commons/Action.java",
                "PL/trunk/ThePlugin/idea/src/main/java/com/atlassian/theplugin/common/crucible/CommentNode.java1",
                "PL/trunk/ThePlugin/idea/src/main/java/com/atlassian/theplugin/common/crucible/CommentNode1.java",
                "PL/trunk/ThePlugin/idea/src/main/java/com/atlassian/theplugin/common/CommentNode.java",
                "PL/trunk/ThePlugin/idea1/src/main/java/com/atlassian/theplugin/common/crucible/CommentNode.java",
                "com/atlassian/theplugin/common/crucible/CommentNode.java",
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


}
