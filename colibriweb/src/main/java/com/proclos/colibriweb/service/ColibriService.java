package com.proclos.colibriweb.service;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.security.Identity;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.FileOutputStream;
import java.io.File;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.persistence.EntityManager;

import org.jdom.Element;
import org.jdom.output.Format;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.log4j.Level;

import at.adaptive.components.hibernate.CriteriaWrapper;

import com.jedox.etl.core.component.ComponentDescriptor;
import com.jedox.etl.core.component.ComponentFactory;
import com.jedox.etl.core.component.ConfigurationException;
import com.jedox.etl.core.component.ITypes;
import com.jedox.etl.core.component.Locator;
import com.jedox.etl.core.component.RuntimeException;
import com.jedox.etl.core.config.Settings;
import com.jedox.etl.core.config.ConfigConverter;
import com.jedox.etl.core.config.ConfigManager;
import com.jedox.etl.core.config.ConfigValidator;
import com.jedox.etl.core.context.IContext;
import com.jedox.etl.core.execution.Execution;
import com.jedox.etl.core.execution.ExecutionException;
import com.jedox.etl.core.execution.ExecutionState;
import com.jedox.etl.core.execution.ResultCodes;
import com.jedox.etl.core.execution.Executor;
import com.jedox.etl.core.execution.ResultCodes.Codes;
import com.jedox.etl.core.execution.ResultCodes.DataTargets;
import com.jedox.etl.core.node.IColumn;
import com.jedox.etl.core.node.Row;
import com.jedox.etl.core.persistence.Datastore;
import com.jedox.etl.core.persistence.DatastoreManager;
import com.jedox.etl.core.project.IProject;
import com.jedox.etl.core.source.ITreeSource;
import com.jedox.etl.core.source.IView.Views;
import com.jedox.etl.core.source.processor.IProcessor;
import com.jedox.etl.core.source.IView;
import com.jedox.etl.core.util.ClassUtil;
import com.jedox.etl.core.util.XMLUtil;
import com.jedox.etl.core.util.NamingUtil;
import com.jedox.etl.core.util.docu.DocuUtil;
import com.jedox.etl.core.util.svg.GraphManager;
import com.jedox.etl.core.writer.CSVWriter;
import com.proclos.colibriweb.common.ContextProperties;
import com.proclos.colibriweb.entity.component.Component;
import com.proclos.colibriweb.entity.component.ComponentTypes;
import com.proclos.colibriweb.entity.component.Project;
import com.proclos.colibriweb.entity.user.ColibriUser;
import com.proclos.colibriweb.session.modules.component.ComponentModule;
import com.proclos.colibriweb.session.modules.component.ProjectModule;
import com.proclos.colibriweb.session.system.AccessUtil;
import com.proclos.colibriweb.session.system.ModuleManager;
import com.proclos.colibriweb.session.system.TaskController;

@Name("ETLServer")
@Scope(ScopeType.STATELESS)  
@WebService(name = "ETLServer", targetNamespace="http://ns.proclos.com", serviceName="ETLServer", endpointInterface = "com.proclos.colibriweb.service.ETLService")  
public class ColibriService implements ETLService {

	
	@In
	private EntityManager entityManager;
	
	@In
	private Identity identity;

	
	private static Log log = LogFactory.getLog(ColibriService.class);
   	
   	
   	//******************* P R I V A T E  M E T H O D S ********************************

   	private Properties getProperties(Variable[] variables) {
   		Properties properties = new Properties();
   		if (variables != null)
   			for (Variable v : variables) {
   				if ((v.getName() != null) && (v.getValue() != null))
   					properties.setProperty(v.getName(), v.getValue());
   			}
   		return properties;
   	}

   	private String getNamesAsString(String[] names) {
   		if (names == null) return "";
   		StringBuffer b = new StringBuffer();
   		for (String name: names) {
   			b.append(name+" ");
   		}
   		return b.toString();
   	}

   	private String getPrintable(String locator) {
   		if (locator == null) return "projects";
   		return locator;
   	}

   	private String getPrintable(String[] values, String defvalue) {
   		StringBuffer result = new StringBuffer();
   		if (values == null) return "";
   		for (String value : values) {
   			if (value == null)
   				result.append(defvalue);
   			else
   				result.append(value+" ");
   		}
   		return result.toString();
   	}	
   	
   	private String getPrintable(String[] locators) {
   		return getPrintable(locators,"projects ");
   	}

   	private void logDescriptor(ResultDescriptor d, boolean errorsOnly) {
   		if (d.getValid()) {
   			if (!errorsOnly && d.getResult()!=null)
   				log.info(d.getResult());
   		}
   		else
   			log.error(d.getErrorMessage());
   	}

   	private void logDescriptor(ExecutionDescriptor d, boolean errorsOnly) {
   		if (d.getValid()) {
   			if (!errorsOnly && d.getResult()!=null)
   				log.info(d.getResult());
   		}
   		else
   			log.error(d.getErrorMessage());
   	}
   	
   	
   	private ComponentOutputDescriptor[] getOutputDescription(Row row) {
   		if (row != null) {
   			ArrayList<ComponentOutputDescriptor> result = new ArrayList<ComponentOutputDescriptor>();
   			for (IColumn column : row.getColumns()) {
   				ComponentOutputDescriptor d = new ComponentOutputDescriptor();
   				d.setName(column.getName());
   				d.setPosition(row.indexOf(column)+1);
   				d.setType(column.getValueType().getCanonicalName());
   				d.setOriginalName((column.getAliasElement() != null) ? column.getAliasElement().getOrigin() : column.getName());
   				result.add(d);
   			}
   			return result.toArray(new ComponentOutputDescriptor[result.size()]);
   		}
   		return null;
   	}

   	private ExecutionDescriptor getDescriptor(ExecutionState state) {
   		ExecutionDescriptor d = new ExecutionDescriptor();
   		d.setId(state.getId());
   		d.setProject(state.getProject());
   		d.setType(state.getType());
   		d.setName(state.getName());
   		d.setExecutionType(state.getExecutionType().toString());
   		d.setErrors(state.getErrors());
   		d.setWarnings(state.getWarnings());
   		d.setStartDate((state.getStartDate() == null) ? 0 : state.getStartDate().getTime());
   		d.setStopDate((state.getStopDate() == null) ? 0 : state.getStopDate().getTime());
   		d.setStatus(state.getString(state.getStatus()));
   		d.setStatusCode(state.getNumeric(state.getStatus()));
   		d.setErrorMessage(state.getFirstErrorMessage());
   		d.setMetadata(getOutputDescription(state.getMetadata()));
   		d.setUserName(state.getUserName());
   		switch (state.getDataTarget()) {
   		case CSV_INLINE: 
   		case XML_INLINE: 
   		case CSV_PERSISTENCE: 
   			d.setResult(state.getData()); break;
   		default: d.setResult(null);
   		}
   		if (!Codes.statusInvalid.equals(state.getStatus()))
   			d.setValid(true);
   		return d;
   	}

   	private ExecutionDescriptor getErrorDescriptor(String message) {
   		log.error(message);
   		ExecutionDescriptor d = new ExecutionDescriptor();
   		d.setErrorMessage(message);
   		d.setErrors(1);
   		ResultCodes codes = new ResultCodes();
   		d.setStatus(codes.getString(Codes.statusFailed));
   		d.setStatusCode(codes.getNumeric(Codes.statusFailed));
   		return d;
   	}

   	private ComponentDependencyDescriptor getErrorDependencyDescriptor(String locator, String message) {
   		log.error(message);
   		ComponentDependencyDescriptor notValidCD= new ComponentDependencyDescriptor();
   		notValidCD.setName(locator);
   		notValidCD.setComponents(new String[0]);
   		notValidCD.setErrorMessage(message);
   		notValidCD.setValid(false);
   		return notValidCD;
   	}	
   

   	/**
   	 * needed for data preview methods
   	 * @param offset starting from 1
   	 * @return real offset starting from 0
   	 * @throws Fault
   	 */
   	private int handleOffset(int offset) throws Fault {
   		if(offset<0)
   			throw new Fault(new Exception("Minimum value of offset is 1."));
   		if(offset>0)
   			offset--;
   		return offset;
   	}
   	
   	
   	private ColibriUser getCurrentUser()
	{
		try
		{
			return (ColibriUser)Contexts.getSessionContext().get(ContextProperties.USER);
		}
		catch(Exception e)
		{
			return null;
		}
	}
   	
   	private Project verifyProject(String name, boolean requireExisting) throws RuntimeException, ConfigurationException {
   		//check if project is there and user is authorized
		CriteriaWrapper criteria = createCriteria(entityManager, Project.class);
		criteria.addCriterion(Restrictions.eq("name", name));
		Project project = (Project)criteria.createCriteria().uniqueResult();
		if (project != null) {
			if (!AccessUtil.hasAccess(getCurrentUser(), project)) throw new RuntimeException("Not authorized");
			try {
	   			ConfigManager.getInstance().getComponent(Locator.parse(name), IContext.defaultName, true);
	   		}
	   		catch (ConfigurationException e) {
	   			//project not found in ConfigManager - load it
	   			ConfigManager.getInstance().add(Locator.parse(name), project.getProjectXML());
	   		}
		} else {
			if (requireExisting) throw new RuntimeException("Project "+name+" not found.");
		}
		return project;
   	}
   	
