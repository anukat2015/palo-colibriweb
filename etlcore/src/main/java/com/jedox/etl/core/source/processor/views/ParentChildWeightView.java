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
package com.jedox.etl.core.source.processor.views;

import java.util.List;

import com.jedox.etl.core.component.RuntimeException;
import com.jedox.etl.core.node.ColumnNodeFactory;
import com.jedox.etl.core.node.CoordinateNode;
import com.jedox.etl.core.node.INamedValue;
import com.jedox.etl.core.node.NamedValue;
import com.jedox.etl.core.node.Row;
import com.jedox.etl.core.node.tree.ITreeElement;
import com.jedox.etl.core.util.NamingUtil;
import com.jedox.palojlib.interfaces.IElement.ElementType;

public class ParentChildWeightView extends ParentChildView {
	
	protected CoordinateNode getWeightCoordinate() {
		CoordinateNode c = ColumnNodeFactory.getInstance().createCoordinateNode(NamingUtil.getElementnameWeight(),null);
		// c.setFallbackDefault("1");
		return c;
	}
	
	protected INamedValue<ElementType> getWeightColumn(ITreeElement node, ITreeElement parent) {
		double weight = node.getWeight(parent);
		NamedValue<ElementType> column = new NamedValue<ElementType>(String.valueOf(weight));
		return column;
	}
	
	public Row fillRow(int current) {
	    List<ITreeElement> arow = getSourceRows().get(current);
		fillColumn((CoordinateNode) getTargetRow().getColumn(0), arow.get(0));
		fillColumn((CoordinateNode) getTargetRow().getColumn(1), arow.get(1));
		fillColumn((CoordinateNode) getTargetRow().getColumn(2), getWeightColumn(arow.get(1),arow.get(0)));
		return getTargetRow();
	}

	public Row setupRow(Row attributeRow) throws RuntimeException {
		//get a parent child row
		Row row = super.setupRow(attributeRow);
		//add the weight coordinate
		row.addColumn(getWeightCoordinate());
		return row;
	}

}
