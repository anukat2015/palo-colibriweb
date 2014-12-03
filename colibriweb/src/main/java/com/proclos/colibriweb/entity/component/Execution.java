package com.proclos.colibriweb.entity.component;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import at.adaptive.components.bean.annotation.SearchField;

import com.jedox.etl.core.execution.ResultCodes;
import com.jedox.etl.core.execution.ResultCodes.Codes;
import com.proclos.colibriweb.entity.BaseEntity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

@Entity
public class Execution extends BaseEntity implements Comparable<Execution>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1091505524037537690L;
	
	private int errors = 0;
	private int warnings = 0;
	private Date startDate;
	private Date stopDate;
	private Codes status = Codes.statusQueued;
	private String type;
	private String firstErrorMessage;
	private String log;
	private Component component;
	private Long etlId;
	
	public int getErrors() {
		return errors;
	}
	public void setErrors(int errors) {
		this.errors = errors;
	}
	public int getWarnings() {
		return warnings;
	}
	public void setWarnings(int warnings) {
		this.warnings = warnings;
	}
	@Temporal(TemporalType.TIMESTAMP)
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	@Temporal(TemporalType.TIMESTAMP)
	public Date getStopDate() {
		return stopDate;
	}
	public void setStopDate(Date stopDate) {
		this.stopDate = stopDate;
	}
	@SearchField(groups = {"listSearchProperties"})
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@SearchField(groups = {"listSearchProperties"})
	@Lob
	@Type(type = "org.hibernate.type.StringClobType") 
	public String getLog() {
		return log;
	}
	public void setLog(String log) {
		this.log = log;
	}
	@SearchField(groups = {"listSearchProperties"})
	@Column(length = 1000)
	public String getFirstErrorMessage() {
		return firstErrorMessage;
	}
	public void setFirstErrorMessage(String firstErrorMessage) {
		this.firstErrorMessage = firstErrorMessage;
	}
	
	
	@Enumerated(EnumType.STRING)
	public Codes getStatus() {
		return status;
	}
	public void setStatus(Codes status) {
		this.status = status;
	}
	
	@ManyToOne(fetch=FetchType.LAZY)
	@SearchField(groups = {"listSearchProperties"})
	public Component getComponent() {
		return component;
	}
	public void setComponent(Component component) {
		this.component = component;
	}
	
	@Transient
	public String getStateString() {
		return new ResultCodes().getString(getStatus());
	}
	
	@Transient
	public String getName() {
		return component.getName();
	}
	public Long getEtlId() {
		return etlId;
	}
	public void setEtlId(Long etlId) {
		this.etlId = etlId;
	}
	@Override
	@Transient
	public int compareTo(Execution ex) {
		if (this.getStartDate() == null && ex.getStartDate() == null) return 0;
		if (this.getStartDate() == null) return -1;
		if (ex.getStartDate() == null) return 1;
		return this.getStartDate().compareTo(ex.getStartDate());
	}

}
