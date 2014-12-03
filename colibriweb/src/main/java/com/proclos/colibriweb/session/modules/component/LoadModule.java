package com.proclos.colibriweb.session.modules.component;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.jedox.etl.core.component.ITypes;
import com.jedox.etl.core.component.Locator;
import com.jedox.etl.core.config.ConfigManager;
import com.jedox.etl.core.connection.IConnection;
import com.jedox.etl.core.connection.MetadataCriteria;
import com.jedox.etl.core.context.IContext;
import com.jedox.etl.core.execution.ExecutionState;
import com.proclos.colibriweb.common.ProxyItem;
import com.proclos.colibriweb.entity.component.ComponentTypes;
import com.proclos.colibriweb.entity.component.Load;

@Name("loadModule")
@Scope(ScopeType.SESSION)
public class LoadModule extends ComponentModule<Load> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2398831013118173115L;
	private String metadataSelector;
	private List<String> metadataCriterias;

	@Override
	public Class<Load> getEntityClass() {
		return Load.class;
	}
	
	public void setNewInstanceProperties() {
		super.setNewInstanceProperties();
		getInstance().setXml("<load/>");
	}
	
	public int getComponentType() {
		return ComponentTypes.LOAD;
	}
	
	public void instanceCreated() {
		super.instanceCreated();
		resetMetadata();
	}
	
	public void instanceSelected() {
		super.instanceSelected();
		resetMetadata();
	}
	
	public List<String> getMetadataCriterias() {
		try {
			if (metadataCriterias == null) {
				if (hasConnection() && getInstance().getConnection() != null)  {
					Locator locator = new Locator().add(getLocator().getRootName()).add(ITypes.Connections).add(getInstance().getConnection().getName());
					IConnection connection = (IConnection) ConfigManager.getInstance().getComponent(locator, IContext.defaultName);
					List<String> result = new ArrayList<String>();
					MetadataCriteria[] metadatas = connection.getMetadataCriterias();
					if (metadatas != null) {
						for (MetadataCriteria c : connection.getMetadataCriterias()) {
							result.add(c.getName());
						}
					}
					metadataCriterias = result;
				} else {
					metadataCriterias = new ArrayList<String>();
				}
			}
			return metadataCriterias;
		}
		catch (Exception e) {
			this.addErrorMessage("Failed to get metadata criterias: "+e.getMessage());
			return new ArrayList<String>();
		}
	}
	
	public void calculateMetadata() {
		metadataCriterias = null; //force reinit
		if (metadataSelector == null && !getMetadataCriterias().isEmpty()) metadataSelector = getMetadataCriterias().get(0);
		if (metadataSelector != null) calculateMetadata(getInstance().getConnection(),metadataSelector);
	}
	
	public void connectionSelectionChanged(ProxyItem item) {
		super.connectionSelectionChanged(item);
		resetMetadata();
	}
	
	public void resetMetadata() {
		metadataCriterias = null;
		metadataSelector = null;
	}
	
	public String getMetadataSelector() {
		return metadataSelector;
	}

	public void setMetadataSelector(String metadataSelector) {
		this.metadataSelector = metadataSelector;
	}
	
	protected boolean isExecutedDataComponent(ExecutionState state) {
		return getInstance().getConnection() != null && getInstance().getConnection().getName().equals(state.getName()) && getManagerName(getInstance().getConnection().getdType(),false).equals(state.getType());
	}

}