   	private List<Integer> getDTypesForLocator(Locator locator) {
   		List<Integer> result = new ArrayList<Integer>();
   		switch (ITypes.Managers.valueOf(locator.getManager())) {
   		case connections: result.add(ComponentTypes.CONNECTION); break;
   		case extracts: result.add(ComponentTypes.EXTRACT); break;
   		case transforms: result.add(ComponentTypes.TRANSFORM); break;
   		case loads: result.add(ComponentTypes.LOAD); break;
   		case jobs: result.add(ComponentTypes.JOB); break;
   		case sources: result.add(ComponentTypes.EXTRACT); result.add(ComponentTypes.TRANSFORM); break;
		default:
			break;
   		};
   		return result;
   	}
   	
   	private Component verifyComponent(Locator locator, boolean requireExisting) throws ConfigurationException, RuntimeException {
   		CriteriaWrapper criteria = createCriteria(entityManager, Component.class);
   		criteria.addCriterion("project", Restrictions.eq("name", locator.getRootName()));
   		criteria.addCriterion(Restrictions.eq("name", locator.getName()));
   		criteria.addCriterion(Restrictions.in("dType", getDTypesForLocator(locator)));
   		Component component = (Component)criteria.createCriteria().uniqueResult();
   		if (component != null) {
   			if (!AccessUtil.hasAccess(getCurrentUser(), component)) throw new RuntimeException("Not authorized");
   			try {
	   			ConfigManager.getInstance().getComponent(locator, IContext.defaultName, true);
	   		}
	   		catch (ConfigurationException e) {
	   			//project component not found in ConfigManager - load full project
	   			ConfigManager.getInstance().add(locator.getRootLocator(), component.getProject().getProjectXML());
	   		}
		} else {
			if (requireExisting) throw new RuntimeException("Component "+locator.toString()+" not found.");
		}
   		return component;
   	}
   	
   	private CriteriaWrapper createCriteria(EntityManager entityManager, Class<?> clazz) {
   		Session session = (Session) entityManager.getDelegate();
   		CriteriaWrapper criteria = new CriteriaWrapper(session,clazz);
   		return criteria;
   	}
   	
   	@SuppressWarnings("unchecked")
	private List<String> getProjectNames() throws Fault {
   		CriteriaWrapper criteria = createCriteria(entityManager, Project.class);
   		AccessUtil.appendAccessCriterion(getCurrentUser(), criteria);
		return (List<String>)criteria.createCriteria().setProjection(Projections.property("name")).list();
   	}	
   	
   
    @WebMethod
    public boolean login(String username, String password)

    {
    /*
      authenticator.getCredentials().setUsername(username);
      authenticator.getCredentials().setPassword(password);
      boolean success = authenticator.authenticate(false);
      */
 	  identity.getCredentials().setUsername(username);
 	  identity.getCredentials().setPassword(password);
      identity.login();
      boolean success = identity.isLoggedIn();
       
      if (success) {
    	  return true;
      }
      log.error("User "+username+" login failed!");
      return false;
    }
    
    @WebMethod  
    public boolean logout() {
    	identity.logout();
    	return !identity.isLoggedIn();
    }
   	
   	// *********************** G E T  M E T H O D E S ********************


      	/**
   	 * gets the configuration of a set of components as XML.
   	 * @param locators an array of paths to the components in the format {component.manager.}component: e.g: "myProject.connections.myConnection".
   	 * @return an array of {@link ResultDescriptor ResultDescriptors}. Result contains the XML configuration of the component.
   	 */
    @WebMethod  
    @Restrict("#{identity.isLoggedIn()}")
   	public ResultDescriptor[] getComponentConfigs(String[] locators) {
   		log.debug("getting component configs for: "+getNamesAsString(locators));
   		List<String> allowedProjects = null;
   		try {
   			allowedProjects = getProjectNames();
   		} catch (Exception e1) {
   			ResultDescriptor desc = new ResultDescriptor();
   			desc.setValid(false);
   			desc.setErrorMessage(e1.getMessage());
   			return new ResultDescriptor[]{desc};
   		}
   		if (locators==null || locators.length==0) {
   			log.debug("number of projects: "+allowedProjects.size());
   			locators = allowedProjects.toArray(new String[0]);
   		}
   					
   		ArrayList<ResultDescriptor> results = new ArrayList<ResultDescriptor>();
   		for (String locator : locators) {
   			ResultDescriptor d = new ResultDescriptor();
   			try {
   				Locator loc = Locator.parse(locator);
   				if(!allowedProjects.contains(loc.getRootName()))
   					throw new Exception("The project " + loc.getRootName() + " does not exist or the user has no sufficient rights.");
   				verifyProject(loc.getRootName(),true);
   				d.setResult((ConfigManager.getInstance().getConfigurationString(loc)));
   			}
   			catch (Exception e) {
   				d.setErrorMessage(e.getMessage());
   			}
   			results.add(d);
   			logDescriptor(d,true);
   		}
   		log.debug("finished");
   		return results.toArray(new ResultDescriptor[results.size()]);
   	}

   	/**
   	 * gets the names of all children hosted by the component or manager denoted by the locator
   	 * @param locator the path to a component or manager: e.g: "myProject.connections", "myProject.connections.myConnection". If null the root project manager is selected.
   	 * @return an array of names
   	 * @throws Fault
   	 */
    @WebMethod  
    @Restrict("#{identity.isLoggedIn()}")
   	public String[] getNames(String locator) throws Fault {
   		log.debug("getting locateable names for: "+getPrintable(locator));
   		try {
   			Locator loc = Locator.parse(locator);
   			List<String> names=null;
   			if(loc.isEmpty()) {
   				names = getProjectNames();
   			} 
   			else {
   				verifyProject(loc.getRootName(),true);
   				names = ConfigManager.getInstance().getNames(loc);
   			}
   			log.debug("finished");
   			return names.toArray(new String[0]);
   		}
   		catch (Exception e) {
   			throw new Fault(new Exception("Failed to execute method getNames for "+getPrintable(locator)+": "+e.getMessage()));
   		}
   	}

   	/**
   	 * gets the locators of all children hosted by the component or manager denoted by the locator
   	 * @param locator the path to a component or manager: e.g: "myProject.connections", "myProject.connections.myConnection". If null the root project manager is selected.
   	 * @return an array of locators
   	 * @throws Fault
   	 */
    @WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public String[] getLocators(String locator) throws Fault {
   		log.debug("getting children component locators for: "+getPrintable(locator));
   		try {
   			String[] names = getNames(locator);
   			String[] locators = new String[names.length];
   			for (int i=0; i<names.length; i++) {
   				locators[i] = Locator.parse(locator).add(names[i]).toString();
   			}
   			log.debug("finished");
   			return locators;
   		}
   		catch (Exception e) {
   			throw new Fault(new Exception("Failed to execute method getLocators for "+getPrintable(locator)+": "+e.getMessage()));
   		}
   	}

   	/**
   	 * gets the component scopes (connections,extracts,transforms,loads,jobs,projects)
   	 * @return the array of the scopes
   	 */
    @WebMethod
   	public String[] getScopes() {
   		log.debug("getting scopes");
   		ArrayList<String> scopes = new ArrayList<String>();
   		for (ITypes.Components c : ITypes.Components.values()) {
   			scopes.add(c.toString()+"s");
   		}
   		log.debug("finished");
   		return scopes.toArray(new String[scopes.size()]);
   	}

   	/**
   	 * gets the types registered for the given component scope
   	 * @param scope the scope type name as registered in the component.xml (e.g. connections). see {@link #getScopes()}
   	 * @param name the name of the component as registered in the component.xml (e.g. File) see {@link #getScopes()}
   	 * @return the array of {#link ComponentTypeDescriptor ComponentTypeDescriptors} registered for this scope
   	 * @throws Fault
   	 */
    @WebMethod
   	public ComponentTypeDescriptor[] getComponentTypes(String scope,String name) throws Fault {
   		log.debug("getting component types descriptors for scope " + scope);
   		ArrayList<ComponentTypeDescriptor> result = new ArrayList<ComponentTypeDescriptor>();
   		List<ComponentDescriptor> descriptors = new ArrayList<ComponentDescriptor>();
   		if (name==null) {
   			 descriptors = ComponentFactory.getInstance().getComponentDescriptorsSorted(scope);
   		}	 
   		else {
   			ComponentDescriptor descriptor=ComponentFactory.getInstance().getComponentDescriptor(name, scope);
   			if (descriptor!=null)
   				descriptors.add(descriptor);
   		}
   		for (ComponentDescriptor d : descriptors) {
   			ComponentTypeDescriptor ce = new ComponentTypeDescriptor();
   			ce.setScope(scope);
   			ce.setType(d.getName());
   			ce.setClassname(d.getClassName());
   			ce.setSchema(ConfigValidator.getInstance().getSchema(d.getClassName()));
   			ce.setCaption(d.getCaption());
   			try {
   				ce.setTree(ClassUtil.implementsInterface(Class.forName(d.getClassName()), ITreeSource.class));
   			} catch (ClassNotFoundException e) {
   				log.error("Class not found: "+e.getMessage());
   			}
   			ce.setAllowedConnectionTypes(d.getAllowedConnectionTypes());
   			int paramSize = d.getParameters().size();
   			String[] paramNames = new String[paramSize];
   			String[] paramValues = new String[paramSize];
   			Iterator<Object> keyIter = d.getParameters().keySet().iterator();
   			for(int i=0;i<d.getParameters().size();i++){
   				String keyName = keyIter.next().toString();
   				paramNames[i]= keyName;
   				paramValues[i]=d.getParameters().getProperty(keyName);
   			}
   			ce.setParametersNames(paramNames);
   			ce.setParametersValues(paramValues);
   			result.add(ce);
   		}
   		log.debug("finished");
   		return result.toArray(new ComponentTypeDescriptor[result.size()]);
   	}

