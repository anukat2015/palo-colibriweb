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
package com.jedox.etl.components.extract;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jedox.etl.components.config.extract.DimensionFilterDefinition;
import com.jedox.etl.components.config.extract.DimensionFilterDefinition.TreeCondition;
import com.jedox.etl.components.config.extract.DimensionFilterDefinition.LogicalOperators;
import com.jedox.etl.core.component.RuntimeException;
import com.jedox.etl.core.logging.MessageHandler;
import com.jedox.etl.core.source.filter.Conditions.Condition;
import com.jedox.etl.core.source.filter.Conditions.Modes;
import com.jedox.etl.core.source.filter.EqualityEvaluator;
import com.jedox.etl.core.util.NamingUtil;
import com.jedox.palojlib.interfaces.*;

/**
 *
 * @author Kais Haddadin
 *
 */
public class DimensionFilter {

	private Map<String,Set<String>> acceptedLists = new HashMap<String,Set<String>>();
	private Map<String,Set<String>> deniedLists = new HashMap<String,Set<String>>();
	private Set<String> elementVisited = new HashSet<String>();
	private IDimension dimension;
	private DimensionFilterDefinition dimensionFilterDefinition;
	private static final Log log = new MessageHandler(LogFactory.getLog(DimensionFilter.class));	
	int cc = 0;
	boolean withAttributes;
	private HashMap<Integer,IElement> singleReadMap = null;
	//private DimensionFilter_exp parentFilter = null;
	
	public class NodeOrderComparator implements Comparator<String> {
		Map<String,Integer> positions = new HashMap<String,Integer>();
		
		public NodeOrderComparator() {
			IElement[] elements = getElements();
			for (int i=0; i<elements.length; i++) {
				positions.put(elements[i].getName(), i);
			}
		}

		@Override
		public int compare(String o1, String o2) {
			//if (positions.get(o1) == null || positions.get(o2) == null) return 0;
			return positions.get(o1).compareTo(positions.get(o2));
		}
		
	}

	public DimensionFilter(IDimension dimension,DimensionFilterDefinition dimensionFilterDefinition, boolean withAttributes) {
			
			this.withAttributes = withAttributes;
			this.dimension = dimension;
			this.dimensionFilterDefinition = dimensionFilterDefinition;
			
			if(isReadSingleElements()){
				String[] fieldNames = this.dimensionFilterDefinition.getFieldNames().toArray(new String[0]);
				if(fieldNames.length!=1 || !fieldNames[0].equals(NamingUtil.getElementnameElement()))
					log.error("Read single elements is not possible if the filter is applied on attributes as well.");
				
				Set<String> accepted = new HashSet<String>();
				acceptedLists.put(NamingUtil.getElementnameElement(), accepted);
				Set<String> denied = new HashSet<String>();
				deniedLists.put(NamingUtil.getElementnameElement(), denied);
			}else{

				//Use dimension cache while filtering
				dimension.getElements(withAttributes);
				dimension.setCacheTrustExpiry(3000);
				for(String field: this.dimensionFilterDefinition.getFieldNames()){
				
					Set<String> accepted = new HashSet<String>();
					accepted = getElementNamesSet();
					acceptedLists.put(field, accepted);
					Set<String> denied = new HashSet<String>();
					deniedLists.put(field, denied);
					
					// set the initial status is until here accept all
					// if no filters leave it as accept all and the Filter is an empty filter (not runnable)
					// or if there is no definition (only appear in case Cube Filter)
					if(dimensionFilterDefinition==null || dimensionFilterDefinition.getTreeConditions().size() == 0){
						return;
					}
					// if and only if the first filter is an accept then deny all
					else if(dimensionFilterDefinition.getTreeConditions(field).get(0).getMode().equals(Modes.ACCEPT)){
						denyAll(field);
					}
				}
			}
	}
	
