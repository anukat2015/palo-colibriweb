package com.proclos.colibriweb.session.system.config;

import java.util.ArrayList;
import java.util.List;

import at.adaptive.components.common.StringUtil;

import com.proclos.colibriweb.session.modules.IModule;

public class ModuleContainer
{
	private IModule<?> parent;
	private List<IModule<?>> children;

	public ModuleContainer(IModule<?> parent)
	{
		super();
		this.parent = parent;
		this.children = new ArrayList<IModule<?>>();
	}

	public void addChild(IModule<?> child)
	{
		children.add(child);
	}

	@Override
	public boolean equals(Object obj)
	{
		return parent.equals(((ModuleContainer)obj).getParent());
	}

	public List<IModule<?>> getChildren()
	{
		return children;
	}

	public IModule<?> getParent()
	{
		return parent;
	}

	@Override
	public int hashCode()
	{
		return parent.hashCode();
	}

	public boolean isChildSelected(String child)
	{
		if(StringUtil.isEmpty(child))
		{
			return false;
		}
		for(IModule<?> module : children)
		{
			if(module.getConfig().getName().equals(child))
			{
				return true;
			}
		}
		return false;
	}
}
