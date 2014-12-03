package com.proclos.colibriweb.session.modules.component;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.Messages;
import org.jdom.CDATA;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import at.adaptive.components.hibernate.CriteriaWrapper;
import at.adaptive.components.validator.ParameterizableUniqueValidator;
import at.adaptive.components.validator.UniqueValidator;
import au.com.bytecode.opencsv.CSVReader;

import com.jedox.etl.core.component.ComponentDescriptor;
import com.jedox.etl.core.component.ComponentFactory;
import com.jedox.etl.core.component.ConfigurationException;
import com.jedox.etl.core.component.IComponent;
import com.jedox.etl.core.component.ITypes;
import com.jedox.etl.core.component.Locator;
import com.jedox.etl.core.config.ConfigConverter;
import com.jedox.etl.core.config.ConfigManager;
import com.jedox.etl.core.context.IContext;
import com.jedox.etl.core.execution.ExecutionException;
import com.jedox.etl.core.execution.ExecutionState;
import com.jedox.etl.core.execution.Executor;
import com.jedox.etl.core.execution.IExecutable;
import com.jedox.etl.core.node.tree.Attribute;
import com.jedox.etl.core.node.tree.ITreeElement;
import com.jedox.etl.core.project.IProject;
import com.jedox.etl.core.scriptapi.APIExtender;
import com.jedox.etl.core.scriptapi.APIExtender.ScriptDependenciesDescriptor;
import com.jedox.etl.core.scriptapi.APIExtender.ScriptDependenciesResult;
import com.jedox.etl.core.source.ISource;
import com.jedox.etl.core.source.ITreeSource;
import com.jedox.etl.core.source.IView;
import com.jedox.etl.core.source.IView.Views;
import com.jedox.etl.core.source.processor.IProcessor;
import com.jedox.etl.core.source.processor.IProcessor.Facets;
import com.jedox.etl.core.source.processor.TreeViewProcessor;
import com.jedox.etl.core.util.CustomClassLoader;
import com.jedox.etl.core.util.XMLUtil;
import com.jedox.etl.core.util.svg.GraphManager;
import com.proclos.colibriweb.common.DataRow;
import com.proclos.colibriweb.common.HeaderColumn;
import com.proclos.colibriweb.common.IUIField;
import com.proclos.colibriweb.common.ProxyItem;
import com.proclos.colibriweb.common.UITreeBuilder;
import com.proclos.colibriweb.common.XSDAnalyzer;
import com.proclos.colibriweb.entity.component.Component;
import com.proclos.colibriweb.entity.component.ComponentOutput;
import com.proclos.colibriweb.entity.component.ComponentTypes;
import com.proclos.colibriweb.entity.component.Connection;
import com.proclos.colibriweb.entity.component.Execution;
import com.proclos.colibriweb.entity.component.Project;
import com.proclos.colibriweb.entity.component.Variable;
import com.proclos.colibriweb.session.common.EditComponent;
import com.proclos.colibriweb.session.common.IEditComponent;
import com.proclos.colibriweb.session.modules.Module;
import com.proclos.colibriweb.session.system.AccessUtil;
import com.proclos.colibriweb.session.system.ModuleManager;
import com.proclos.colibriweb.session.system.TaskController;