	protected IElement getElementInSingleReadMap(int elementId){
		// if there no map at all, then it was not filtered in singleRead Operation
		if(singleReadMap==null)
			return null;
		// if there is a map, then it "must" deliver the element with the ID that is given 
		IElement e = singleReadMap.get(elementId);
		if(e==null)
			log.error("Element with id " + elementId + " does not exist in the single read element map.");
		return e;
	}
	
	protected IElement[] getElements() {
		return dimension.getElements(withAttributes);
	}
	
	protected Set<String> getElementNamesSet() {
		Set<String> result = new HashSet<String>();
		for (IElement e : getElements()) {
			result.add(e.getName());
		}
		return result;
	}

	protected void denyAll(String field) {
		deniedLists.get(field).addAll(acceptedLists.get(field));
		acceptedLists.get(field).clear();
	}

	protected void acceptAll(String field) {
		acceptedLists.get(field).addAll(deniedLists.get(field));
		deniedLists.get(field).clear();
	}
	protected void acceptOnlyBasis(String field) {
		acceptedLists.get(field).clear();
		deniedLists.get(field).clear();
		for(IElement e: getElements()){
			if(e.getChildCount()==0)
				acceptedLists.get(field).add(e.getName());
		}
	}

	protected void acceptRootToConsolidate(String field) {
		acceptedLists.get(field).clear();
		deniedLists.get(field).clear();
		for(IElement e:getElements()){
			if(e.getChildCount()!=0)
				acceptedLists.get(field).add(e.getName());
		}
	}

	public String getName() {
		return dimension.getName();
	}

	public boolean isEmpty(){
		return (dimensionFilterDefinition.getTreeConditions().size() == 0);
	}

	public void acceptRootElements(String field) {
		IElement[] elements = dimension.getRootElements(false);
		for (IElement element : elements) {
			acceptedLists.get(field).add(element.getName());
			deniedLists.get(field).remove(element.getName());
		}
	}


	public String[] process() {
		
		Set<String> fieldNames = this.dimensionFilterDefinition.getFieldNames();
		
		if(fieldNames.size()==0){
			IElement[] elements = dimension.getElements(false);
			String[] names = new String[elements.length];
			for (int i=0;i<elements.length;i++) {
				names[i] = elements[i].getName();
			}
			return names;
		}
		
		Set<String> finalResult = new TreeSet<String>(new NodeOrderComparator());
		if(this.dimensionFilterDefinition.getLogicalOperator().equals(LogicalOperators.OR)){
			for(String field:fieldNames){
				finalResult.addAll(acceptedLists.get(field));
			}
		}else{
			if(fieldNames.size()>0){
				Iterator<String> iter = fieldNames.iterator();
				finalResult.addAll(acceptedLists.get(iter.next()));
				while(iter.hasNext()){
					finalResult.retainAll(acceptedLists.get(iter.next()));
				}
			}
		}
		return finalResult.toArray(new String[finalResult.size()]);}


	public IElement[] getElements(String[] names) {
		ArrayList<IElement> elementList = new ArrayList<IElement>();
		for (int i=0; i<names.length; i++) {
			String name = names[i];
			if(!isReadSingleElements())
				elementList.add(dimension.getElementByName(name, withAttributes));
			else{
				if(singleReadMap==null){
					singleReadMap = new HashMap<Integer, IElement>();
				}
				IElement e = dimension.getSingleElement(name,withAttributes);
				if(e!=null){
					elementList.add(e);
					singleReadMap.put(((com.jedox.palojlib.main.Element)e).getId(), e);
				}
			}
		}
		return elementList.toArray(new IElement[0]);
	}

	protected ArrayList<String> filter(Condition a,Set<String> searchRange) {
		ArrayList<String> tempResult =  new ArrayList<String>();
		for(String s:searchRange){
			try {
				if(a.evaluate(s)){
						tempResult.add(s);
				}
			}
			catch (RuntimeException e) {
				log.warn("Cannot evaluate filter condition: "+e.getMessage());
			}

		}
		return tempResult;
	}

