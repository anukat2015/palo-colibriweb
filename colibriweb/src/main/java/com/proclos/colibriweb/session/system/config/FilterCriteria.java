package com.proclos.colibriweb.session.system.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jdom.Element;
import org.jdom.Namespace;

import at.adaptive.components.hibernate.AliasContainer;

import com.proclos.colibriweb.session.system.EntityUtil;
import com.proclos.colibriweb.session.system.TypeConversionUtil;

public class FilterCriteria
{

	public static enum Operators
	{
		and, 
		or, 
		not, 
		eq, 
		eqProperty, 
		ge, 
		geProperty, 
		gt, 
		gtProperty, 
		idEq, 
		ilike, 
		in, 
		isEmpty, 
		isNotEmpty, 
		isNotNull, 
		isNull, 
		le, 
		leProperty, 
		like, 
		lt, 
		ltProperty, 
		ne, 
		neProperty, 
		sizeEq, 
		sizeGe, 
		sizeGt, 
		sizeLe, 
		sizeLt, 
		sizeNe,
		sqExists,
		sqNotExists,
		sqPropertyEq,
		sqPropertyEqAll,
		sqPropertyGe,
		sqPropertyGeAll,
		sqPropertyGeSome,
		sqPropertyGt,
		sqPropertyGtAll,
		sqPropertyGtSome,
		sqPropertyIn,
		sqPropertyLe,
		sqPropertyLeAll,
		sqPropertyLeSome,
		sqPropertyLt,
		sqPropertyLtAll,
		sqPropertyLtSome,
		sqPropertyNe,
		sqPropertyNotIn;
	}

	public static enum Joins
	{
		inner, left
	}

	private Operators operator;
	private String field;
	private Object value;
	private Joins join;
	private Class<?> clazz;
	private boolean isPlaceholder = false;
	private List<FilterCriteria> criteria = new ArrayList<FilterCriteria>();
	private static final Log log = LogFactory.getLog(FilterCriteria.class);

	public FilterCriteria() {}
	
