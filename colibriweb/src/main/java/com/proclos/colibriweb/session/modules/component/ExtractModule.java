package com.proclos.colibriweb.session.modules.component;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.proclos.colibriweb.entity.component.ComponentTypes;
import com.proclos.colibriweb.entity.component.Extract;

@Name("extractModule")
@Scope(ScopeType.SESSION)
public class ExtractModule extends ComponentModule<Extract> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1747465717500097631L;

	@Override
	public Class<Extract> getEntityClass() {
		return Extract.class;
	}
	
	@Override
	protected String getUniqueNameExpression() {
		return "(dType = "+ComponentTypes.EXTRACT +" or dType = "+ComponentTypes.TRANSFORM+")";
	}
	
	public void setNewInstanceProperties() {
		super.setNewInstanceProperties();
		getInstance().setXml("<extract/>");
	}
	
	public void instanceSelected() {
		super.instanceSelected();
	}
	
	public int getComponentType() {
		return ComponentTypes.EXTRACT;
	}
}
