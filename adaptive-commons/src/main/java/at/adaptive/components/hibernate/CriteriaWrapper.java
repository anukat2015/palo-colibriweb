package at.adaptive.components.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Subqueries;

public class CriteriaWrapper
{
	private static final Logger logger = Logger.getLogger(CriteriaWrapper.class);

	public static final String CRITERIA_ROOT_NAME = "ROOT";

	private CriterionContainer root = new CriterionContainer(CRITERIA_ROOT_NAME, null);

	private Session session;
	private Class<?> entityClass;
	private boolean useSubSelectIdFetching = false;
	private String idPropertyName = "id";
	private List<AliasContainer> aliasContainers = new ArrayList<AliasContainer>();

	public CriteriaWrapper(Session session, Class<?> entityClass)
	{
		this.session = session;
		this.entityClass = entityClass;
	}

	public void addaliasContainer(String path, String alias, int joinType)
	{
		addAliasContainer(new AliasContainer(path, alias, joinType));
	}

	public void addAliasContainer(AliasContainer aliasContainer)
	{
		aliasContainers.add(aliasContainer);
	}

	public void addCriterion(AssociationPath associationPath, Criterion criterion)
	{
		// List<String> propertyNames = splitPath(path);
		updateCriterionContainerTree(associationPath.iterator(), root, criterion, associationPath.getGroup());
	}

	public void addCriterion(Criterion criterion)
	{
		addCriterion(new AssociationPath(), criterion);
	}

	// public void add(AssociationPath associationPath, Criterion criterion)
	// {
	// addCriterion(associationPath, criterion);
	// }
	//
	// public void add(Criterion criterion)
	// {
	// addCriterion(new AssociationPath(), criterion);
	// }
	//
	// public void add(String associationPath, Criterion criterion)
	// {
	// addCriterion(new AssociationPath(associationPath), criterion);
	// }

	public void addCriterion(String associationPath, Criterion criterion)
	{
		addCriterion(new AssociationPath(associationPath), criterion);
	}

	public void addOrder(AssociationPath associationPath, Order order)
	{
		// List<String> propertyNames = splitPath(path);
		updateCriterionContainerTree(associationPath.iterator(), root, order, associationPath.getGroup());
	}

	public void addOrder(Order order)
	{
		addOrder(new AssociationPath(), order);
	}

	public void addOrder(String associationPath, Order order)
	{
		addOrder(new AssociationPath(associationPath), order);
	}

	public void clearOrders()
	{
		root.clearAllOrders();
	}

	// CSCHW Optimized count strategy
	public int countResults()
	{
		Criteria criteria = null;
		if(aliasContainers.isEmpty())
		{
			criteria = createCriteria(true); // delivers normal (no subselect) criteria in this case
			criteria.setProjection(Projections.rowCount());
		}
		else
		{
			criteria = session.createCriteria(entityClass);
			updateCriteria(criteria, false, null, true);
			for(AliasContainer aliasContainer : aliasContainers)
			{
				criteria.createAlias(aliasContainer.getPath(), aliasContainer.getAlias(), aliasContainer.getJoinType());
			}
			criteria.setProjection(Projections.countDistinct("id"));
		}
		return ((Long)criteria.list().get(0)).intValue();
	}

	public Criteria createCriteria()
	{
		return createCriteria(false);
	}

	public void addAliasMap(Map<String, AliasContainer> newAliases)
	{
		Map<String, AliasContainer> existing = new HashMap<String, AliasContainer>();
		for(AliasContainer a : getAliasContainers())
		{
			existing.put(a.getPath(), a);
		}
		for(AliasContainer a : newAliases.values())
		{
			if(!existing.containsKey(a.getPath()))
			{
				addAliasContainer(a);
				existing.put(a.getPath(), a);
			}
			else
			{
				if(a.getJoinType() == CriteriaSpecification.LEFT_JOIN)
				{ // left join overrules inner join
					logger.warn("Filter criteria specify different join types. LEFT_JOIN takes precedence.");
					aliasContainers.remove(existing.get(a.getPath()));
					addAliasContainer(a);
					existing.put(a.getPath(), a);
				}
			}

		}
	}

	public Criteria createNonUniqueCriteria()
	{
		Criteria criteria = session.createCriteria(entityClass);
		updateCriteria(criteria, true, null, true);
		for(AliasContainer aliasContainer : aliasContainers)
		{
			criteria.createAlias(aliasContainer.getPath(), aliasContainer.getAlias(), aliasContainer.getJoinType());
		}
		return criteria;
	}

