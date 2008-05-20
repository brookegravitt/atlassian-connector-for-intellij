package com.atlassian.theplugin.eclipse;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.eclipse.util.PluginUtil;

public class EclipseLogger extends LoggerImpl {
	
	private ILog eclipseLogger;

	public EclipseLogger(ILog eclipseLogger) {
		this.eclipseLogger = eclipseLogger;
		setInstance(this);
	}

	public void log(int level, String aMsg, Throwable t) {
		
		IStatus status = null;
		
		switch (level) {
			case LoggerImpl.LOG_VERBOSE:
			case LoggerImpl.LOG_DEBUG:
			case LoggerImpl.LOG_INFO:
				status = new Status(IStatus.INFO, PluginUtil.getPluginName(), aMsg, t);
				break;
			case LoggerImpl.LOG_ERR:
				status = new Status(IStatus.ERROR, PluginUtil.getPluginName(), aMsg, t);
				break;
			case LoggerImpl.LOG_WARN:
			default:
				status = new Status(IStatus.WARNING, PluginUtil.getPluginName(), aMsg, t);
		}
		
		eclipseLogger.log(status);

	}

}
