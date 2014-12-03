package com.proclos.colibriweb.entity.component;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.proclos.colibriweb.common.IUIField;
import com.proclos.colibriweb.common.XSDAnalyzer;


@Entity
@DiscriminatorValue(ComponentTypes.FUNCTION_Value)
public class Function extends Component {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7243081273553458125L;
	
	private XSDAnalyzer.ComplexFieldValue functionField;
	private Transform transform;
	private boolean inEdit;
	
	public Function() {
		setdType(ComponentTypes.FUNCTION);
	}
	
	public Function(XSDAnalyzer.ComplexFieldValue functionField) {
		this.functionField = functionField;
		setdType(ComponentTypes.FUNCTION);
		setId(System.nanoTime());
	}

	//@ManyToOne(fetch = FetchType.LAZY)
	@Transient
	public Transform getTransform() {
		return transform;
	}

	public void setTransform(Transform transform) {
		this.transform = transform;
	}
	
	public void setName(String name) {
		if (functionField != null) {
			IUIField a = functionField.getAttributes().get("name");
			a.setValue(name);
		} 
		super.setName(name);
	}
	
	public void setDescription(String description) {
		if (functionField != null) {
			XSDAnalyzer.ComplexFieldValue a = functionField.getChildOfType("comment");
			if (a == null) {
				a = functionField.getEditComponent(functionField.getType().getChildren().get("comment")).create();
				functionField.getChildren().add(a);
			}
			a.setValue(description);
		} 
		super.setDescription(description);
	}
	
	public void setType(String type) {
		if (functionField != null) {
			IUIField a = functionField.getAttributes().get("type");
			a.setValue(type);
		} 
		super.setType(type);
	}
	
	@Transient
	public String getName() {
		if (functionField != null) {
			IUIField a = functionField.getAttributes().get("name");
			return (String)a.getValue();
		} else return super.getName();
	}
	
	@Transient
	public String getType() {
		if (functionField != null) {
			IUIField a = functionField.getAttributes().get("type");
			return (String)a.getValue();
		} else return super.getType();
	}
	
	@Transient
	public String getDescription() {
		if (functionField != null) {
			IUIField a = functionField.getChildOfType("comment");
			return (a != null) ? (String)a.getValue() : null;
		} else return super.getDescription();
	}
	
	@Transient
	public XSDAnalyzer.ComplexFieldValue getFunctionField() {
		return functionField;
	}
	
	@Transient
	public String getOutputType() {
		String type = String.class.getSimpleName();
		if (functionField != null) {
			XSDAnalyzer.ComplexFieldValue p = functionField.getChildOfType("parameters");
			if (p != null) {
				IUIField f = p.getChildOfType("type");
				if (f != null && f.getValue() != null) type = f.getValue().toString().trim();
			}
		}
		return type;
	}

	@Transient
	public boolean isInEdit() {
		return inEdit;
	}

	public void setInEdit(boolean inEdit) {
		this.inEdit = inEdit;
	}
	
	public void toggleEdit() {
		this.inEdit = !this.inEdit;
	}
	
	/*
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(obj == null)
		{
			return false;
		}
		if(getClass() != obj.getClass())
		{
			return false;
		}
		if (obj instanceof Function) {
			Function other = (Function)obj;
			if(getName() == null)
			{
				if(other.getName() != null)
				{
					return false;
				}
			}
			else if(!getName().equals(other.getName()))
			{
				return false;
			}
		}
		return true;
	}
	*/
	
	
	
}
