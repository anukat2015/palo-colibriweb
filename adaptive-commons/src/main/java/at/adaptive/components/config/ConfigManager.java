package at.adaptive.components.config;

import static org.jboss.seam.ScopeType.APPLICATION;

import java.beans.XMLDecoder;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.ServletLifecycle;

import at.adaptive.components.common.StringUtil;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.hibernate.SessionFactory;
import org.hibernate.jmx.StatisticsService;
import org.hibernate.ejb.HibernateEntityManagerFactory;

import java.lang.management.ManagementFactory;

/**
 * Manager class for handling component configurations
 * 
 * @author Bernhard Hablesreiter
 * 
 */
@Name("at.adaptive.components.config.configManager")
@Scope(APPLICATION)
@Install(precedence = Install.FRAMEWORK)
@BypassInterceptors
@Startup
public class ConfigManager
{
	private static final Logger logger = Logger.getLogger(ConfigManager.class);

	private static final String DEFAULT_NAME = "DEFAULT";

	private Map<String, Map<String, BaseComponentConfig>> configs;

	/**
	 * Returns the instance of the restriction manager
	 * 
	 * @return the instance of the restriction manager
	 */
	public static ConfigManager instance()
	{
		if(!Contexts.isApplicationContextActive())
		{
			throw new IllegalStateException("No active application context");
		}
		ConfigManager instance = (ConfigManager)Component.getInstance(ConfigManager.class, ScopeType.APPLICATION);
		if(instance == null)
		{
			throw new IllegalStateException("No ConfigManager could be created");
		}
		return instance;
	}

	/**
	 * Creates a new instance of ConfigManager
	 */
	public ConfigManager()
	{
		readConfigs();
	}

	/**
	 * Reads the configurations
	 */
	private void readConfigs()
	{
		try
		{
			logger.info("Reading components configurations");
			configs = new HashMap<String, Map<String, BaseComponentConfig>>();
			// URI uri = new URI(System.getProperty("jboss.server.config.url"));
			// File configDir = new File(new File(uri), "adaptive");
			ServletContext servletContext = ServletLifecycle.getServletContext();
			File configDir = new File(servletContext.getRealPath("WEB-INF/config"));
			// URL url = ResourceLoader.instance().getResource("/config");
			if(configDir == null || !configDir.exists())
			{
				logger.warn("Config directory could not be found. If Adaptive component-configuration is required, place configuration files in \"xyz.ear/config\"");
				return;
			}
			// File configDir = new File(url.toURI());
			if(configDir.exists() && configDir.isDirectory())
			{
				// // get sub directories
				// File[] subDirectories = getAllSubDirectories(configDir);
				// for(File subDirectory : subDirectories)
				// {
				// readConfigs(subDirectory, subDirectory.getName());
				// }
				readConfigs(configDir, DEFAULT_NAME);
			}
			else
			{
				logger.error("Config dir does not exist: " + configDir.getAbsolutePath());
			}
		}
		catch(Exception e)
		{
			logger.error("Error reading component configurations", e);
		}
	}

	/**
	 * Reads the configurations for a specified directory and directory name
	 * 
	 * @param directory
	 *            the directory
	 * @param directoryName
	 *            the directory name
	 * @throws Exception
	 *             on error
	 */
	private void readConfigs(File directory, String directoryName) throws Exception
	{
		File[] configFiles = getAllConfigFiles(directory);
		for(int i = 0; i < configFiles.length; i++)
		{
			File configFile = configFiles[i];
			try
			{
				XMLDecoder decoder = new XMLDecoder(new FileInputStream(configFile));
				BaseComponentConfig config = (BaseComponentConfig)decoder.readObject();
				decoder.close();
				String componentName = config.getComponentName();
				if(StringUtil.isEmpty(componentName))
				{
					componentName = configFile.getName().substring(0, configFile.getName().lastIndexOf('.'));
				}
				logger.info("Configuration for component read: " + componentName);
				Map<String, BaseComponentConfig> configMap;
				if(configs.containsKey(directoryName))
				{
					configMap = configs.get(directoryName);
				}
				else
				{
					configMap = new HashMap<String, BaseComponentConfig>();
				}
				configMap.put(componentName, config);
				configs.put(directoryName, configMap);
			}
			catch(Exception e)
			{
				logger.info("Could not read configuration: " + configFile + ". Probably not a configurable base component. Skipping...");
			}
		}
	}

	/**
	 * Returns a list of all sub directories for a specified directory
	 * 
	 * @param configDir
	 *            the directory
	 * @return a list of all sub directories for the specified directory
	 */
	private File[] getAllSubDirectories(File configDir)
	{
		return configDir.listFiles(new FileFilter()
		{
			public boolean accept(File pathname)
			{
				return pathname.isDirectory();
			}
		});
	}

	/**
	 * Returns a list of all configuration files (ending with ".xml") for a specified directory
	 * 
	 * @param configDir
	 *            the directory
	 * @return a list of all configuration files (ending with ".xml") for the specified directory
	 */
	private File[] getAllConfigFiles(File configDir)
	{
		return configDir.listFiles(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".xml");
			}
		});
	}

	/**
	 * Returns the configuration for a specified component name
	 * 
	 * @param componentName
	 *            the compoment name
	 * @return the configuration for the specified name on success or <code>null</code> if the configuration could not be found
	 */
	public BaseComponentConfig getConfig(String componentName)
	{
		String contextPath = null;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if(facesContext != null)
		{
			ExternalContext externalContext = facesContext.getExternalContext();
			if(externalContext != null)
			{
				contextPath = externalContext.getRequestContextPath();
				if(contextPath != null)
				{
					contextPath = contextPath.replaceAll("\\/", "");
					if(configs.containsKey(contextPath))
					{
						return configs.get(contextPath).get(componentName);
					}
				}
			}
		}
		if(configs.containsKey(DEFAULT_NAME))
		{
			return configs.get(DEFAULT_NAME).get(componentName);
		}
		return null;
	}

	/**
	 * Resets the configuration manager
	 */
	public void reset()
	{
		readConfigs();
	}
}