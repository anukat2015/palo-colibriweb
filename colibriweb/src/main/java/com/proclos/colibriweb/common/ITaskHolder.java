package com.proclos.colibriweb.common;

import org.jboss.seam.async.QuartzTriggerHandle;

import com.proclos.colibriweb.entity.component.FetchInterval;

public interface ITaskHolder {
	
	public Long getId();
	public String getName();
	public Boolean getIsManual();
	public void setIsManual(Boolean isManual);
	public QuartzTriggerHandle getHandle();
	public void setHandle(QuartzTriggerHandle handle);
	public FetchInterval getInterval();
	public String getContactPerson();
	public String getContactOnError();

}