public abstract class ComponentModule<T extends Component> extends Module<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7465432017450429211L;
	
	@In(create = true)
	TaskController taskController;
	
	@In(create=true) 
	ProjectModule projectModule;
	
	private com.jedox.etl.core.execution.Execution etlExecution;
	private Map<String,List<Component>> dependantsMap;
	protected XSDAnalyzer analyzer;
	private List<Component> availableConnections;
	private List<Component> allConnections; 
	protected List<Component> availableSources;
	private List<Component> allSources;
	private IEditComponent<ProxyItem> sourcesEditComponent;
	private IEditComponent<ProxyItem> connectionEditComponent;
	private UniqueValidator nameValidator;
	private List<HeaderColumn> dataHeader = new ArrayList<HeaderColumn>();
	private List<DataRow> data = new ArrayList<DataRow>();
	private List<DataRow> filteredData = new ArrayList<DataRow>();
	private DataRow[] selectedData;
	private List<UITreeBuilder> complexUIBuilders;
	private List<ComponentOutput> inputList;
	private IEditComponent<Variable> variableEditComponent;
	private boolean checkMessages = false;
	private boolean xmlMode = false;
	private boolean exportSelection = false;
	private boolean showDataOnComplete;
	private Map<String,Component> localComponentChache = new HashMap<String,Component>();
	private CloneHelper cloneHelper;
	private int sampleSize = 1000;
	private String sampleFormat = IView.Views.NONE.toString();
	private boolean preparingExecution = false;
	private String flowGraphXML = null;
	private IExecutable executable;

	private class SourcesEditComponent extends EditComponent<ProxyItem> {
		
		private Component instance;

		public SourcesEditComponent(List<ProxyItem> items,Integer minItems, Integer maxItems, Component instance) {
			super(items, ProxyItem.class, minItems, maxItems);
			this.instance = instance;
			if (minItems > items.size()) {
				int amount = minItems - items.size();
				for (int i=0; i<amount; i++) {
					create();
				}
			}
			if (maxItems == 1 && items.isEmpty()) {
				create();
			}
		}
		
		public ProxyItem createNewItem() {
			inputList = null;
			ProxyItem item = super.createNewItem();
			XSDAnalyzer.ComplexFieldValue v = analyzer.createSource();
			if (availableSources != null && !availableSources.isEmpty()) {
				item.setComponent(availableSources.get(0));
				instance.getSources().add(availableSources.get(0));
				if (v != null) {
					v.setInput(item.getComponent().getName());
					IUIField type = v.getAttributes().get("type");
					if (type != null) { //set type in case of execution (in job)
						type.setValue(getComponentTypeName(item.getComponent().getdType()));
					}
				}
			}
			return item;
		}
		
		public void delete(ProxyItem item) {
			super.delete(item);
			inputList = null;
			analyzer.deleteSource(item.getComponent().getName());
			instance.getSources().remove(item.getComponent());
		}
	}
	
	private class ConnectionEditComponent extends EditComponent<ProxyItem> {
		private Component instance;

		public ConnectionEditComponent(List<ProxyItem> items,Integer minItems, Integer maxItems, Component instance) {
			super(items, ProxyItem.class, minItems, maxItems);
			this.instance = instance;
			if (minItems > items.size()) {
				int amount = minItems - items.size();
				for (int i=0; i<amount; i++) {
					create();
				}
			}
		}
		
		public ProxyItem createNewItem() {
			ProxyItem item = super.createNewItem();
			XSDAnalyzer.ComplexFieldValue v = analyzer.createConnection();
			if (availableConnections != null && !availableConnections.isEmpty()) {
				item.setComponent(availableConnections.get(0));
				instance.setConnection((Connection)availableConnections.get(0));
				if (v != null) v.setInput(item.getComponent().getName());
			}
			return item;
		}
		
		public void delete(ProxyItem item) {
			super.delete(item);
			analyzer.deleteConnection();
			instance.setConnection(null);
		}
	}
	
	protected void applyCustomFilters(CriteriaWrapper criteriaWrapper) {
		AccessUtil.appendAccessCriterion(getCurrentUser(), criteriaWrapper);
		super.applyCustomFilters(criteriaWrapper);
	}
	
	protected String getUniqueNameExpression() {
		return "dType = "+getComponentType();
	}
	
	public abstract int getComponentType();
	
	@Override
	protected void initialize()
	{
		super.initialize();
	}
	
	public void clearAll() {
		super.clearAll();
		etlExecution = null;
		dependantsMap = new HashMap<String,List<Component>>();
		data.clear();
		dataHeader.clear();
		analyzer = null;
		availableConnections = null;
		allConnections = null;
		availableSources = null;
		allSources = null;
		sourcesEditComponent = null;
		variableEditComponent = null;
		complexUIBuilders = null;
		inputList = null;
		xmlMode = false;
		flowGraphXML = null;
		localComponentChache.clear();
		executable = null;
	}
	
	public void validateName(FacesContext context, UIComponent component, Object obj) throws ValidatorException
	{
		nameValidator = new ParameterizableUniqueValidator("from Component component where component.name is not null and component.project.id="+getInstance().getProject().getId()+" and "+getUniqueNameExpression()+" and lower(component.name) like :value", "Name not valid", "Name already in use");
		String name = (String)obj;
		if(name != null)
		{
			name = name.toLowerCase().trim();
		}
		nameValidator.validate(getInstance().getName(), context, component, name);
	}
	
	public void instanceCreated() {
		super.instanceCreated();
	}
	
	protected void init() {
		etlExecution = null;
		dependantsMap = new HashMap<String,List<Component>>();
		data.clear();
		dataHeader.clear();
		complexUIBuilders = null;
		inputList = null;
		xmlMode = false;
		flowGraphXML = null;
		localComponentChache.clear();
		executable = null;
		initComponentFromXML();
		initSourcesEditComponent();
		initConnectionEditComponent();
		initVariableEditComponent();
	}

	public void instanceSelected() {
		super.instanceSelected();
		List<String> types = getAvailableTypes();
		if (getInstance() != null && getInstance().getType() != null && !types.contains(getInstance().getType())) { //check if we have 
			for (String t : types) {
				if (t.equalsIgnoreCase(getInstance().getType())) {
					getInstance().setType(t);
					break;
				}
			}
		}
		if (getInstance() != null && getInstance().getType() != null && !types.contains(getInstance().getType())) {
			addWarnMessage("Component type "+getInstance().getType()+" is not supported.");
		}
		init();
		List<Execution> executions = getInstance().getExecutions();
		Collections.sort(executions);
		Execution execution = (executions.isEmpty() ? null : executions.get(executions.size()-1));
		try {
			etlExecution = (execution == null ? null : Executor.getInstance().getExecution(execution.getEtlId()));
		} catch (ExecutionException e) {
			etlExecution = null;
		}
		cloneHelper = new CloneHelper(getInstance());
	}
	
	protected Project findReferenceProject() {
		XSDAnalyzer.ComplexFieldValue externalProject = analyzer.getExternalProject();
		if (externalProject != null) {
			try {
				Project project = (Project)getEntityManager().createQuery("from Project where name = :name").setParameter("name", externalProject.getInput()).getSingleResult();
				return project;
			}
			catch (Exception e) {
				return getInstance().getProject();
			}
		} else {
			return getInstance().getProject();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void calculateAvailableSources() throws Exception {
		Project project = findReferenceProject();
		Query query = getAvailableSourcesQuery();
		query.setParameter("projectId",project.getId());
		allSources = query.getResultList();
		for (Component s : allSources) {
			Hibernate.initialize(s.getOutputDescription());
		}
		if (analyzer.getMaxSources() > 0) {
			//query available sources
			if (analyzer.isTreeSourceRequired()) {
				availableSources = new ArrayList<Component>();
				for (Component c : allSources) {
					if (isTreeSource(c)) availableSources.add(c);
				}
			} else {
				availableSources = allSources;
			}
			availableSources.remove(getInstance());
			getInstance().getSources().clear();
			for (XSDAnalyzer.ComplexFieldValue v : analyzer.getSources()) {
				if (v.getInput() != null) for (Component source : availableSources) {
					if (source.getName().equals(v.getInput())) getInstance().getSources().add(source);
				}
			}
		} else availableSources = new ArrayList<Component>();
	}
	
	protected Query getAvailableSourcesQuery() {
		return getEntityManager().createQuery("from Component component where component.active = true and (component.dType="+ComponentTypes.EXTRACT+" or component.dType="+ComponentTypes.TRANSFORM+") and component.project.id=:projectId order by component.name, component.dType");
	}
	
	@SuppressWarnings("unchecked")
	protected void calculateAvailableConnections(ComponentDescriptor d) throws Exception {
		Project project = findReferenceProject();
		Query query = getEntityManager().createQuery("from Component component where component.active = true and component.dType="+ComponentTypes.CONNECTION+" and component.project.id=:projectId order by component.name, component.dType");
		query.setParameter("projectId",project.getId());
		allConnections = query.getResultList();
		if (analyzer.hasConnection()) {
			List<String> allowedConnections = new ArrayList<String>();
			if (d.getAllowedConnectionTypes() != null) for (String s : d.getAllowedConnectionTypes()) {//explicitly restricted connection types
				if (!"*".equals(s)) allowedConnections.add(s.toLowerCase());
			}
			//query avaliable connections
			availableConnections = new ArrayList<Component>();
			for (Component c : allConnections) {
				ComponentDescriptor cd = ComponentFactory.getInstance().getComponentDescriptor(c.getType(), getManagerName(c.getdType(),false));
				if (allowedConnections.isEmpty()) {
					if (analyzer.getConnectionInterface().isAssignableFrom(CustomClassLoader.getInstance().loadClass(cd.getClassName()))) availableConnections.add(c);
				}
				else {
					if (allowedConnections.contains(c.getType().toLowerCase())) availableConnections.add(c);
				}
			}
			if (analyzer.hasConnection()) {
				XSDAnalyzer.ComplexFieldValue v = analyzer.getConnection();
				if (v != null) {
					if (v.getInput() != null) for (Component connection : availableConnections) {
						if (connection.getName().equals(v.getInput())) getInstance().setConnection((Connection)connection);
					}
				}
			}
		} else availableConnections = new ArrayList<Component>();
	}
	
	public void externalProjectSet() {
		try {
			getInstance().getSources().clear();
			getInstance().setConnection(null);
			calculateAvailableConnections(ComponentFactory.getInstance().getComponentDescriptor(getInstance().getType(), getManagerName()));
			calculateAvailableSources();
			initConnectionEditComponent();
			initSourcesEditComponent();
		}
		catch (Exception e) {
			this.addErrorMessage("Cannot set external reference project: "+e.getMessage());
		}
	}
	
	protected void initComponentFromXML() {
		try {
			String name = getInstance().getType();
			analyzer = getInstance().getAnalyzer(); //try reuse
			Element xml = null;
			if (getInstance().getXml() != null) {
				xml = XMLUtil.stringTojdom(getInstance().getXml());
			}
			if (analyzer == null) {
				ComponentDescriptor d = ComponentFactory.getInstance().getComponentDescriptor(name, getManagerName());
				analyzer = new XSDAnalyzer(CustomClassLoader.getInstance().loadClass(d.getClassName()), getComponentTypeName(getComponentType()));
				if (xml != null) getInstance().setCode(analyzer.applyValues(xml));
				analyzer.initFields(xml == null);
				calculateAvailableConnections(d);
				calculateAvailableSources();
				//init variables
				if (xml != null) {
					List<?> variables = xml.getChildren("variable");
					for (Object o : variables) {
						Element v = (Element)o;
						Variable var = new Variable();
						var.setName(v.getAttributeValue("name"));
						var.setValue(v.getChildTextTrim("default"));
						var.setComment(v.getChildTextTrim("comment"));
						getInstance().getVariables().add(var);
					}
				}
				complexUIBuilders = null;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			this.addErrorMessage(Messages.instance().get("etl.update.failed")+": "+e.getMessage());
		}
	}
	
	protected void validateXML(Locator loc, Element config, Element oldConfig) throws Exception {
		try {
			ConfigManager.getInstance().add(loc, config);
			ConfigManager.getInstance().validate(loc);
		}
		finally {
			ConfigManager.getInstance().add(loc, oldConfig);
		}
	}
	
	public void updateXMLFromCode() {
		Element root = new Element(getComponentTypeName(getComponentType()));
		root.setAttribute("name", getInstance().getName());
		root.setAttribute("type",getInstance().getType());
		String newXML = "<root>"+getInstance().getCode()+"</root>";
		String oldXML = getInstance().getXml();
		try {
			Element e = XMLUtil.stringTojdom(newXML);
			Element oldConfig = oldXML != null ? XMLUtil.stringTojdom(oldXML) : root;
			root.addContent(e.cloneContent());
			validateXML(getLocator(), root, oldConfig);
			newXML = XMLUtil.jdomToString(root,Format.getPrettyFormat().setOmitDeclaration(true));
			getInstance().setXml(newXML);
			instanceSelected();
			persist();
		}
		catch (Exception e) {
			getInstance().setXml(oldXML);
			this.addErrorMessage(Messages.instance().get("etl.update.failed")+": "+e.getMessage());
		}
	}
	
	public void validateFromCode() {
		Element root = new Element(getComponentTypeName(getComponentType()));
		root.setAttribute("name", getInstance().getName());
		root.setAttribute("type",getInstance().getType());
		String newXML = "<root>"+getInstance().getCode()+"</root>";
		String oldXML = getInstance().getXml();
		try {
			Element e = XMLUtil.stringTojdom(newXML);
			Element oldConfig = XMLUtil.stringTojdom(oldXML);
			root.addContent(e.cloneContent());
			validateXML(getLocator(), root, oldConfig);
			this.addInfoMessage(Messages.instance().get("etl.validation.ok"));
		}
		catch (Exception e) {
			this.addErrorMessage(Messages.instance().get("etl.update.failed")+": "+e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void updateCodeFromModel(Element xml) {
		try {
			StringBuffer buffer = new StringBuffer();
			for (Element e : (List<Element>)xml.getChildren()) {
				XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setOmitDeclaration(true));
				buffer.append(outputter.outputString(e)+"\n");
			}
			getInstance().setCode(buffer.toString());
		}
		catch (Exception e) {
			this.addErrorMessage(Messages.instance().get("etl.update.failed")+": "+e.getMessage());
		}
	}
	
	public void switchToEditor() {
		xmlMode = false;
	}
	
	public void switchToXML() {
		try {
			updateCodeFromModel(generateXML());
			xmlMode = true;
		}
		catch (Exception e) {
			this.addErrorMessage(Messages.instance().get("etl.update.failed")+": "+e.getMessage());
		}
	}
	
	public boolean isXmlMode() {
		return xmlMode;
	}
	
	public boolean isTreeSource(Component component) {
		if (component != null) {
			try {
				ComponentDescriptor sd = ComponentFactory.getInstance().getComponentDescriptor(component.getType(), getManagerName(component.getdType(),false));
				return (ITreeSource.class.isAssignableFrom(CustomClassLoader.getInstance().loadClass(sd.getClassName())));
			}
			catch (ClassNotFoundException e) {
				this.addErrorMessage("No class found for component "+component.getName()+" of type "+component.getType());
			}
		}
		return false;
	}
	
	public List<UITreeBuilder> getComplexRootNodes() {
		if (complexUIBuilders == null) {
			complexUIBuilders = getComplexRootNodes(analyzer);
		}
		return complexUIBuilders;
	}
	
	public List<UITreeBuilder> getComplexRootNodes(XSDAnalyzer analyzer) {
		List<UITreeBuilder> complexUIBuilders = new ArrayList<UITreeBuilder>();
		if (analyzer != null) {
			int i = 0;
			complexUIBuilders = new ArrayList<UITreeBuilder>();
			XSDAnalyzer.ComplexFieldValue sv = analyzer.getUIFieldValueRoot();
			if (!sv.getType().getChildren().isEmpty()) {
				complexUIBuilders.add(new UITreeBuilder(sv,getInstance(),i++));
			}
		}
		return complexUIBuilders;
	}
	
	
	public void setNewInstanceProperties() {
		super.setNewInstanceProperties();
		getInstance().setdType(getComponentType());
		getInstance().setProject(projectModule.getInstance());
		getInstance().setCode("");
		List<String> typeList = getAvailableTypes();
		if (!typeList.isEmpty()) {
			getInstance().setType(typeList.get(0));
			typeChanged();
		}
	}
	
	public boolean isExecuting() {
		return (etlExecution != null && etlExecution.isActive());
	}
	
	protected boolean isExecutedDataComponent(ExecutionState state) {
		return getInstance().getName() != null && getInstance().getName().equals(state.getName()) && getManagerName(getInstance().getdType(),false).equals(state.getType());
	}
	
	public List<HeaderColumn> getDataHeader() {
		if (etlExecution != null && etlExecution.isFinished()) {
			ExecutionState state = etlExecution.getExecutionState();
			if (dataHeader.isEmpty() && isExecutedDataComponent(state)) {
				String data = etlExecution.getExecutionState().getData();
				if (data != null && !data.isEmpty()) {
					CSVReader reader = new CSVReader(new StringReader(data), ';', '\"');
					try {
						String[] header = reader.readNext();
						reader.close();
						for (int i=0; i<header.length; i++) {
							HeaderColumn c = new HeaderColumn();
							c.setId(i);
							c.setName(header[i]);
							dataHeader.add(c);
						}
					}
					catch (IOException e) {}
				}
			}
			return dataHeader;
		}
		return dataHeader;
	}
	
	public List<DataRow> getData() {
		if (etlExecution != null && etlExecution.isFinished()) {
			ExecutionState state = etlExecution.getExecutionState();
			if ((data.isEmpty()) && isExecutedDataComponent(state)) {
				if (executable != null && executable instanceof ITreeSource) {
					calculateDataFromTree();
				} else {
					String rawData = etlExecution.getExecutionState().getData();
					if (rawData != null && !rawData.isEmpty()) {
						CSVReader reader = new CSVReader(new StringReader(rawData), ';', '\"');
						try {
							String[] header = reader.readNext(); //header
							List<String> headerList = new ArrayList<String>();
							for (int i=0; i<header.length; i++) {
								headerList.add("c"+i);
							}
							String[] dataLine = reader.readNext();
							int lineNumber = 0;
							while (dataLine != null) {
								DataRow row = new DataRow();
								row.setId(new Long(lineNumber));
								row.setData(dataLine);
								row.setHeader(headerList);
								data.add(row);
								dataLine = reader.readNext();
								lineNumber++;
							}
							reader.close();
						}
						catch (IOException e) {}
						filteredData.clear();
						filteredData.addAll(data);
					}
				}
			}
			return data;
		}
		return data;
	}
	
	public void stopExecution() {
		if (etlExecution != null && !etlExecution.isFinished()) {
			try {
				Executor.getInstance().stop(etlExecution.getKey());
			} catch (ExecutionException e) {
				error("Error stopping execution");			
			}
		}
	}
	
	protected String getManagerName(int dType, boolean asSources) {
		switch (dType) {
		case ComponentTypes.CONNECTION : return ITypes.Connections;
		case ComponentTypes.EXTRACT: return asSources ? ITypes.Sources : ITypes.Extracts;
		case ComponentTypes.TRANSFORM: return asSources ? ITypes.Sources : ITypes.Transforms;
		case ComponentTypes.FUNCTION: return ITypes.Functions;
		case ComponentTypes.LOAD: return ITypes.Loads;
		case ComponentTypes.JOB: return ITypes.Jobs;
		default: {
			//error("Unknown component type cannot be tranlated to etl manager.");
			return null;
		}
		}
	}
	
	protected String getManagerName() {
		return getManagerName(getComponentType(),false);
	}
	
	public String getComponentTypeName(int dType) {
		String managerName = getManagerName(dType,false);
		return managerName != null ? managerName.substring(0,managerName.length()-1) : null;
	}
	
	public XSDAnalyzer getAnalyzer() {
		return analyzer;
	}
	
	protected int getComponentType(String managerName) {
		switch (ITypes.Managers.valueOf(managerName)) {
		case connections: return  ComponentTypes.CONNECTION;
		case extracts: return ComponentTypes.EXTRACT;
		case transforms: return ComponentTypes.TRANSFORM;
		case loads: return  ComponentTypes.LOAD;
		case jobs: return  ComponentTypes.JOB;
		case functions: return ComponentTypes.FUNCTION;
		default: return -1;
		}
	}
	
	public void validateComponent() {
		Locator locator = getLocator();
		try {
			addToBackend();
			ConfigManager.getInstance().validate(locator);
			this.addInfoMessage(Messages.instance().get("etl.validation.ok"));
		}
		catch (ExecutionException e) {
			this.addErrorMessage(Messages.instance().get("etl.xml.invalid")+": "+e.getMessage());
		}
		catch (ConfigurationException ce) {
			this.addErrorMessage(Messages.instance().get("etl.validation.error")+": "+ce.getMessage());
		}
	}
	
	public void testComponent() {
		Locator locator = getLocator();
		checkMessages = true;
		try {
			addToBackend();
			etlExecution = Executor.getInstance().createTest(locator, new Properties());
			if (etlExecution.getExecutable() instanceof ISource) {
				etlExecution.getExecutable().getParameter().setProperty("sample", String.valueOf(200));
				etlExecution.getExecutable().getParameter().setProperty("format", "pcwa");
			}
			Long id = etlExecution.getKey();
			Executor.getInstance().addExecution(etlExecution);
			taskController.invokeExecution(new Long(0), getInstance().getId(), id,false, getInstance().getId());
		} catch (Exception e) {
			this.addErrorMessage(Messages.instance().get("etl.test.failed")+": " + e.getMessage());
		}
		ModuleManager.instance().getModuleForName("execution").setDirty(true);
	}
	
	public void execute() {
		preparingExecution = true;
		Locator locator = getLocator();
		checkMessages = true;
		try {
			addToBackend();
			etlExecution = Executor.getInstance().createExecution(locator, new Properties());
			if (etlExecution.getExecutable() instanceof ISource) {
				etlExecution.getExecutable().getParameter().setProperty("sample", String.valueOf(200));
				etlExecution.getExecutable().getParameter().setProperty("format", "pcwa");
			}
			Long id = etlExecution.getKey();
			Executor.getInstance().addExecution(etlExecution);
			taskController.invokeExecution(new Long(0), getInstance().getId(), id,true, getInstance().getId());
		} catch (Exception e) {
			this.addErrorMessage(Messages.instance().get("etl.execution.failed")+": " + e.getMessage());
		}
		ModuleManager.instance().getModuleForName("execution").setDirty(true);
		preparingExecution = false;
	}
	
	protected String addToBackend() throws ExecutionException {
		try {
			if (hasVariables()) {
				List<Variable> newVariables = new ArrayList<Variable>();
				newVariables.addAll(variableEditComponent.getItems());
				getInstance().getVariables().clear();
				getInstance().getVariables().addAll(newVariables);
			}
			String xml = generateXMLString();
			Locator locator = getLocator();
			Element element = XMLUtil.stringTojdom(xml);
			ConfigManager.getInstance().add(locator, element);
			return xml;
		}
		catch (Exception e) {
			throw new ExecutionException(e);
		}
	}
	
	protected Locator getLocator() {
		return getLocator(getInstance());
	}
	
	protected Locator getLocator(Component instance) {
		Locator locator = new Locator().add(instance.getProject().getName()).add(getManagerName(instance.getdType(),false)).add(instance.getName());
		locator.setSessioncontext(this.getCurrentUser().getUsername());
		return locator;
	}
	
	public String persist() {
		try {
			getInstance().setXml(addToBackend());	
			ConfigConverter converter = new ConfigConverter();
			Element element = converter.convert(XMLUtil.stringTojdom(getInstance().getXml()), String.valueOf(ConfigConverter.currentVersion), IProject.Declaration.lazy);
			String convertedXML = XMLUtil.jdomToString(element, Format.getPrettyFormat().setOmitDeclaration(true));
			
			if (!getInstance().getXml().trim().equals(convertedXML.trim())) { //config converter has made changes. reinitialize component editor
				getInstance().setXml(convertedXML);
				initComponentFromXML();
				addToBackend();
			}
			ConfigManager.getInstance().validate(getLocator());
			cloneHelper = new CloneHelper(getInstance());
		}
		catch (Exception e) {
			e.printStackTrace();
			this.addErrorMessage(Messages.instance().get("etl.save.failed")+": "+e.getMessage());
		}
		boolean reinit = isManaged() && !getEntityManager().contains(getInstance());
		String result = super.persist();
		if (reinit) {//Component has been merged. Reinit transient properties from xml. also reinit editors, which need to update their reference to the instance
			initComponentFromXML();
			initSourcesEditComponent();
			initConnectionEditComponent();
		}
		projectModule.setDirty();
		return result;
	}
	
	public List<String> getAvailableTypes(Component component) {
		List<String> result = new ArrayList<String>(); 
		String defaultType = null;
		List<ComponentDescriptor> descriptors = ComponentFactory.getInstance().getComponentDescriptors(getManagerName(component.getdType(),false));
		for (ComponentDescriptor d : descriptors) {
			if (d.isDefault()) {
				defaultType = d.getName();
			} else {
				if (!d.isDeprecated()) result.add(d.getName());
			}
		}
		Collections.sort(result);
		if (defaultType != null) result.add(0,defaultType);
		return result;
	}
	
	public List<String> getAvailableTypes() {
		List<String> types = getAvailableTypes(getInstance());
		if (!types.contains(getInstance().getType())) types.add(getInstance().getType());
		return types;
	}
	
	public String getAccessString() {
		switch (getInstance().getProject().getPublicationState()) {
		case 1 : return "PRIVATE";
		case 2 : return "USER";
		default: return "UNKNOWN";
		}
	}
	
	public void nameChanged() {
	}
	
	public void typeChanged() {
		getInstance().setAnalyzer(null);
		getInstance().setXml(null);
		getInstance().setConnection(null);
		getInstance().getSources().clear();
		init();
		getComplexRootNodes();
	}
	
	public void xmlChanged() {
	}
	
	public void descriptionChanged() {
	}

	
	protected Element generateField(XSDAnalyzer.ComplexFieldValue value) {
		Element element = new Element(value.getName());
		if (value.isValueHolder() && value.getValue() != null) {
			if (value.isScript()) {
				CDATA script = new CDATA(value.getValue().toString());
				element.addContent(script);
			} else {
				element.setText(value.getValue().toString());
				if (value.isEncrypted()) {
					element.setAttribute("encrypted", "true");
				}
			}
		}
		for (XSDAnalyzer.AttributeField af : value.getType().getAttributes()) {
			Object attrValue = value.getAttributes().get(af.getName()).getValue();
			if (attrValue != null && !attrValue.toString().isEmpty()) {
				element.setAttribute(af.getName(), attrValue.toString());
			}
		}
		if (value.isCompacted()) {
			element.addContent(generateField(value.getCompactedValue()));
		}
		for (XSDAnalyzer.ComplexFieldValue v : value.getChildren()) {
			if (isXMLGenerationEnabled(v)) element.addContent(generateField(v));
		}
		return element;
	}
	
	protected boolean isXMLGenerationEnabled(XSDAnalyzer.ComplexFieldValue v) {
		return v.isEnabled() && (!(!v.isRequired() && v.getAttributeList().isEmpty() && v.isValueHolder() && v.getValue() == null)); //ignore disabled and empty optional simple value fields
	}
	
	protected List<Element> generateFields() throws Exception {
		List<Element> elements = new ArrayList<Element>();
		for (XSDAnalyzer.ComplexFieldValue v : analyzer.getRootComplexFieldValue().getChildren()) {
			if (isXMLGenerationEnabled(v)) elements.add(generateField(v)); 
		}
		return elements;
	}
	
	protected List<Element> generateVariables() throws Exception {
		List<Element> variables = new ArrayList<Element>();
		for (Variable variable : getInstance().getVariables()) {
			Element v = new Element("variable");
			v.setAttribute("name", variable.getName());
			Element value = new Element("default");
			value.setText(variable.getValue());
			v.addContent(value);
			Element comment = new Element("comment");
			CDATA cdata= new CDATA(variable.getComment());
			comment.addContent(cdata);
			v.addContent(comment);
			variables.add(v);
		}
		return variables;
	}
	
	protected Element generateWrapperXML(Component component) throws Exception {
		Element root = new Element(getComponentTypeName(component.getdType()));
		root.setAttribute("name", getInstance().getName());
		root.setAttribute("type",getInstance().getType());
		//comment			
		Element comment = new Element("comment");
		root.addContent(comment);
		CDATA cdata= new CDATA(component.getDescription());
		comment.addContent(cdata);
		return root;
	}
	
	public Element generateXML() throws Exception {
		if (analyzer != null && getInstance() != null) {
			Element root = generateWrapperXML(getInstance());
			//other UI Fields
			root.addContent(generateFields());
			//variables
			root.addContent(generateVariables());
			return root;
		}
		return null;
	}
	
	public String generateXMLString() {
		try {
			if (analyzer != null && getInstance() != null) {
				Element root = generateXML();
				XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setOmitDeclaration(true));
				return outputter.outputString(root);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			this.addErrorMessage(Messages.instance().get("etl.update.failed")+": "+e.getMessage());
		}
		return null;
	}
	
	public List<Project> getProjectInList() {
		List<Project> result = new ArrayList<Project>();
		result.add(getInstance().getProject());
		return result;
	}
	
	private Component executeQueryComponent(String name, Long projectId, List<Integer> dTypeList, boolean raiseNotFound) {
		String queryString = "from Component component where component.dType in (:dTypeList) and component.name=:name and component.project.id=:projectId";
		Query query = getEntityManager().createQuery(queryString);
		query.setParameter("dTypeList",dTypeList);
		query.setParameter("name", name);
		query.setParameter("projectId",projectId);
		Component component = null;
		try {
			component = (Component)query.getSingleResult();
		} catch (NoResultException e) { //try fix for component name has changed within entitymanager
			EntityManager manager = taskController.getTaskEntityManager();
			query = manager.createQuery(queryString);
			query.setParameter("dTypeList",dTypeList);
			query.setParameter("name", name);
			query.setParameter("projectId",projectId);
			try {
				component = (Component)query.getSingleResult();
				Hibernate.initialize(component.getOutputDescription());
			} catch (NoResultException e1) {
				if (raiseNotFound) this.addErrorMessage("Component "+name+" does not exist.");
			}
			finally {
				manager.close();
			}
		}
		return component;
	}
	
	private String getComponentKey(Long projectId, int dType, String name) {
		return projectId.longValue()+"."+dType+"."+name;
	}
	
	public Component findSource(String name) {
		if (name == null || name.isEmpty()) return null;
		Component component = localComponentChache.get(getComponentKey(getInstance().getProject().getId(),ComponentTypes.SOURCE,name));
		if (component == null) {
			List<Integer> dTypeList = new ArrayList<Integer>();
			dTypeList.add(ComponentTypes.EXTRACT);
			dTypeList.add(ComponentTypes.TRANSFORM);
			component = executeQueryComponent(name, getInstance().getProject().getId(),dTypeList,true);
			if (component != null) {
				localComponentChache.put(getComponentKey(getInstance().getProject().getId(),ComponentTypes.SOURCE,name), component);
				localComponentChache.put(getComponentKey(getInstance().getProject().getId(),component.getdType(),name), component);
			}
		}
		return component;
	}
	
	protected Component findComponent(int dType, String name) {
		return findComponent(dType, name, getInstance().getProject().getId(),true);
	}
	
	protected Component findComponent(int dType, String name, Long projectId, boolean raiseNotFound) {
		if (name == null || name.isEmpty()) return null;
		Component component = localComponentChache.get(getComponentKey(projectId,dType,name));
		if (component == null) {
			List<Integer> dTypeList = new ArrayList<Integer>();
			dTypeList.add(dType);
			component = executeQueryComponent(name, projectId, dTypeList, raiseNotFound);
			if (component != null) localComponentChache.put(getComponentKey(projectId,dType,name), component);
		}
		return component;
	}
	
	public List<Component> getDirectDependants(String scope) {
		if (dependantsMap.containsKey(scope)) {
			return dependantsMap.get(scope);
		} else {
			List<Component> results = new ArrayList<Component>();
			try {
				IProject project = (IProject)ConfigManager.getInstance().getComponent(new Locator().add(getInstance().getProject().getName()), IContext.defaultName);
				ConfigManager.getInstance().initProjectComponents(project, IContext.defaultName, true);
				List<String> deps = project.getDirectDependents(getLocator().toString(), false);
				for (String s : deps) {
					Locator l = Locator.parse(s);
					if (l.getManager().equals(scope.toLowerCase())) {
						int dType = getComponentType(l.getManager());
						results.add(findComponent(dType,l.getName()));
					}
				}
			}
			catch (Exception e) {
				error("Error getting direct dependencies: "+e.getMessage());
			}
			dependantsMap.put(scope, results);
			return results;
		}
	}
	
	public List<Component> getDirectDependencies(String scope) {
		List<Component> results = new ArrayList<Component>();
		try {
			IProject project = (IProject)ConfigManager.getInstance().getComponent(new Locator().add(getInstance().getProject().getName()), IContext.defaultName);
			List<String> deps = project.getDirectDependencies(getLocator().toString(), false);
			for (String s : deps) {
				Locator l = Locator.parse(s);
				if (l.getManager().equals(scope.toLowerCase())) {
					int dType = getComponentType(l.getManager());
					results.add(findComponent(dType,l.getName()));
				}
			}
		}
		catch (Exception e) {
			error("Error getting direct dependencies: "+e.getMessage());
		}
		return results;
	}
	
	
	public void generateFlowGraph() {
		try {
			IProject project = ConfigManager.getInstance().getProject(getInstance().getProject().getName());
			List<Locator> invalids = ConfigManager.getInstance().initProjectComponents(project, IContext.defaultName, true);
			Properties graphProperties = new Properties();
			graphProperties.setProperty("viewType", "all");
			graphProperties.setProperty("orientation",String.valueOf(SwingConstants.NORTH));
			String application = Expressions.instance().createValueExpression("#{facesContext.externalContext.request.contextPath}").getValue().toString();
			graphProperties.setProperty("linkPrefix", application+"/resolvecomponent.seam?locator=");
			//graphProperties.setProperty("onClick", "Jedox.studio.etl.flowGraphClick('%')");
			//graphProperties.setProperty("onClick", "window.close()");
			Locator componentLocator = getLocator();
			flowGraphXML = GraphManager.getInstance().getSVG(project,componentLocator.toString(),graphProperties,invalids, -1l);
		}
		catch (Exception e) {
			e.printStackTrace();
			this.addErrorMessage(Messages.instance().get("etl.flowgraph.failed")+": "+e.getMessage());
		}
	}
	
	public String getFlowGraph() {
		return flowGraphXML != null ? flowGraphXML : "";
	}
	
	@SuppressWarnings("unchecked")
	protected void preRemove(Component instance) {
		setDirty();
		try {
			setInstance((T)instance);
			getEntityManager().refresh(getInstance());
			ConfigManager.getInstance().removeElement(getLocator());
			ConfigManager.getInstance().getProjectManager().remove(instance.getProject().getName());
			clearInstance();
		}
		catch (Exception e) {
			this.addErrorMessage(Messages.instance().get("etl.xml.delete.failed")+": "+e.getMessage());
		}
	}
	
	public void delete(Object object) {
		if (object instanceof Long) {
			Component instance = getEntityManager().find(Component.class, (Long)object);
			preRemove(instance);
			super.delete(object);
			projectModule.setDirty();
		}
	}
	
	public String remove() {
		preRemove(getInstance());
		String result = super.remove();
		projectModule.setDirty();
		return result;
	}
	
	public Long getCurrentETLId() {
		return (etlExecution != null) ? etlExecution.getKey() : null;
	}

	public String getEtlLogs() {
		try {
			if (preparingExecution) return "Prepare to run...";
			return (etlExecution != null && etlExecution.getExecutionState() != null && etlExecution.getExecutionState().getMessageWriter() != null) ? etlExecution.getExecutionState().getMessageWriter().getMessagesText() : "";
		} catch (Exception e) {
			e.printStackTrace();
			return "Failure "+e.getMessage();
		}
	}
	
	public void setEtlLogs(String etlLogs) {
		//do nothing
	}
	
	public boolean hasConnection() {
		if (analyzer != null) return analyzer.hasConnection();
		return false;
	}
	
	public boolean isConnectionRequired() {
		if (analyzer != null) return analyzer.isConnectionRequired();
		return false;
	}
	
	public List<Component> getAvailableConnections() {
		return availableConnections;
	}	
	
	public List<Component> getConnections(String category) {
		if (category.equals(":NONE")) return new ArrayList<Component>();
		if (category.equals(":ALL")) return allConnections;
		if (category.equals(":MODULE")) return getAvailableConnections();
		List<Component> result = new ArrayList<Component>();
		try {
			Class<?> categoryClass = CustomClassLoader.getInstance().loadClass(category);
			for (Component c : allConnections) {
				ComponentDescriptor d = ComponentFactory.getInstance().getComponentDescriptor(c.getType(), ITypes.Connections);
				Class<?> connectionClass = CustomClassLoader.getInstance().loadClass(d.getClassName());
				if (categoryClass.isAssignableFrom(connectionClass)) result.add(c);
			}
		}
		catch (Exception e) {
			this.addWarnMessage("Connection interface "+category+" is not resolvable: "+e.getMessage());
		}
		return result;
	}
	
	public List<Component> getSources(String category, Component component) {
		if (category.equals(":NONE")) return new ArrayList<Component>();
		if (category.equals(":ALL")) return allSources;
		if (category.equals(":MODULE")) return getAvailableSources();
		if (category.equals(":LOCAL")) return component.getSources();
		List<Component> result = new ArrayList<Component>();
		if (category.equals(":TREE")) { 
			Class<?> sourceInterface = ITreeSource.class;
			for (Component c : allSources) {
				try {
					ComponentDescriptor d = ComponentFactory.getInstance().getComponentDescriptor(c.getType(), getManagerName(c.getdType(),false));
					Class<?> sourceClass = CustomClassLoader.getInstance().loadClass(d.getClassName());
					if (sourceInterface.isAssignableFrom(sourceClass)) result.add(c);
				}
				catch (Exception e) {
					this.addWarnMessage("Source interface "+category+" is not resolvable: "+e.getMessage());
				}
			}
		}
		return result;
	}
	
	public void setComponentExtraConnection(Component component, String connectionName) {
		if (component.getAnalyzer() != null && !component.getAnalyzer().hasConnection()) { //only allow this for components with no regular connections, such as functions
			for (Component c : allConnections) {
				if (c.getName().equals(connectionName)) {
					component.setConnection((Connection)c);
					break;
				}
			}
		}
	}
	
	public boolean isSourceRequired() {
		if (analyzer != null) return analyzer.getMinSources() > 0;
		return false;
	}
	
	public int getMaxSources() {
		if (analyzer != null) return analyzer.getMaxSources();
		return 0;
	}
	
	public boolean hasVariables() {
		if (analyzer != null) return analyzer.hasVariables();
		return false;
	}
	
	public List<Component> getAvailableSources() {
		return availableSources;
	}
	
	public List<String> getComponentInputList() {
		List<String> inputs = new ArrayList<String>();
		for (ComponentOutput co : getComponentTypedInputList()) {
			if (!inputs.contains(co.getName())) inputs.add(co.getName());
		}
		return inputs;
	}
	
	public List<ComponentOutput> getComponentExtraSourcesTypedInputList(Component component) {
		List<ComponentOutput> inputList = new ArrayList<ComponentOutput>();
		List<Component> extraSources = getExtraSources(component);
		for (int i=0; i< extraSources.size(); i++) {
			Component c = extraSources.get(i);
			for (ComponentOutput o : getOutputDescription(c)) {
				o.setSourcePosition(i+1);
				inputList.add(o);
			}
		}
		Collections.sort(inputList);
		return inputList;
	}
	
	public List<ComponentOutput> getComponentTypedInputList() {
		if (inputList == null) {
			inputList = new ArrayList<ComponentOutput>();
			for (int i=0; i< getInstance().getSources().size(); i++) {
				Component c = getInstance().getSources().get(i);
				for (ComponentOutput o : getOutputDescription(c)) {
					o.setSourcePosition(i+1);
					inputList.add(o);
				}
			}
			Collections.sort(inputList);
		}
		return inputList;
	}
	
	private List<String> getComponentNames(List<Component> components) {
		List<String> result = new ArrayList<String>();
		if (components != null) for (Component c : components) result.add(c.getName());
		return result;
	}
	
	public String getEditorAutoCompletion() {
		if (analyzer != null) return analyzer.getAutoCompletion(getComponentInputList(),getComponentNames(getAvailableSources()),getComponentNames(getAvailableConnections()));
		return "";
	}
	
	public void sourceSelectionChanged(ProxyItem item) {
		int index = sourcesEditComponent.getItems().indexOf(item);
		if (index >= 0) {
			if (index < getInstance().getSources().size()) getInstance().getSources().remove(index);
			getInstance().getSources().add(index, item.getComponent());
			XSDAnalyzer.ComplexFieldValue source = analyzer.getSources().get(index);
			if (source != null) {
				source.setInput(item.getComponent().getName());
				IUIField type = source.getAttributes().get("type");
				if (type != null) { //set type in case of execution (in job)
					type.setValue(getComponentTypeName(item.getComponent().getdType()));
				}
			}
		}
	}
	
	public void connectionSelectionChanged(ProxyItem item) {
		getInstance().setConnection((Connection)item.getComponent());
		XSDAnalyzer.ComplexFieldValue connection = analyzer.getConnection();
		if (connection != null) connection.setInput(item.getComponent().getName());
	}
	
	private void initSourcesEditComponent()
	{
		if (analyzer != null) {
			List<ProxyItem> list = new ArrayList<ProxyItem>();
			for (Component c : getInstance().getSources()) {
				ProxyItem p = new ProxyItem();
				p.setComponent(c);
				list.add(p);
			}
			sourcesEditComponent = new SourcesEditComponent(list, analyzer.getMinSources(), getMaxSources(),getInstance());
		}
		else {
			sourcesEditComponent = new SourcesEditComponent(new ArrayList<ProxyItem>(),0, 0, getInstance());
		}
	}
	
	private void initConnectionEditComponent()
	{
		if (analyzer != null) {
			List<ProxyItem> list = new ArrayList<ProxyItem>();
			if (getInstance().getConnection() != null) {
				ProxyItem p = new ProxyItem();
				p.setComponent(getInstance().getConnection());
				list.add(p);
			}
			setConnectionEditComponent(new ConnectionEditComponent(list, analyzer.isConnectionRequired() ? 1 : 0, 1 ,getInstance()));
		}
		else {
			setConnectionEditComponent(new ConnectionEditComponent(new ArrayList<ProxyItem>(),0, 0, getInstance()));
		}
	}

	public IEditComponent<ProxyItem> getSourcesEditComponent()
	{
		return sourcesEditComponent;
	}
	
	protected void setSourcesEditComponent(IEditComponent<ProxyItem> sourcesEditComponent) {
		this.sourcesEditComponent = sourcesEditComponent;
	}
	
	public void calculatePreview() {
		etlExecution = null;
		data.clear();
		dataHeader.clear();
		Locator locator = getLocator();
		checkMessages = true;
		showDataOnComplete = true;
		try {
			addToBackend();
			etlExecution = Executor.getInstance().createData(locator, new Properties(),sampleFormat);
			if (etlExecution.getExecutable() instanceof ISource) {
				etlExecution.getExecutable().getParameter().setProperty("sample", String.valueOf(sampleSize));
			}
			Long id = etlExecution.getKey();
			Executor.getInstance().addExecution(etlExecution);
			executable = etlExecution.getExecutable();
			if (executable != null && executable instanceof ITreeSource) {
				ITreeSource treeSource = (ITreeSource)executable;
				treeSource.setPreserveTreeOnInvalidate(true);
			}
			taskController.invokeExecution(new Long(0), getInstance().getId(), id,false,getInstance().getId());
		} catch (Exception e) {
			this.addErrorMessage(Messages.instance().get("etl.execution.failed")+": " + e.getMessage());
		}
	}
	
	private void buildPreviewTreeNode(ITreeElement e, TreeNode parent) {
		TreeNode node = new DefaultTreeNode(e,parent);
		for (ITreeElement c : e.getChildren()) {
			buildPreviewTreeNode(c, node);
		}
	}
	
	public TreeNode getPreviewTree() {
		TreeNode root = new DefaultTreeNode(null,null);
		if (etlExecution != null && etlExecution.isFinished() && executable != null && executable instanceof ITreeSource) {
			ITreeSource treeSource = (ITreeSource)executable;
			for (ITreeElement e : treeSource.getTreeManager().getRootElements(true)) {
				buildPreviewTreeNode(e, root);
			}
		}
		return root;
	}
	
	public List<String> getPreviewTreeAttributes() {
		List<String> result = new ArrayList<String>();
		if (executable != null && executable instanceof ITreeSource) {
			ITreeSource treeSource = (ITreeSource)executable;
			try {
				for (Attribute a : treeSource.getTreeManager().getAttributes()) {
					result.add(a.getName());
				}
			}
			catch (Exception e) {};
		}
		return result;
	}
	
	public Long getPushId() {
		return getInstance().getId() != null ? getInstance().getId() : getInstance().hashCode();
	}
	
	public void calculateOutputDescription(Component component, String format) {
		if (format == null && isTreeSource(component)) {
			XSDAnalyzer.ComplexFieldValue sourceFieldValue = analyzer.getSource(component.getName());
			if (sourceFieldValue != null) {
				IUIField a = sourceFieldValue.getAttributes().get("format");
				if (a != null) {
					format = (String)a.getValue();
					if (format == null) format = getSampleFormat();
				}
			}
		}
		inputList = null;
		Locator locator = new Locator().add(getInstance().getProject().getName()).add(getManagerName(component.getdType(),false)).add(component.getName());
		checkMessages = true;
		try {
			etlExecution = Executor.getInstance().createOutputDescription(locator, new Properties(), format);
			Long id = etlExecution.getKey();
			Executor.getInstance().addExecution(etlExecution);
			taskController.invokeExecution(new Long(0),component.getId(), id,false,getPushId());
		} catch (Exception e) {
			this.addErrorMessage(Messages.instance().get("etl.execution.failed")+": " + e.getMessage());
		}
	}
	
	public void calculateOutputDescription(Component component) {
		calculateOutputDescription(component,null);
	}
	
	public void calculateMetadata(Component connection, String selector) {
		data.clear();
		dataHeader.clear();
		Locator locator = new Locator().add(getInstance().getProject().getName()).add(ITypes.Connections).add(connection.getName());
		checkMessages = true;
		showDataOnComplete = true;
		try {
			addToBackend();
			Properties metadataProperties = new Properties();
			metadataProperties.setProperty("selector", selector);
			etlExecution = Executor.getInstance().createMetadata(locator, new Properties(), metadataProperties);
			Long id = etlExecution.getKey();
			Executor.getInstance().addExecution(etlExecution);
			taskController.invokeExecution(new Long(0), connection.getId(), id,false,getInstance().getId());
		} catch (Exception e) {
			this.addErrorMessage(Messages.instance().get("etl.execution.failed")+": " + e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<ComponentOutput> updateComponentOutput(Component component, ExecutionState state) {
		if (state.getMetadata() != null && component.getName().equals(state.getName()) && getManagerName(component.getdType(),false).equals(state.getType())) {//component output has been update async
			List<ComponentOutput> result = (List<ComponentOutput>)getEntityManager().createQuery("from ComponentOutput o where o.component.id=:id").setParameter("id", component.getId()).getResultList();	
			component.getOutputDescription().clear();
			component.getOutputDescription().addAll(result);
			etlExecution.getExecutionState().setMetadata(null);
			return result;
		}
		return null;
	}
	
	public List<ComponentOutput> getOutputDescription(Component component) {
		if (etlExecution != null && etlExecution.getExecutionState().getMetadata() != null) {
			ExecutionState state = etlExecution.getExecutionState();
			List<ComponentOutput> result = updateComponentOutput(component, state);
			if (result != null) return result;
		}
		return component.getOutputDescription();
	}
	
	protected StringBuffer getComplexEditorsJavascript(XSDAnalyzer.ComplexFieldValue f, String prefix, String operation) {
		StringBuffer result = new StringBuffer();
		if (f.isScript()) {
			result.append("if (typeof "+prefix+f.getType().getName()+" !== 'undefined')"+" "+prefix+f.getType().getName()+"."+operation+";");
		}
		for (XSDAnalyzer.ComplexFieldValue c : f.getChildren()) {
			if (c.isEnabled() && !c.getType().hasCustomHandle()) result.append(getComplexEditorsJavascript(c,prefix,operation).toString());
		}
		return result;
	}
	
	public String getEditorsToSave() {
		StringBuffer result = new StringBuffer();
		if (analyzer != null) {
			for (XSDAnalyzer.ComplexFieldValue f : analyzer.getRootComplexFieldValue().getChildren()) {
				if (f.isEnabled() && !f.getType().hasCustomHandle()) result.append(getComplexEditorsJavascript(f,getInstance().getInternalName(),"save()").toString());
			}
		}
		return result.toString();
	}
	
	private void initVariableEditComponent()
	{
		variableEditComponent = new EditComponent<Variable>(getInstance().getVariables(), Variable.class,0,Integer.MAX_VALUE);
	}

	public IEditComponent<Variable> getVariableEditComponent()
	{
		return variableEditComponent;
	}
	
	private void checkShowPreview() {
		if (showDataOnComplete) {
			RequestContext.getCurrentInstance().execute("PF('dataDisplayPanel').show()");
			RequestContext.getCurrentInstance().update("mainform:dataDisplay");
			showDataOnComplete = false;
		}
	}
	
	public void checkMessages() {
		if (checkMessages) {
			if (etlExecution != null) {
				ExecutionState state = etlExecution.getExecutionState();
				updateComponentOutput(getInstance(), state);
				for (Component s : getAllSources()) {
					updateComponentOutput(s, state);
				}
				switch (state.getStatus()) {
				case statusOK: {
					this.addInfoMessage(state.getString(state.getStatus()));
					checkShowPreview();
					break;
				}
				case statusWarnings: {
					this.addWarnMessage(state.getString(state.getStatus()));
					checkShowPreview();
					break;
				}
				case statusStopped: {
					this.addWarnMessage(state.getString(state.getStatus()));
					break;
				}
				default: {
					this.addErrorMessage(state.getFirstErrorMessage());
					break;
				}
				}
			}
			checkMessages = false;
			/*
			try {
				Thread.sleep(100);
			}
			catch (Exception e) {};
			*/
		}
	}
	
	protected Map<IUIField,Locator> getComponentScripts(XSDAnalyzer.ComplexFieldValue field) {
		Map<IUIField,Locator> result = new HashMap<IUIField,Locator>();
		if (field.isScript() && field.getValue() != null) result.put(field, getLocator());
		for (XSDAnalyzer.ComplexFieldValue f : field.getChildren()) {
			result.putAll(getComponentScripts(f));
		}
		return result;
	}
	
	protected Map<IUIField,Locator> getComponentScripts() {
		Map<IUIField,Locator> result = new HashMap<IUIField,Locator>();
		if (analyzer != null) for (XSDAnalyzer.ComplexFieldValue sf : analyzer.getRootComplexFieldValue().getChildren()) {
			result.putAll(getComponentScripts(sf));
		}
		return result;
	}
	
	protected List<XSDAnalyzer.ComplexFieldValue> getComponentFieldInputs(XSDAnalyzer.ComplexFieldValue field, String defaultValue, ITypes.Managers type) {
		List<XSDAnalyzer.ComplexFieldValue> result = new ArrayList<XSDAnalyzer.ComplexFieldValue>();
		boolean isMatch = false;
		switch (type) {
		case connections : isMatch = field.isConnectionField(); break;
		case sources: isMatch = field.isSourceField(); break;
		case projects: isMatch = field.isProjectField(); break;
		default: isMatch = field.isInputField(); //TODO check if we need this
		}
		if (isMatch) {
			String input = field.getInput();
			String fieldName = (input != null && !input.isEmpty()) ? input : defaultValue;
			if (fieldName != null) {
				field.setInput(fieldName);
				result.add(field);
			}
		}
		for (XSDAnalyzer.ComplexFieldValue childField : field.getChildren()) {
			result.addAll(getComponentFieldInputs(childField,defaultValue,type));
		}
		return result;
	}
	
	protected List<IUIField> getAllComponentNameAttributes(ITypes.Managers type) {
		List<IUIField> result = new ArrayList<IUIField>();
		if (analyzer != null) {
			for (XSDAnalyzer.ComplexFieldValue field : analyzer.getRootComplexFieldValue().getChildren()) {
				for (XSDAnalyzer.ComplexFieldValue f : getComponentFieldInputs(field,null,type)) {
					if (f.getAttributes().containsKey("nameref")) {
						result.add(f.getAttributes().get("nameref"));
					} else {
						result.add(f);
					}
				};
			}
		}
		return result;
	}
	
	private String buildParameterString(String[] parameters) {
		StringBuffer result = new StringBuffer();
		if (parameters != null) {
			for (int i=0; i<parameters.length-1; i++) {
				result.append(parameters[i]+",");
			}
			result.append(parameters[parameters.length-1]);
		}
		return result.toString();
	}
	
	protected String updateScript(Locator referenceLocator, Locator dependantLocator, String newName, String script) {
		try {
			IComponent component = ConfigManager.getInstance().getComponent(dependantLocator, IContext.defaultName);
			ScriptDependenciesResult result = APIExtender.getInstance().getGuessedDependencies(component, Arrays.asList(new String[]{script}));
			for (ScriptDependenciesDescriptor d : result.descriptors) {
				if (d.component.getLocator().toString().equalsIgnoreCase(referenceLocator.toString())) {
					String origString = d.methodString+buildParameterString(d.parameters)+")";
					d.parameters[d.scan.position] = "\""+newName+"\"";
					String replaceString = d.methodString+buildParameterString(d.parameters)+")";
					script = script.replace(origString, replaceString);
				}
			}
		}
		catch (Exception e) {
			error("Error renaming dependency in script: "+e.getMessage());
		}
		return script;
	}
	
	public Component copyInstance(Component toCopy, String newName, Project target) {
		String typeName = getComponentTypeName(toCopy.getdType());
		@SuppressWarnings("unchecked")
		ComponentModule<? extends Component> module = (ComponentModule<? extends Component>)ModuleManager.instance().getModuleForName(typeName);
		Component component = findComponent(toCopy.getdType(), newName,target.getId(),false);
		if (component != null) {
			module.select(component.getId());
		} else {
			module.createNewInstance();
		}
		module.getInstance().setProject(target);
		module.getInstance().setType(toCopy.getType());
		module.getInstance().setName(newName);
		module.getInstance().setActive(true);
		module.getInstance().setDescription(toCopy.getDescription());
		module.getInstance().setXml(toCopy.getXml());
		module.instanceSelected();
		module.persist();
		return module.getInstance();
	}
	
	public void rename(String newName, boolean checkDependants, Project target) throws Exception {
		Component currentInstance = getInstance();
		IProject project = (IProject)ConfigManager.getInstance().getProject(target.getName());
		ConfigManager.getInstance().initProjectComponents(project, IContext.defaultName, true);
		Locator locator = getLocator();
		List<String> deps = project.getDirectDependents(locator.toString(), false);
		String oldName = getInstance().getName();
		if (checkDependants && !deps.isEmpty()) {
			Component tempInstance = copyInstance(currentInstance,newName,target);
			List<Component> dependencies = new ArrayList<Component>();
			for (String s : deps) {
				Locator l = Locator.parse(s);
				int dType = getComponentType(l.getManager());
				dependencies.add(findComponent(dType,l.getName(),currentInstance.getProject().getId(),true));
			}
			for (Component c : dependencies) {
				String typeName = getComponentTypeName(c.getdType());
				@SuppressWarnings("unchecked")
				ComponentModule<? extends Component> module = (ComponentModule<? extends Component>)ModuleManager.instance().getModuleForName(typeName);
				module.select(c.getId());
				module.instanceSelected();
				if (currentInstance.getdType() == ComponentTypes.CONNECTION) {
					for (IUIField sf : module.getAllComponentNameAttributes(ITypes.Managers.connections)) {
						if (sf.getValue() != null && sf.getValue().equals(oldName)) sf.setValue(newName);
					}
				} else {
					for (IUIField sf : module.getAllComponentNameAttributes(ITypes.Managers.sources)) {
						if (sf.getValue() != null && sf.getValue().equals(oldName)) sf.setValue(newName);
					}
				}
				Map<IUIField,Locator> scriptMap = module.getComponentScripts();
				for (IUIField scriptField : scriptMap.keySet()) {
					scriptField.setValue(updateScript(locator,scriptMap.get(scriptField),newName,scriptField.getValue().toString()));
				}
				module.persist();
				module.clearAll();
				module.setDirty();
			}
			this.delete(tempInstance.getId());
		}
		ConfigManager.getInstance().removeElement(locator);
		this.select(currentInstance.getId());
		this.instanceSelected();
		getInstance().setName(newName);
		this.persist();
		//projectModule.setDirty();
		projectModule.reselect();
		/*
		ConfigManager.getInstance().getProjectManager().remove(locator.getRootName()); //remove default context components to fix bug in fucntion dependencies
		project = (IProject)ConfigManager.getInstance().getProject(currentInstance.getProject().getName());
		ConfigManager.getInstance().validate(locator.getRootLocator());
		*/
	}
	
	protected Set<String> getSourceFieldInputStrings(XSDAnalyzer.ComplexFieldValue field) {
		Set<String> result = new LinkedHashSet<String>();
		for (XSDAnalyzer.ComplexFieldValue sourceField : getComponentFieldInputs(field, getAvailableSources().size() > 0 ? getAvailableSources().get(0).getName() : null, ITypes.Managers.sources)) {
			result.add(sourceField.getInput());
		}
		return result;
	}
	
	protected List<Component> getExtraSources(Component component) {
		List<Component> result = new ArrayList<Component>();
		Set<String> sourceInputs = new LinkedHashSet<String>();
		XSDAnalyzer componentAnalzer = component.getAnalyzer() != null ? component.getAnalyzer() : this.analyzer;
		if (componentAnalzer != null) for (XSDAnalyzer.ComplexFieldValue field : componentAnalzer.getRootComplexFieldValue().getChildren()) {
			if (field.isEnabled()) sourceInputs.addAll(getSourceFieldInputStrings(field));
		}
		for (String s : sourceInputs) {
			boolean isStandard = false;
			for (Component standardSource : component.getSources()) {
				if (standardSource.getName().equals(s)) {
					isStandard = true;
					break;
				}
			}
			if (!isStandard) {
				for (Component source : getAvailableSources()) {
					if (source.getName().equals(s)) {
						result.add(source);
						break;
					}
				}
			}
		}
		return result;
	}
	
	public List<Component> getAllSources() {
		List<Component> sources = new ArrayList<Component>();
		sources.addAll(getInstance().getSources());
		sources.addAll(getExtraSources(getInstance()));
		return sources;
	}
	
	public CloneHelper getCloneHelper() {
		return cloneHelper;
	}
	
	private void copyDependenciesTree(Component toCopy, Project target, Map<String, List<String>> map, int mode, Component root, String newRootName) {
		List<String> deps = map.get(getLocator(toCopy).toString());
		if (deps != null) for (String depQName : deps) {
			Locator l = Locator.parse(depQName);
			Component depComponent = findComponent(getComponentType(l.getManager()), l.getName(), root.getProject().getId(),true);
			if (mode == 1) copyInstance(depComponent, depComponent.getName(), target); //dependants pre order
			copyDependenciesTree(depComponent, target, map, mode, root, newRootName);
		}
		if (mode == 0) copyInstance(toCopy, toCopy.equals(root) ? newRootName : toCopy.getName(), target); //dependencies post order
	}
	
	public void cloneInstance() {
		try {
			Project currentProject = getInstance().getProject();
			Project target = getInstance().getProject();
			Component currentInstance = getInstance();
			Locator locator = getLocator(currentInstance);
			if (cloneHelper.getTarget() == CloneHelper.targetExisting) {
				projectModule.select(cloneHelper.getTargetProject().getId());
				target = projectModule.getInstance();
			}
			if (cloneHelper.getTarget() == CloneHelper.targetNew) {
				projectModule.createNewInstance();
				projectModule.getInstance().setName(cloneHelper.getNewProjectName());
				projectModule.getInstance().setType(cloneHelper.getNewProjectType());
				projectModule.persist();
				target = projectModule.getInstance();
			}
			if (cloneHelper.getMode() == CloneHelper.modeCopy) {
				String newComponentName = StringUtils.isEmpty(cloneHelper.getNewComponentName()) ? currentInstance.getName() : cloneHelper.getNewComponentName();
				IProject project = (IProject)ConfigManager.getInstance().getProject(currentInstance.getProject().getName());
				if (cloneHelper.isWithDependencies()) {
					Map<String, List<String>> map = project.getAllDependencies(locator.toString(), false);
					copyDependenciesTree(currentInstance, target, map, 0, currentInstance, newComponentName);
				} else {
					copyInstance(currentInstance, newComponentName, target);
				}
				if (currentProject != target) {
					projectModule.getEntityManager().refresh(target);
					projectModule.select(target.getId());
					projectModule.setDirty();
				}
			}
			if (cloneHelper.getMode() == CloneHelper.modeMove) {
				rename(cloneHelper.getNewComponentName(),cloneHelper.isUpdateDependencies(),target);
			}
			if (cloneHelper.getMode() == CloneHelper.modeDelete) {
				this.delete(currentInstance.getId());
				Redirect.instance().setViewId("/modules/"+getComponentTypeName(currentInstance.getdType())+"/list.xhtml");
				Redirect.instance().execute();
			} else {
				cloneHelper = new CloneHelper(getInstance());
				/*
				Redirect.instance().setViewId("/modules/"+getComponentTypeName(currentInstance.getdType())+"/edit.xhtml");
				Redirect.instance().setParameter("id", getInstance().getId());
				Redirect.instance().execute();
				*/
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			this.addErrorMessage(e.getMessage());
		}
	}
	
	public void calculateDataFromTree() {
		if (etlExecution != null && etlExecution.isFinished() && executable != null && executable instanceof ITreeSource && !Views.NONE.toString().equals(sampleFormat)) {
			try {
				ITreeSource treeSource = (ITreeSource)executable;
				IProcessor processor = new TreeViewProcessor(treeSource,Views.valueOf(sampleFormat));
				processor.setFacet(Facets.OUTPUT);
				processor.setOwner(treeSource);
				processor.initialize();
				dataHeader.clear();
				for (int i=0; i<processor.current().size(); i++) {
					HeaderColumn c = new HeaderColumn();
					c.setId(i);
					c.setName(processor.current().getColumn(i).getName());
					dataHeader.add(c);
				}
				data.clear();
				int lineNumber = 0;
				while (processor.next() != null) {
					DataRow row = new DataRow();
					row.setId(new Long(lineNumber));
					row.setData(processor.current().getColumnValues().toArray(new String[processor.current().size()]));
					row.setHeader(processor.current().getColumnNames());
					data.add(row);
					lineNumber++;
				}
				filteredData.clear();
				filteredData.addAll(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public List<DataRow> getFilteredData() {
		return filteredData;
	}

	public void setFilteredData(List<DataRow> filteredData) {
		this.filteredData = filteredData;
	}

	public int getSampleSize() {
		return sampleSize;
	}

	public void setSampleSize(int sampleSize) {
		this.sampleSize = sampleSize;
	}

	public String getSampleFormat() {
		return sampleFormat;
	}

	public void setSampleFormat(String sampleFormat) {
		if (sampleFormat != null)
			this.sampleFormat = sampleFormat;
	}
	
	public List<String> getAvailableTreeViews() {
		List<String> result = new ArrayList<String>();
		for (IView.Views v : IView.Views.values()) {
			result.add(v.toString());
		}
		result.remove(Views.NONE.toString());
		result.remove(Views.FHWA.toString());
		result.add(0,Views.NONE.toString());
		return result;
	}

	public DataRow[] getSelectedData() {
		return selectedData;
	}

	public void setSelectedData(DataRow[] selectedData) {
		this.selectedData = selectedData;
	}

	public boolean isExportSelection() {
		return exportSelection;
	}

	public void setExportSelection(boolean exportSelection) {
		this.exportSelection = exportSelection;
	}

	

	public IEditComponent<ProxyItem> getConnectionEditComponent() {
		return connectionEditComponent;
	}

	public void setConnectionEditComponent(IEditComponent<ProxyItem> connectionEditComponent) {
		this.connectionEditComponent = connectionEditComponent;
	}
}