	public Criteria createCriteria(boolean ignoreOrders)
	{
		Criteria criteria = session.createCriteria(entityClass);
		boolean subSelect = false;
		// check for sub-select id fetching
		if(useSubSelectIdFetching)
		{
			if(idPropertyName == null)
			{
				// no id property specified
				logger.warn("Sub-Select id-fetching has been enabled, but no id property name has been set! Using defaults...");
			}
			else
			{
				// do subselect
				subSelect = true;
			}
		}
		if(subSelect || !aliasContainers.isEmpty())
		{
			// check for any groups present
			List<String> groups = getGroups();
			if(groups.size() > 0)
			{
				// updateCriteria(criteria, true, null, ignoreOrders); commented by cschw, since we use no else
				// iterate over groups
				for(String group : groups)
				{
					// create a detached criteria for every group
					DetachedCriteria groupDetachedCriteria = DetachedCriteria.forClass(entityClass);
					int criterionsAdded = updateDetachedCriteria(groupDetachedCriteria, false, group, true); // ignore orders subselect, since postgres cannot project on distinct id else
					if(criterionsAdded > 0)
					{
						groupDetachedCriteria.setProjection(Projections.distinct(Projections.id()));
						criteria.add(Subqueries.propertyIn(idPropertyName, groupDetachedCriteria));
					}
				}
			}
			// determine existing implicit aliases

			// manage root level alias filter constructs
			DetachedCriteria detachedCriteria = DetachedCriteria.forClass(entityClass);
			int criterionsAdded = updateDetachedCriteria(detachedCriteria, true, null, true); // ignore groups and orders
			if(criterionsAdded > 0)
			{
				detachedCriteria.setProjection(Projections.distinct(Projections.id()));
				Set<String> existing = root.getAllChildrenAssociationPaths();
				for(AliasContainer aliasContainer : aliasContainers)
				{
					if(!existing.contains(aliasContainer.getPath())) detachedCriteria.createAlias(aliasContainer.getPath(), aliasContainer.getAlias(), aliasContainer.getJoinType());
				}
				criteria.add(Subqueries.propertyIn("id", detachedCriteria));
				for(Order o : root.getOrders())
				{ // add root orders only
					criteria.addOrder(o);
				}
			}
		}
		else
		{
			updateCriteria(criteria, false, null, ignoreOrders);
		}
		return criteria;
	}

	public DetachedCriteria createDetachedCriteria()
	{
		return createDetachedCriteria(false);
	}