   	/**
   	 * validates component configs. if a config for a component is given, validation is done with respect to that config. Else it is assumed, that the component already exists and validation is done with respect to its existing config.
   	 * @param locators the paths of the components to validate in the format {component.manager.}component: e.g: "myProject.connections.myConnection".
   	 * @param configs the configs for some components (optional).
   	 * @return an array of {@link ResultDescriptor ResultDescriptors}, one per component. Result is "OK", if validation was successful.
   	 * @throws Fault
   	 */
    @WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ResultDescriptor[] validateComponents(String[] locators, String[] configs) throws Fault {
   		log.debug("validating component configs "+getNamesAsString(locators));
   		try {
   			ArrayList<ResultDescriptor> results = new ArrayList<ResultDescriptor>();

   			for (int i=0; i<locators.length; i++) {
   				ResultDescriptor d = new ResultDescriptor();
   				if (configs == null || i >= configs.length || configs[i] == null ) { //assume that components are present
   					try {
   						Locator loc = Locator.parse(locators[i]);
   						verifyProject(loc.getRootName(),true);
   						ConfigManager.getInstance().validate(loc);
   						d.setResult("OK");
   					}
   					catch (Exception e) {
   						d.setErrorMessage("Failed: "+e.getMessage());
   					}
   				}
   				else { //add temporary config
   					String locatorString = locators[i];
   					Locator loc = Locator.parse(locatorString);
   					String tempProjectName = NamingUtil.internal(loc.getRootName()+String.valueOf(System.nanoTime()));
   					verifyProject(loc.getRootName(),false);
   					ConfigManager.getInstance().copyProject(loc.getName(), tempProjectName);
   					String oldRootName = loc.getRootName();
   					loc.setRootName(tempProjectName);
   					Element config = XMLUtil.stringTojdom(configs[i]);
   					if (loc.isRoot() || loc.isEmpty()) {//if locator denotes a project.  set temp name in config.
   						config.setAttribute("name",tempProjectName);
   					}
   					ConfigManager.getInstance().add(loc, config);
   					try {
   						ConfigManager.getInstance().validate(loc);
   						d.setResult("OK");
   					}
   					catch (Exception e) {
   						String errorMessage = e.getMessage().replaceAll(tempProjectName,oldRootName);
   						d.setErrorMessage("Failed: "+ errorMessage);
   					}
   					finally {
   						ConfigManager.getInstance().removeElement(loc.getRootLocator());
   					}
   				}
   				results.add(d);
   				logDescriptor(d,true);
   			}
   			log.debug("finished");
   			return results.toArray(new ResultDescriptor[results.size()]);
   		}
   		catch (Exception e) {
   			throw new Fault(new Exception("Failed to validate "+getPrintable(locators)+": "+e.getMessage()));
   		}
   	}

   	/**
   	 * migrates component configs to actual format
   	 * @param configs the configs for some components to migrate
   	 * @return an array of {@link ResultDescriptor ResultDescriptors}. Result is the migrated configuration, if successful.
   	 * @throws Fault
   	 */
    @WebMethod
   	public ResultDescriptor[] migrateComponents(String[] configs) throws Fault {
   		log.debug("migrating component configs...");
   		try {
   			ArrayList<ResultDescriptor> results = new ArrayList<ResultDescriptor>();
   			for (int i=0; i<configs.length; i++) {
   				ResultDescriptor d = new ResultDescriptor();
   				try { 
   					String result = XMLUtil.jdomToString(new ConfigConverter().convert(XMLUtil.stringTojdom(configs[i]),null,null));
   					d.setResult(result);
   				}
   				catch (Exception e) {
   					d.setErrorMessage("Failed: "+e.getMessage());
   				}
   				results.add(d);
   				logDescriptor(d,true);
   			}
   			log.debug("finished");
   			return results.toArray(new ResultDescriptor[results.size()]);
   		}
   		catch (Exception e) {
   			throw new Fault(new Exception("Failed to migrate: "+e.getMessage()));
   		}
   	}
   	
    @WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ResultDescriptor[] updateComponents(String[] locators, String[] configs) throws Fault {
   		log.debug("checking components for update...");
   		if (locators == null) locators = new String[configs.length];
   		if (locators.length != configs.length) {
   			throw new Fault(new Exception("Number of locators has to be identical to number of configs given."));
   		}
   		ArrayList<ResultDescriptor> results = new ArrayList<ResultDescriptor>();
   		boolean conflict = false;
   		for (int i=0; i<locators.length; i++) {
   			String locator = locators[i];
   			String config = configs[i];
   			ResultDescriptor d = new ResultDescriptor();
   			results.add(d);
   			d.setErrorMessage("Newer component exists on server.");
   			d.setResult("not added");
   			try {
   				Element element = XMLUtil.stringTojdom(config);
   				Locator loc = Locator.parse(locator);
   				if (loc.isEmpty()) {
   					//if locator is empty get correct name from config.
   					loc = Locator.parse(element.getAttributeValue("name"));
   					locator = loc.toString();
   				} 
   				else  { //set locator name in config.
   					element.setAttribute("name", loc.getName());
   				}
   				Date existingTimestamp = null;
   				Project project = verifyProject(loc.getRootName(),false);
   				if (project != null) {
	   				if (loc.isRoot()) {
	   					existingTimestamp = project.getModificationDate();
	   				} else {
	   					Component existing = verifyComponent(loc,false);
	   					if (existing != null) existingTimestamp = existing.getModificationDate();
	   				}
   				}
   				if (existingTimestamp != null) {
   					String updateTimestamp = element.getAttributeValue("modified","0");
   					if (existingTimestamp.getTime() > Long.parseLong(updateTimestamp)) {
   						// More recent version of component available 
   						conflict = true;
   						d.setResult("conflict");
   					}
   				}
   			}
   			catch (Exception e) {
   				d.setErrorMessage("Failed to add "+locator+": "+e.getMessage());
   			}
   		}
   		if (conflict)
   			return results.toArray(new ResultDescriptor[results.size()]); 
   		else 			
   			return addComponents(locators,configs);
   	}
   	

   	/**
   	 * adds components to a given manger
   	 * @param locators the paths of the new components in the format {component.manager.}component: e.g: "myProject.connections.myConnection". If null the root project manager is selected.
   	 * @param configs the configuration XMLs of the components to add.
   	 * @return an array of {@link ResultDescriptor ResultDescriptors}. Result is "added component [locator]" or "replaced component [locator]", if successful.
   	 * @throws Fault
   	 */
    @WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ResultDescriptor[] addComponents(String[] locators, String[] configs) throws Fault {
   		log.debug("adding components "+getNamesAsString(locators));
   		if (locators == null) locators = new String[configs.length];
   		if (locators.length != configs.length) {
   			throw new Fault(new Exception("Number of locators has to be identical to number of configs given."));
   		}
   		ArrayList<ResultDescriptor> results = new ArrayList<ResultDescriptor>();
   		for (int i=0; i<locators.length; i++) {
   			String locator = locators[i];
   			String config = configs[i];
   			ResultDescriptor d = new ResultDescriptor();
   			try {
   				entityManager.getTransaction().begin();
   				Element element = XMLUtil.stringTojdom(config);
   				Locator loc = Locator.parse(locator);
   				if (element.getChild("project")!=null )
   					// Avoid adding of repository.xml
   					throw new Exception("File is not a valid ETL-project, it can not be added.");					
   				if (loc.isEmpty()) {
   					//if locator is empty get correct name from config.
   					loc = Locator.parse(element.getAttributeValue("name"));
   					locator = loc.toString();
   				} 
   				else  { //set locator name in config.
   					element.setAttribute("name", loc.getName());
   				}
   				Project project = verifyProject(loc.getRootName(),false);
   				if (loc.isRoot()) {
   					ProjectModule projectModule = (ProjectModule)ModuleManager.instance().getModuleForName("project");
   					projectModule.importInternal(element);
   					d.setResult(project == null ? "added component " : "replaced component " + locator);
				} else if (project != null) {
					
   					Component component = verifyComponent(loc, false);
   					if (component != null) {
   						component.setXml(XMLUtil.jdomToString(element, Format.getPrettyFormat().setOmitDeclaration(true)));
   						d.setResult("replaced component "+locator);
   					} else {
   	   					ProjectModule projectModule = (ProjectModule)ModuleManager.instance().getModuleForName("project");
   						component = projectModule.createComponent(loc.getName(), getDTypesForLocator(loc).get(0));
   						component.setProject(project);
   						entityManager.persist(component);
   						d.setResult("added component "+locator);
   					}
   				}
   				entityManager.getTransaction().commit();
   			}
   			catch (Exception e) {
   				entityManager.getTransaction().rollback();
   				d.setErrorMessage("Failed to add "+locator+": "+e.getMessage());
   			}
   			results.add(d);
   			logDescriptor(d,false);
   		}
   		log.debug("finished");
   		return results.toArray(new ResultDescriptor[results.size()]);
   	}
   	
