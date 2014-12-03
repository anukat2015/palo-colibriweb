package com.proclos.colibriweb.session.modules.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.Messages;
import org.jdom.Element;

import com.jedox.etl.core.component.ComponentDescriptor;
import com.jedox.etl.core.component.ComponentFactory;
import com.jedox.etl.core.component.ITypes;
import com.jedox.etl.core.component.Locator;
import com.jedox.etl.core.util.CustomClassLoader;
import com.jedox.etl.core.util.XMLUtil;
import com.proclos.colibriweb.common.IUIField;
import com.proclos.colibriweb.common.UITreeBuilder;
import com.proclos.colibriweb.common.XSDAnalyzer;
import com.proclos.colibriweb.entity.component.ComponentOutput;
import com.proclos.colibriweb.entity.component.ComponentTypes;
import com.proclos.colibriweb.entity.component.Function;
import com.proclos.colibriweb.entity.component.Transform;

@Name("transformModule")
@Scope(ScopeType.SESSION)
public class TransformModule extends ComponentModule<Transform> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 470195602828893882L;
	private Map<Function,List<UITreeBuilder>> functionBuilderMap = new HashMap<Function,List<UITreeBuilder>>();
	
	@Override
	public Class<Transform> getEntityClass() {
		return Transform.class;
	}
	
	@Override
	protected String getUniqueNameExpression() {
		return "(dType = "+ComponentTypes.EXTRACT+" or dType = "+ComponentTypes.TRANSFORM+")";
	}
	
	public void setNewInstanceProperties() {
		super.setNewInstanceProperties();
	}
	
	
	public int getComponentType() {
		return ComponentTypes.TRANSFORM;
	}
	
	
	public void removeFunction(Function function) {
		getInstance().getFunctions().remove(function);
		XSDAnalyzer.ComplexFieldValue  functions = analyzer.getRootComplexFieldValue().getChildOfType(ITypes.Functions);
		if (functions != null) {
			functions.deleteChild(function.getFunctionField());
		}
	}
	
	public void createFunction(){
		XSDAnalyzer.ComplexFieldValue  functions = analyzer.getRootComplexFieldValue().getChildOfType(ITypes.Functions);
		if (functions == null) {
			functions = analyzer.getRootComplexFieldValue().getEditComponent(analyzer.getRootComplexField().getChildren().get(ITypes.Functions)).create();
			analyzer.getRootComplexFieldValue().getChildren().add(functions);
		}
		if (functions != null) {
			XSDAnalyzer.ComplexFieldValue functionField = functions.getEditComponent(functions.getType().getChildren().get("function")).create();
			functions.getChildren().add(0,functionField);
			Function f = new Function(functionField);
			f.setTransform(getInstance());
			Set<String> names = new HashSet<String>();
			for (Function existing : getInstance().getFunctions()) {
				names.add(existing.getName());
			}
			for (int i=getInstance().getFunctions().size()+1;;i++) {
				if (!names.contains("Function"+i)) {
					f.setName("Function"+i);
					break;
				}
			}
			getInstance().getFunctions().add(0,f);
			XSDAnalyzer analyzer = getFunctionAnalyzer(f, null);
			f.setAnalyzer(analyzer);
			f.setInEdit(true);
			functionBuilderMap.put(f, getFunctionUIBuilder(f,functionField));
		}
	}
	
	public List<Function> getFunctions() {
		List<Function> result = new ArrayList<Function>();
		result.addAll(getInstance().getFunctions());
		return result;
	}
	
	public boolean hasFunctions() {
		return analyzer == null ? false : analyzer.hasFunctions();
	}
	
	public void instanceCreated() {
		super.instanceCreated();
		functionBuilderMap.clear();
	}
	
	private List<UITreeBuilder> getFunctionUIBuilder(Function function, XSDAnalyzer.ComplexFieldValue root) {
		List<UITreeBuilder> complexUIBuilders = new ArrayList<UITreeBuilder>();
		int i = 0;
		function.getAnalyzer().initAllFields(root);
		for (XSDAnalyzer.ComplexFieldValue v : root.getChildren()) {
			if (!v.getType().hasCustomHandle()) {
				v.setEnabled(true);
				complexUIBuilders.add(new UITreeBuilder(v,function,i++));
			}
		}
		return complexUIBuilders;
	}
	
	private XSDAnalyzer getFunctionAnalyzer(Function f, Element xml) {
		XSDAnalyzer analyzer = getInstance().getAnalyzer(); //try reuse
		try {
			if (analyzer == null) {
				String type = f.getType() != null ? f.getType() : getAvailableTypes(f).get(0);
				f.setType(type);
				List<String> types = getAvailableTypes(f);
				if (!types.contains(f.getType())) { //check if we have a case problem 
					for (String t : types) {
						if (t.equalsIgnoreCase(f.getType())) {
							f.setType(t);
							break;
						}
					}
				}
				ComponentDescriptor d = ComponentFactory.getInstance().getComponentDescriptor(type, getManagerName(f.getdType(),false));
				analyzer = new XSDAnalyzer(CustomClassLoader.getInstance().loadClass(d.getClassName()), getComponentTypeName(f.getdType()));
				if (xml != null) f.setCode(analyzer.applyValues(xml));
				analyzer.initFields(xml == null);
				f.getFunctionField().getType().getChildren().clear(); //substitute real types
				f.getFunctionField().getType().getChildren().putAll(analyzer.getRootComplexField().getChildren());
				f.getFunctionField().getChildren().clear();
				f.getFunctionField().getChildren().addAll(analyzer.getRootComplexFieldValue().getChildren());
				f.getFunctionField().clearEditors();
				f.getFunctionField().init();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			this.addErrorMessage(Messages.instance().get("etl.update.failed")+": "+e.getMessage());
		}
		return analyzer;
	}
	
	public void instanceSelected() {
		super.instanceSelected();
	}
	
	public void initComponentFromXML() {
		List<Function> oldFunctions = new ArrayList<Function>();
		oldFunctions.addAll(getInstance().getFunctions());
		super.initComponentFromXML();
		functionBuilderMap.clear();
		try {
			if (hasFunctions()) { //rebuild functions from xml
				getInstance().getFunctions().clear();
				XSDAnalyzer.ComplexFieldValue  functions = analyzer.getRootComplexFieldValue().getChildOfType(ITypes.Functions);
				if (functions != null) {
					for (XSDAnalyzer.ComplexFieldValue function : functions.getChildrenOfType("function")) {
						if (function.isEnabled()) {
							Function f = new Function(function);
							f.setTransform(getInstance());
							getInstance().getFunctions().add(f);
						}
					}
				}
				if (getInstance().getXml() != null) {
					Element xml = XMLUtil.stringTojdom(getInstance().getXml());
					Element functionsXML = xml.getChild(ITypes.Functions);
					if (functionsXML != null) {
						for (int i=0; i<functionsXML.getChildren().size(); i++) {
							Element functionXML = (Element)functionsXML.getChildren().get(i);
							Function f = getInstance().getFunctions().get(i);
							f.setAnalyzer(getFunctionAnalyzer(f, functionXML));
							functionBuilderMap.put(f, getFunctionUIBuilder(f, f.getFunctionField()));
						}
					}
				}
				//restore in edit info
				for (int i=0; i<oldFunctions.size();i++) {
					getInstance().getFunctions().get(i).setInEdit(oldFunctions.get(i).isInEdit());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			this.addErrorMessage(Messages.instance().get("etl.update.failed")+": "+e.getMessage());
		}
	}
	
	public List<UITreeBuilder> getFunctionBuilder(Function function) {
		return functionBuilderMap.get(function);
	}
	
	public List<ComponentOutput> getComponentTypedInputList() {
		List<ComponentOutput> inputs = super.getComponentTypedInputList();
		Set<String> names = new HashSet<String>();
		for (ComponentOutput o : inputs) {
			names.add(o.getName());
		}
		for (Function f : getInstance().getFunctions()) {
			if (!names.contains(f.getName())) {
				ComponentOutput fo = new ComponentOutput();
				fo.setId(new Long(0));
				fo.setName(f.getName());
				String type = f.getOutputType();
				fo.setType(type);
				fo.setComponent(getInstance());
				inputs.add(fo);
			}
		}
		Collections.sort(inputs);
		return inputs;
	}
	
	public List<ComponentOutput> getFunctionTypedInputList(Function f) {
		List<ComponentOutput> inputs = getComponentTypedInputList();
		Iterator<ComponentOutput> iterator = inputs.iterator();
		while (iterator.hasNext()) {
			ComponentOutput o = iterator.next();
			if (o.getName().equals(f.getName()) && o.getId().longValue()==0) iterator.remove();
		}
		return inputs;
	}
	
	public void validateFunctionName(FacesContext context, UIComponent component, Object obj) throws ValidatorException
	{
		Object oldValue = ((UIInput) component).getValue();
		String name = (String)obj;
		if(name != null)
		{
			name = name.toLowerCase().trim();
		}
		if (oldValue != null) {
			oldValue = oldValue.toString().toLowerCase().trim();
		}
		Set<String> names = new HashSet<String>();
		for (Function f : getInstance().getFunctions()) {
			names.add(f.getName().toLowerCase());
		}
		names.remove(oldValue);
		names.add(name);
		if (names.size() < getInstance().getFunctions().size()) {
			throw new ValidatorException(createErrorMessage("Name already in use"));
		}
		
	}
	
	public void functionNameChanged(Function function) {
	}
	
	public void functionTypeChanged(Function function) {
		function.setAnalyzer(null);
		function.setAnalyzer(getFunctionAnalyzer(function, null));
		functionBuilderMap.put(function, getFunctionUIBuilder(function, function.getFunctionField()));
	}
	
	public String getFunctionEditorToSave(Function function) {
		return getComplexEditorsJavascript(function.getFunctionField(), function.getInternalName(), "save()").toString();
	}
	
	public String getFunctionEditorToRefresh(Function function) {
		return getComplexEditorsJavascript(function.getFunctionField(), function.getInternalName(), "refresh()").toString();
	}
	
	public String getFunctionEditorToSave() {
		StringBuffer buffer = new StringBuffer();
		for (Function f : getInstance().getFunctions()) {
			if (f.isInEdit())
				buffer.append(getComplexEditorsJavascript(f.getFunctionField(), f.getInternalName(), "save()").toString());
		}
		return buffer.toString();
	}
	
	public String getFunctionEditorToRefresh() {
		StringBuffer buffer = new StringBuffer();
		for (Function f : getInstance().getFunctions()) {
			if (f.isInEdit())
				buffer.append(getComplexEditorsJavascript(f.getFunctionField(), f.getInternalName(), "refresh()").toString());
		}
		return buffer.toString();
	}
	
	public String getEditorsToSave() {
		return super.getEditorsToSave()+getFunctionEditorToSave();
	}
	
	protected Map<IUIField,Locator> getComponentScripts() {
		Map<IUIField,Locator> result = super.getComponentScripts();
		for (Function f : getInstance().getFunctions()) {
			if (f.getFunctionField() != null) for (XSDAnalyzer.ComplexFieldValue sf : f.getFunctionField().getChildren()) {
				for (IUIField field : getComponentScripts(sf).keySet()) {
					result.put(field,getLocator().clone().add(ITypes.Functions).add(f.getName()));
				}
			}
		}
		return result;
	}
	 
	 

}
