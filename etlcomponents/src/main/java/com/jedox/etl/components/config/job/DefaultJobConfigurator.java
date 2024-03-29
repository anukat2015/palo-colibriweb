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
*   @author Gerhard Weis, proclos OG, Wien, Austria
*   @author Andreas Fröhlich, Jedox AG, Freiburg, Germany
*   @author Kais Haddadin, Jedox AG, Freiburg, Germany
*/
package com.jedox.etl.components.config.job;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import com.jedox.etl.core.component.ConfigurationException;
import com.jedox.etl.core.component.ITypes;
import com.jedox.etl.core.component.Locator;
import com.jedox.etl.core.config.job.JobConfigurator;
/**
 * 
 * @author Christian Schwarzinger. Mail: christian.schwarzinger@proclos.com
 *
 */
public class DefaultJobConfigurator extends JobConfigurator {

	private LinkedList<Locator> locators = new LinkedList<Locator>();
	private static final Log log = LogFactory.getLog(DefaultJobConfigurator.class);
	
	protected void isRunnable() throws ConfigurationException{
		List<?> executionList = getChildren(getXML(),"execution");
		if (executionList.size()==0) {
			throw new ConfigurationException("At least one job or load should be included in job " + getName() + ".");
		}	
	}
		
	protected  void addLocator(Locator loc, Element executableRef) {
		locators.add(loc);
	}
	
	protected void setLocators() throws ConfigurationException {
		List<?> executableRefList = getXML().getChildren();
		HashSet<String> executableRefNames = new HashSet<String>();
		for (int j=0; j<executableRefList.size(); j++) {
			Element executableRef = (Element) executableRefList.get(j);
			if (executableRef.getName().equalsIgnoreCase("execution")) {
				String executableRefType = executableRef.getAttributeValue("type");
				String executableRefName = executableRef.getAttributeValue("nameref");
				String combinedName = executableRefType + "." + executableRefName;
				if(executableRefNames.contains(combinedName)){
					throw new ConfigurationException(executableRefType+ " " + executableRefName + " is already included in Job "+ this.getName()+".");
				}
				else if(executableRefName.equals(this.getName()) && executableRefType.equalsIgnoreCase(ITypes.Components.job.toString())){
					throw new ConfigurationException("Job " + executableRefName + " can not be included in Job "+ this.getName()+".");
				}
				else{
					executableRefNames.add(combinedName);
				}
				if ((executableRefName != null)) {
					try {
						ITypes.Managers manager = ITypes.Managers.valueOf(executableRefType+"s");
						Locator loc = getLocator().getRootLocator().add(manager.toString()).add(executableRefName);
						addLocator(loc,executableRef);
					}
					catch (Exception e) {
						String message = executableRefType+ " "+executableRefName+" referenced in Job "+getName()+" depends on a faulty component.";
						log.debug(message);
						throw new ConfigurationException(e);
					}
				}
			}
		}
		executableRefNames.clear();
	}
	
	public LinkedList<Locator> getLocators(){
		return locators;
	}
				
	@Override
	public void configure() throws ConfigurationException {
		super.configure();
		isRunnable();
		setLocators();
	}

}