   	/**
   	 * rename components
   	 * @param locators the paths of the new components in the format {component.manager.}component: e.g: "myProject.connections.myConnection". If null the root project manager is selected.
   	 * @param newNames the new names of the component.
   	 * @return an array of {@link ResultDescriptor ResultDescriptors}. Result is "added component [locator]" or "replaced component [locator]", if successful.
   	 * @throws Fault
   	 */
    @WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ResultDescriptor[] renameComponents(String[] locators, String[] newNames, boolean updateReferences) throws Fault {
   		log.debug("renaming components "+getNamesAsString(locators));
   		if (locators.length != newNames.length) {
   			throw new Fault(new Exception("Number of locators has to be identical to number of new names given."));
   		}
   		ArrayList<ResultDescriptor> results = new ArrayList<ResultDescriptor>();
   		for (int i=0; i<locators.length; i++) {
   			String locator = locators[i];
   			String newName = newNames[i];
   			ResultDescriptor d = new ResultDescriptor();
   			try {
   				entityManager.getTransaction().begin();
   				if(newName!=null && (newName.isEmpty() || newName.contains(".")))
   					throw new RuntimeException("newName \"" + newName + "\" is not a valid value for the new component name.");

   				Locator loc = Locator.parse(locator);	
   				if (loc.isEmpty()) {
   					throw new RuntimeException("Locator can not be empty.");
   				}
   				Element element = ConfigManager.getInstance().get(loc);
   				if (element==null)
   					throw new ConfigurationException("Locator " + loc + " does not exist.");			
   				if (loc.isRoot()) {
   					Project project = verifyProject(loc.getRootName(),true);
   					if(verifyProject(newName,false)!=null)
   						throw new ConfigurationException("A project with name " + newName + " already exists.");
   					project.setName(newName);
   				} else {
   					//Locator newloc = Locator.parse(loc.getRootName() + "." + loc.getManager() + "." + newName);
   					Locator newloc = loc.clone().reduce().add(newName);
   					if (loc.getManager().equals(ITypes.Managers.extracts.toString()) || loc.getManager().equals(ITypes.Managers.transforms.toString())) newloc.setManager(ITypes.Managers.sources.toString());
   					Component component = verifyComponent(loc,true);
   					if(verifyComponent(newloc,false)!=null)
   						throw new ConfigurationException("A component with name " + newName + " already exists.");
   					String type = loc.getManager().substring(0,loc.getManager().length()-1);
   	   				@SuppressWarnings("unchecked")
					ComponentModule<? extends Component> module = (ComponentModule<? extends Component>)ModuleManager.instance().getModuleForName(type);
   					module.select(component.getId());
   					module.rename(newName, updateReferences, component.getProject());
   				}
   				d.setResult("renamed component "+locator+" to "+newName);
   			}
   			catch (Exception e) {
   				entityManager.getTransaction().rollback();
   				d.setErrorMessage("Failed to rename "+locator+": "+e.getMessage());
   			}
   			results.add(d);
   			logDescriptor(d,false);
			entityManager.getTransaction().commit();
   		}
   		log.debug("finished");
   		return results.toArray(new ResultDescriptor[results.size()]);
   	}

   	/**
   	 * removes components
   	 * @param locators the paths to the components in the format {component.manager.}component: e.g: "myProject.connections.myConnection".
   	 * @return an array of {@link ResultDescriptor ResultDescriptors}. Result is "removed component [locator]", if successful.
   	 * @throws Fault
   	 */
    @WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ResultDescriptor[] removeComponents(String[] locators) throws Fault {
   		log.debug("removing components "+getNamesAsString(locators));
   		try {
   			entityManager.getTransaction().begin();
   			ArrayList<ResultDescriptor> results = new ArrayList<ResultDescriptor>();

   			for (String locator: locators) {
   				ResultDescriptor d = new ResultDescriptor();
   				Locator loc = Locator.parse(locator);
   				Project project = verifyProject(loc.getRootName(),false);
   				if (project != null) {
   					if (loc.isRoot()) {
   						entityManager.remove(project);
   						d.setResult("removed component "+locator);
   					} else {
   						Component component = verifyComponent(loc,false);
   						if (component != null) {
   							entityManager.remove(component);
   	   						d.setResult("removed component "+locator);
   						} else {
   		   					d.setErrorMessage(locator +" does not exist.");
   						}
   					}
   				} else {
	   				d.setErrorMessage(locator +" does not exist.");
				}
   				results.add(d);
   				logDescriptor(d,false);
   			}
   			log.debug("finished");
   			entityManager.getTransaction().commit();
   			return results.toArray(new ResultDescriptor[results.size()]);
   		}
   		catch (Exception e) {
   			entityManager.getTransaction().rollback();
   			throw new Fault(new Exception("Failed to remove components for "+getPrintable(locators)+": "+e.getMessage()));
   		}
   	}


   	/**
   	 * get the metadata for a connection component. This can be applied to relational connections, file connections and connections of type Palo.
   	 * In "settings" a selector has to be specified which defines which kind of metadata information should be retrieved in the result (e.g. catalogs of a relational connection).
   	 * Beside the selector, filter criteria can be given in the settings array. The possible values of the selector and the filter criteria depend on the connection type. 
   	 * For a list of possible values for each connection type see the ETL Server Manual.  
   	 * @param locator the path to the connection components in the format {component.manager.}component: e.g: "myProject.connections.myPaloConnection".
   	 * @param settings variables including selector and filter criteria. Selector is obligatory, filters are optional. 
   	 * @param variables the variables forming the context of the execution in the form: name=value
   	 * @return
   	 */
    @WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ExecutionDescriptor getMetadata(String locator, Variable[] settings, Variable[] variables) {
   		log.debug("getting metadata for "+getPrintable(locator));			
   		Properties properties = getProperties(variables);
   		Properties internalSettings = getProperties(settings);
   		try {
   			Locator loc = Locator.parse(locator);
   			
   			verifyComponent(loc,true);
   			Execution e = Executor.getInstance().createMetadata(loc, properties, internalSettings);
   			e.getExecutionState().setDataTarget(DataTargets.CSV_INLINE);
   			Executor.getInstance().addExecution(e);
   			Executor.getInstance().runExecution(e.getKey());
   			ExecutionDescriptor d = getDescriptor(Executor.getInstance().getExecutionState(e.getKey(), true));
   			log.debug("finished");
   			return d;
   		}
   		catch (Exception e) {
   			return getErrorDescriptor("Failed to retrieve data for "+getPrintable(locator)+": "+e.getMessage());
   		}
   	}

   	/**
   	 * Uploads a file to the internal data repository.
   	 * @param name the name of the file to write to
   	 * @param data the data to write into the file.
   	 * @return a {@link ResultDescriptor}. Result is "added file [name]" or "replaced file [name]" if successful.
   	 */
    @WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ResultDescriptor uploadFile(String name, byte[] data) {
   		log.debug("Upload data file " + name);
   		ResultDescriptor d = new ResultDescriptor();
   		String dir = Settings.getInstance().getDataDir();
   		File file = new File(dir+File.separator+name);
   		try {
   			if (file.exists())
   				d.setResult("replaced file "+name);
   			else
   				d.setResult("added file "+name);
   			FileOutputStream stream = new FileOutputStream(file);
   			stream.write(data);
   			stream.close();
   		}
   		catch (Exception e) {
   			d.setErrorMessage("Failed to upload file "+name+": "+e.getMessage());
   		}
   		logDescriptor(d,false);
   		log.debug("finished");
   		return d;
   	}


