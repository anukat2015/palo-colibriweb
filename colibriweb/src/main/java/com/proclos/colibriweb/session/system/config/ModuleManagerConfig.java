package com.proclos.colibriweb.session.system.config;

import java.util.ArrayList;
import java.util.List;

public class ModuleManagerConfig
{
	private List<ModuleConfig> moduleConfigs = new ArrayList<ModuleConfig>();

	public void addModuleConfig(int pos, ModuleConfig config)
	{
		moduleConfigs.add(pos, config);
	}

	public void addModuleConfig(ModuleConfig config)
	{
		moduleConfigs.add(config);
	}

	public List<ModuleConfig> getModuleConfigs()
	{
		return moduleConfigs;
	}

}
