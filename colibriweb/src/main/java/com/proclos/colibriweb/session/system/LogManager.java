package com.proclos.colibriweb.session.system;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("logManager")
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class LogManager {
	
	private String globalLogLevel;
	private String taskLogLevel;
	
	@Create
	public void create() {
		globalLogLevel = Logger.getRootLogger().getLevel().toString();
		taskLogLevel = Logger.getRootLogger().getLevel().toString();
	}
	
	public void setGlobalLogLevel(String logLevel) {
		this.globalLogLevel = logLevel;
		
	}
	
	public String getGlobalLogLevel() {
		return globalLogLevel;
	}
	
	public void applyGlobalLogLevel() {
		Logger.getRootLogger().setLevel(Level.toLevel(globalLogLevel));
	}

	public void setTaskLogLevel(String taskLogLevel) {
		this.taskLogLevel = taskLogLevel;
	}

	public String getTaskLogLevel() {
		return taskLogLevel;
	}
	
	public void applyTaskLogLevel() {
		Logger.getLogger(TaskController.class).setLevel(Level.toLevel(taskLogLevel));
	}

}
