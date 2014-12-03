package com.proclos.colibriweb.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.cxf.interceptor.Fault;


@WebService(name = "ETLServer", targetNamespace="http://ns.proclos.com")
@SOAPBinding(style=SOAPBinding.Style.RPC)
public interface ETLService {
	
	@WebMethod(action="http://ns.proclos.com/addComponents") 
	public ResultDescriptor[] addComponents(@WebParam(name = "locators")String[] locators, @WebParam(name = "configs")String[] configs) throws Fault;
	
	@WebMethod(action="http://ns.proclos.com/addExecution") 
	public ExecutionDescriptor addExecution(@WebParam(name = "locator")String locator, @WebParam(name = "variables")Variable[] variables) throws Fault;
	
	@WebMethod(action="http://ns.proclos.com/calculateComponentGraph") 
	public ResultDescriptor calculateComponentGraph(@WebParam(name = "locator")String locator, @WebParam(name = "settings")Variable[] settings);
	
	@WebMethod(action="http://ns.proclos.com/drillThrough") 
	public ResultDescriptor drillThrough(@WebParam(name = "datastore")String datastore, @WebParam(name = "names")String[] names, @WebParam(name = "values")String[] values,@WebParam(name = "lengths")int[] lengths, @WebParam(name = "lines")int lines) throws Fault;
	
	@WebMethod(action="http://ns.proclos.com/drillThroughInfo") 
	public DrillthroughInfoDescriptor[] drillThroughInfo(@WebParam(name = "datastore")String datastore) throws Fault;
	
	@WebMethod(action="http://ns.proclos.com/execute") 
	public ExecutionDescriptor execute(@WebParam(name = "locator")String locator, @WebParam(name = "variables")Variable[] variables) throws Fault;
   
	@WebMethod(action="http://ns.proclos.com/getComponentConfigs") 
	public ResultDescriptor[] getComponentConfigs(@WebParam(name = "locators")String[] locators);
   	
	@WebMethod(action="http://ns.proclos.com/getComponentDependencies") 
	public ComponentDependencyDescriptor[] getComponentDependencies(@WebParam(name = "locator")String locator, @WebParam(name = "includeVariables")boolean includeVariables) throws Fault;
   	
	@WebMethod(action="http://ns.proclos.com/getComponentDependents") 
	public ComponentDependencyDescriptor[] getComponentDependents(@WebParam(name = "locator")String locator) throws Fault;
   	
	@WebMethod(action="http://ns.proclos.com/getComponentDirectDependencies") 
	public ComponentDependencyDescriptor getComponentDirectDependencies(@WebParam(name = "locator")String locator, @WebParam(name = "includeVariables")boolean includeVariables) throws Fault;
   	
	@WebMethod(action="http://ns.proclos.com/getComponentDirectDependents") 
	public ComponentDependencyDescriptor getComponentDirectDependents(@WebParam(name = "locator")String locator) throws Fault;
   	
	@WebMethod(action="http://ns.proclos.com/getComponentOutputs") 
	public ExecutionDescriptor getComponentOutputs(@WebParam(name = "locator")String locator, @WebParam(name = "variables")Variable[] variables, @WebParam(name = "view")String view, @WebParam(name = "waitForTermination")Boolean waitForTermination) throws Fault;
   	
	@WebMethod(action="http://ns.proclos.com/getComponentTypes") 
	public ComponentTypeDescriptor[] getComponentTypes(@WebParam(name = "scope")String scope, @WebParam(name = "name")String name) throws Fault;
   	
	@WebMethod(action="http://ns.proclos.com/getData") 
	public ExecutionDescriptor getData(@WebParam(name = "locator")String locator, @WebParam(name = "variables")Variable[] variables, @WebParam(name = "view")String view, @WebParam(name = "lines")int lines, @WebParam(name = "start")int start, @WebParam(name = "waitForTermination")Boolean waitForTermination, @WebParam(name = "outputFormat")String outputFormat) throws Fault;
    
	@WebMethod(action="http://ns.proclos.com/getExecutionHistory") 
	public ExecutionDescriptor[] getExecutionHistory(@WebParam(name = "project")String project, @WebParam(name = "type")String type, @WebParam(name = "name")String name, @WebParam(name = "after")long after, @WebParam(name = "before")long before, @WebParam(name = "status")String status) throws Fault;
   	
	@WebMethod(action="http://ns.proclos.com/getExecutionList") 
	public ExecutionDescriptor[] getExecutionList(@WebParam(name = "project")String project, @WebParam(name = "types")String[] types, @WebParam(name = "name")String name, @WebParam(name = "after")long after, @WebParam(name = "before")long before, @WebParam(name = "statuses")String[] statuses) throws Fault;
   	
	@WebMethod(action="http://ns.proclos.com/getExecutionListPaged") 
	public ExecutionDescriptor[] getExecutionListPaged(@WebParam(name = "project")String project, @WebParam(name = "types")String[] types, @WebParam(name = "name")String name, @WebParam(name = "after")long after, @WebParam(name = "ubefore")long before, @WebParam(name = "statuses")String[] statuses, @WebParam(name = "start")int start, @WebParam(name = "pagesize")int pagesize) throws Fault;
   	