	public FilterCriteria(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	public void setOperator(Operators operator)
	{
		this.operator = operator;
	}

	public Operators getOperator()
	{
		return operator;
	}

	public void setField(String field)
	{
		this.field = field;
	}

	public String getField()
	{
		return field;
	}

	public void setValue(Object value)
	{
		this.value = value;
		if("?".equals(value)) isPlaceholder = true;
	}

	public Object getValue()
	{
		return value;
	}

	public void setJoin(Joins join)
	{
		this.join = join;
	}

	public Joins getJoin()
	{
		return join;
	}
	
	public boolean isNegation() {
		return Operators.not.equals(getOperator());
	}

	public boolean isJunction()
	{
		return Operators.and.equals(getOperator()) || Operators.or.equals(getOperator());
	}

	public List<FilterCriteria> getCriteria()
	{
		return criteria;
	}

	private String getAlias(String path, int join)
	{
		//return path;
		return path.replace(".", "_");
	}

	public String getFieldForRestriction()
	{
		String joinPath = getFieldJoinPath(field);
		if(joinPath != null)
		{
			return getAlias(joinPath, getCriteriaJoin(join)) + field.substring(field.lastIndexOf("."));
		}
		return field;
	}

	private String getValueAsString()
	{
		return (value == null) ? null : value.toString();
	}

	public String getPropertyValueForRestriction()
	{
		if(operator.toString().endsWith("Property"))
		{
			String joinPath = getFieldJoinPath(getValueAsString());
			if(joinPath != null)
			{
				return getAlias(joinPath, getCriteriaJoin(join)) + getValueAsString().substring(getValueAsString().lastIndexOf("."));
			}
		}
		return getValueAsString();
	}

	public Object getValueForRestriction()
	{
		if(value == null) return null;
		if(value instanceof String)
		{
			ValueExpression<Object> valueExpression = Expressions.instance().createValueExpression(getValueAsString());
			return valueExpression.getValue();
		}
		return value;
	}

	private String getFieldJoinPath(String field)
	{
		if(field == null) return null;
		int pos = field.lastIndexOf(".");
		if(pos == -1) return null;
		return field.substring(0, pos);
	}

	private AliasContainer createAlias(String path, int join)
	{
		if(path == null) return null;
		AliasContainer a = new AliasContainer(path, getAlias(path, join), join);
		return a;
	}

	public Map<String, AliasContainer> getJoinAlias()
	{
		Map<String, AliasContainer> result = new HashMap<String, AliasContainer>();
		AliasContainer a = createAlias(getFieldJoinPath(field), getCriteriaJoin(join));
		if(a != null)
		{
			result.put(a.getPath(), a);
		}
		if(operator.toString().endsWith("Property"))
		{
			AliasContainer va = createAlias(getFieldJoinPath(getValueAsString()), getCriteriaJoin(join));
			if(va != null) result.put(va.getPath(), va);
		}
		return result;
	}

	public Map<String, AliasContainer> getAllJoinAliases()
	{
		Map<String, AliasContainer> result = new HashMap<String, AliasContainer>();
		result.putAll(getJoinAlias());
		for(FilterCriteria c : criteria)
		{
			result.putAll(c.getAllJoinAliases());
		}
		for(AliasContainer a : result.values())
		{
			for(AliasContainer dependent : getContainersForPath(a))
			{
				if(!result.containsKey(dependent.getAlias())) result.put(dependent.getAlias(), dependent);
			}
		}
		return result;
	}

	private static FilterCriteria buildFromXML(FilterCriteria criteria, Element element, Namespace namespace)
	{
		criteria.setOperator(Operators.valueOf(element.getAttributeValue("op", "and")));
		String clazzName = element.getAttributeValue("clazz");
		if(clazzName != null)
		{
			String fullName = "com.proclos.colibriweb.entity." + clazzName;
			try
			{
				Class<?> clazz = Class.forName(fullName);
				criteria.setClazz(clazz);
			}
			catch(Exception e)
			{
				log.warn("Class " + fullName + " not found used in criteria definition.");
			}
		}
		for(Object o : element.getChildren("criterion", namespace))
		{
			Element criterionElement = (Element)o;
			FilterCriteria c = new FilterCriteria();
			c.setOperator(Operators.valueOf(criterionElement.getAttributeValue("op", "eq")));
			c.setField(criterionElement.getAttributeValue("field"));
			c.setValue(criterionElement.getAttributeValue("value"));
			c.setJoin(Joins.valueOf(criterionElement.getAttributeValue("join", "inner")));
			criteria.getCriteria().add(c);
		}
		for(Object o : element.getChildren("criteria", namespace))
		{
			Element criteriaElement = (Element)o;
			FilterCriteria c = new FilterCriteria();
			criteria.getCriteria().add(buildFromXML(c, criteriaElement, namespace));
		}
		return criteria;
	}

	public static FilterCriteria buildFromXML(Element element, Namespace namespace)
	{
		if(element != null)
		{
			return buildFromXML(new FilterCriteria(), element, namespace);
		}
		return null;
	}

	public int getCriteriaJoin(Joins join)
	{
		if(join == null) return -1;
		switch(join)
		{
			case left:
				return CriteriaSpecification.LEFT_JOIN;
			default:
				return CriteriaSpecification.INNER_JOIN;
		}
	}

	private Junction getCriteriaJunction(Operators operator)
	{
		switch(operator)
		{
			case and:
				return Restrictions.conjunction();
			default:
				return Restrictions.disjunction();
		}
	}

	private Integer getInteger(Object value)
	{
		if(value == null) return null;
		try
		{
			return Integer.parseInt(value.toString());
		}
		catch(Exception e)
		{
			return null;
		}
	}

	private Object convert(Class<?> clazz, String path, Object value)
	{
		EntityUtil eu = EntityUtil.getInstance();
		Field f = eu.getField(clazz, path);
		if(f != null)
		{
			Class<?> fclazz = f.getType();
			try
			{
				return TypeConversionUtil.convert(value, fclazz);
			}
			catch(Exception e)
			{
				log.warn("Restriction value conversion problem: Value " + value + " cannot be converted to " + fclazz.getCanonicalName() + ".");
				return null;
			}
		}
		else
		{
			log.warn("Field " + path + " cannot be found in " + clazz.getCanonicalName() + ".");
			return null;
		}
	}

	private Criterion getRestriction(FilterCriteria c)
	{
		switch(c.getOperator())
		{
			case eqProperty:
				return Restrictions.eqProperty(c.getFieldForRestriction(), c.getPropertyValueForRestriction());
			case geProperty:
				return Restrictions.geProperty(c.getFieldForRestriction(), c.getPropertyValueForRestriction());
			case gtProperty:
				return Restrictions.gtProperty(c.getFieldForRestriction(), c.getPropertyValueForRestriction());
			case leProperty:
				return Restrictions.leProperty(c.getFieldForRestriction(), c.getPropertyValueForRestriction());
			case neProperty:
				return Restrictions.neProperty(c.getFieldForRestriction(), c.getPropertyValueForRestriction());
			case ltProperty:
				return Restrictions.ltProperty(c.getFieldForRestriction(), c.getPropertyValueForRestriction());
			case ilike:
				return Restrictions.ilike(c.getFieldForRestriction(), c.getValueForRestriction());
			case like:
				return Restrictions.like(c.getFieldForRestriction(), c.getValueForRestriction());
			case isEmpty:
				return Restrictions.isEmpty(c.getFieldForRestriction());
			case isNotEmpty:
				return Restrictions.isNotEmpty(c.getFieldForRestriction());
			case isNotNull:
				return Restrictions.isNotNull(c.getFieldForRestriction());
			case isNull:
				return Restrictions.isNull(c.getFieldForRestriction());
			case in:
			{
				Object v = c.getValueForRestriction();
				if(v instanceof Collection) return Restrictions.in(c.getFieldForRestriction(), (Collection<?>)v);
				if(v != null && v.getClass().isArray())
				{
					return Restrictions.in(c.getFieldForRestriction(), (Object[])v);
				}
				return null;
			}
		}
		if(!c.getOperator().toString().startsWith("size"))
		{
			Object v = convert(c.getClazz(), c.getField(), c.getValueForRestriction());
			if(v == null) return null;
			switch(c.getOperator())
			{
				case eq:
					return Restrictions.eq(c.getFieldForRestriction(), v);
				case ge:
					return Restrictions.ge(c.getFieldForRestriction(), v);
				case gt:
					return Restrictions.gt(c.getFieldForRestriction(), v);
				case idEq:
					return Restrictions.idEq(v);
				case le:
					return Restrictions.le(c.getFieldForRestriction(), v);
				case lt:
					return Restrictions.lt(c.getFieldForRestriction(), v);
				case ne:
					return Restrictions.ne(c.getFieldForRestriction(), v);
			}
		}
		else
		{
			Integer v = getInteger(c.getValueForRestriction());
			if(v == null) return null;
			switch(c.getOperator())
			{
				case sizeEq:
					return Restrictions.sizeEq(c.getFieldForRestriction(), v);
				case sizeGe:
					return Restrictions.sizeGe(c.getFieldForRestriction(), v);
				case sizeGt:
					return Restrictions.sizeGt(c.getFieldForRestriction(), v);
				case sizeLe:
					return Restrictions.sizeLe(c.getFieldForRestriction(), v);
				case sizeNe:
					return Restrictions.sizeNe(c.getFieldForRestriction(), v);
			}
		}
		return null;
	}
	
	private boolean isSubquery() {
		return getOperator().toString().startsWith("sq");
	}
	
	private Criterion getSubqueryCriterion(FilterCriteria criteria, DetachedCriteria detachedCriteria) {
		switch(criteria.getOperator()) 
		{
		case sqExists: return Subqueries.exists(detachedCriteria);
		case sqNotExists: return Subqueries.notExists(detachedCriteria);
		case sqPropertyEq: return Subqueries.propertyEq(criteria.getField(), detachedCriteria);
		case sqPropertyEqAll:  return Subqueries.propertyEqAll(criteria.getField(), detachedCriteria);
		case sqPropertyGe:  return Subqueries.propertyGe(criteria.getField(), detachedCriteria);
		case sqPropertyGeAll:  return Subqueries.propertyGeAll(criteria.getField(), detachedCriteria);
		case sqPropertyGeSome:  return Subqueries.propertyGeSome(criteria.getField(), detachedCriteria);
		case sqPropertyGt:  return Subqueries.propertyGt(criteria.getField(), detachedCriteria);
		case sqPropertyGtAll: return Subqueries.propertyGtAll(criteria.getField(), detachedCriteria);
		case sqPropertyGtSome:  return Subqueries.propertyGtSome(criteria.getField(), detachedCriteria);
		case sqPropertyIn:  return Subqueries.propertyIn(criteria.getField(), detachedCriteria);
		case sqPropertyLe:  return Subqueries.propertyLe(criteria.getField(), detachedCriteria);
		case sqPropertyLeAll:  return Subqueries.propertyLeAll(criteria.getField(), detachedCriteria);
		case sqPropertyLeSome:  return Subqueries.propertyLeSome(criteria.getField(), detachedCriteria);
		case sqPropertyLt:  return Subqueries.propertyLt(criteria.getField(), detachedCriteria);
		case sqPropertyLtAll:  return Subqueries.propertyLtAll(criteria.getField(), detachedCriteria);
		case sqPropertyLtSome:  return Subqueries.propertyLtSome(criteria.getField(), detachedCriteria);
		case sqPropertyNe:  return Subqueries.propertyNe(criteria.getField(), detachedCriteria);
		case sqPropertyNotIn: return Subqueries.propertyNotIn(criteria.getField(), detachedCriteria);
		default: return null;
		}
	}

	private Criterion getCriterion(Class<?> clazz, FilterCriteria c)
	{
		if ((c.isNegation() || c.isJunction()) && c.getCriteria().isEmpty()) {
			log.warn("Operator "+getOperator().toString()+" may not be used on empty set. It is ignored.");
			return null;
		}
		if (c.isNegation()) {
			c.setOperator(Operators.and); //set operator to AND to interpret this criteria as negated and junction
			Criterion negation = Restrictions.not(getCriterion(clazz,c));
			c.setOperator(Operators.not);
			return negation;
		}
		else if(c.isJunction())
		{
			Junction j = getCriteriaJunction(c.getOperator());
			for(FilterCriteria cc : c.getCriteria())
			{
				Criterion crit = getCriterion(clazz, cc);
				if(cc != null)
				{
					j.add(crit);
				}
			}
			return j;
		}
		else if (c.isSubquery()) {
			DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getClazz());
			for(FilterCriteria cc : c.getCriteria())
			{
				Criterion crit = getCriterion(getClazz(), cc);
				if(cc != null)
				{
					detachedCriteria.add(crit);
				}
			}
			if (c.getValue() != null) detachedCriteria.setProjection(Projections.property(c.getValue().toString()));
			return getSubqueryCriterion(c,detachedCriteria);
		}
		else
		{
			if(c.getClazz() != null && !c.getClazz().equals(clazz))
			{
				log.warn("Exlicit class " + c.getClazz().getCanonicalName() + " set for criteria will be ignored, since criteria is used in context of given class " + clazz.getCanonicalName() + ".");
			}
			c.setClazz(clazz);
			Criterion result = getRestriction(c);
			if(result == null) log.warn("Criterion of type " + c.getOperator().toString() + " cannot be built on field " + c.getField() + " with value " + c.getValue() + ".");
			return result;
		}
	}

