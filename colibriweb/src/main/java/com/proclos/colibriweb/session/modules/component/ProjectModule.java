package com.proclos.colibriweb.session.modules.component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.persistence.NoResultException;
import javax.swing.SwingConstants;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.Messages;
import org.jboss.seam.security.AuthorizationException;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import at.adaptive.components.hibernate.CriteriaWrapper;
import at.adaptive.components.validator.ParameterizableUniqueValidator;
import at.adaptive.components.validator.UniqueValidator;

import com.jedox.etl.core.component.ConfigurationException;
import com.jedox.etl.core.component.IComponent;
import com.jedox.etl.core.component.ITypes;
import com.jedox.etl.core.component.Locator;
import com.jedox.etl.core.config.ConfigManager;
import com.jedox.etl.core.context.IContext;
import com.jedox.etl.core.project.IProject;
import com.jedox.etl.core.util.XMLUtil;
import com.jedox.etl.core.util.svg.GraphManager;
import com.proclos.colibriweb.entity.component.Component;
import com.proclos.colibriweb.entity.component.ComponentTypes;
import com.proclos.colibriweb.entity.component.Connection;
import com.proclos.colibriweb.entity.component.Extract;
import com.proclos.colibriweb.entity.component.Job;
import com.proclos.colibriweb.entity.component.Load;
import com.proclos.colibriweb.entity.component.Project;
import com.proclos.colibriweb.entity.component.Transform;
import com.proclos.colibriweb.entity.component.Variable;
import com.proclos.colibriweb.session.common.EditComponent;
import com.proclos.colibriweb.session.common.IEditComponent;
import com.proclos.colibriweb.session.modules.MetadataTreeFilter;
import com.proclos.colibriweb.session.modules.Module;
import com.proclos.colibriweb.session.system.AccessUtil;
import com.proclos.colibriweb.session.system.ModuleManager;

