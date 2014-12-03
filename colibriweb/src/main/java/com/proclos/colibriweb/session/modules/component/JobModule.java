package com.proclos.colibriweb.session.modules.component;

import java.util.Date;


import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.async.QuartzTriggerHandle;

import com.proclos.colibriweb.entity.component.ComponentTypes;
import com.proclos.colibriweb.entity.component.FetchInterval;
import com.proclos.colibriweb.entity.component.Job;
import com.proclos.colibriweb.session.system.TaskController;

@Name("jobModule")
@Scope(ScopeType.SESSION)
public class JobModule extends ComponentModule<Job> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7003807756385003559L;
	
	@In(create = true)
	TaskController taskController;
	private FetchInterval initialPeriod;
	

	@Override
	public Class<Job> getEntityClass() {
		return Job.class;
	}
	
	public int getComponentType() {
		return ComponentTypes.JOB;
	}
	
	public void deactivate(Job instance) {
		super.deactivate(instance);
		instance.setIsManual(true);
	}
	
	public void instanceCreated() {
		super.instanceCreated();
		initialPeriod = null;
	}

	public void instanceSelected() {
		super.instanceSelected();
		initialPeriod = getInstance().getInterval();
	}
	
	public void setNewInstanceProperties() {
		super.setNewInstanceProperties();
		getInstance().setIsManual(true);
		getInstance().setXml("<job/>");
	}
	
	public String persist() {
		try {
			if (getInstance().getInterval() == null || getInstance().getIsManual()) {
				QuartzTriggerHandle handle = getInstance().getHandle();
				if (handle != null) {
					handle.pause();
					handle.cancel();
					getInstance().setHandle(null);
				}
			} else {
				if (getInstance().getHandle() != null && !equalsEntity(getInstance().getInterval(), initialPeriod)) {
					// existing
					QuartzTriggerHandle handle = getInstance().getHandle();
					handle.pause();
					handle.cancel();
					getInstance().setHandle(null);
				}
				if (getInstance().getHandle() == null) {
					persistEntity(getInstance(), false, true);
					QuartzTriggerHandle handle = taskController.invokeExecution(getInstance().getInterval().getName(), getInstance().getId(),getLocator().toString());
					getInstance().setHandle(handle);
					/*
					Long duration = taskController.getDurationFromInterval(getInstance().getInterval().getName());
					if (duration != null) {
						QuartzTriggerHandle handle = taskController.invokeExecution(duration, getInstance().getId(),getLocator());
						getInstance().setHandle(handle);
					}
					*/
				}
			}
		} catch (Exception e) {
			error("Cannot schedule job", e);
		}
		getInstance().setModificationDate(new Date());
		return super.persist();
	}
	
	protected Query getAvailableSourcesQuery() {
		return getEntityManager().createQuery("from Component component where component.active = true and (component.dType="+ComponentTypes.JOB+" or component.dType="+ComponentTypes.LOAD+") and component.project.id=:projectId order by component.name, component.dType");
	}
	
}
