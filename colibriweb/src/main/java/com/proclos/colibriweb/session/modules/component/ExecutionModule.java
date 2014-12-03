package com.proclos.colibriweb.session.modules.component;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.jedox.etl.core.execution.ExecutionException;
import com.jedox.etl.core.execution.Executor;
import com.proclos.colibriweb.entity.component.Component;
import com.proclos.colibriweb.entity.component.Execution;
import com.proclos.colibriweb.entity.component.Project;
import com.proclos.colibriweb.session.modules.Module;

@Name("executionModule")
@Scope(ScopeType.SESSION)
public class ExecutionModule extends Module<Execution> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7422054027667325664L;
	private com.jedox.etl.core.execution.Execution etlExecution;
	private String etlLogs = "";

	@Override
	public Class<Execution> getEntityClass() {
		return Execution.class;
	}
	
	public List<Project> getProjectInList() {
		List<Project> result = new ArrayList<Project>();
		result.add(getInstance().getComponent().getProject());
		return result;
	}
	
	public List<Component> getComponentInList() {
		List<Component> result = new ArrayList<Component>();
		result.add(getInstance().getComponent());
		return result;
	}
	
	public void instanceCreated() {
		super.instanceCreated();
		etlExecution = null;
		setEtlLogs("");
	}

	public void instanceSelected() {
		super.instanceSelected();
		Execution execution = getInstance();
		try {
			etlExecution = Executor.getInstance().getExecution(execution.getEtlId());
			setEtlLogs(etlExecution.getExecutionState().getMessageWriter().getMessagesText());
		} catch (ExecutionException e) {
			etlExecution = null;
			setEtlLogs(execution.getLog());
		}
	}

	public String getEtlLogs() {
		return etlExecution != null ? etlExecution.getExecutionState().getMessageWriter().getMessagesText() : etlLogs;
	}

	public void setEtlLogs(String etlLogs) {
		this.etlLogs = etlLogs;
	}
	
	public void stopExecution() {
		if (etlExecution != null && !etlExecution.isFinished()) {
			try {
				Executor.getInstance().stop(etlExecution.getKey());
			} catch (ExecutionException e) {
				error("Error stopping execution");			
			}
		}
	}
	
	public boolean isExecuting() {
		return (etlExecution != null && etlExecution.isActive());
	}
	
	public com.jedox.etl.core.execution.Execution getExecution() {
		return etlExecution;
	}
	
	public String getListIcon(Execution instance) {
		if (instance.getErrors() > 0) return "/img/messagebox_critical.png";
		if (instance.getWarnings() > 0) return "/img/messagebox_warning.png";
		return "/img/messagebox_info.png";
	}
	
	public void deleteAll() {
		List<Execution> results = getResults();
		for (Execution e : results) {
			this.delete(e.getId());
		}
	}

}