@Name("projectModule")
@Scope(ScopeType.SESSION)
public class ProjectModule extends Module<Project> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2750555500296849074L;
	private byte[] importXML;
	private String initialProjectName;
	private MetadataTreeFilter filter;
	
	private IEditComponent<Variable> parameterEditComponent;
	private UniqueValidator nameValidator;
	private String xml;
	private TreeNode selectedNode;
	private String flowGraphXML = null;

	@Override
	public Class<Project> getEntityClass() {
		return Project.class;
	}
	
	public void setNewInstanceProperties() {
		super.setNewInstanceProperties();
		if (getCurrentUser() != null) {
			getInstance().setContactPerson(getCurrentUser().getCompleteName());
			getInstance().setContactOnError(getCurrentUser().getEmail());
		}
	}
	
	public void instanceCreated() {
		super.instanceCreated();
		initParameterEditComponent();
		filter = new MetadataTreeFilter();
		filter.setContent(getComponentTree());
		selectedNode = null;
		setDependentModulesDirty();
		xml = null;
		flowGraphXML = null;
	}
	
	public void instanceSelected() {
		super.instanceSelected();
		initParameterEditComponent();
		initialProjectName = getInstance().getName();
		filter = new MetadataTreeFilter();
		filter.setContent(getComponentTree());
		selectedNode = null;
		setDependentModulesDirty();
		xml = null;
		flowGraphXML = null;
	}
	
	public MetadataTreeFilter getFilter() {
		return filter;
	}
	
	protected void applyCustomFilters(CriteriaWrapper criteriaWrapper) {
		AccessUtil.appendAccessCriterion(getCurrentUser(), criteriaWrapper);
		super.applyCustomFilters(criteriaWrapper);
	}
	
	public Project findProject(String name) throws AuthorizationException {
		try {
			Project result = (Project)getEntityManager().createQuery("from Project project where lower(project.name) = :name").setParameter("name", name.toLowerCase()).getSingleResult();
			if (!AccessUtil.hasAccess(getCurrentUser(), result)) throw new AuthorizationException(messages.get("notAuthorized"));
			return result;
		}
		catch (NoResultException e) {
			return new Project();
		}
	}
	
	public Component createComponent(String name, int dType) {
		Component component = null;
		switch (dType) {
		case ComponentTypes.CONNECTION : component = new Connection(); break;
		case ComponentTypes.EXTRACT: component = new Extract(); break;
		case ComponentTypes.TRANSFORM: component = new Transform(); break;
		case ComponentTypes.LOAD: component = new Load(); break;
		case ComponentTypes.JOB: component = new Job(); break;
		default: error("Invalid Component type to create: "+dType);
		}
		if (component != null) component.setdType(dType);
		return component;
	}
	
	public Component findComponent(String name, int dType, Long projectId) {
		if (projectId == null) return createComponent(name,dType);
		try {
			return (Component)getEntityManager().createQuery("from Component component where component.name = :name and component.dType = :dType and component.project.id = :projectId").setParameter("name", name).setParameter("dType", dType).setParameter("projectId", projectId).getSingleResult();
		}
		catch (NoResultException e) {
			return createComponent(name,dType);
		}
	}
	
	public Project importInternal(Element element) throws Exception {
		getEntityManager().clear();
		Project projectUI = null;
		String projectName = element.getAttributeValue("name");
		Locator locator = new Locator().add(projectName);
		ConfigManager.getInstance().removeElement(locator);
		ConfigManager.getInstance().add(locator, element);
		element = ConfigManager.getInstance().get(locator); //return converted form
		projectUI = findProject(projectName);
		projectUI.setActive(true);
		projectUI.setName(projectName);
		projectUI.setContactPerson(getCurrentUser().getCompleteName());
		projectUI.setContactOnError(getCurrentUser().getEmail());
		//header
		Element headers = element.getChild("headers");
		if (headers != null) {
			List<?> headerList = headers.getChildren("header");
			for (Object o : headerList) {
				Element h = (Element)o;
				if ("comment".equals(h.getAttributeValue("name"))) {
					projectUI.setDescription(h.getChildText("comment"));
				}
			}
		}
		projectUI.setContactPerson(getCurrentUser().getCompleteName());
		projectUI.setContactOnError(getCurrentUser().getEmail());
		IProject project = (IProject)ConfigManager.getInstance().getComponent(locator, IContext.defaultName, false);
		//add variables
		projectUI.getVariables().clear();
		Element variablesElement = element.getChild("variables");
		if (variablesElement != null) {
			List<?> variables = variablesElement.getChildren("variable");
			for (Object o : variables) {
				Element variable = (Element)o;
				Variable v = new Variable();
				v.setName(variable.getAttributeValue("name"));
				v.setValue(variable.getChildText("default"));
				v.setComment(variable.getChildText("comment"));
				projectUI.getVariables().add(v);
			}
		}
		getEntityManager().persist(projectUI);
		Set<Long> presentSet = new HashSet<Long>();
		for (IComponent c : project.getModelComponents()) {
			Locator l = c.getLocator();
			Component component = null;
			switch (ITypes.Managers.valueOf(l.getManager().toLowerCase())) {
			case connections: {
				component = findComponent(l.getName(),ComponentTypes.CONNECTION,projectUI.getId()); break;
			}
			case extracts: {
				component = findComponent(l.getName(),ComponentTypes.EXTRACT,projectUI.getId()); break;
			}
			case transforms: {
				component = findComponent(l.getName(),ComponentTypes.TRANSFORM,projectUI.getId()); break;
			}
			case loads: {
				component = findComponent(l.getName(),ComponentTypes.LOAD,projectUI.getId()); break;
			}
			case jobs: {
				component = findComponent(l.getName(),ComponentTypes.JOB,projectUI.getId()); break;
			}
			default:
				break;
			}
			if (component != null) {
				component.setName(l.getName());
				component.setProject(projectUI);
				component.setActive(true);
				Element e = ConfigManager.getInstance().findElement(c.getLocator());
				e.removeAttribute("modified");
				e.removeAttribute("modifiedBy");
				component.setXml(XMLUtil.jdomToString(e,Format.getPrettyFormat().setOmitDeclaration(true)));
				component.setType(e.getAttributeValue("type"));
				getEntityManager().persist(component);
				getEntityManager().flush();
				presentSet.add(component.getId());
				/*
				switch (component.getdType()) {
				case ComponentTypes.CONNECTION: projectUI.getConnections().add((Connection)component); break;
				case ComponentTypes.EXTRACT: projectUI.getExtracts().add((Extract)component); break;
				case ComponentTypes.TRANSFORM: projectUI.getTransforms().add((Transform)component); break;
				case ComponentTypes.LOAD: projectUI.getLoads().add((Load)component); break;
				case ComponentTypes.JOB: projectUI.getJobs().add((Job)component); break;
				}
				*/
			}
		}
		//remove components now present any more after import
		List<Component> allComponents = new ArrayList<Component>();
		allComponents.addAll(projectUI.getConnections());
		allComponents.addAll(projectUI.getExtracts());
		allComponents.addAll(projectUI.getTransforms());
		allComponents.addAll(projectUI.getLoads());
		allComponents.addAll(projectUI.getJobs());
		for (Component c : allComponents) {
			if (!presentSet.contains(c.getId())) {
				getEntityManager().remove(c);
				projectUI.getConnections().remove(c);
				projectUI.getExtracts().remove(c);
				projectUI.getTransforms().remove(c);
				projectUI.getLoads().remove(c);
				projectUI.getJobs().remove(c);
			}
		}
		getEntityManager().flush();
		setDirty();
		setDependentModulesDirty();
		//this.select(getInstance().getId());
		setInstance(getEntityManager().find(Project.class, projectUI.getId()));
		getEntityManager().refresh(getInstance());
		addToBackend();
		return projectUI;
	}
	
	private Project importInternal(String projectString) throws Exception {
		Element element = XMLUtil.stringTojdom(projectString);
		return importInternal(element);
	}
	
	public void importProject(FileUploadEvent event) {  
		importXML = event.getFile().getContents();
		if (importXML != null) {
			try {
				String str = new String(importXML, "UTF-8");
				Project projectUI = importInternal(str);
				addToBackend();
				xml = generateXML();
				this.addInfoMessage("Project "+projectUI.getName()+" imported / updated.");
				Redirect.instance().setViewId("/modules/project/edit.xhtml");
				Redirect.instance().setParameter("id", projectUI.getId());
				Redirect.instance().execute();
			} catch (UnsupportedEncodingException e) {
				error("Unsupported Encoding: "+e.getMessage());
			} catch (IOException e) {
				this.addErrorMessage(Messages.instance().get("etl.import.readError")+": "+e.getMessage());
			} catch (JDOMException e) {
				e.printStackTrace();
				this.addErrorMessage(Messages.instance().get("etl.import.parseError")+": "+e.getMessage());
			} catch (ConfigurationException e) {
				e.printStackTrace();
				this.addErrorMessage(Messages.instance().get("etl.xml.invalid")+": "+e.getMessage());
			}
			catch (Exception e) {
				e.printStackTrace();
				this.addErrorMessage(e.getMessage());
			}
			importXML = null;
		}
	}
	
