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


import com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListener;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.ui.AtlassianTableView;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.CrucibleStatusListener;
import com.atlassian.theplugin.commons.crucible.*;
import com.atlassian.theplugin.commons.crucible.ReviewDataInfo;
import com.atlassian.theplugin.commons.crucible.api.CustomFilterData;
import com.atlassian.theplugin.commons.crucible.api.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.*;
import com.atlassian.theplugin.commons.crucible.api.ReviewItemData;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.scope.packageSet.NamedScopeManager;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CrucibleTableToolWindowPanel extends JPanel implements CrucibleStatusListener {
    private JEditorPane editorPane;
    private ListTableModel listTableModel;
    private AtlassianTableView table;
    private final transient CrucibleServerFacade crucibleFacade;
    private static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);
    private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
    private static final String WAITING_INFO_TEXT = "Waiting for Crucible review info.";

    private static CrucibleTableToolWindowPanel instance;

    public ProgressAnimationProvider getProgressAnimation() {
        return progressAnimation;
    }

    public static CrucibleTableToolWindowPanel getInstance(ProjectConfigurationBean projectConfigurationBean) {
        if (instance == null) {
            instance = new CrucibleTableToolWindowPanel(CrucibleServerFacadeImpl.getInstance(),
                    projectConfigurationBean);
        }
        return instance;
    }

    public CrucibleTableToolWindowPanel(CrucibleServerFacade crucibleFacade,
                                        ProjectConfigurationBean projectConfigurationBean) {
        super(new BorderLayout());

        this.crucibleFacade = crucibleFacade;
        setBackground(UIUtil.getTreeTextBackground());

        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup toolbar = (ActionGroup) actionManager.getAction("ThePlugin.CrucibleToolWindowToolBar");
        add(actionManager.createActionToolbar(
                "atlassian.toolwindow.toolbar", toolbar, true).getComponent(), BorderLayout.NORTH);

        editorPane = new ToolWindowCrucibleContent();
        editorPane.setEditorKit(new ClasspathHTMLEditorKit());
        JScrollPane pane = setupPane(editorPane, wrapBody(WAITING_INFO_TEXT));
        editorPane.setMinimumSize(ED_PANE_MINE_SIZE);
        add(pane, BorderLayout.SOUTH);

        TableColumnInfo[] columns = CrucibleTableColumnProvider.makeColumnInfo();

        listTableModel = new ListTableModel(columns);
        listTableModel.setSortable(true);
        table = new AtlassianTableView(listTableModel,
                projectConfigurationBean.getCrucibleConfiguration().getTableConfiguration());
        table.prepareColumns(columns, CrucibleTableColumnProvider.makeRendererInfo());

        table.addMouseListener(new CrucibleContextMenuMouseAdapter());

        JScrollPane tablePane = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tablePane.setWheelScrollingEnabled(true);
        add(tablePane, BorderLayout.CENTER);

        progressAnimation.configure(this, tablePane, BorderLayout.CENTER);
    }

    private JPopupMenu createContextMenu(final ReviewDataInfoAdapter reviewAdapter) {
        JPopupMenu contextMenu = new JPopupMenu();
        contextMenu.add(makeWebUrlMenu("View", reviewAdapter.getReviewUrl()));
/*
        contextMenu.addSeparator();
		contextMenu.add(makeMenuItem("Open review items", null, reviewAdapter, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openReviewFiles(reviewAdapter);
			}
		}));
		contextMenu.add(makeMenuItem("Get comments", null, reviewAdapter, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getComments(reviewAdapter);
			}
		}));
		contextMenu.add(makeMenuItem("Show file difference", null, reviewAdapter, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openItemDiff(reviewAdapter);
			}
		}));
		contextMenu.add(makeMenuItem("Create scope", null, reviewAdapter, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addReviewScope(reviewAdapter);
			}
		}));
		contextMenu.add(makeMenuItem("Refresh tree", null, reviewAdapter, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ProjectView.getInstance(IdeaHelper.getCurrentProject()).changeView("Scope", "CR-6");
//				ProjectView.getInstance(IdeaHelper.getCurrentProject()).refresh();
			}
		}));

*/
        contextMenu.addSeparator();
		contextMenu.add(makeMenuItem("Open draft filter items", null, reviewAdapter, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openPredefinedFilter(reviewAdapter);
			}
		}));

        contextMenu.addSeparator();
		contextMenu.add(makeMenuItem("Open custom filter items", null, reviewAdapter, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openCustomFilter(reviewAdapter);
			}
		}));

        return contextMenu;
    }

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
/*
        filter.setTitle("test2");
        filter.setAuthor("mwent");
        filter.setCreator("mwent");
        filter.setModerator("mwent");
        filter.setReviewer("sginter");
*/
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


    private JMenuItem makeMenuItem(String menuName,
                                   Icon icon,
                                   ReviewDataInfoAdapter reviewAdapter,
                                   ActionListener listener) {
        JMenuItem item = new JMenuItem();
        item.setIcon(icon);
        item.setText(menuName);
        item.addActionListener(listener);
        return item;
    }

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
*/

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

/*    
    private List<VirtualFile> getReviewVirtualFiles(ReviewDataInfoAdapter reviewAdapter) {
        List<VirtualFile> files = new ArrayList<VirtualFile>();
        try {
            List<ReviewItemData> items = crucibleFacade.getReviewItems(reviewAdapter.getServer(), reviewAdapter.getPermaId());
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
            List<GeneralComment> items = crucibleFacade.getComments(reviewAdapter.getServer(), reviewAdapter.getPermaId());

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
*/

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
                /*
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
                */
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

/*
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

    private JMenuItem makeWebUrlMenu(String menuName, final String url) {
        JMenuItem viewInBrowser = new JMenuItem();
        viewInBrowser.setText(menuName);
        viewInBrowser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BrowserUtil.launchBrowser(url);
            }
        });
        return viewInBrowser;
    }

    private JScrollPane setupPane(JEditorPane pane, String initialText) {
        pane.setText(initialText);
        JScrollPane scrollPane = new JScrollPane(pane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setWheelScrollingEnabled(true);
        return scrollPane;
    }

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

    private String wrapBody(String s) {
        return "<html>" + HtmlBambooStatusListener.BODY_WITH_STYLE + s + "</body></html>";

    }

    public AtlassianTableView getTable() {
        return table;
    }

    public void resetState() {

        updateReviews(new ArrayList<ReviewDataInfo>());
    }

    private class CrucibleContextMenuMouseAdapter extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            /*
            if (e.getClickCount() == 1) {
                ReviewDataInfoAdapter reviewDataInfo = (ReviewDataInfoAdapter) table.getSelectedObject();
                if (reviewDataInfo != null) {
                    addReviewScope(reviewDataInfo);
                }
            } else {
            */
                if (e.getClickCount() == 2) { // on double click, just open the issue
                    ReviewDataInfoAdapter reviewDataInfo = (ReviewDataInfoAdapter) table.getSelectedObject();
                    if (reviewDataInfo != null) {
                        BrowserUtil.launchBrowser(reviewDataInfo.getReviewUrl());
                    }
                }
            //}
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) { // on right click, show a context menu for this issue
            if (e.isPopupTrigger() && table.isEnabled()) {
                ReviewDataInfoAdapter review = (ReviewDataInfoAdapter) table.getSelectedObject();

                if (review != null) {
                    Point p = new Point(e.getX(), e.getY());
                    JPopupMenu contextMenu = createContextMenu(review);
                    contextMenu.show(table, p.x, p.y);
                }
            }
        }
    }
}