	public Criterion getCriterion(Class<?> clazz)
	{
		return getCriterion(clazz, this);
	}

	public Criterion getCriterion()
	{
		if(getClazz() != null) return getCriterion(getClazz(), this);
		else
		{
			log.warn("Class for criteria definition not set.");
			// this will give no results at all.
			return Restrictions.isNull("id");
		}
	}

	private List<AliasContainer> getContainersForPath(AliasContainer a)
	{
		List<AliasContainer> result = new ArrayList<AliasContainer>();
		result.add(a);
		String joinPath = getFieldJoinPath(a.getPath());
		// get all path step by step to be joined.
		while(joinPath != null)
		{
			AliasContainer dependent = createAlias(joinPath, a.getJoinType());
			result.add(dependent);
			joinPath = getFieldJoinPath(joinPath);
		}
		return result;
	}

	/*
	 * public void translateAliasesToWrapper(CriteriaWrapper wrapper) { wrapper.setIdPropertyName("id"); //wrapper.setUseSubSelectIdFetching(true); Map<String,AliasContainer> existing = new
	 * HashMap<String,AliasContainer>(); for (AliasContainer a : wrapper.getAliasContainers()) { existing.put(a.getAlias(), a); } for (AliasContainer a : getAllJoinAliases().values()) { if
	 * (!existing.containsKey(a.getAlias())) wrapper.addAliasContainer(a); existing.put(a.getAlias(), a); } }
	 */

	public void replacePlaceholder(Object value)
	{
		if(isPlaceholder) setValue(value);
		for(FilterCriteria c : criteria)
		{
			c.replacePlaceholder(value);
		}
	}

	private void setClazz(Class<?> clazz)
	{
		this.clazz = clazz;
		for(FilterCriteria c : criteria)
		{
			c.setClazz(clazz);
		}
	}

	public Class<?> getClazz()
	{
		return clazz;
	}

}
