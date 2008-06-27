/**
 * Copyright (c) 2008 Atlassian.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **/
/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/
package com.atlassian.theplugin.eclipse.view.bamboo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import com.atlassian.theplugin.eclipse.preferences.Activator;

public class BambooServers extends ViewPart {

	public static final String VIEW_ID = BambooServers.class.getName();
	
	protected BambooTreeViewer bambooTree;
	//protected RepositoriesRoot root;
	protected DrillDownAdapter ddAdapter;
	protected Action showBrowserAction;
	protected IPartListener2 partListener;
	
	public BambooServers() {
		super();
	}
	
	public static MenuManager newMenuInstance(final ISelectionProvider provider) {
		MenuManager menuMgr = new MenuManager();
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
        		MenuManager sub = new MenuManager(Activator.getDefault().getResource("RepositoriesView.New"), "addMenu");
        		sub.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        		sub.add(new Separator("mainGroup"));
        		sub.add(new Separator("managementGroup"));
        		sub.add(new Separator("repositoryGroup"));
        		Action newRepositoryLocation = new Action(Activator.getDefault().getResource("RepositoriesView.RepositoryLocation")) {
					public void run() {
						//new NewRepositoryLocationAction().run(this);
					}
        		};
        		newRepositoryLocation.setImageDescriptor(Activator.getDefault().getImageDescriptor("icons/objects/repository.gif"));
        		sub.add(newRepositoryLocation);
        		manager.add(sub);
        		
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

				manager.add(new Separator("checkoutGroup"));
                
        		sub = new MenuManager(Activator.getDefault().getResource("RepositoriesView.OpenWith"), "openWithMenu");
        		sub.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        		sub.add(new Separator("dynamicGroup"));
        		IStructuredSelection selection = (IStructuredSelection)provider.getSelection();
        		/*if (selection.size() == 1) {
        			Object item = selection.getFirstElement();
        			if (item instanceof RepositoryFile) {
        				String name = ((RepositoryFile)item).getRepositoryResource().getName();
        				IEditorDescriptor []editors = Activator.getDefault().getWorkbench().getEditorRegistry().getEditors(name);
        				for (int i = 0; i < editors.length; i++) {
        					if (!editors[i].getId().equals(EditorsUI.DEFAULT_TEXT_EDITOR_ID)) {
        						final OpenFileWithAction openAction = new OpenFileWithAction(editors[i].getId(), false);
        		        		Action wrapper = new Action(editors[i].getLabel()) {
        							public void run() {
        								openAction.run(this);
        							}
        		        		};
        						openAction.selectionChanged(wrapper, selection);
        						sub.add(wrapper);
        					}
        				}
        			}
        		}*/
        		sub.add(new Separator("fixedGroup"));
        		manager.add(sub);
                
                manager.add(new Separator("miscGroup"));
                
        		sub = new MenuManager(Activator.getDefault().getResource("RepositoriesView.Refactor"), "refactorMenu");
        		sub.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        		sub.add(new Separator("mainGroup"));
        		manager.add(sub);
        		
				manager.add(new Separator("locationGroup"));
				
                manager.add(new Separator("propertiesGroup"));
                
                manager.add(new Separator("refreshGroup"));

                manager.add(new Separator("importExportGroup"));
            }

        });
        menuMgr.setRemoveAllWhenShown(true);
        return menuMgr;
	}

	public void createPartControl(Composite parent) {
		this.bambooTree = new BambooTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		//this.bambooTree.setContentProvider(new BambooContentProvider(this.bambooTree));
		this.bambooTree.setLabelProvider(new WorkbenchLabelProvider());
		this.getSite().setSelectionProvider(this.bambooTree);
		//this.bambooTree.setInput(this.root = new RepositoriesRoot());
		//this.repositoryTree.setSorter(new ViewSorter())
		
		this.ddAdapter = new DrillDownAdapter(this.bambooTree);
		
		// popup menu
        Tree tree = this.bambooTree.getTree(); 
        MenuManager menuMgr = BambooServers.newMenuInstance(this.bambooTree);
        tree.setMenu(menuMgr.createContextMenu(tree));
        this.getSite().registerContextMenu(menuMgr, this.bambooTree);
        
        // toolbar
        IActionBars actionBars = getViewSite().getActionBars();
        IToolBarManager tbm = actionBars.getToolBarManager();
        this.ddAdapter.addNavigationActions(tbm);
        Action tAction = null;
        tbm.add(tAction = new Action(Activator.getDefault().getResource("SVNView.Refresh.Label")) {
            public void run() {
                if (BambooServers.this.bambooTree.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection)BambooServers.this.bambooTree.getSelection();
                    BambooServers.this.handleRefresh(selection);
                }
            }
        }); 
        tAction.setImageDescriptor(Activator.getDefault().getImageDescriptor("icons/common/refresh.gif"));
        tAction.setToolTipText(Activator.getDefault().getResource("SVNView.Refresh.ToolTip"));
        
		tbm.add(new Separator("collapseAllGroup"));
		
        tbm.add(tAction = new Action(Activator.getDefault().getResource("RepositoriesView.CollapseAll.Label")) {
			public void run() {
			    BambooServers.this.bambooTree.collapseAll();				
			}
        }); 
        tAction.setImageDescriptor(Activator.getDefault().getImageDescriptor("icons/common/collapseall.gif"));
        tAction.setToolTipText(Activator.getDefault().getResource("RepositoriesView.CollapseAll.ToolTip"));
        
		tbm.add(new Separator("repositoryGroup"));
        
        tbm.add(tAction = new Action(Activator.getDefault().getResource("RepositoriesView.NewLocation.Label")) {
			public void run() {
				//new NewRepositoryLocationAction().run(this);
			}
        }); 
        tAction.setImageDescriptor(Activator.getDefault().getImageDescriptor("icons/views/repositories/new_location.gif"));
        tAction.setToolTipText(Activator.getDefault().getResource("RepositoriesView.NewLocation.ToolTip"));
        
        tbm.add(this.showBrowserAction = new Action(Activator.getDefault().getResource("RepositoriesView.ShowBrowser.Label"), Action.AS_CHECK_BOX) {
			public void run() {
				if (this.isChecked()) {
					BambooServers.this.showRepositoryBrowser(true);
				}
				else {
					BambooServers.this.hideRepositoryBrowser();
				}
			}			
        });        
        this.showBrowserAction.setImageDescriptor(Activator.getDefault().getImageDescriptor("icons/views/repositories/browser.gif"));
        this.showBrowserAction.setToolTipText(Activator.getDefault().getResource("RepositoriesView.ShowBrowser.ToolTip"));

        this.bambooTree.getControl().addKeyListener(new KeyAdapter() {
        	public void keyPressed(KeyEvent event) {
    			if (BambooServers.this.bambooTree.getSelection() instanceof IStructuredSelection) {
        			IStructuredSelection selection = (IStructuredSelection)BambooServers.this.bambooTree.getSelection();
	        		if (event.keyCode == SWT.F5) {
	    				BambooServers.this.handleRefresh(selection);
	        		}
	        		else if (event.keyCode == SWT.DEL) {
	        		    BambooServers.this.handleDeleteKey(selection);
	        		}
    			}
        	}
        });
        
		this.bambooTree.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				ISelection selection = e.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structured = (IStructuredSelection)selection;
					if (structured.size() == 1) {
						BambooServers.this.handleDoubleClick(structured);
					}
				}
			}
		});
		
		this.partListener = new IPartListener2() {
			public void partVisible(IWorkbenchPartReference partRef) {
				if (partRef.getId().equals(BambooServers.VIEW_ID)) {
					BambooServers.this.refreshRepositoriesImpl(false);
				}				
			}
			public void partHidden(IWorkbenchPartReference partRef) {
			}
			public void partInputChanged(IWorkbenchPartReference partRef) {
			}
			public void partOpened(IWorkbenchPartReference partRef) {
			}
			public void partDeactivated(IWorkbenchPartReference partRef) {
			}
			public void partClosed(IWorkbenchPartReference partRef) {
				if (partRef.getId().equals(BambooServers.VIEW_ID)) {
					BambooServers.this.getViewSite().getPage().removePartListener(this);
				}
			}
			public void partBroughtToTop(IWorkbenchPartReference partRef) {
			}
			public void partActivated(IWorkbenchPartReference partRef) {
			}
		};
		
		this.getViewSite().getPage().addPartListener(this.partListener);
		
		//Setting context help
	    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.repositoryViewContext");
	}
	
	public void dispose() {
		super.dispose();
		this.getViewSite().getPage().removePartListener(this.partListener);		
	}
	
	public void setFocus() {
		this.bambooTree.getControl().setFocus();
	}
	
	public static void refresh(Object where) {
		BambooServers.refresh(where, null);
	}
	
	public static void refresh(Object where, BambooTreeViewer.IRefreshVisitor visitor) {
		BambooServers instance = BambooServers.instance();
		if (instance != null) {
			instance.bambooTree.refresh(where, visitor, false);
		}
	}
	
	public static void refreshRepositories(boolean deep) {
		BambooServers instance = BambooServers.instance();
		if (instance != null) {
			instance.refreshRepositoriesImpl(deep);
		}
	}

	public BambooTreeViewer getRepositoryTree() {
		return this.bambooTree;
	}
	
	public void refreshButtonsState() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		//boolean isBrowserVisible = SVNTeamPreferences.getRepositoryBoolean(store, SVNTeamPreferences.REPOSITORY_SHOW_BROWSER_NAME);
		//this.showBrowserAction.setChecked(isBrowserVisible);
		this.showBrowserAction.setChecked(false);
	}
	
	public static BambooServers instance() {
		final BambooServers []view = new BambooServers[1];
		Display.getCurrent().syncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
				if (window != null && window.getActivePage() != null) {
					view[0] = (BambooServers)window.getActivePage().findView(BambooServers.VIEW_ID);
				}
			}
		});
		return view[0];
	}
	
	protected void refreshRepositoriesImpl(boolean deep) {
		/*if (deep) {
			this.root.refresh();
		}
		else {
			this.root.softRefresh();
		}*/
		this.bambooTree.refresh();
	}

	protected void showRepositoryBrowser(final boolean force) {
		/*final IWorkbenchPage page = this.getSite().getPage();
		UIMonitorUtility.doTaskBusyDefault(new AbstractNonLockingOperation("Operation.ShowBrowser") {
            protected void runImpl(IProgressMonitor monitor) throws Exception {
        		RepositoryBrowser browser = (RepositoryBrowser)page.showView(RepositoryBrowser.VIEW_ID);
    			ISelection selection = RepositoriesView.this.bambooTree.getSelection();
    			browser.selectionChanged(new SelectionChangedEvent(RepositoriesView.this.bambooTree, selection));
            }
        });*/
	}
	
	protected void hideRepositoryBrowser() {
    	IWorkbenchPage page = this.getSite().getPage();
    	IViewPart part = page.findView(BambooServers.VIEW_ID);
    	if (part != null) {
        	page.hideView(part);
    	}
	}

	protected void handleRefresh(IStructuredSelection selection) {
	    /*Action tmp = new Action() {};
	    AbstractSVNTeamAction action = null;
	    
	    action = new RefreshAction();
	    action.selectionChanged(tmp, selection);
	    action.setActivePart(tmp, RepositoriesView.this);
	    if (tmp.isEnabled()) {
		    action.run(tmp);
	    }
	    
    	action = new RefreshRepositoryLocationAction();
	    action.selectionChanged(tmp, selection);
	    action.setActivePart(tmp, RepositoriesView.this);
	    if (tmp.isEnabled()) {
		    action.run(tmp);
	    }*/
	}

	protected void handleDeleteKey(IStructuredSelection selection) {
	    /*Action tmp = new Action() {}; 
	    AbstractSVNTeamAction action = new DeleteAction();
	    action.selectionChanged(tmp, selection);
	    action.setActivePart(tmp, RepositoriesView.this);
	    if (tmp.isEnabled()) {
		    action.run(tmp);
	    }
	    else {
		    action = new DiscardRevisionLinksAction();
		    action.selectionChanged(tmp, selection);
		    action.setActivePart(tmp, RepositoriesView.this);
		    if (tmp.isEnabled()) {
			    action.run(tmp);
		    }
		    else {
			    action = new DiscardRepositoryLocationAction();
			    action.selectionChanged(tmp, selection);
			    action.setActivePart(tmp, RepositoriesView.this);
			    if (tmp.isEnabled()) {
				    action.run(tmp);
			    }
		    }
	    }*/
	}
	
	protected void handleDoubleClick(IStructuredSelection selection) {
	    /*Action tmp = new Action() {};
	    AbstractSVNTeamAction action = new OpenFileAction();
	    action.selectionChanged(tmp, selection);
	    action.setActivePart(tmp, RepositoriesView.this);
	    if (tmp.isEnabled()) {
		    action.run(tmp);
	    }*/
	}
	
	public boolean canGoBack() {
		return this.ddAdapter.canGoBack();
	}
	
	public void goBack() {
		this.ddAdapter.goBack();
	}

}

