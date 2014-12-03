package com.proclos.colibriweb.session.modules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.primefaces.model.TreeNode;

import at.adaptive.components.common.CollectionUtil;
import at.adaptive.components.common.StringUtil;


public class MetadataTreeFilter implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4500373748291105014L;

	private String filter;

	private TreeNode content;
	private Set<String> filteredItems = new LinkedHashSet<String>();
	private List<TreeNode> rootNodes = new ArrayList<TreeNode>();
	

	public Boolean adviseNodeOpened(TreeNode item)
	{
		if(StringUtil.isEmpty(filter))
		{
			return false;
		}
		if(isFiltered(item))
		{
			return Boolean.TRUE;
		}
		return false;
	}

	public List<TreeNode> autocomplete(Object suggest)
	{
		String filter = (String)suggest;
		if(StringUtil.isEmpty(filter) || filter.length() < 2)
		{
			return new ArrayList<TreeNode>(0);
		}
		List<TreeNode> itemList = new ArrayList<TreeNode>();
		for(TreeNode metadata : content.getChildren())
		{
			addItemsToList(metadata, itemList, filter);
		}
		return itemList;
	}

	public void filter()
	{
		long l1 = System.currentTimeMillis();
		filteredItems.clear();
		if(StringUtil.isNotEmpty(filter))
		{
			matchFilter(content,filteredItems);
			//for (TreeNode metadata: content.getMetadata()) matchFilter(metadata, filteredItems);
		}
		long l2 = System.currentTimeMillis();
		System.err.println("processing time: " + (l2 - l1) + " ms.");
	}

	public TreeNode getContent()
	{
		return content;
	}
	
	public List<TreeNode> getRootNodes() {
		return rootNodes;
	}

	public String getFilter()
	{
		return filter;
	}

	public boolean isFiltered(TreeNode item)
	{
		if(item == null)
		{
			return false;
		}
		if(StringUtil.isEmpty(filter))
		{
			return true;
		}
		return filteredItems.contains(item.getData());
	}

	public void resetFilter()
	{
		setFilter(null);
		filter();
	}

	public void setContent(TreeNode content)
	{
		this.content = content;
		rootNodes.clear();
		rootNodes.addAll(content.getChildren());
		resetFilter();
	}

	public void setFilter(String filter)
	{
		this.filter = filter;
	}

	private void addItemsToList(TreeNode metadata, List<TreeNode> itemList, String filter)
	{
		if(matches(filter, metadata))
		{
			itemList.add(metadata);
		}
		if(CollectionUtil.isNotEmpty(metadata.getChildren()))
		{
			for(TreeNode child : metadata.getChildren())
			{
				addItemsToList(child, itemList, filter);
			}
		}
	}

	private void addMatch(TreeNode item, Set<String> matched)
	{
		matched.add(item.getData().toString());
	}

	private boolean matches(String filter, TreeNode item)
	{
		return item.getData().toString().toLowerCase().contains(filter.toLowerCase());
	}

	private void matchFilter(TreeNode item, Set<String> matched)
	{
		if(matches(filter, item))
		{
			addMatch(item, matched);
			item.setExpanded(true);
			TreeNode parent = item.getParent();
			while(parent != null)
			{
				addMatch(parent, matched);
				parent.setExpanded(true);
				parent = parent.getParent();
			}
		} else {
			item.setExpanded(false);
		}
		for(TreeNode child : item.getChildren())
		{
			matchFilter(child, matched);
		}
	}
}