	@WebMethod(action="http://ns.proclos.com/getExecutionLog") 
	public ResultDescriptor getExecutionLog(@WebParam(name = "id")Long id, @WebParam(name = "type")String type, @WebParam(name = "timestamp")Long timestamp) throws Fault;
   	
	@WebMethod(action="http://ns.proclos.com/getExecutionLogPaged") 
	public ResultDescriptor getExecutionLogPaged(@WebParam(name = "id")Long id, @WebParam(name = "type")String type, @WebParam(name = "timestamp")Long timestamp, @WebParam(name = "start")int start, @WebParam(name = "pagesize")int pagesize) throws Fault;
   	
	@WebMethod(action="http://ns.proclos.com/getExecutionStatus") 
	public ExecutionDescriptor getExecutionStatus(@WebParam(name = "id")Long id, @WebParam(name = "waitForTermination")boolean waitForTermination) throws Fault;
   	
	@WebMethod(action="http://ns.proclos.com/getExecutionStatusCodes") 
	public ExecutionStatusCode[] getExecutionStatusCodes();
   
	@WebMethod(action="http://ns.proclos.com/getLocators") 
	public String[] getLocators(@WebParam(name = "locator")String locator) throws Fault;
   	
	@WebMethod(action="http://ns.proclos.com/getMetadata") 
	public ExecutionDescriptor getMetadata(@WebParam(name = "locator")String locator, @WebParam(name = "settings")Variable[] settings, @WebParam(name = "variables")Variable[] variables);
   
   	@WebMethod(action="http://ns.proclos.com/getNames") 
   	public String[] getNames(@WebParam(name = "locator")String locator) throws Fault;
   	
   	@WebMethod(action="http://ns.proclos.com/getProjectDocumentation") 
   	public ResultDescriptor getProjectDocumentation(@WebParam(name = "locator")String locator, @WebParam(name = "graphLocators")String[] graphLocators) throws Fault;
   	
   	@WebMethod(action="http://ns.proclos.com/getScopes") 
   	public String[] getScopes();
   	
   	@WebMethod(action="http://ns.proclos.com/getServerStatus") 
   	public ServerStatus getServerStatus() throws Fault;
   	
   	@WebMethod(action="http://ns.proclos.com/getTreeFormats") 
   	public String[] getTreeFormats();
   	
   	@WebMethod(action="http://ns.proclos.com/login") 
    public boolean login(@WebParam(name = "username")String username, @WebParam(name = "password")String password);
    
   	@WebMethod(action="http://ns.proclos.com/logout") 
   	public boolean logout();
   	
   	@WebMethod(action="http://ns.proclos.com/migrateComponents") 
   	public ResultDescriptor[] migrateComponents(@WebParam(name = "configs")String[] configs) throws Fault;
   	
   	@WebMethod(action="http://ns.proclos.com/removeComponents") 
   	public ResultDescriptor[] removeComponents(@WebParam(name = "locators")String[] locators) throws Fault;
   	
   	@WebMethod(action="http://ns.proclos.com/removeExecutions") 
   	public ExecutionDescriptor[] removeExecutions(@WebParam(name = "ids")Long[] ids);
   	
   	@WebMethod(action="http://ns.proclos.com/renameComponents") 
   	public ResultDescriptor[] renameComponents(@WebParam(name = "locators")String[] locators, @WebParam(name = "newNames")String[] newNames, @WebParam(name = "updateReferences")boolean updateReferences) throws Fault;
   	
   	@WebMethod(action="http://ns.proclos.com/runExecution") 
   	public ExecutionDescriptor runExecution(@WebParam(name = "id")Long id);
   	
   	@WebMethod(action="http://ns.proclos.com/stopExecution") 
   	public ExecutionDescriptor stopExecution(@WebParam(name = "id")Long id);
   	
   	@WebMethod(action="http://ns.proclos.com/testComponent") 
   	public ExecutionDescriptor testComponent(@WebParam(name = "locator")String locator, @WebParam(name = "variables")Variable[] variables, @WebParam(name = "waitForTermination")Boolean waitForTermination);
   	
   	@WebMethod(action="http://ns.proclos.com/updateComponents") 
   	public ResultDescriptor[] updateComponents(@WebParam(name = "locators")String[] locators, @WebParam(name = "configs")String[] configs) throws Fault;
   	
   	@WebMethod(action="http://ns.proclos.com/uploadFile") 
   	public ResultDescriptor uploadFile(@WebParam(name = "name")String name, @WebParam(name = "data")byte[] data);
   	
   	@WebMethod(action="http://ns.proclos.com/validateComponents") 
   	public ResultDescriptor[] validateComponents(@WebParam(name = "locators")String[] locators, @WebParam(name = "configs")String[] configs) throws Fault;
	

}