   	/**
   	 * executes a bulk drillthrough call on a given drillthrough datastore based on a (names,values) filter criterion. The size of values has to a multiple of the size of names, each multiple forming its own condition.
   	 * @param datastore the name of the drillthrough datastore to perform the drillthrough for. The name is in the form 'Palo-DB-Name'.'Palo-Cube-Name' in upper case.
   	 * @param names for each request in the bulk the names of the columns to filter.
   	 * @param values for each request in the bulk the values, the cells of the filtered columns have to have, also in one dimensional array, but not all paths.
   	 * @param lines the maximum number of lines to be returned. 0 return all lines.
   	 * @return a {@link ResultDescriptor}. Result contains matching data as csv.
   	 * @throws Fault
   	 */
    @WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ResultDescriptor drillThrough(String datastore, String[] names, String[] values,int[] lengths, int lines) throws Fault {
   		log.debug("Starting drillthrough on datastore "+datastore);
   		ResultDescriptor d = new ResultDescriptor();
   		try {
   			StringWriter out = new StringWriter();
   			if (lines == 0)
   				lines = Integer.MAX_VALUE;

   			Datastore ds = DatastoreManager.getInstance().get(datastore);
   			if (ds == null) {
   				// Datastore names in upper case since ETL 3.2. old names still found
   				ds = DatastoreManager.getInstance().get(datastore.toUpperCase());
   			}
   			if (ds != null) {
   				CSVWriter writer = new CSVWriter(out);
   				writer.setAutoClose(false); //do not close on end of write, since we may use multiple writes due bulk mode

   				if (lines-writer.getLinesOut() > 0) {
   					try {
   						IProcessor processor = ds.getFilteredProcessor(names, values,lengths,lines-writer.getLinesOut());
   						writer.write(processor);
   					}
   					catch (RuntimeException ce) {
   						d.setErrorMessage("Error in Drillthrough for "+datastore+": " + ce.getMessage());
   					}
   				}
   				writer.close();
   				ds.close();
   				d.setResult(out.toString());				
   			}
   			else {
   				String message = "No Drillthrough defined for "+datastore;
   				log.error(message);
   				d.setErrorMessage(message);
   			}
   		}
   		catch (Exception e) {
   			d.setErrorMessage("Failed to execute method drillThrough for "+datastore+": "+e.getMessage());
   		}
   		logDescriptor(d,true);
   		log.debug("finished");
   		return d;
   	}


   	/** gets information about the location of the drillthrough datastore 
   	 * @param datastore the name of the drillthrough datastore. The name is in the form 'Palo-DB-Name'.'Palo-Cube-Name' in upper casename the name of the executables - null for no filter.
   	 * @return the drillthrough info containing Connector type, Connector, Schema name and Table name
   	 * @throws Fault
   	 */
    @WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public DrillthroughInfoDescriptor[] drillThroughInfo(String datastore) throws Fault {
   		log.debug("Starting drillThroughInfo for datastore "+datastore);
   		try {
   			ArrayList<DrillthroughInfoDescriptor> result = new ArrayList<DrillthroughInfoDescriptor>();
   			for (String d : DatastoreManager.getInstance().getKeys()) {
   				Datastore ds = DatastoreManager.getInstance().get(d);
   				if (!ds.getConnector().equals("Temporary") && (datastore==null || ds.getLocator().equals(datastore))) { 
   					DrillthroughInfoDescriptor dsdescr = new DrillthroughInfoDescriptor();
   					dsdescr.setConnector(ds.getConnector());
   					dsdescr.setDatastore(ds.getLocator());
   					dsdescr.setSchemaname(ds.getSchemaName());
   					dsdescr.setTablename(ds.getTableName());
   					dsdescr.setConnectorType(ds.getConnectorType().toString());
   					result.add(dsdescr);
   				}
   			}
   			return result.toArray(new DrillthroughInfoDescriptor[result.size()]);
   		}
   		catch (Exception e) {
   			throw new Fault(new Exception("Failed to execute method drillThroughInfo: "+e.getMessage()));
   		}
   	}



   	//******************************* R U N T I M E ******************************

   	/**
   	 * adds an execution. Only allowed for components implementing the IExecutable Interface (e.g. jobs and loads)
   	 * @param locator the path to the component in the format {component.manager.}component: e.g: "myProject.jobs.default".
   	 * @param variables the variables forming the context of the execution in the form: name=value
   	 * @return an {@link ExecutionDescriptor} holding the id of the execution
   	 * @throws Fault
   	 */
    @WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ExecutionDescriptor addExecution(String locator, Variable[] variables) throws Fault {
   		log.debug("adding executions for: "+getPrintable(locator));
   		try {
   			//build context properties
   			Properties properties = getProperties(variables);
   			ExecutionDescriptor d = null;
   			Locator loc=null;
   			try {
   				loc = Locator.parse(locator);
   				verifyComponent(loc,true);
   				Execution e = Executor.getInstance().createExecution(loc,properties);
   				Executor.getInstance().addExecution(e);
   				d = getDescriptor(e.getExecutionState());
   			}
   			catch (Exception e) {
   				d = getErrorDescriptor("Failed to execute "+((loc==null)?locator:loc.getDisplayName())+": "+e.getMessage());
   			}
   			log.debug("finished");
   			return d;
   		}
   		catch (Exception e) {
   			throw new Fault(new Exception(e.getMessage()));
   		}
   	}

   	/**
   	 * removes executions. Only allowed for components implementing the IExecutable Interface (e.g. jobs and loads)
   	 * @param ids array of the Executions IDs
   	 * @return for each execution an {@link ExecutionDescriptor}
   	 */
    @WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ExecutionDescriptor[] removeExecutions(Long[] ids) {
   		log.debug("removing executions");
   		ExecutionDescriptor[] result = new ExecutionDescriptor[ids.length];
   		for (int i=0; i<ids.length; i++) {
   			Long id = ids[i];
   			try {
   				ExecutionState state = Executor.getInstance().removeExecution(id,false);
   				if (state != null){
   					result[i] = getDescriptor(state);
   				}
   				else {
   					result[i] = getErrorDescriptor("Failed to remove execution "+id+": Execution not found");
   				}
   			}
   			catch (ExecutionException e) {
   				result[i] = getErrorDescriptor("Failed to remove execution "+id+": "+e.getMessage());
   			} 
   			logDescriptor(result[i],false);
   		}
   		entityManager.createQuery("delete from Execution where etlId in (:ids)").setParameter("ids", Arrays.asList(ids)).executeUpdate();
   		log.debug("finished");
   		return result;
   	}

   	/**
   	 * runs an existing execution indicated by an execution id.
   	 * @param id of the execution.
   	 * @return an {@link ExecutionDescriptor}
   	 */
    @WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ExecutionDescriptor runExecution(Long id) {
   		log.debug("running execution "+id);
   		ExecutionDescriptor descriptor;
   		try {
   			ExecutionState state = Executor.getInstance().getExecutionState(id, false);
   			Locator loc = Locator.parse(state.getProject()).add(state.getType()).add(state.getName());
   			Component component = verifyComponent(loc,true);
			TaskController controller = (TaskController)org.jboss.seam.Component.getInstance("taskController", ScopeType.APPLICATION);
			controller.invokeExecution(0l, component.getId(), id, true, -1l);
   			descriptor = getDescriptor(state);
   		}
   		catch (Exception ee) {
   			descriptor = getErrorDescriptor("Failed to run execution "+id+": "+ee.getMessage());
   		} 
   		log.debug("finished");
   		return descriptor;
   	}

   	/** 
   	 * adds an execution and runs it immediately
   	 * @param locator the path to the component in the format {component.manager.}component: e.g: "myProject.jobs.default".
   	 * @param variables the variables forming the context of the execution in the form: name=value
   	 * @return an {@link ExecutionDescriptor} holding the id of the execution
   	 * @throws Fault
   	 */
    @WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ExecutionDescriptor execute(String locator, Variable[] variables) throws Fault {
   		ExecutionDescriptor descriptor = addExecution(locator,variables);
   		if (!descriptor.getStatusCode().equals("0")) {
   			log.error(descriptor.getErrorMessage());
   		}
   		else {	
   			log.debug("running directly execution "+descriptor.getId());
   			try {
   				//delegate to taskcontroller to persist execution object
   				Component component = verifyComponent(Locator.parse(locator),true);
   				TaskController controller = (TaskController)org.jboss.seam.Component.getInstance("taskController", ScopeType.APPLICATION);
   				controller.invokeExecution(0l, component.getId(), descriptor.getId(), true, -1l);
   			}
   			catch (Exception ee) {
   				descriptor = getErrorDescriptor("Failed to run execution "+descriptor.getId()+": "+ee.getMessage());
   			} 
   		}	
   		log.debug("finished");
   		return descriptor;		
   	}
   	
   	
   	/**
   	 * stops an existing execution indicated by an execution id.
   	 * @param id of the execution.
   	 * @return an {@link ExecutionDescriptor}
   	 */
    @WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ExecutionDescriptor stopExecution(Long id) {
   		log.debug("stopping execution: "+id);
   		try {
   			ExecutionDescriptor ed = new ExecutionDescriptor();
   			if (id == -1) { //stop all active executions
   				String activeExecutions = "";
   				for (ExecutionState s : Executor.getInstance().getUnfinishedExecutions()) {					
   					try {
   						verifyProject(s.getProject(),true);
   					} catch (Exception e) {
   						continue;
   					}
   					activeExecutions = activeExecutions.concat(s.getId() + ", ");
   					Executor.getInstance().stop(s.getId());
   				}
   				if(!activeExecutions.equals(""))
   					activeExecutions = activeExecutions.substring(0, activeExecutions.length()-2);
   				if(activeExecutions.length() != 0){
   					ed.setResult("The following executions have been killed: " + activeExecutions  + ".");
   				}else{
   					ed.setResult("No active executions are on the server.");
   				}
   			}
   			else {
   				ExecutionState state = Executor.getInstance().getExecutionState(id, false);
   				if(state.getStatus().equals(Codes.statusStopping) || state.getStatus().equals(Codes.statusRunning) || state.getStatus().equals(Codes.statusQueued)){
   					verifyProject(state.getProject(),true);
					state = Executor.getInstance().stop(id);
   					ed=getDescriptor(state);
   				}else{
   					throw new RuntimeException("Execution has status \"" + new ResultCodes().getString(state.getStatus()) + "\" and therefore can not be stopped.");
   				}
   				
   			}
   			logDescriptor(ed,false);
   			return ed; //empty descriptor with the list of stopped or aborted executions (if existed)			
   		}
   		catch (Exception e) {
   			return getErrorDescriptor("Failed to stop execution "+id+": "+e.getMessage());
   		}
   	}

