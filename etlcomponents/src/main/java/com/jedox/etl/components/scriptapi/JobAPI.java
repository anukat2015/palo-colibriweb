/**
*   @brief <Description of Class>
*
*   @file
*
*   Copyright (C) 2008-2014 Jedox AG
*
*   This program is free software; you can redistribute it and/or modify it
*   under the terms of the GNU General Public License (Version 2) as published
*   by the Free Software Foundation at http://www.gnu.org/copyleft/gpl.html.
*
*   This program is distributed in the hope that it will be useful, but WITHOUT
*   ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
*   FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
*   more details.
*
*   You should have received a copy of the GNU General Public License along with
*   this program; if not, write to the Free Software Foundation, Inc., 59 Temple
*   Place, Suite 330, Boston, MA 02111-1307 USA
*
*   If you are developing and distributing open source applications under the
*   GPL License, then you are free to use Palo under the GPL License.  For OEMs,
*   ISVs, and VARs who distribute Palo with their products, and do not license
*   and distribute their source code under the GPL, Jedox provides a flexible
*   OEM Commercial License.
*
*   Developed by proclos OG, Wien on behalf of Jedox AG. Intellectual property
*   rights has proclos OG, Wien. Exclusive worldwide exploitation right
*   (commercial copyright) has Jedox AG, Freiburg.
*
*   @author Christian Schwarzinger, proclos OG, Wien, Austria
*   @author Andreas Fröhlich, Jedox AG, Freiburg, Germany
*   @author Kais Haddadin, Jedox AG, Freiburg, Germany
*/
package com.jedox.etl.components.scriptapi;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.jedox.etl.core.component.ConfigurationException;
import com.jedox.etl.core.component.IComponent;
import com.jedox.etl.core.component.IManager;
import com.jedox.etl.core.component.ITypes;
import com.jedox.etl.core.component.Locator;
import com.jedox.etl.core.component.RuntimeException;
import com.jedox.etl.core.connection.IConnection;
import com.jedox.etl.core.execution.Execution;
import com.jedox.etl.core.execution.ExecutionException;
import com.jedox.etl.core.execution.ExecutionState;
import com.jedox.etl.core.execution.Executor;
import com.jedox.etl.core.job.IJob;
import com.jedox.etl.core.scriptapi.BaseAPI;
import com.jedox.etl.core.scriptapi.Scanable;
import com.jedox.etl.core.scriptapi.State;
import com.jedox.etl.core.source.ISource;
import com.jedox.etl.core.source.IView;
import com.jedox.etl.core.source.processor.IProcessor;

/**
 * Enhancements of common Scripting API for usage in jobs. Useful for workflow in ETL Server
 * @author Christian Schwarzinger. Mail: christian.schwarzinger@proclos.com
 *
 */
public class JobAPI extends BaseAPI {
	
	private IJob job;
	private Set<String> usedComponents = new HashSet<String>();

	public boolean isUsable(IComponent component) {
		if (component instanceof IJob && super.isUsable(component)) {
			job = (IJob)component;
			return true;
		}
		return false;
	}

	private State execute(Execution execution) throws ExecutionException {
		checkDependencyUsage(getComponentLocator(execution.getExecutable().getLocator()).toString());
		ExecutionState state = execution.getExecutable().getContext().getState();
		if (job.isExecutable()) {
			execution.execute(Thread.currentThread().getName());
		}else{
			Locator loc = execution.getExecutable().getLocator();
			log.info(loc.getDisplayName()+ " is not executable");
		}
		return new State(state,true);
	}

