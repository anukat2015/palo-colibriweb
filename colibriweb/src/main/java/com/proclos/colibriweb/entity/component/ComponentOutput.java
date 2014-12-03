package com.proclos.colibriweb.entity.component;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.jedox.etl.core.persistence.hibernate.IPersistable;

@Entity
public class ComponentOutput implements Serializable, IPersistable, Comparable<ComponentOutput> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4409979819189107163L;
	private Long id;
	private Integer version;
	private Component component;
	private String name;
	private String type;
	private Integer sourcePosition;

	@Version
	public Integer getVersion()
	{
		return version;
	}

	public void setVersion(Integer version)
	{
		this.version = version;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId()
	{
		return id;
	}
	
	public void setId(Long id)
	{
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@ManyToOne(fetch=FetchType.LAZY)
	public Component getComponent() {
		return component;
	}
	
	public void setComponent(Component component) {
		this.component = component;
	}

	@Override
	@Transient
	public int compareTo(ComponentOutput o) {
		return getName().compareTo(o.getName());
	}
/*	
	@Transient
	public boolean equals(Object other) {
		return (other instanceof ComponentOutput) && name.equals(((ComponentOutput)other).getName());
	}
*/	
	@Transient
	public String getDisplayType() {
		if (type == null) return "";
		if (type.indexOf(".") > 0) return type.substring(type.lastIndexOf(".")+1);
		return type;
	}

	@Transient
	public Integer getSourcePosition() {
		return sourcePosition;
	}

	public void setSourcePosition(Integer sourcePosition) {
		this.sourcePosition = sourcePosition;
	}
}