	public DetachedCriteria createDetachedCriteria(boolean ignoreOrders)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(entityClass);
		updateDetachedCriteria(criteria, false, null, ignoreOrders);
		return criteria;
	}

	public List<AliasContainer> getAliasContainers()
	{
		return aliasContainers;
	}

	public String getIdPropertyName()
	{
		return idPropertyName;
	}

	public boolean isUseSubSelectIdFetching()
	{
		return useSubSelectIdFetching;
	}

	public void setAliasContainers(List<AliasContainer> aliasContainers)
	{
		this.aliasContainers = aliasContainers;
	}

	public void setIdPropertyName(String idPropertyName)
	{
		this.idPropertyName = idPropertyName;
	}

	public void setUseSubSelectIdFetching(boolean useSubSelectIdFetching)
	{
		this.useSubSelectIdFetching = useSubSelectIdFetching;
	}

	// private List<String> splitPath(String path)
	// {
	// path = unifyPath(path);
	// return Arrays.asList(path.split("\\."));
	// }

	// private String unifyPath(String path)
	// {
	// if(path == null)
	// {
	// path = "";
	// }
	// path = CRITERIA_ROOT_NAME + "." + path;
	// return path;
	// }
	public int updateCriteria(Criteria criteria, boolean ignoreGroups, String group)
	{
		return updateCriteria(criteria, ignoreGroups, group, false);
	}

	public int updateCriteria(Criteria criteria, boolean ignoreGroups, String group, boolean ignoreOrders)
	{
		return updateCriteria(0, criteria, root, ignoreGroups, group, ignoreOrders);
	}

	public int updateDetachedCriteria(DetachedCriteria criteria, boolean ignoreGroups, String group)
	{
		return updateDetachedCriteria(criteria, ignoreGroups, group, false);
	}

	public int updateDetachedCriteria(DetachedCriteria criteria, boolean ignoreGroups, String group, boolean ignoreOrders)
	{
		return updateDetachedCriteria(0, criteria, root, ignoreGroups, group, ignoreOrders);
	}

	private List<String> getGroups()
	{
		List<String> groups = new ArrayList<String>();
		updateGroups(root, groups);
		return groups;
	}

	private int updateCriteria(int count, Criteria criteria, CriterionContainer criterionContainer, boolean ignoreGroups, String group, boolean ignoreOrders)
	{
		List<CriterionContainer> children = criterionContainer.getChildren();
		for(Iterator<CriterionContainer> childIterator = children.iterator(); childIterator.hasNext();)
		{
			CriterionContainer child = childIterator.next();
			if(ignoreGroups && child.getGroup() != null)
			{
				count = updateCriteria(count, criteria, child, ignoreGroups, group, ignoreOrders);
			}
			else
			{
				boolean create = false;
				if(group != null)
				{
					if(child.getGroup() != null && group.equals(child.getGroup()))
					{
						create = true;
					}
					else
					{
						count = updateCriteria(count, criteria, child, ignoreGroups, group, ignoreOrders);
					}
				}
				else
				{
					create = true;
				}
				if(create)
				{
					Criteria subCriteria = criteria.createCriteria(child.getPath(), child.getAlias(), Criteria.LEFT_JOIN);
					count = updateCriteria(count, subCriteria, child, ignoreGroups, group, ignoreOrders);
				}
			}
		}
		if(group != null)
		{
			if(criterionContainer.getGroup() == null || !group.equals(criterionContainer.getGroup()))
			{
				return count;
			}
		}
		else if(ignoreGroups && criterionContainer.getGroup() != null)
		{
			return count;
		}
		List<Criterion> criterions = criterionContainer.getCriterions();
		for(Iterator<Criterion> criterionsIterator = criterions.iterator(); criterionsIterator.hasNext();)
		{
			Criterion criterion = criterionsIterator.next();
			if(criterion != null)
			{
				criteria.add(criterion);
				count++;
			}
		}
		if(!ignoreOrders)
		{
			List<Order> orders = criterionContainer.getOrders();
			for(Iterator<Order> ordersIterator = orders.iterator(); ordersIterator.hasNext();)
			{
				Order order = ordersIterator.next();
				criteria.addOrder(order);
				count++;
			}
		}
		return count;
	}

	private void updateCriterionContainerTree(Iterator<String> iterator, CriterionContainer criterionContainer, Criterion criterion, String group)
	{
		if(iterator != null && iterator.hasNext())
		{
			String path = iterator.next();
			CriterionContainer child = criterionContainer.findChild(path, group);
			if(child == null)
			{
				child = new CriterionContainer(path, group);
				criterionContainer.addChild(child);
			}
			updateCriterionContainerTree(iterator, child, criterion, group);
		}
		else
		{
			criterionContainer.addCriterion(criterion);
		}
	}

	private void updateCriterionContainerTree(Iterator<String> iterator, CriterionContainer criterionContainer, Order order, String group)
	{
		if(iterator != null && iterator.hasNext())
		{
			String path = iterator.next();
			CriterionContainer child = criterionContainer.findChild(path, group);
			if(child == null)
			{
				child = new CriterionContainer(path, group);
				criterionContainer.addChild(child);
			}
			updateCriterionContainerTree(iterator, child, order, group);
		}
		else
		{
			criterionContainer.addOrder(order);
		}
	}

	private int updateDetachedCriteria(int count, DetachedCriteria criteria, CriterionContainer criterionContainer, boolean ignoreGroups, String group, boolean ignoreOrders)
	{
		List<CriterionContainer> children = criterionContainer.getChildren();
		for(Iterator<CriterionContainer> childIterator = children.iterator(); childIterator.hasNext();)
		{
			CriterionContainer child = childIterator.next();
			if(ignoreGroups && child.getGroup() != null)
			{
				count = updateDetachedCriteria(count, criteria, child, ignoreGroups, group, ignoreOrders);
			}
			else
			{
				boolean create = false;
				if(group != null)
				{
					if(child.getGroup() != null && group.equals(child.getGroup()))
					{
						create = true;
					}
					else
					{
						count = updateDetachedCriteria(count, criteria, child, ignoreGroups, group, ignoreOrders);
					}
				}
				else
				{
					create = true;
				}
				if(create)
				{
					DetachedCriteria subCriteria = criteria.createCriteria(child.getPath(), child.getAlias(), Criteria.LEFT_JOIN);
					count = updateDetachedCriteria(count, subCriteria, child, ignoreGroups, group, ignoreOrders);
				}
			}
		}
		if(group != null)
		{
			if(criterionContainer.getGroup() == null || !group.equals(criterionContainer.getGroup()))
			{
				return count;
			}
		}
		else if(ignoreGroups && criterionContainer.getGroup() != null)
		{
			return count;
		}
		List<Criterion> criterions = criterionContainer.getCriterions();
		for(Iterator<Criterion> criterionsIterator = criterions.iterator(); criterionsIterator.hasNext();)
		{
			Criterion criterion = criterionsIterator.next();
			if(criterion != null)
			{
				criteria.add(criterion);
				count++;
			}
		}
		if(!ignoreOrders)
		{
			List<Order> orders = criterionContainer.getOrders();
			for(Iterator<Order> ordersIterator = orders.iterator(); ordersIterator.hasNext();)
			{
				Order order = ordersIterator.next();
				criteria.addOrder(order);
				count++;
			}
		}
		return count;
	}

	private void updateGroups(CriterionContainer criterionContainer, List<String> groups)
	{
		List<CriterionContainer> children = criterionContainer.getChildren();
		for(Iterator<CriterionContainer> childIterator = children.iterator(); childIterator.hasNext();)
		{
			CriterionContainer child = childIterator.next();
			if(child.getGroup() != null && !groups.contains(child.getGroup()))
			{
				groups.add(child.getGroup());
			}
			updateGroups(child, groups);
		}
	}

	private class CriterionContainer
	{
		private String path;
		private String group;
		private List<Criterion> criterions;
		private List<Order> orders;
		private CriterionContainer parent;
		private List<CriterionContainer> children;

		public CriterionContainer(String path, String group)
		{
			this.path = path;
			this.group = group;
			this.criterions = new ArrayList<Criterion>(0);
			this.orders = new ArrayList<Order>(0);
			this.children = new ArrayList<CriterionContainer>(0);
		}

		public void addChild(CriterionContainer child)
		{
			children.add(child);
			child.setParent(this);
		}

		public void addCriterion(Criterion criterion)
		{
			if(criterion != null)
			{
				criterions.add(criterion);
			}
		}

		public void addOrder(Order order)
		{
			if(order != null)
			{
				orders.add(order);
			}
		}

		@Override
		public boolean equals(Object obj)
		{
			if(obj instanceof CriterionContainer)
			{
				CriterionContainer criterionContainer = (CriterionContainer)obj;
				if(this.path != null && criterionContainer.getPath() != null)
				{
					return this.getPath().equals(criterionContainer.getPath());
				}
				else if(this.getPath() == null && criterionContainer.getPath() == null)
				{
					return true;
				}
			}
			return false;
		}

		public CriterionContainer findChild(String path, String group)
		{
			if(this.path.equals(path) && groupEquals(group))
			{
				return this;
			}
			for(Iterator<CriterionContainer> iterator = children.iterator(); iterator.hasNext();)
			{
				CriterionContainer child = iterator.next();
				CriterionContainer child2 = child.findChild(path, group);
				if(child2 != null)
				{
					return child2;
				}
			}
			return null;
		}

		public List<CriterionContainer> getChildren()
		{
			return children;
		}

		public List<Criterion> getCriterions()
		{
			return criterions;
		}

		public String getGroup()
		{
			return group;
		}

		public List<Order> getOrders()
		{
			return orders;
		}

		public CriterionContainer getParent()
		{
			return parent;
		}

		public String getPath()
		{
			return path;
		}

		@Override
		public int hashCode()
		{
			return path.hashCode();
		}

		private boolean groupEquals(String group)
		{
			if(this == root)
			{
				return true;
			}
			if(this.group == null && group == null || this.group == group)
			{
				return true;
			}
			if((this.group == null && group != null) || (this.group != null && group == null))
			{
				return false;
			}
			return this.group.equals(group);
		}

		private void setParent(CriterionContainer parent)
		{
			this.parent = parent;
		}

		public void clearAllOrders()
		{
			for(CriterionContainer c : getChildren())
			{
				c.clearAllOrders();
			}
			orders.clear();
		}
		
		public String getFullPath() {
			StringBuffer path = new StringBuffer(getPath());
			CriterionContainer p = getParent();
			while (p != null && p != root) {
				path.insert(0, p.getPath()+".");
				p = p.getParent();
			}
			return path.toString();
		}
		
		public String getAlias() {
			return getFullPath().replace(".", "_");
		}

		public Set<String> getAllChildrenAssociationPaths()
		{
			Set<String> result = new HashSet<String>();
			for(CriterionContainer childContainer : getChildren())
			{
				if(childContainer.getPath() != null) result.add(childContainer.getFullPath());
				result.addAll(childContainer.getAllChildrenAssociationPaths());
			}
			return result;
		}

		/*
		 * //TODO CSCHW: this is a hack to get the property name. Anyone implementing a better solution is welcome. private String getOrderProperty(Order order) { return
		 * order.toString().split(" ")[0]; }
		 * 
		 * public Set<String> getOrderProperties() { Set<String> orderset = new HashSet<String>(); for (CriterionContainer c : getChildren()) { orderset.addAll(c.getOrderProperties()); } for (Order o
		 * : orders) { orderset.add((this != root ? path+"." : "")+getOrderProperty(o)); } return orderset; }
		 */

	}
}