   	/**
   	 * get the status of an existing execution indicated by an execution id.
   	 * @param id of the execution.
   	 * @param waitForTermination indicates wither the result should be handed only at the end of the execution.
   	 * @return an {@link ExecutionDescriptor}
   	 */
    @WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ExecutionDescriptor getExecutionStatus(Long id, boolean waitForTermination) throws Fault {
   		ExecutionDescriptor d = new ExecutionDescriptor();
   		try {
   			log.debug("getting status for execution: "+id);
   			try {
   				ExecutionState state = Executor.getInstance().getExecutionState(id, waitForTermination);
   				d = getDescriptor(state);
   				verifyProject(state.getProject(),true);
   				log.debug("finished");
   				return d;
   			}
   			catch (ExecutionException e) {
   				return getErrorDescriptor("Failed to get status for execution "+id+": "+e.getMessage());
   			}
   		}
   		catch (Exception e) {
   			throw new Fault(new Exception("Failed to execute method getExecutionStatus for execution "+id+": "+e.getMessage()));
   		}
   	}

   	/**
   	 * gets the (filtered) histories of executions, this includes all possible executions
   	 * @param project the name of the project as filter criterion - null for no filter
   	 * @param type the type of executables (jobs, loads, transforms, extracts) - null for no filter
   	 * @param name the name of the executables - null for no filter.
   	 * @param after a start date timestamp as filter criterion - 0 for no filter
   	 * @param before a end date timestamp as filter criterion - 0 for no filter
   	 * @param status the status of the execution as filter criterion - null for no filter
   	 * @return an array of {@link ExecutionDescriptor ExecutionDescriptors} matching the query
   	 */
    @WebMethod   
    @Restrict("#{identity.isLoggedIn()}")
    public ExecutionDescriptor[] getExecutionHistory(String project, String type, String name, long after, long before, String status) throws Fault {
   		String[] types = null;
   		String[] statuses = null;
   		if(type!=null) {
   			types = new String[1];
   			types[0]=type;
   		}	
   		if(status!=null) {
   			statuses = new String[1];
   			statuses[0]=status;
   		}	
   		return getExecutionList(project, types, name, after, before, statuses);
   	}
   
   	
   	/**
   	 * gets the (filtered) histories of executions, this includes all possible executions
   	 * @param project the name of the project as filter criterion - null for no filter
   	 * @param types array of type of executables (jobs, loads, transforms, extracts) - null for no filter
   	 * @param name the name of the executables - null for no filter.
   	 * @param after a start date timestamp as filter criterion - 0 for no filter
   	 * @param before a end date timestamp as filter criterion - 0 for no filter
   	 * @param statuses array of statuses of the execution as filter criterion - null for no filter
   	 * @return an array of {@link ExecutionDescriptor ExecutionDescriptors} matching the query
   	 */
   	@WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ExecutionDescriptor[] getExecutionListPaged(String project, String[] types, String name, long after, long before, String[] statuses, int start, int pagesize) throws Fault {
   		log.debug("Getting execution history.");
   		try {
   			log.debug("Project:"+project+" Name:"+name+" After:"+after+" Before:"+before+" Types: "+getPrintable(types," ")+" Stati: "+getPrintable(statuses," ")+".");
   			if (start!=0 || pagesize!=0)
   				log.debug("Start: "+start+" Pagesize: "+pagesize);
   			
   			CriteriaWrapper criteria = createCriteria(entityManager, com.proclos.colibriweb.entity.component.Execution.class);
			if (project != null) criteria.addCriterion("component.project", Restrictions.eq("name", project));
			if (name != null) criteria.addCriterion("component",Restrictions.eq("name", name));
			if (types != null) criteria.addCriterion(Restrictions.in("type", types));
			if (statuses != null) criteria.addCriterion(Restrictions.in("status",statuses));
			if (after != 0) criteria.addCriterion(Restrictions.ge("startDate",new Date(after)));
			if (before != 0) criteria.addCriterion(Restrictions.le("startDate",new Date(before)));
			AccessUtil.appendAccessCriterion("component.project", getCurrentUser(), criteria);
			criteria.addOrder(Order.asc("startDate"));
			Criteria c = criteria.createCriteria();
			c.setFirstResult(start);
			if (pagesize > 0) c.setMaxResults(pagesize);
			@SuppressWarnings("unchecked")
			List<com.proclos.colibriweb.entity.component.Execution> executions = (List<com.proclos.colibriweb.entity.component.Execution>)c.list();
   			ExecutionDescriptor[] result = new ExecutionDescriptor[executions.size()];
   			for (int i=0; i<executions.size(); i++) {
   				ExecutionDescriptor d = new ExecutionDescriptor();
   				com.proclos.colibriweb.entity.component.Execution e = executions.get(i);
   				result[i] = d;
   				d.setErrorMessage(e.getFirstErrorMessage());
   				d.setErrors(e.getErrors());
   				d.setId(e.getEtlId());
   				d.setName(e.getName());
   				d.setProject(e.getComponent().getProject().getName());
   				d.setStartDate(e.getStartDate().getTime());
   				d.setStopDate(e.getStopDate().getTime());
   				d.setStatus(e.getStateString());
   				d.setStatusCode(new ResultCodes().getNumeric(e.getStatus()));
   				d.setType(e.getType());
   				d.setWarnings(e.getWarnings());
   				d.setValid(!Codes.statusInvalid.equals(e.getStatus()));
   			}
   			log.debug("Retrieved exections: "+executions.size());
   			log.debug("finished");
   			return result;
   		}
   		catch (Exception e) {
   			throw new Fault(new Exception("Failed to execute method getExecutionListPaged for project "+project+": "+e.getMessage()));
   		}
   	}
   	
   	
   	/**
   	 * gets the (filtered) histories of executions, this includes all possible executions
   	 * @param project the name of the project as filter criterion - null for no filter
   	 * @param types array of type of executables (jobs, loads, transforms, extracts) - null for no filter
   	 * @param name the name of the executables - null for no filter.
   	 * @param after a start date timestamp as filter criterion - 0 for no filter
   	 * @param before a end date timestamp as filter criterion - 0 for no filter
   	 * @param statuses array of statuses of the execution as filter criterion - null for no filter
   	 * @return an array of {@link ExecutionDescriptor ExecutionDescriptors} matching the query
   	 */
   	@WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ExecutionDescriptor[] getExecutionList(String project, String[] types, String name, long after, long before, String[] statuses) throws Fault {
   		return getExecutionListPaged(project,types, name, after, before, statuses, 0,0);
   	}
   	
   	
   	/**
   	 * gets the log of an execution
   	 * @param id the id of the execution.
   	 * @param timestamp the end time point where the log after this time stamp will be ignored in the result
   	 * @return a {@link ResultDescriptor}. Result contains the log.
   	 */
   	@WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ResultDescriptor getExecutionLog(Long id, String type, Long timestamp) throws Fault {
   		return getExecutionLogPaged(id,type,timestamp,0,0);
   	}

   	/**
   	 * gets the log of an execution
   	 * @param id the id of the execution.
   	 * @param timestamp the end time point where the log after this time stamp will be ignored in the result
   	 * @return a {@link ResultDescriptor}. Result contains the log.
   	 */
	@WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ResultDescriptor getExecutionLogPaged(Long id, String type, Long timestamp, int start,int pagesize) throws Fault {
   		log.debug("Getting execution log for execution id: " + id);
   		ResultDescriptor d = new ResultDescriptor();
   		try {
   			CriteriaWrapper criteria = createCriteria(entityManager, com.proclos.colibriweb.entity.component.Execution.class);
   			criteria.addCriterion(Restrictions.eq("etlId",id));
   			AccessUtil.appendAccessCriterion("component.project", getCurrentUser(), criteria);
   			criteria.addOrder(Order.desc("creationDate"));
   		   	@SuppressWarnings("unchecked")
   			List<com.proclos.colibriweb.entity.component.Execution> executions = (List<com.proclos.colibriweb.entity.component.Execution>)criteria.createCriteria().list();
   		   	if (!executions.isEmpty()) {
   		   		com.proclos.colibriweb.entity.component.Execution execution = executions.get(0);
	   			StringBuffer buffer = new StringBuffer();
	   			if (execution != null) {
	   				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	   				String log = execution.getLog();
	   				String[] lines = log.split("\\r?\\n");
	   				int count = 0;
	   				for (int i=start; i<lines.length; i++) {
	   					String line = lines[i];
	   					if (timestamp != null) {
	   						String datePart = line.substring(0,19);
	   						Date date = format.parse(datePart);
	   						if (date.getTime() < timestamp) continue;
	   					}
	   					if (type != null) {
	   						String typePart = line.substring(25, 29);
	   						Level level = Level.toLevel(typePart);
	   						Level filter = Level.toLevel(type);
	   						if (!level.isGreaterOrEqual(filter)) continue;
	   					}
	   					buffer.append(line);
	   					buffer.append("\n");
	   					count++;
	   					if (count >= pagesize && pagesize != 0) break;
	   				}
	   			}
	   			d.setResult(buffer.toString());
   		   	} else {
   		   		d.setErrorMessage("Execution with id "+id+" not found.");
   		   	}
   			logDescriptor(d,true);
   			log.debug("finished");
   			return d;
   		}
   		catch (Exception e) {
   			d.setErrorMessage("Failed to get log: "+e.getMessage());
   			throw new Fault(new Exception("Failed to execute method getExecutionLog for execution "+id+": "+e.getMessage()));
   		}
   	}
   	

