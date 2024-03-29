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
* 	Portions of the code developed by proclos OG, Wien on behalf of Jedox AG.
* 	Intellectual property rights for these portions has proclos OG Wien, 
* 	or otherwise Jedox AG, Freiburg. Exclusive worldwide exploitation right 
* 	(commercial copyright) has Jedox AG, Freiburg.
*
*   @author Christian Schwarzinger, proclos OG, Wien, Austria
*   @author Andreas Fröhlich, Jedox AG, Freiburg, Germany
*   @author Kais Haddadin, Jedox AG, Freiburg, Germany
*/
package com.jedox.etl.components.config.transform;

import java.util.List;

import org.jdom.Element;

import com.jedox.etl.core.aliases.AliasMap;
import com.jedox.etl.core.component.ConfigurationException;
import com.jedox.etl.core.component.IComponent;
import com.jedox.etl.core.component.RuntimeException;
import com.jedox.etl.core.config.source.TableSourceConfigurator;
import com.jedox.etl.core.config.transform.ITransformConfigurator;
import com.jedox.etl.core.config.transform.TransformConfigUtil;
import com.jedox.etl.core.node.Row;


public class TableUnionConfigurator extends TableSourceConfigurator implements
		ITransformConfigurator {

	private Row row;
	private TransformConfigUtil util;

	public Row getRow() throws RuntimeException {
		/*
		if (manager == null) {
			try {
				manager = util.getColumns();
				setAliasMap(AliasMap.build(manager.getRow().getOutputDescription()));
			} 
			catch (ConfigurationException e) {
				throw new RuntimeException(e);
			}
		}
		*/
		return row;
	}

	public List<IComponent> getSources() throws ConfigurationException {
		return util.getSources();
	}

	public List<IComponent> getFunctions() throws ConfigurationException {
		return util.getFunctions();
	}
	
	public List<Element> getLoopElements() throws ConfigurationException {
		List<Element> loops = getChildren(getXML(),"loop");
		if (loops.size()>1)
			throw new ConfigurationException("Only one Loop Source allowed.");
		return loops;
	}
	
	public void configure() throws ConfigurationException {
		try {
			setName(getXML().getAttributeValue("name"));
			util = new TransformConfigUtil(getXML(),getLocator(),getContext());
			//manager = util.getColumns();
			setAliasMap(new AliasMap());
		}
		catch (Exception e) {
			throw new ConfigurationException("Failed to configure transform "+getName()+": "+e.getMessage());
		}
	}
}