	private void filterOnlyNodes(String field,Modes m, List<String> filteredNodes, boolean allElementsInSearchRange){

		if(m.equals(Modes.ACCEPT)){
			for (String s: filteredNodes){
				acceptedLists.get(field).add(s);
				deniedLists.get(field).remove(s);
			}
		}
		else{
			for (String s: filteredNodes){
				acceptedLists.get(field).remove(s);
				deniedLists.get(field).add(s);
			}
		}
	}

	private void filterOnlyRoots(String field,Modes m, List<String> filteredNodes, boolean allElementsInSearchRange){

		if(allElementsInSearchRange){						
			for(String fn:filteredNodes){
				IElement e = dimension.getElementByName(fn, withAttributes);
				if(e.getParentCount()==0){
					acceptedLists.get(field).add(e.getName());
					deniedLists.get(field).remove(e.getName());
				}
			}
			 return;
		}
		
		for (String s: filteredNodes){
			if(!elementVisited.contains(s)){
				elementVisited.add(s);
				IElement e = dimension.getElementByName(s, withAttributes);
				if(e.getParentCount() != 0){
					ArrayList<String> parents = new ArrayList<String>();
					IElement[] parentsElements =  e.getParents();
					for(int i=0;i<parentsElements.length;i++)
						parents.add(parentsElements[i].getName());
					filterOnlyRoots(field,m,parents, allElementsInSearchRange);
				}
				else{
					if(m.equals(Modes.ACCEPT)){
						acceptedLists.get(field).add(s);
						deniedLists.get(field).remove(s);
					}
					else
					{
						acceptedLists.get(field).remove(s);
						deniedLists.get(field).add(s);
					}
				}
			}
		}
	}

	private void filterNodesToBases(String field,Modes m, List<String> filteredNodes, boolean allElementsInSearchRange){

		if(allElementsInSearchRange){	
			acceptAll(field, filteredNodes);
			return;
		}
		
		for (String s: filteredNodes){
			if(!elementVisited.contains(s)){
				elementVisited.add(s);
			if(m.equals(Modes.ACCEPT)){
				acceptedLists.get(field).add(s);
				deniedLists.get(field).remove(s);
				}
				else
				{
					acceptedLists.get(field).remove(s);
					deniedLists.get(field).add(s);
				}
			IElement e = dimension.getElementByName(s, withAttributes);
			if(e.getChildCount() != 0){
				ArrayList<String> children = new ArrayList<String>();
				IElement[] childrenElements =  e.getChildren();
				for(int i=0;i<childrenElements.length;i++)
					children.add(childrenElements[i].getName());
				filterNodesToBases(field,m,children,allElementsInSearchRange);
			}
		}
		}
	}

	private void filterNodesToConsolidates(String field,Modes m, List<String> filteredNodes, boolean allElementsInSearchRange){

		for (String s: filteredNodes){
			if(!elementVisited.contains(s)){
				elementVisited.add(s);
			IElement e = dimension.getElementByName(s, withAttributes);
			if(e.getChildCount() != 0){
				if(m.equals(Modes.ACCEPT)){
					acceptedLists.get(field).add(s);
					deniedLists.get(field).remove(s);
					}
					else
					{
						acceptedLists.get(field).remove(s);
						deniedLists.get(field).add(s);
					}
				ArrayList<String> children = new ArrayList<String>();
				IElement[] childrenElements =  e.getChildren();
				for(int i=0;i<childrenElements.length;i++)
					children.add(childrenElements[i].getName());
				filterNodesToConsolidates(field,m,children,allElementsInSearchRange);
			}
		}
		}
	}