   	/**
   	 * prepares, waits for and fetches data of the component.
   	 * @param locator the path to the component in the format component{.manager.component}: e.g: "myProject.sources.mySource". Must not be null.
   	 * @param variables the variables used to to define a runtime context.
   	 * @param view the view of the data. relevant only for components providing a tree data representation
   	 * @param lines number of line of the data to be delivered
   	 * @return an {@link ExecutionDescriptor} holding the data in its result property.
   	 * @throws Fault
   	 */
   	@WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ExecutionDescriptor getData(String locator, Variable[] variables, String view, int lines,int start, Boolean waitForTermination, String outputFormat) throws Fault {
   		log.debug("retrieving data for "+getPrintable(locator));	
   		if (waitForTermination == null)
   			waitForTermination = Boolean.TRUE;
   		Properties properties = getProperties(variables);
   		try {
   			Locator loc = Locator.parse(locator);
   			verifyProject(loc.getRootName(),true);
   			Execution e;
   			try {
   				e = Executor.getInstance().createData(loc, properties,view);
   			}
   			catch (Exception ex) {
   				// Validation error during initialization phase
   				return getErrorDescriptor(ex.getMessage());
   			}
   			e.getExecutable().getParameter().setProperty("sample", String.valueOf(lines));
   			start = handleOffset(start);
   			
   			e.getExecutable().getParameter().setProperty("offset", String.valueOf(start));
   			if(outputFormat==null || outputFormat.equalsIgnoreCase("csv"))
   				e.getExecutionState().setDataTarget(DataTargets.CSV_INLINE);
   			else if(outputFormat.equalsIgnoreCase("xml"))
   				e.getExecutionState().setDataTarget(DataTargets.XML_INLINE);
   			else
   				throw new Exception("OutputFormat can only be csv or xml");
   			Executor.getInstance().addExecution(e);
   			Executor.getInstance().runExecution(e.getKey());
   			ExecutionDescriptor d = getDescriptor(Executor.getInstance().getExecutionState(e.getKey(), waitForTermination));
   			log.debug("finished");
   			return d;
   		}
   		catch (Exception ex) {
   			return getErrorDescriptor("Failed to retrieve data for "+getPrintable(locator)+": "+ex.getMessage());
   		}
   	}

   	/**
   	 * Gets the output description of a component.
   	 * @param locator locator the path to the component in the format component{.manager.component}: e.g: "myProject.sources.mySource". Must not be null.
   	 * @param variables the variables used to to define a runtime context.
   	 * @param view the view of the data. relevant only for components providing a tree data representation
   	 * @return an {@link ExecutionDescriptor} holding the ComponentOutputDescription in its metadata property.
   	 * @throws Fault
   	 */
   	@WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ExecutionDescriptor getComponentOutputs(String locator, Variable[] variables, String view, Boolean waitForTermination) throws Fault {
   		log.debug("getting component output for "+getPrintable(locator));		
   		if (waitForTermination == null)
   			waitForTermination = Boolean.TRUE;
   		Properties properties = getProperties(variables);
   		try {
   			Locator loc = Locator.parse(locator);
   			verifyProject(loc.getRootName(),true);
   			Execution e = Executor.getInstance().createOutputDescription(loc, properties,view);
   			e.getExecutionState().setDataTarget(DataTargets.CSV_INLINE);
   			Executor.getInstance().addExecution(e);
   			Executor.getInstance().runExecution(e.getKey());
   			ExecutionDescriptor d = getExecutionStatus(e.getKey(),waitForTermination);
   			log.debug("finished");			
   			return d;
   		}
   		catch (Exception e) {
   			return getErrorDescriptor("Failed to retrieve component outputs for "+getPrintable(locator)+": "+e.getMessage());
   		}
   	}


   	/**
   	 * Tests the components in runtime
   	 * @param locator the paths to the components in the format {component.manager.}component: e.g: "myProject.jobs.default".
   	 * @param variables the variables used to to define a runtime context.
   	 * @return an {@link ExecutionDescriptor}
   	 */
   	@WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ExecutionDescriptor testComponent(String locator,Variable[] variables, Boolean waitForTermination) {
   		log.debug("testing component "+getPrintable(locator));					
   		if (waitForTermination == null)
   			waitForTermination = Boolean.TRUE;
   		Properties properties = getProperties(variables);
   		try {
   			Locator loc = Locator.parse(locator);
   			verifyProject(loc.getRootName(),true);
   			Execution e;
   			try {
   				e = Executor.getInstance().createTest(loc, properties);
   			}
   			catch (Exception ex) {
   				// Validation error during initialization phase
   				return getErrorDescriptor(ex.getMessage());
   			}
   			Executor.getInstance().addExecution(e);
   			Executor.getInstance().runExecution(e.getKey());
   			ExecutionDescriptor d = getExecutionStatus(e.getKey(),waitForTermination);
   			log.debug("finished");
   			return d;
   		}
   		catch (Exception ex) {
   			return getErrorDescriptor("Failed to test "+getPrintable(locator)+": "+ex.getMessage());
   		}
   	}

   	/**
   	 * Gets the recursive list of all ingoing dependent components of a component e.g. all loads of a job
   	 * @param locator the path to the component in the format component.manager.component: e.g: "myProject.jobs.default".
   	 * @param includeVariables if set dependencies to project variables are also returned
   	 * @return the list of all components from which the input-component is dependent
   	 * @throws Fault
   	 */
   	@WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ComponentDependencyDescriptor[] getComponentDependencies(String locator, boolean includeVariables) throws Fault {
   		log.debug("getting component dependencies for "+getPrintable(locator));		
   		try {			
   			Locator loc = Locator.parse(locator);
   			verifyProject(loc.getRootName(),true);
   			// if it is a variable then it is empty
   			if(loc.getManager().equals(ITypes.Managers.variables.toString())){
   				ComponentDependencyDescriptor empty= new ComponentDependencyDescriptor();
   				empty.setName(loc.toString());
   				empty.setComponents(new String[0]);
   				return new ComponentDependencyDescriptor[]{empty};
   			}
   			IProject project = (IProject) ConfigManager.getInstance().getProject(loc.getRootName());
   			ConfigManager.getInstance().initProjectComponents(project, IContext.defaultName, true); 
   			Map<String,List<String>> deps = project.getAllDependencies(locator,includeVariables);
   			ComponentDependencyDescriptor[] result = new ComponentDependencyDescriptor[deps.keySet().size()];
   			Iterator<String> iterator = deps.keySet().iterator();
   			int i = 0;
   			while (iterator.hasNext()) {
   				String key = iterator.next();
   				List<String> line = deps.get(key);
   				result[i] = new ComponentDependencyDescriptor();
   				result[i].setName(key);
   				result[i].setComponents(line.toArray(new String[line.size()]));
   				i++;
   			}
   			log.debug("finished");
   			return result;
   		}
   		catch (Exception e) {
   			ComponentDependencyDescriptor notValidCD=getErrorDependencyDescriptor(locator,"Failed to get dependencies for "+locator+": "+e.getMessage());
   			return new ComponentDependencyDescriptor[]{notValidCD};
   		}
   	}