/*
	
	public byte[] getUploadedFile() {
		return importXML;
	}

	public void setUploadedFile(byte[] uploadedFile) {
		this.importXML = uploadedFile;
	}
*/	
	
	private void initParameterEditComponent()
	{
		parameterEditComponent = new EditComponent<Variable>(getInstance().getVariables(), Variable.class,0,Integer.MAX_VALUE);
	}

	public IEditComponent<Variable> getParameterEditComponent()
	{
		return parameterEditComponent;
	}
	
	
	private String getType(Component component) {
		switch (component.getdType()) {
		case ComponentTypes.CONNECTION : return ITypes.Components.connection.toString();
		case ComponentTypes.EXTRACT: return ITypes.Components.extract.toString();
		case ComponentTypes.TRANSFORM: return ITypes.Components.transform.toString();
		case ComponentTypes.LOAD: return ITypes.Components.load.toString();
		case ComponentTypes.JOB: return ITypes.Components.job.toString();
		default: {
			error("Unknown component type cannot be tranlated to etl manager.");
			return null;
		}
		}
	}
	
	private void buildSubTree(DefaultTreeNode parent, List<? extends Component> components) {
		Map<String,DefaultTreeNode> typeMap = new HashMap<String,DefaultTreeNode>();
		for (Component c : components) {
			DefaultTreeNode type = typeMap.get(c.getType());
			if (type == null) {
				String componentType = c.getType();
				if (componentType == null) componentType = "Standard";
				type = new DefaultTreeNode(componentType,parent);
				typeMap.put(componentType, type);
			}
		}
		for (Component c : components) {
			DefaultTreeNode type = typeMap.get(c.getType());
			DefaultTreeNode item = new DefaultTreeNode(getType(c),c.getName(),type);
		}
	}
	
	private List<? extends Component> getOrderedComponentList(List<? extends Component> components) {
		List<Component> list = new ArrayList<Component>();
		list.addAll(components);
		Collections.sort(list);
		return list;
	}
	
	public DefaultTreeNode getComponentTree() {
		DefaultTreeNode projectItem = new DefaultTreeNode(getInstance().getName());
		projectItem.setExpanded(true);
		DefaultTreeNode connections = new DefaultTreeNode("Connections",projectItem);
		DefaultTreeNode extracts = new DefaultTreeNode("Extracts",projectItem);
		DefaultTreeNode transforms = new DefaultTreeNode("Transforms",projectItem);
		DefaultTreeNode loads = new DefaultTreeNode("Loads",projectItem);
		DefaultTreeNode jobs = new DefaultTreeNode("Jobs",projectItem);
		
		buildSubTree(connections,getOrderedComponentList(getInstance().getConnections()));
		buildSubTree(extracts,getOrderedComponentList(getInstance().getExtracts()));
		buildSubTree(transforms,getOrderedComponentList(getInstance().getTransforms()));
		buildSubTree(loads,getOrderedComponentList(getInstance().getLoads()));
		buildSubTree(jobs,getOrderedComponentList(getInstance().getJobs()));
		
		return projectItem;
	}
	
	private Component findComponentInList(List<? extends Component> list, String name) {
		for (Component c : list) {
			if (c.getName().equals(name)) return c;
		}
		return null;
	}
	
	public void onNodeExpand(NodeExpandEvent event) {
		event.getTreeNode().setExpanded(true);
	}
	
	public void onNodeCollapse(NodeCollapseEvent event) {
		event.getTreeNode().setExpanded(false);
	}
	
	public void onNodeSelect(NodeSelectEvent event) {
		  if (event.getTreeNode().getChildCount() == 0) {
			  Component component = null;
			  try {
				  switch (ITypes.Components.valueOf(event.getTreeNode().getType())) {
					case connection: component = findComponentInList(getInstance().getConnections(), event.getTreeNode().getData().toString()); break;
					case extract: component = findComponentInList(getInstance().getExtracts(), event.getTreeNode().getData().toString()); break;
					case transform: component = findComponentInList(getInstance().getTransforms(), event.getTreeNode().getData().toString()); break;
					case load: component = findComponentInList(getInstance().getLoads(), event.getTreeNode().getData().toString()); break;
					case job: component = findComponentInList(getInstance().getJobs(), event.getTreeNode().getData().toString()); break;
					default:break;
				  }
			  } catch (Exception e){};
			  if (component != null) {
				  String viewId = "/modules/"+event.getTreeNode().getType()+"/edit.xhtml";
				  Redirect.instance().setViewId(viewId);
				  Redirect.instance().setParameter("id", component.getId());
				  Redirect.instance().execute();
			  }			 
		  } else {
		    // Expand or Contract the Tree Node.
		    if (event.getTreeNode().isExpanded())
		         event.getTreeNode().setExpanded(false);
		    else 
		         event.getTreeNode().setExpanded(true);
		  }
	}
	
	public TreeNode getSelectedNode() {  
	    return selectedNode;  
	}  
	  
	public void setSelectedNode(TreeNode selectedNode) {  
	    this.selectedNode = selectedNode;  
	}  
	
	public void generateFlowGraph() {
		try {
			IProject project = ConfigManager.getInstance().getProject(getInstance().getName());
			List<Locator> invalids = ConfigManager.getInstance().initProjectComponents(project, IContext.defaultName, true);
			Properties graphProperties = new Properties();
			graphProperties.setProperty("viewType", "all");
			graphProperties.setProperty("orientation",String.valueOf(SwingConstants.NORTH));
			String application = Expressions.instance().createValueExpression("#{facesContext.externalContext.request.contextPath}").getValue().toString();
			graphProperties.setProperty("linkPrefix", application+"/resolvecomponent.seam?locator=");
			//graphProperties.setProperty("onClick", "Jedox.studio.etl.flowGraphClick('%')");
			//graphProperties.setProperty("onClick", "window.close()");
			Locator componentLocator = Locator.parse(getInstance().getName());
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
	
	@Override
	protected void initialize()
	{
		super.initialize();
	}
	
	public void validateName(FacesContext context, UIComponent component, Object obj) throws ValidatorException
	{
		String name = (String)obj;
		if(name != null)
		{
			name = name.toLowerCase().trim();
		}
		nameValidator = new ParameterizableUniqueValidator("from Project project where project.name is not null and lower(project.name) like :value", "Name not valid", "Project with this name already exists");
		nameValidator.validate(getInstance().getName(), context, component, name);
	}
	
	private Element addToBackend() throws Exception {
		Locator locator = new Locator().add(getInstance().getName());
		Element projectElement = getInstance().getProjectXML();
		ConfigManager.getInstance().removeElement(locator);
		ConfigManager.getInstance().getProjectManager().remove(getInstance().getName());
		ConfigManager.getInstance().add(locator, projectElement);
		return projectElement;
	}
	
	public String persist() {
		try {
			List<Variable> newVariables = new ArrayList<Variable>();
			newVariables.addAll(parameterEditComponent.getItems());
			getInstance().getVariables().clear();
			getInstance().getVariables().addAll(newVariables);
			Element projectElement = addToBackend();
			projectElement.removeAttribute("modified");
			projectElement.removeAttribute("modifiedBy");
			initialProjectName = getInstance().getName();
			xml = XMLUtil.jdomToString(projectElement);
			return super.persist();
		} catch (Exception e) {
			this.addErrorMessage(Messages.instance().get("etl.save.project.failed")+": "+e.getMessage());
		}
		return null;
	}
	
	protected void preRemove(Project instance) {
		super.setDirty();
		setDependentModulesDirty();
		try {
			ConfigManager.getInstance().removeElement(new Locator().add(instance.getName()));
		}
		catch (Exception e) {
			this.addErrorMessage(Messages.instance().get("etl.xml.delete.failed")+": "+e.getMessage());
		}
	}
	
	protected void setDependentModulesDirty() {
		ModuleManager.instance().getModuleForName(ITypes.Components.connection.toString()).setDirty(true);
		ModuleManager.instance().getModuleForName(ITypes.Components.extract.toString()).setDirty(true);
		ModuleManager.instance().getModuleForName(ITypes.Components.transform.toString()).setDirty(true);
		ModuleManager.instance().getModuleForName(ITypes.Components.load.toString()).setDirty(true);
		ModuleManager.instance().getModuleForName(ITypes.Components.job.toString()).setDirty(true);
		ModuleManager.instance().getModuleForName("execution").setDirty(true);
	}
	
	public void setDirty() {
		super.setDirty();
		try { //invalidate backend project deps calc.
			if (getInstance() != null && getInstance().getId() != null && getInstance().getName() != null) {
				IProject project = ConfigManager.getInstance().getProject(getInstance().getName());
				project.invalidate();
				project.setDirty();
				getEntityManager().refresh(getInstance());
			}
		}
		catch (Exception e) {};
		xml = null;
		//setDependentModulesDirty();
	}
	
	public void clearAll() {
		//super.clearAll();
		super.setDirty();
	}
	
	public void delete(Object object) {
		if (object instanceof Long) {
			Project instance = getEntityManager().find(Project.class, (Long)object);
			preRemove(instance);
			super.delete(object);
		}
	}
	
	public String remove() {
		preRemove(getInstance());
		return super.remove();
	}
	
	public String generateXML() {
		String xml = "";
		try {
			xml = XMLUtil.jdomToString(getInstance().getProjectXML());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return xml;
	}

	public String getXml() {
		if (xml == null) {
			xml = generateXML();
		}
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}
	
	public void updateXML() {
		try {
			Project projectUI = importInternal(xml);
			xml = generateXML();
			this.addInfoMessage("Project "+projectUI.getName()+" imported / updated.");
			Redirect.instance().setViewId("/modules/project/edit.xhtml");
			Redirect.instance().setParameter("id", projectUI.getId());
			Redirect.instance().execute();
		} catch (UnsupportedEncodingException e) {
			error("Unsupported Encoding: "+e.getMessage());
		} catch (IOException e) {
			this.addErrorMessage(Messages.instance().get("etl.import.readError")+": "+e.getMessage());
		} catch (JDOMException e) {
			this.addErrorMessage(Messages.instance().get("etl.import.parseError")+": "+e.getMessage());
		} catch (ConfigurationException e) {
			e.printStackTrace();
			this.addErrorMessage(Messages.instance().get("etl.xml.invalid")+": "+e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
			this.addErrorMessage(e.getMessage());
		}
			
	}
	
	protected Set<String> getDeactivateFieldNames()
	{
		Set<String> names = new HashSet<String>();
		names.add(ITypes.Connections);
		names.add(ITypes.Extracts);
		names.add(ITypes.Transforms);
		names.add(ITypes.Loads);
		names.add(ITypes.Jobs);
		setDependentModulesDirty();
		return names;
	}
	
	protected Set<String> getActivateFieldNames() {
		return getDeactivateFieldNames();
	}
	
	

}