	private void filterRootToNodes(String field, Modes m, List<String> filteredNodes, boolean allElementsInSearchRange){

		if(allElementsInSearchRange){	
			acceptAll(field, filteredNodes);
			return;
		}
		
		for (String s: filteredNodes){
			if(!elementVisited.contains(s)){
				elementVisited.add(s);
			if(m.equals(Modes.ACCEPT)){
				acceptedLists.get(field).add(s);
				deniedLists.get(field).remove(s);
				}
				else
				{
					acceptedLists.get(field).remove(s);
					deniedLists.get(field).add(s);
				}
			IElement e = dimension.getElementByName(s, withAttributes);
			if(e != null && e.getParentCount() != 0){
				ArrayList<String> parents = new ArrayList<String>();
				IElement[] parentsElements =  e.getParents();
				for(int i=0;i<parentsElements.length;i++)
					parents.add(parentsElements[i].getName());
				filterRootToNodes(field,m,parents,allElementsInSearchRange);
			}
		}
		}
	}

	private void acceptAll(String field, List<String> filteredNodes) {
		// add ALL to accepted
		for(String fn:filteredNodes){
			acceptedLists.get(field).add(fn);
			deniedLists.get(field).remove(fn);
		}
		 return;
	}

	private void filterRootToBases(String field, Modes m, List<String> filteredNodes, boolean allElementsInSearchRange){

		if(allElementsInSearchRange){	
			acceptAll(field, filteredNodes);
			return;
		}
		
		filterRootToNodes(field, m, filteredNodes, allElementsInSearchRange);
		elementVisited.clear();
		filterNodesToBases(field,m, filteredNodes, allElementsInSearchRange);
	}
	
	private void filterRootToConsolidates(String field,Modes m, List<String> filteredNodes, boolean allElementsInSearchRange){

		if(allElementsInSearchRange){						
			for(String fn:filteredNodes){
				IElement e = dimension.getElementByName(fn, withAttributes);
				if(e.getChildCount()!=0){
					acceptedLists.get(field).add(e.getName());
					deniedLists.get(field).remove(e.getName());
				}
			}
			 return;
		}
		
		//replace the leaves with their parents if existed (if they are also roots, ignore them)
		ArrayList<String> processedFilteredNodes = new ArrayList<String>();
		for(String s:filteredNodes){
			IElement e = dimension.getElementByName(s, withAttributes);
			if(e.getChildCount() == 0){
				for(IElement p:e.getParents()){
					processedFilteredNodes.add(p.getName());
				}
			}
			else{
				processedFilteredNodes.add(e.getName());
			}
		}

		filterRootToNodes(field,m, processedFilteredNodes, allElementsInSearchRange);
		elementVisited.clear();
		filterNodesToConsolidates(field,m, processedFilteredNodes, allElementsInSearchRange);
	}

	private void filterOnlyBases(String field, Modes m, List<String> filteredNodes, boolean allElementsInSearchRange){

		if(allElementsInSearchRange){						
			for(String fn:filteredNodes){
				IElement e = dimension.getElementByName(fn, withAttributes);
				if(e.getChildCount()==0){
					acceptedLists.get(field).add(e.getName());
					deniedLists.get(field).remove(e.getName());
				}
			}
			 return;
		}
		
		for (String s: filteredNodes){
			if(!elementVisited.contains(s)){

				elementVisited.add(s);
				IElement e = dimension.getElementByName(s, withAttributes);;
				if(e.getChildCount() != 0){
					ArrayList<String> children = new ArrayList<String>();
					IElement[] childrenElements =  e.getChildren();
					for(int i=0;i<childrenElements.length;i++)
						children.add(childrenElements[i].getName());
					filterOnlyBases(field,m,children,allElementsInSearchRange);
				}
				else{
					if(m.equals(Modes.ACCEPT)){
						acceptedLists.get(field).add(s);
						deniedLists.get(field).remove(s);
					}
					else
					{
						acceptedLists.get(field).remove(s);
						deniedLists.get(field).add(s);
					}
				}
			}
		}
	}
	
