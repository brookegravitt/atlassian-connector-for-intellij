package com.atlassian.theplugin.eclipse.tmp;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;


public class handler extends AbstractHandler implements IHandler, IElementUpdater  {
	
	private static final String TOGGLE_ID = "toggleContext.contextId";
	
	private static boolean checked = false;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		checked = !checked;
		IWorkbenchWindow window = 
			HandlerUtil.getActiveWorkbenchWindowChecked(event);
				MessageDialog.openInformation(
					window.getShell(), "MenuEclipseArticle Plug-in",
					"Hello, Eclipse world");
				return null;

	}

	public void updateElement(UIElement element, Map parameters) {
		String contextId = (String) parameters.get(TOGGLE_ID);
//		element.setChecked(ICommandService contextActivations.get(contextId) != null);
		
		element.setChecked(checked);

		
	}
	
	

}
