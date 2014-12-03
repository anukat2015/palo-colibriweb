package com.proclos.colibriweb.entity.component;

import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.Type;

import com.proclos.colibriweb.entity.BaseEntity;

@Entity
public class Variable extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7712783544952859194L;
	
	private String name;
	private String value;
	private String comment;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@Lob
	@Type(type = "org.hibernate.type.StringClobType") 
	public String getComment() {
		return comment;
	}
	public void setComment(String commment) {
		this.comment = commment;
	}

}