	protected Map<String,List<String>> mapAttributeRange(String searchAttribute, Set<String> elementSet) {
		Map<String,List<String>> result = new HashMap<String,List<String>>();
		for (String elementName : elementSet) {
			IElement element = dimension.getElementByName(elementName, withAttributes);;
			if (element != null) {
				Object attributeValue = element.getAttributeValue(searchAttribute);
				if (attributeValue == null || attributeValue.toString().trim().isEmpty()) {
					// to be able to filter attributes using isEmpty
					attributeValue = "";
				}
					
				List<String> elements = result.get(attributeValue);
				if (elements == null) {
					elements = new ArrayList<String>();
					result.put(attributeValue.toString(), elements);
				}
				elements.add(element.getName());
				
			}
		}
		return result;
	}

	public void configure() {
		//filter all elements
		for(String field: dimensionFilterDefinition.getFieldNames()){
			List<TreeCondition> fieldConditions = dimensionFilterDefinition.getTreeConditions(field);
			for (int i=0;i<fieldConditions.size();i++) {
				if(isReadSingleElements()){
					String value  = ((EqualityEvaluator)(fieldConditions.get(i).getEvaluator())).getEqualityValue().toString();
					if (dimension.getElementByName(value, false) != null) {
						acceptedLists.get(field).add(value);
					}
				}else{
					Set<String> searchRange =  new HashSet<String>();
					TreeCondition tc = fieldConditions.get(i);
					if((tc.getMode().equals(Modes.ACCEPT))){ 	
						searchRange = getElementNamesSet();
					}else
						searchRange.addAll(acceptedLists.get(field));

					ArrayList<String> tempResult = null;
					if (tc.getFieldName().equals(NamingUtil.getElementnameElement())) {
						tempResult = filter(tc,searchRange);
					} else {
						Map<String,List<String>> nameMap = mapAttributeRange(tc.getFieldName(),searchRange);
						ArrayList<String> attributeResult = filter(tc,nameMap.keySet());
						Set<String> tempResultSet = new HashSet<String>();
						for (String s : attributeResult) {
							tempResultSet.addAll(nameMap.get(s));
						}
						tempResult = new ArrayList<String>();
						tempResult.addAll(tempResultSet);
						
					}
					
					boolean allElementsInSearchRange = tc.getMode().equals(Modes.ACCEPT) && tempResult.size()==searchRange.size();
										
					switch (tc.getFilterMode()) {
					case nodesToBases : {
						filterNodesToBases(field,tc.getMode(),tempResult,allElementsInSearchRange);
						elementVisited.clear();
						break;
					}
					case rootToBases: {
						filterRootToBases(field,tc.getMode(),tempResult,allElementsInSearchRange);
						elementVisited.clear();
						break;
					}
					case rootToNodes: {
						filterRootToNodes(field,tc.getMode(),tempResult,allElementsInSearchRange);
						elementVisited.clear();
						break;
					}
					case onlyBases: {
						filterOnlyBases(field,tc.getMode(),tempResult,allElementsInSearchRange);
						elementVisited.clear();
						break;
					}
					case onlyNodes: {
						// no effect for allElementsInSearchRange
						filterOnlyNodes(field,tc.getMode(),tempResult,allElementsInSearchRange);
						elementVisited.clear();
						break;
					}
					case onlyRoots: {						
						filterOnlyRoots(field,tc.getMode(),tempResult,allElementsInSearchRange);
						elementVisited.clear();
						break;
					}
					case rootToConsolidates: {
						filterRootToConsolidates(field,tc.getMode(),tempResult,allElementsInSearchRange);
						elementVisited.clear();
						break;
					}
					default: break;
					}
					/*for testing
					 * System.out.println( "  My result after " + a.getFilterMode());*/
					/*for(String s:acceptedLists.get(field)){
				System.out.println( s + " - ");
			}*/
				}
			}
		}
	}
	
	// special logic for single elements only available for palojlib dimensions (not in IElement, used for XMLA/Olap4j-Wrapper) 
	public boolean isReadSingleElements() {
		return (dimension instanceof com.jedox.palojlib.main.Dimension && dimensionFilterDefinition.isReadSingleElements());
	}


}
