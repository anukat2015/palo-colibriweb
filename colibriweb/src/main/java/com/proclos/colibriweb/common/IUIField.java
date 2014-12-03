package com.proclos.colibriweb.common;

public interface IUIField {
	
	public String getName();
	public Object getValue();
	public void setValue(Object value);
	public boolean isRequired();
	public boolean isString();
	public boolean isInteger();
	public boolean isFloat();
	public boolean isEnum();
	public boolean isBoolean();
	public boolean isScript();
	public String getAnnotation();
	public boolean isMetadataField();
	public boolean isInputField();
	public boolean isSourceField();
	public boolean isConnectionField();
	public boolean isNormalField();
	public String getTypeName();
	public String getScriptType();
	public boolean isVisible();
	public boolean isProjectField();

}