	protected Properties getApiProperties() {
		Properties properties = job.getContext().getExternalVariables();
		properties.putAll(super.getApiProperties());			
		return properties;
	}
	/** executes a job with all properties as variables
	 * @param name the name of the job
	 * @return the execution state of the job
	 * @throws RuntimeException
	 */
	public State executeJob(@Scanable(type=ITypes.Managers.jobs) String name) throws RuntimeException {
		try {
			Execution execution = Executor.getInstance().createExecution(getContextLocator(ITypes.Jobs,name), getApiProperties(),job.getContext().getState());
			return execute(execution);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** executes a load with all properties as variables
	 * @param name the name of the load
	 * @return the execution state of the load
	 * @throws RuntimeException
	 */
	public State executeLoad(@Scanable(type=ITypes.Managers.loads) String name) throws RuntimeException {
		try {
			Execution execution = Executor.getInstance().createExecution(getContextLocator(ITypes.Loads,name), getApiProperties(),job.getContext().getState());
			return execute(execution);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** executes a source with all properties as variables
	 * @param name the name of the source
	 * @return the execution state of the source
	 * @throws RuntimeException
	 */
	public State executeSource(@Scanable(type=ITypes.Managers.sources) String name) throws RuntimeException {
		try {
			Execution execution = Executor.getInstance().createExecution(getContextLocator(ITypes.Sources,name), getApiProperties(),job.getContext().getState());
			return execute(execution);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public State testJob(@Scanable(type=ITypes.Managers.jobs) String name) throws RuntimeException {
		try {
			Execution execution = Executor.getInstance().createTest(getContextLocator(ITypes.Jobs,name), getApiProperties());
			return execute(execution);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public State testLoad(@Scanable(type=ITypes.Managers.loads) String name) throws RuntimeException {
		try {
			Execution execution = Executor.getInstance().createTest(getContextLocator(ITypes.Loads,name), getApiProperties());
			return execute(execution);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public State testSource(@Scanable(type=ITypes.Managers.sources) String name) throws RuntimeException {
		try {
			Execution execution = Executor.getInstance().createTest(getContextLocator(ITypes.Sources,name), getApiProperties());
			return execute(execution);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public State testConnection(@Scanable(type=ITypes.Managers.connections) String name) throws RuntimeException {
		try {
			Execution execution = Executor.getInstance().createTest(getContextLocator(ITypes.Connections,name), getApiProperties());
			return execute(execution);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/** 
		@deprecated  
	 */
	public State getConnectionMetadata(String name, Properties metadataProps) throws RuntimeException {
		log.error("ETL API Method is deprecated. A selector has to be specified");
		return null;
	}

	public State getConnectionMetadata(@Scanable(type=ITypes.Managers.connections) String name, String selector, Properties metadataProps) throws RuntimeException {
		if (metadataProps==null) {
			metadataProps = new Properties();
		}	
		metadataProps.put("selector", selector);		
		try {
			Execution execution = Executor.getInstance().createMetadata(getContextLocator(ITypes.Connections,name), getApiProperties(), metadataProps);
			return execute(execution);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
		
	public IConnection getConnection(@Scanable(type=ITypes.Managers.connections) String name) throws RuntimeException  {
		try {
			checkDependencyUsage(getContextLocator(ITypes.Connections,name).toString());
			return (IConnection)job.getContext().getComponent(getContextLocator(ITypes.Connections,name));
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
	
	public State getJobState() {
		return new State(job.getContext().getState(),false);
	}
	
	private Locator getComponentLocator(Locator locator) {
		if (locator.getManager().equals(ITypes.Extracts) || locator.getManager().equals(ITypes.Transforms)) {
			return getContextLocator(ITypes.Sources,locator.getName());
		}
		return locator;
	}
	
	
	private void checkDependencyUsage(String name) {
		IManager componentManager = job.getManager(ITypes.Any);
		usedComponents.add(name);
		if (componentManager != null) 
			for (IComponent c : componentManager.getAll()) {
				if (getComponentLocator(c.getLocator()).toString().equals(name))
					return;
			}
		log.debug("Job "+job.getName()+" implicitly depends on source "+Locator.parse(name).getName()+" via script, but does not explicitly declare it.");
	}
	
	
	public IProcessor setupProcessor(@Scanable(type=ITypes.Managers.sources) String name, String format, int size) throws RuntimeException {
		checkDependencyUsage(getContextLocator(ITypes.Sources,name).toString());
		return super.setupProcessor(name, format, size);
	}
		
	@SuppressWarnings("deprecation")
	public void close() {
		IManager componentManager = job.getManager(ITypes.Any);
		if (componentManager != null) {
			for (IComponent c : componentManager.getAll()) {
				Locator locator = getComponentLocator(c.getLocator());
				if (!usedComponents.contains(locator.toString())) {
					log.debug("Job "+job.getName()+" declares unused dependency on "+locator.getManager().substring(0,locator.getManager().length()-1)+" "+c.getName()+".");
				}
			}
		}
		super.close();
	}
	
	public void clearContexts() {
		for (ISource s : job.getContext().getState().getSourcesToClean()) {
			s.clearCache();
			//invalidate view sources of this sources
			IManager sourceManager = s.getManager(ITypes.Sources);
			if (sourceManager != null) {
				for (IComponent c : sourceManager.getAll()) {
					if (c instanceof IView && c instanceof ISource) { //ViewSource cleanup
						((ISource)c).invalidate();
						((ISource)c).clearCache();
					}
				}
			}
		}
		job.getContext().getState().getSourcesToClean().clear();
	}
	
	public boolean isExecutable() {
		return job.isExecutable();
	}

	


}
