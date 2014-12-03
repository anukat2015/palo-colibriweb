package com.proclos.colibriweb.session.system.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.log.Log;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.DOMBuilder;
import org.w3c.dom.Document;

import com.proclos.colibriweb.session.system.config.FilterCondition.Languages;
import com.proclos.colibriweb.session.system.config.FilterGroup.FilterModes;
import com.proclos.colibriweb.session.system.config.FilterGroup.JSFTypes;

@Name("moduleManagerConfigHandler")
@Scope(ScopeType.SESSION)
@AutoCreate
public class ModuleManagerConfigHandler implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5876913561608301000L;
	@Logger
	private Log logger;
	private ModuleManagerConfig moduleManagerConfig;

	public ModuleManagerConfig getModuleManagerConfig()
	{
		return moduleManagerConfig;
	}

	@SuppressWarnings("unchecked")
	public void loadConfiguration(String filePath, String xsdPath) throws ModuleConfigException
	{
		ConfigReader reader = new ConfigReader("Module", filePath, xsdPath);
		Document serviceDocument = reader.getConfigDocument();
		org.jdom.Document document = new DOMBuilder().build(serviceDocument);
		Element root = document.getRootElement();
		Namespace namespace = root.getNamespace();
		List<Element> models = root.getChildren("module", namespace);
		for(Element model : models)
		{
			ModuleConfig config = readModuleConfig(model, namespace);
			moduleManagerConfig.addModuleConfig(config);
			loadSubModules(model.getChild("submodules", namespace), config, namespace);
		}
	}

	@Create
	public void readConfig()
	{
		try
		{
			moduleManagerConfig = new ModuleManagerConfig();
			ServletContext ctx = ServletLifecycle.getServletContext();
			String filePath = ctx.getRealPath("/WEB-INF/config/modules/modules.xml");
			String xsdPath = ctx.getRealPath("/WEB-INF/config/modules/modules.xsd");
			loadConfiguration(filePath, xsdPath);
		}
		catch(Exception e)
		{
			logger.error("Error reading module manager config", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadFilters(Element filterElement, ModuleConfig config, Namespace namespace)
	{
		config.getFilters().clear();
		if(filterElement == null)
		{
			return;
		}
		List<Element> filterGroups = filterElement.getChildren();
		for(Element g : filterGroups)
		{
			List<FilterDefinition> definitions = new ArrayList<FilterDefinition>();
			List<Element> filterItems = g.getChildren();
			for(Element i : filterItems)
			{
				FilterDefinition d = new FilterDefinition();
				d.setName(i.getAttributeValue("name"));
				String cDefinition = i.getChildTextNormalize("condition", namespace);
				Element condition = i.getChild("condition", namespace);
				if(condition != null)
				{
					String cLanguage = i.getChild("condition", namespace).getAttributeValue("type", Languages.hql.toString());
					boolean cShortNotation = i.getChild("condition", namespace).getAttributeValue("short", String.valueOf(cLanguage.equals(Languages.hql.toString()))).equalsIgnoreCase("true");
					d.setCondition(cDefinition, cLanguage, cShortNotation);
				}
				Element criteria = i.getChild("criteria",namespace);
				if (criteria != null) {
					FilterCriteria fc = FilterCriteria.buildFromXML(criteria, namespace);
					d.setCriteria(fc);
				}
				d.setDefaultValue(i.getChildTextNormalize("default", namespace));
				definitions.add(d);
			}
			FilterGroup<?> filter = null;
			JSFTypes type = JSFTypes.valueOf(g.getName());
			String enabledExpression = g.getAttributeValue("enabled");
			switch(type)
			{
				case selectmanycheckbox: {
					filter = new ManyCheckboxFilter<String>(g.getAttributeValue("name"), definitions);
					filter.setMode(FilterModes.valueOf(g.getAttributeValue("mode", FilterModes.or.toString())));
					break;
				}
				case selectonemenu: {
					filter = new OneMenuFilter<String>(g.getAttributeValue("name"), definitions);
					filter.setMode(FilterModes.valueOf(g.getAttributeValue("mode", FilterModes.or.toString())));
					break;
				}
				case selectoneradio: {
					filter = new OneRadioFilter<String>(g.getAttributeValue("name"), definitions);
					filter.setMode(FilterModes.valueOf(g.getAttributeValue("mode", FilterModes.or.toString())));
					break;
				}
				case selectmanydate: {
					filter = new ManyDateFilter<Date>(g.getAttributeValue("name"), definitions);
					filter.setMode(FilterModes.valueOf(g.getAttributeValue("mode", FilterModes.and.toString())));
					break;
				}
				case nondisplay: {
					filter = new NonDisplayFilter<String>(g.getAttributeValue("name"), definitions);
					filter.setMode(FilterModes.valueOf(g.getAttributeValue("mode", FilterModes.or.toString())));
					filter.setDisplay(false);
					break;
				}
				default:
					logger.error("Filter type " + type.toString() + " not supported.");
			}
			if(filter != null)
			{
				filter.setEnabledExpression(enabledExpression);
				config.getFilters().add(filter);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadExportConfiguration(Element configElement, ModuleConfig config, Namespace namespace)
	{
		config.getColumnDefinitions().clear();
		if(configElement == null)
		{
			return;
		}
		List<Element> columnDefinitionElements = configElement.getChildren();
		for(Element element : columnDefinitionElements)
		{
			String text = element.getAttributeValue("text");
			String value = element.getAttributeValue("value");
			if(text == null || value == null)
			{
				continue;
			}
			config.addExportColumn(text, value);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadSubModules(Element submodulesElement, ModuleConfig config, Namespace namespace)
	{
		if(submodulesElement == null)
		{
			return;
		}
		List<Element> submodules = submodulesElement.getChildren();
		for(Element submodule : submodules)
		{
			ModuleConfig submoduleConfig = readModuleConfig(submodule, namespace);
			submoduleConfig.setParent(config);
			moduleManagerConfig.addModuleConfig(submoduleConfig);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void loadSorterValues(Element sorterElement, ModuleConfig config, Namespace namespace) {
		if(sorterElement == null)
		{
			return;
		}
		List<Element> sorterItems = sorterElement.getChildren();
		for(Element sorterItem : sorterItems)
		{
			if (sorterItem.getAttributeValue("enabled", "true").equalsIgnoreCase("true")) {
				SelectItem item = new SelectItem();
				item.setValue(sorterItem.getAttributeValue("name"));
				item.setLabel(sorterItem.getAttributeValue("label"));
				item.setDescription(sorterItem.getAttributeValue("mode"));
				config.getSorterValues().add(item);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private ModuleConfig readModuleConfig(Element model, Namespace namespace)
	{
		ModuleConfig config = new ModuleConfig();
		config.setName(model.getAttributeValue("name"));
		config.setLabel(model.getAttributeValue("label", config.getName()));
		config.setLocation(model.getAttributeValue("location",""));
		config.setMinSearchChars(Integer.parseInt(model.getAttributeValue("minSearchChars","2")));
		config.setNavigable(model.getAttributeValue("navigable", "true").equalsIgnoreCase("true"));
		config.setAdministrable(model.getAttributeValue("administrable", "true").equalsIgnoreCase("true"));
		config.setLockable(model.getAttributeValue("lockable", "false").equalsIgnoreCase("true"));
		Element paramsElement = model.getChild("parameters", namespace);
		if(paramsElement != null)
		{
			List<Element> parameter = paramsElement.getChildren("parameter", namespace);
			for(Element p : parameter)
			{
				config.addProperty(p.getAttributeValue("name"), p.getTextNormalize());
			}
		}
		Element depsElement = model.getChild("dependencies", namespace);
		if(depsElement != null)
		{
			List<Element> dependencies = depsElement.getChildren("dependency", namespace);
			for(Element d : dependencies)
			{
				String dependencyName = d.getAttributeValue("name");
				// get events
				List<Element> events = d.getChildren("event", namespace);
				for(Element event : events)
				{
					// get event type
					String eventType = event.getAttributeValue("type");
					// get actions
					List<Element> actions = event.getChildren("action", namespace);
					for(Element action : actions)
					{
						String executeValue = action.getAttributeValue("execute");
						if(executeValue != null)
						{
							// create a dependency action
							DependencyAction dependencyAction = new DependencyAction();
							dependencyAction.setExecuteValue(executeValue);
							config.addDependencyAction(dependencyName, eventType, dependencyAction);
						}
					}
				}
				// get properties
				List<Element> properties = d.getChildren("property", namespace);
				for(Element property : properties)
				{
					String propertyValue = property.getAttributeValue("value");
					String propertySource = property.getAttributeValue("source");
					String propertyInputValue = property.getAttributeValue("inputValue");
					// create a dependency property
					DependencyProperty dependencyProperty = new DependencyProperty();
					dependencyProperty.setDependency(dependencyName);
					dependencyProperty.setValue(propertyValue);
					dependencyProperty.setSource(propertySource);
					dependencyProperty.setInputValue(propertyInputValue);
					Element dependencyPropertyParamsElement = property.getChild("parameters", namespace);
					if(dependencyPropertyParamsElement != null)
					{
						List<Element> parameter = dependencyPropertyParamsElement.getChildren("parameter", namespace);
						for(Element p : parameter)
						{
							dependencyProperty.addProperty(p.getAttributeValue("name"), p.getTextNormalize());
						}
					}
					config.addDependencyProperty(dependencyName, dependencyProperty);
				}
				// get restrictions
				List<Element> restrictions = d.getChildren("restriction", namespace);
				for(Element restriction : restrictions)
				{
					String restrictionValue = restriction.getAttributeValue("value");
					// create a dependency restriction
					DependencyRestriction dependencyRestriction = new DependencyRestriction();
					dependencyRestriction.setValue(restrictionValue);
					config.addDependencyRestriction(dependencyName, dependencyRestriction);
				}
				// add dependency
				config.addDependency(dependencyName);
			}
		}
		loadFilters(model.getChild("filterDefinition", namespace), config, namespace);
		loadExportConfiguration(model.getChild("exportConfiguration", namespace), config, namespace);
		loadSorterValues(model.getChild("sorterDefinition", namespace), config, namespace);
		return config;
	}
}
