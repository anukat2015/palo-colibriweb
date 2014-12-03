package com.proclos.colibriweb.entity.component;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.jboss.seam.async.QuartzTriggerHandle;

import com.proclos.colibriweb.common.ITaskHolder;

@Entity
@DiscriminatorValue(ComponentTypes.JOB_Value)
public class Job extends Component implements ITaskHolder {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5887909968651641705L;
	
	private FetchInterval interval;
	private QuartzTriggerHandle handle;
	private Boolean isManual = true;

	@Override
	public Boolean getIsManual() {
		return isManual;
	}

	@Override
	public void setIsManual(Boolean isManual) {
		this.isManual = isManual;
	}

	@Override
	public QuartzTriggerHandle getHandle() {
		return handle;
	}

	@Override
	public void setHandle(QuartzTriggerHandle handle) {
		this.handle = handle;
	}

	@Override
	public FetchInterval getInterval() {
		return interval;
	}
	
	public void setInterval(FetchInterval interval) {
		this.interval = interval;
	}

}
