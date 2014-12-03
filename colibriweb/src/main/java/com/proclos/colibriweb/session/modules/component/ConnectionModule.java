package com.proclos.colibriweb.session.modules.component;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.primefaces.event.FileUploadEvent;

import com.jedox.etl.core.config.ConfigManager;
import com.jedox.etl.core.config.Settings;
import com.jedox.etl.core.connection.IConnection;
import com.jedox.etl.core.connection.MetadataCriteria;
import com.jedox.etl.core.context.IContext;
import com.proclos.colibriweb.entity.component.ComponentTypes;
import com.proclos.colibriweb.entity.component.Connection;

@Name("connectionModule")
@Scope(ScopeType.SESSION)
public class ConnectionModule extends ComponentModule<Connection> {
	
	private byte[] uploadedFile;
	private String filename;
	private String metadataSelector;
	private List<String> metadataCriterias = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = -5425622423309335007L;

	@Override
	public Class<Connection> getEntityClass() {
		return Connection.class;
	}
	
	public void setNewInstanceProperties() {
		super.setNewInstanceProperties();
	}
	
	public int getComponentType() {
		return ComponentTypes.CONNECTION;
	}
	
	/*
	public byte[] getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(byte[] uploadedFile) {
		this.uploadedFile = uploadedFile;
	}
	*/
	
	
	public void importFile(FileUploadEvent event) { 
		uploadedFile = event.getFile().getContents();
		filename = event.getFile().getFileName();
		if (uploadedFile != null) {
			try {
				FileOutputStream fos = new FileOutputStream(Settings.getInstance().getDataDir()+File.separator+filename);
				fos.write(uploadedFile);
				fos.close();
				this.addInfoMessage("Sucessfully imported file: "+filename);
			}
			catch (Exception e) {
				this.addErrorMessage("Failed to import file: "+e.getMessage());
			}
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
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
				IConnection connection = (IConnection) ConfigManager.getInstance().getComponent(getLocator(), IContext.defaultName);
				List<String> result = new ArrayList<String>();
				MetadataCriteria[] metadatas = connection.getMetadataCriterias();
				if (metadatas != null) {
					for (MetadataCriteria c : connection.getMetadataCriterias()) {
						result.add(c.getName());
					}
				}
				metadataCriterias = result;
			}
			return metadataCriterias;
		}
		catch (Exception e) {
			this.addErrorMessage("Failed to get metadata criterias: "+e.getMessage());
			return new ArrayList<String>();
		}
	}
	
	public void calculateMetadata() {
		metadataCriterias = null;
		if (metadataSelector == null && !getMetadataCriterias().isEmpty()) metadataSelector = getMetadataCriterias().get(0);
		if (metadataSelector != null) calculateMetadata(getInstance(),metadataSelector);
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
	
}
