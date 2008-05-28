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


import com.atlassian.theplugin.commons.crucible.CrucibleStatusListener;
import com.atlassian.theplugin.commons.crucible.ReviewDataInfo;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.configuration.ProjectToolWindowTableConfiguration;
import com.atlassian.theplugin.idea.ui.AbstractTableToolWindowPanel;
import com.atlassian.theplugin.idea.ui.TableColumnProvider;
import com.atlassian.theplugin.idea.bamboo.BambooTableColumnProviderImpl;
import com.intellij.ide.BrowserUtil;
import com.intellij.util.ui.UIUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CrucibleTableToolWindowPanel extends AbstractTableToolWindowPanel implements CrucibleStatusListener {
    //private final transient CrucibleServerFacade crucibleFacade;
    private static CrucibleTableToolWindowPanel instance;
    private TableColumnProvider columnProvider;

    protected String getInitialMessage() {
        return "Waiting for Crucible review info.";
    }

    protected String getToolbarActionGroup() {
        return "ThePlugin.CrucibleToolWindowToolBar";
    }

    protected String getPopupActionGroup() {
        return "ThePlugin.Crucible.ReviewPopupMenu";
    }

    protected TableColumnProvider getTableColumnProvider() {
        if (columnProvider == null) {
            columnProvider = new CrucibleTableColumnProviderImpl();
        }        
        return columnProvider;
    }

    protected ProjectToolWindowTableConfiguration getTableConfiguration() {
        return projectConfiguration.getCrucibleConfiguration().getTableConfiguration();
    }

    public static CrucibleTableToolWindowPanel getInstance(ProjectConfigurationBean projectConfigurationBean) {
        if (instance == null) {
            instance = new CrucibleTableToolWindowPanel(projectConfigurationBean);
        }
        return instance;
    }

    public CrucibleTableToolWindowPanel(ProjectConfigurationBean projectConfigurationBean) {
        super(projectConfigurationBean);
        //crucibleFacade = CrucibleServerFacadeImpl.getInstance();
    }

    protected void handlePopupClick(Object selectedObject) {
    }

    protected void handleDoubleClick(Object selectedObject) {
    }

    public void viewReview() {
        ReviewDataInfoAdapter reviewDataInfo = (ReviewDataInfoAdapter) table.getSelectedObject();
        if (reviewDataInfo != null) {
            BrowserUtil.launchBrowser(reviewDataInfo.getReviewUrl());
        }
    }

/*
    private void openPredefinedFilter(ReviewDataInfoAdapter reviewAdapter) {
        try {
            List<ReviewData> rev = crucibleFacade.getReviewsForFilter(reviewAdapter.getServer(), PredefinedFilter.Drafts);
            for (ReviewData reviewDataInfo : rev) {
                System.out.println("reviewDataInfo.getPermaId().getId() = " + reviewDataInfo.getPermaId().getId());
                System.out.println("reviewDataInfo.getAuthor() = " + reviewDataInfo.getAuthor());
                //System.out.println("reviewDataInfo.getReviewers() = " + reviewDataInfo.getReviewers());
            }
        } catch (RemoteApiException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ServerPasswordNotProvidedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void openCustomFilter(ReviewDataInfoAdapter reviewAdapter) {
        CustomFilterData filter = new CustomFilterData();

        filter.setTitle("test2");
        filter.setAuthor("mwent");
        filter.setCreator("mwent");
        filter.setModerator("mwent");
        filter.setReviewer("sginter");

        filter.setState(new String[]{"Draft", "Summarize", "Closed"});
        filter.setOrRoles(true);
        //filter.setAllReviewersComplete(false);


        try {
            List<ReviewData> rev = crucibleFacade.getReviewsForCustomFilter(reviewAdapter.getServer(), filter);
            for (ReviewData reviewDataInfo : rev) {
                System.out.println("reviewDataInfo.getPermaId().getId() = " + reviewDataInfo.getPermaId().getId());
                System.out.println("reviewDataInfo.getAuthor() = " + reviewDataInfo.getAuthor());
                //System.out.println("reviewDataInfo.getReviewers() = " + reviewDataInfo.getReviewers());
            }
        } catch (RemoteApiException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ServerPasswordNotProvidedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
*/

    /*
        private void openItemDiff(ReviewDataInfoAdapter reviewAdapter) {
            VirtualFile vFile = FileDocumentManager.getInstance().getFile(
                    FileEditorManager.getInstance(IdeaHelper.getCurrentProject()).getSelectedTextEditor().getDocument());
            if (vFile != null) {
                final List<ReviewItemVirtualFile> files = getReviewVersionedVirtualFiles(reviewAdapter);
                for (ReviewItemVirtualFile file : files) {
                    if (file.getVirtualFile().getPath().equals(vFile.getPath())) {
                        List<String> rev = new ArrayList<String>();
                        rev.add(file.getFromRevision());
                        rev.add(file.getToRevision());
                        List<VcsFileRevision> revisions = VcsIdeaHelper.getFileRevisions(file.getVirtualFile(), rev);
                        if (revisions.size() == 2) {
                            showDiff(file.getVirtualFile(), revisions.get(0), revisions.get(1));
                        }
                    }

                }
            }
        }


        private List<ReviewItemVirtualFile> getReviewVersionedVirtualFiles(ReviewDataInfoAdapter reviewAdapter) {
            List<ReviewItemVirtualFile> files = new ArrayList<ReviewItemVirtualFile>();
            try {
                List<ReviewItemData> items = crucibleFacade.getReviewItems(
                        reviewAdapter.getServer(), reviewAdapter.getPermaId());
                VirtualFile baseDir = IdeaHelper.getCurrentProject().getBaseDir();
                String baseUrl = VcsIdeaHelper.getRepositoryUrlForFile(baseDir);
                for (ReviewItemData item : items) {
                    if (item.getToPath().startsWith(baseUrl)) {
                        String relUrl = item.getToPath().substring(baseUrl.length());
                        VirtualFile vf = VfsUtil.findRelativeFile(relUrl, baseDir);
                        if (vf != null) {
                            files.add(new ReviewItemVirtualFile(vf, item));
                        }
                    }
                }
            } catch (RemoteApiException e1) {
                // @todo handle exception - not used methd currently
            } catch (ServerPasswordNotProvidedException e1) {
                // @todo handle exception - not used methd currently
            }
            return files;
        }

        private List<VirtualFile> getReviewVirtualFiles(ReviewDataInfoAdapter reviewAdapter) {
            List<VirtualFile> files = new ArrayList<VirtualFile>();
            try {
                List<ReviewItemData> items = crucibleFacade.getReviewItems(
                reviewAdapter.getServer(), reviewAdapter.getPermaId());
                VirtualFile baseDir = IdeaHelper.getCurrentProject().getBaseDir();
                String baseUrl = VcsIdeaHelper.getRepositoryUrlForFile(baseDir);
                for (ReviewItemData item : items) {
                    if (item.getToPath().startsWith(baseUrl)) {
                        String relUrl = item.getToPath().substring(baseUrl.length());
                        VirtualFile vf = VfsUtil.findRelativeFile(relUrl, baseDir);
                        if (vf != null) {
                            files.add(vf);
                        }
                    }
                }
            } catch (RemoteApiException e1) {
                // @todo handle exception - not used methd currently
            } catch (ServerPasswordNotProvidedException e1) {
                // @todo handle exception - not used methd currently
            }
            return files;
        }

        public void openReviewFiles(ReviewDataInfoAdapter reviewAdapter) {
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(IdeaHelper.getCurrentProject());
            for (ReviewItemVirtualFile vf : getReviewVersionedVirtualFiles(reviewAdapter)) {
                fileEditorManager.openFile(vf.getVirtualFile(), false);
            }
        }

        public void getComments(ReviewDataInfoAdapter reviewAdapter) {
            try {
                List<GeneralComment> items = crucibleFacade
                .getComments(reviewAdapter.getServer(), reviewAdapter.getPermaId());

                for (GeneralComment item : items) {
                    System.out.println(item.getClass().getName() + " -> User: " + item.getUser() + " left comment: "
                            + item.getMessage() + " on " + item.getCreateDate().toString());
                    if (item instanceof VersionedComment) {
                        VersionedComment c = (VersionedComment) item;
                        System.out.print("c.getReviewItemId() + " + c.getReviewItemId().getId());
                        if (c.isFromLineInfo()) {
                            System.out.print(" fromLines:  " + c.getFromStartLine() + " - " + c.getFromEndLine());
                        }
                        if (c.isToLineInfo()) {
                            System.out.print(" toLines:  " + c.getToStartLine() + " - " + c.getToEndLine());
                        }
                        System.out.println("");
                    }
                    for (GeneralComment reply : item.getReplies()) {
                        System.out.println(reply.getClass().getName() + " -> User: " + reply.getUser() + " replied: "
                                + reply.getMessage() + " on " + reply.getCreateDate().toString());
                    }
                }
            } catch (RemoteApiException e1) {
                // @todo handle exception - not used methd currently
            } catch (ServerPasswordNotProvidedException e1) {
                // @todo handle exception - not used methd currently
            }
        }

        private void addReviewScope(final ReviewDataInfoAdapter reviewAdapter) {
            new Thread(new AddReviewScopeWorker(reviewAdapter), "atlassian-crucible-apply-scope").start();
        }


        private final class AddReviewScopeWorker implements Runnable {
            private ReviewDataInfoAdapter reviewAdapter;

            private AddReviewScopeWorker(ReviewDataInfoAdapter reviewAdapter) {
                this.reviewAdapter = reviewAdapter;
            }

            public void run() {
                IdeaHelper.getScopeFiles().clear();
                CrucibleReviewScopeProvider provider =
                        CrucibleReviewScopeProvider.getCrucibleScopeProvider(IdeaHelper.getCurrentProject());
                String previousReviewScope = IdeaHelper.getCurrentProjectComponent().getReviewId();
                if (previousReviewScope != null) {
                    if (provider.isScopeDefined(previousReviewScope)) {
                        provider.removeScope(previousReviewScope);
                    }
                }
                String scopeName = reviewAdapter.getPermaId().getId();

                if (provider.isScopeDefined(scopeName)) {
                    provider.removeScope(scopeName);
                }

                final List<ReviewItemVirtualFile> files = getReviewVersionedVirtualFiles(reviewAdapter);
                if (!files.isEmpty()) {
                    IdeaHelper.getScopeFiles().addAll(files);

                    provider.addScope(scopeName, provider.new ToReviewAbstractPackageSet() {
                        public boolean contains(PsiFile psiFile, NamedScopesHolder namedScopesHolder) {
                            final VirtualFile virtualFile = psiFile.getVirtualFile();
                            for (ReviewItemVirtualFile file : files) {
                                if (file.getVirtualFile().equals(virtualFile)) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    });

                    IdeaHelper.getCurrentProjectComponent().setReviewId(scopeName);
                }
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        NamedScopeManager.getInstance(IdeaHelper.getCurrentProject()).fireScopeListeners();
    //						ProjectView.getInstance(IdeaHelper.getCurrentProject()).changeView("Project");
                    }
                });

            }
        }


        private void showDiff(VirtualFile file, VcsFileRevision first, VcsFileRevision last) {
            SimpleDiffRequest diffData = new SimpleDiffRequest(IdeaHelper.getCurrentProject(), "Diff Tool");
            diffData.setContentTitles(
                    "Repository revision: " + first.getRevisionNumber().asString(),
                    "Repository revision: " + last.getRevisionNumber().asString());

            DiffContent firstContent = VcsIdeaHelper.getFileRevisionContent(file, first);
            DiffContent lastContent = VcsIdeaHelper.getFileRevisionContent(file, last);

            diffData.setContents(firstContent, lastContent);

            DiffManager.getInstance().getDiffTool().show(diffData);
        }
    */

    public void updateReviews(Collection<ReviewDataInfo> reviews) {
        List<ReviewDataInfoAdapter> reviewDataInfoAdapters = new ArrayList<ReviewDataInfoAdapter>();
        for (ReviewDataInfo review : reviews) {
            reviewDataInfoAdapters.add(new ReviewDataInfoAdapter(review));
        }

        listTableModel.setItems(reviewDataInfoAdapters);
        listTableModel.fireTableDataChanged();
        table.revalidate();
        table.setEnabled(true);
        table.setForeground(UIUtil.getActiveTextColor());
        StringBuffer sb = new StringBuffer();
        sb.append("Loaded <b>");
        sb.append(reviews.size());
        sb.append(" open code reviews</b> for you.");
        editorPane.setText(wrapBody(sb.toString()));
    }

    public void resetState() {
        updateReviews(new ArrayList<ReviewDataInfo>());
    }
}