   	/**
   	 * Gets the list of all directly ingoing dependent components of a component e.g. all loads of a job
   	 * @param locator the path to the component in the format component.manager.component: e.g: "myProject.jobs.default".
   	 * @param includeVariables if set dependencies to project variables are also returned
   	 * @return the list of all components from which the input-component is dependent
   	 * @throws Fault
   	 */
   	@WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ComponentDependencyDescriptor getComponentDirectDependencies(String locator, boolean includeVariables) throws Fault {
   		log.debug("getting direct component dependencies for "+getPrintable(locator));						
   		try {
   			Locator loc = Locator.parse(locator);
   			verifyProject(loc.getRootName(),true);
   			ComponentDependencyDescriptor result = new ComponentDependencyDescriptor();
   			result.setName(locator);
   			result.setComponents(new String[0]);			
   			if(loc.getManager().equals(ITypes.Managers.variables.toString()))
   				return result;
   			
   			IProject project = (IProject) ConfigManager.getInstance().getProject(loc.getRootName());
   			ConfigManager.getInstance().initProjectComponents(project, IContext.defaultName, true); 
   			List<String> line = project.getDirectDependencies(locator,includeVariables);	
   			result.setComponents(line.toArray(new String[line.size()]));
   			log.debug("finished");
   			return result;
   		}
   		catch (Exception e) {
   			return getErrorDependencyDescriptor(locator,"Failed to get direct dependencies for "+locator+": "+e.getMessage());
   		}
   	}
   	
   	
   	/**
   	 * Gets the recursive list of all outgoing components of a component e.g. all jobs which include a load  
   	 * @param locator the path to the component in the format component.manager.component: e.g: "myProject.jobs.default".
   	 * @return the list of all components which depend on the input-component
   	 * @throws Fault
   	 */
   	@WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ComponentDependencyDescriptor[] getComponentDependents(String locator) throws Fault {
   		log.debug("getting component dependents for "+getPrintable(locator));				
   		try {
   			Locator loc = Locator.parse(locator);
   			verifyProject(loc.getRootName(),true);
   			IProject project = (IProject) ConfigManager.getInstance().getProject(loc.getRootName());
   			ConfigManager.getInstance().initProjectComponents(project, IContext.defaultName, true); 
   			Map<String,List<String>> deps = null;
   			if(loc.getManager().equals(ITypes.Managers.variables.toString()))
   				deps = project.getAllDependents(locator,true);
   			else
   				deps = project.getAllDependents(locator,false);
   			ComponentDependencyDescriptor[] result = new ComponentDependencyDescriptor[deps.keySet().size()];
   			Iterator<String> iterator = deps.keySet().iterator();
   			int i = 0;
   			while (iterator.hasNext()) {
   				String key = iterator.next();
   				List<String> line = deps.get(key);
   				result[i] = new ComponentDependencyDescriptor();
   				result[i].setName(key);
   				result[i].setComponents(line.toArray(new String[line.size()]));
   				i++;
   			}
   			log.debug("finished");
   			return result;
   		}
   		catch (Exception e) {
   			ComponentDependencyDescriptor notValidCD=getErrorDependencyDescriptor(locator,"Failed to get dependents for "+locator+": "+e.getMessage());
   			return new ComponentDependencyDescriptor[]{notValidCD};
   		}
   	}

   	/**
   	 * Gets the list of all directly outgoing components of a component e.g. all jobs which include a load 
   	 * @param locator the path to the component in the format component.manager.component: e.g: "myProject.jobs.default".
   	 * @return the list of all components which depend on the input-component
   	 * @throws Fault
   	 */
   	@WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ComponentDependencyDescriptor getComponentDirectDependents(String locator) throws Fault {
   		log.debug("getting direct component dependents for "+getPrintable(locator));								
   		try {
   			Locator loc = Locator.parse(locator);
   			verifyProject(loc.getRootName(),true);
   			IProject project = (IProject) ConfigManager.getInstance().getProject(loc.getRootName());
   			ConfigManager.getInstance().initProjectComponents(project, IContext.defaultName, true); 
   			List<String> line = null;
   			if(loc.getManager().equals(ITypes.Managers.variables.toString()))
   				line = project.getDirectDependents(locator,true);
   			else
   				line = project.getDirectDependents(locator,false);
   			ComponentDependencyDescriptor result = new ComponentDependencyDescriptor();
   			result.setName(locator);
   			result.setComponents(line.toArray(new String[line.size()]));
   			log.debug("finished");			
   			return result;
   		}
   		catch (Exception e) {
   			return getErrorDependencyDescriptor(locator,"Failed to get direct dependents for "+locator+": "+e.getMessage());
   		}
   	}
   	
   	@WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ResultDescriptor getProjectDocumentation(String locator, String[] graphLocators) throws Fault {
   		log.debug("getting project documentation for "+getPrintable(locator));				
   		try {
   			ResultDescriptor d = new ResultDescriptor();
   			String projectName = null;
   			try {
   				Locator loc = Locator.parse(locator);
   				projectName = loc.getRootName();
   				verifyProject(projectName,true);
   				IProject project = ConfigManager.getInstance().getProject(loc.getRootLocator().getName());					
   				DocuUtil util = new DocuUtil(project, loc, graphLocators, null);
   				d.setResult(util.getDocu());			
   			}
   			catch (ConfigurationException e) {
   				String message="Could not generate documentation for project "+projectName+": "+e.getMessage();
   				log.error(message);
   				d.setErrorMessage(message);
   				return d;
   			}
   			log.debug("finished");
   			return d; 
   		}	
   		catch (Exception e) {
   			e.printStackTrace();
   			throw new Fault(new Exception("Failed to get project documentation for "+locator+": "+e.getMessage()));
   		}
   	}

   	@WebMethod
    @Restrict("#{identity.isLoggedIn()}")
   	public ResultDescriptor calculateComponentGraph(String locator, Variable[] settings) {
   		log.debug("calculating flow graph for "+getPrintable(locator));
   		ResultDescriptor d = new ResultDescriptor();
   		try {
   			Locator loc = Locator.parse(locator);
   			verifyProject(loc.getRootName(),true);
   			IProject project;
   			List<Locator> invalidComponents = new ArrayList<Locator>(); 
   			// get project config 
   			project = (IProject) ConfigManager.getInstance().getProject(loc.getRootLocator().getName());
   			invalidComponents = ConfigManager.getInstance().initProjectComponents(project, IContext.defaultName, true);
   			// Check if component exists
   			if(ConfigManager.getInstance().findElement(loc)==null)
   				throw new Exception("Component " + loc.toString() + " does not exist.");
   			Properties graphProperties = getProperties(settings);
			String application = Expressions.instance().createValueExpression("#{facesContext.externalContext.request.contextPath}").getValue().toString();
			graphProperties.setProperty("linkPrefix", application+"/resolvecomponent.seam?locator=");
   			String svg = GraphManager.getInstance().getSVG(project, locator,graphProperties,invalidComponents,Long.MIN_VALUE);
   			d.setResult(svg);
   		}
   		catch (Exception e) {
   			d.setErrorMessage("Failed to calculate graph for "+locator+": "+e.getMessage());
   			d.setValid(false);
   			log.error(d.getErrorMessage());
   		}
   		log.debug("finished");
   		return d;
   	}

   	/**
   	 * gets the server current status, info like free memory,used memory,...
   	 * @return the {@link ServerStatus}
   	 * @throws Fault
   	 */
   	@WebMethod
   	public ServerStatus getServerStatus() throws Fault {
   		log.debug("getting server status");
   		try{
   			ServerStatus result = new ServerStatus();
   			result.setVersion(Settings.getInstance().getVersion());
   			result.setAutosaving(Settings.getInstance().getAutoSave());
   			result.setValidating(Settings.getInstance().getContext(Settings.ProjectsCtx).getProperty("validate","true").equalsIgnoreCase("true"));
   			result.setLogLevel(Settings.getInstance().getContext(Settings.ProjectsCtx).getProperty("loglevel","true"));
   			result.setFreeMemory(Runtime.getRuntime().freeMemory());
   			result.setMaxMemory(Runtime.getRuntime().maxMemory());
   			result.setTotalMemory(Runtime.getRuntime().totalMemory());
   			result.setProcessorsAvailable(Runtime.getRuntime().availableProcessors());
   			result.setRunningExecutions(Executor.getInstance().getRunningExecutions().size());
   			result.setDatastores(DatastoreManager.getInstance().getKeys());
   			log.debug("finished");
   			return result;
   		}
   		catch (Exception e) {
   			throw new Fault(new Exception(e.getMessage()));
   		}
   	}


   	/**
   	 * gets the list of all formats available to be rendered by a tree (e.g. PCW for Parent-Child-Weight)
   	 * @return array with the tree formats
   	 */
   	@WebMethod
   	public String[] getTreeFormats() {
   		log.debug("getting tree formats");
   		ArrayList<String> treeFormats = new ArrayList<String>();
   		for (IView.Views view : IView.Views.values()) {
   			if (!view.equals(Views.NONE) && !view.equals(Views.FHWA))
   				treeFormats.add(view.toString());
   		}
   		log.debug("finished");
   		return treeFormats.toArray(new String[treeFormats.size()]);
   	}	
   	
   	/**
   	 * gets the list of all codes describing the status of an execution (e.g. numericCode 10, Description: Completed successfully)
   	 * @return array with the execution status codes
   	 */	
   	@WebMethod
   	public ExecutionStatusCode[] getExecutionStatusCodes() {
   		log.debug("getting execution status codes");		
   		List<ExecutionStatusCode> statusCodes = new ArrayList<ExecutionStatusCode>();
   		ResultCodes resultCodes = new ResultCodes();
   		for (ResultCodes.Codes resultCode : ResultCodes.Codes.values()) {			
   			ExecutionStatusCode statusCode = new ExecutionStatusCode();
   			statusCode.setNumericCode(Integer.parseInt(resultCodes.getNumeric(resultCode)));
   			statusCode.setDescription(resultCodes.getString(resultCode));
   			statusCodes.add(statusCode);
   		}
   		Collections.sort(statusCodes);
   		log.debug("finished");						
   		return statusCodes.toArray(new ExecutionStatusCode[statusCodes.size()]);
   	}	
   	
}

