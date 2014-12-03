package com.proclos.colibriweb.entity;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
public abstract class ProcessableEntity extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4331777824472977415L;
	
	private Integer processCount = 0;;
	private Integer processTime = 0;
	private Integer lastProcessTime = 0;
	
	public void setProcessCount(Integer processCount) {
		this.processCount = processCount;
	}
	
	public Integer getProcessCount() {
		return processCount;
	}
	
	public void setProcessTime(Integer processTime) {
		this.processTime = processTime;
	}
	
	public Integer getProcessTime() {
		return processTime;
	}
	
	public void setLastProcessTime(Integer lastProcessTime) {
		this.lastProcessTime = lastProcessTime;
	}
	
	public Integer getLastProcessTime() {
		return lastProcessTime;
	}
	
	@Transient
	public int getAverageProcessTime() {
		return (processCount != null && processCount > 0) ? processTime / processCount : 0;
	}
	
	@Transient
	public void addNewProcessTime(Integer processTime) {
		lastProcessTime = processTime;
		if (this.processTime == null) this.processTime = 0;
		this.processTime += lastProcessTime;
		if (processCount == null) processCount = 0;
		processCount ++;
	}

}
