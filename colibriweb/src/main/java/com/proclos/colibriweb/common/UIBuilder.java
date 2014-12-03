package com.proclos.colibriweb.common;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import au.com.bytecode.opencsv.CSVReader;

import com.jedox.etl.core.component.ITypes;
import com.jedox.etl.core.component.Locator;
import com.jedox.etl.core.config.ConfigManager;
import com.jedox.etl.core.connection.IConnection;
import com.jedox.etl.core.connection.MetadataCriteria;
import com.jedox.etl.core.context.IContext;
import com.proclos.colibriweb.entity.component.Component;
import com.proclos.colibriweb.entity.component.ComponentOutput;
import com.proclos.colibriweb.entity.component.Connection;

public class UIBuilder {
	
	private Component instance;
	private Map<String,List<String>> metadataLookup = new HashMap<String,List<String>>();
	
	public UIBuilder(Component instance) {
		this.instance = instance;
	}
	
	public List<ComponentOutput> getInputList(List<ComponentOutput> available, List<ComponentOutput> extras, IUIField field) {
		List<ComponentOutput> result = new ArrayList<ComponentOutput>();
		result.addAll(available);
		if (field.getValue() != null && !field.getValue().toString().isEmpty()) {
			boolean inputPresent = false;
			for (ComponentOutput o : available) if (o.getName().equals(field.getValue())) {
				inputPresent = true;
				break;
			}
			if (!inputPresent) {
				ComponentOutput input = new ComponentOutput();
				input.setName(field.getValue().toString());
				input.setType("?????");
				input.setId(new Long(0));
				result.add(input);
			}
		}
		//check for annotated restriction on a certain source
		String annotation = field.getAnnotation();
		if (annotation.startsWith("input.$")) { //only deliver inputs of certain source. Also consider extra sources 
			List<ComponentOutput> allInputs = new ArrayList<ComponentOutput>();
			allInputs.addAll(result);
			if (extras != null) allInputs.addAll(extras);
			String xPath = annotation.substring(7);
			String sourceName = evalXPath(xPath);
			if (sourceName != null) {
				Iterator<ComponentOutput> i = allInputs.iterator();
				while (i.hasNext()) {
					ComponentOutput o = i.next();
					if (o.getComponent() != null && !sourceName.equals(o.getComponent().getName())) i.remove();
				}
			}
			return allInputs;
		}
		else if (annotation.startsWith("input.")) { //only deliver inputs of certain source
			String position = annotation.substring(6);
			try {
				Integer pos = Integer.parseInt(position);
				Iterator<ComponentOutput> i = result.iterator();
				while (i.hasNext()) {
					ComponentOutput o = i.next();
					if (!pos.equals(o.getSourcePosition())) i.remove();
				}
			}
			catch (NumberFormatException e) {}
		} else { //deliver inputs with unique name only
			Set<String> uniqueNames = new HashSet<String>();
			Iterator<ComponentOutput> i = result.iterator();
			while (i.hasNext()) {
				ComponentOutput o = i.next();
				if (uniqueNames.contains(o.getName())) i.remove();
				uniqueNames.add(o.getName());
			}
		}
		return result;
	}
	
	protected String evalXPath(String expression) {
		return null; //not available for simple builder
	}
	
	public void calculateMetadataList(IUIField field) {
		Connection connection = instance.getConnection();
		if (connection != null) {
			String annotation = field.getAnnotation();
			List<String> result = new ArrayList<String>();
			try {
				Locator locator = new Locator().add(connection.getProject().getName()).add(ITypes.Connections).add(connection.getName());
				IConnection etlConnection = (IConnection) ConfigManager.getInstance().getComponent(locator, IContext.defaultName);
				String[] parts = annotation.split(";");
				String[] selectorParts = parts[0].split("\\.");
				if (selectorParts.length == 3) {
					String selector = selectorParts[1];
					String column = selectorParts[2];
					Properties properties = new Properties();
					for (int i=1; i<parts.length; i++) {
						String[] paramParts = parts[i].split("=");
						if (paramParts.length == 2) {
							if (paramParts[1].startsWith("$")) paramParts[1] = evalXPath(paramParts[1].substring(1)); //XPATH evaluation
							if (paramParts[1] != null) properties.setProperty(paramParts[0], paramParts[1]);
						}
					}
					properties.setProperty("selector", selector);
					properties.setProperty("database", etlConnection.getDatabase());
					String rawData = null;
					try {
						rawData = etlConnection.getMetadata(properties);
					} catch (com.jedox.etl.core.component.RuntimeException e) { //fallback when cube does not exist
						properties.remove("cube");
						rawData = etlConnection.getMetadata(properties);
					}
					if (rawData != null && !rawData.isEmpty()) {
						CSVReader reader = new CSVReader(new StringReader(rawData), ';', '\"');
						try {
							String[] header = reader.readNext(); //header
							List<String> headerList = Arrays.asList(header);
							int index = headerList.indexOf(column);
							if (index == -1) index = headerList.indexOf(column.toUpperCase()); //fix for relational metadata, which can be upper case or lower case
							if (index != -1) {
								String[] dataLine = reader.readNext();
								while (dataLine != null) {
									result.add(dataLine[index]);
									dataLine = reader.readNext();
								}
							} else {
								addErrorMessage("Column "+column+" is no metadata column.");
							}
							reader.close();
						}
						catch (IOException e) {}
					}
				} else if (selectorParts.length==2 && selectorParts[1].equals("selector")) {
					for (MetadataCriteria c : etlConnection.getMetadataCriterias()) {
						result.add(c.getName());
					}
				}
				else if (selectorParts.length==2 && selectorParts[1].equals("filter")) {
					Properties properties = new Properties();
					for (int i=1; i<parts.length; i++) {
						String[] paramParts = parts[i].split("=");
						if (paramParts.length == 2) {
							if (paramParts[1].startsWith("$")) paramParts[1] = evalXPath(paramParts[1].substring(1)); //XPATH evaluation
							if (paramParts[1] != null) properties.setProperty(paramParts[0], paramParts[1]);
						}
					}
					String name = properties.getProperty("selector");
					if (name != null) {
						for (String s : etlConnection.getMetadataCriteria(name).getFilters()) {
							result.add(s);
						}
					}
					else {
						addErrorMessage("Selector is not yet choosen.");
					}
				}
				metadataLookup.put(annotation, result);
			} catch (Exception e) {
				addErrorMessage("Metadata cannot be calculated: "+e.getMessage());
			}
		} else {
			addErrorMessage("Component has no connection.");
		}
		
	}
	
	public List<String> getMetadataList(IUIField field) {
		String annotation = field.getAnnotation();
		List<String> result = new ArrayList<String>();
		List<String> metadata = metadataLookup.get(annotation);
		if (metadata != null) {
			result.addAll(metadata);
		}
		if (field.getValue() != null && !field.getValue().toString().isEmpty() && result.isEmpty()) {
			result.add(field.getValue().toString());
		}
		if (field.getValue() != null && !field.getValue().toString().isEmpty() && !result.contains(field.getValue().toString()) && !result.isEmpty()) {
			field.setValue(result.get(0));
		}
		return result;
	}
	
	protected void addErrorMessage(String message) {
		StatusMessages.instance().addFromResourceBundleOrDefault(Severity.ERROR, message, message, new Object[0]);
	}
	
	public Component getInstance() {
		return instance;
	}

}
