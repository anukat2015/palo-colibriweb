package com.proclos.colibriweb.common;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jasypt.util.text.BasicTextEncryptor;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.jedox.etl.components.connection.OLAPConnection;
import com.jedox.etl.core.component.ITypes;
import com.jedox.etl.core.config.ConfigValidator;
import com.jedox.etl.core.config.Settings;
import com.jedox.etl.core.connection.IRelationalConnection;
import com.jedox.etl.core.function.IFunction;
import com.jedox.etl.core.job.IJob;
import com.proclos.colibriweb.session.common.EditComponent;
import com.sun.xml.xsom.parser.AnnotationContext;
import com.sun.xml.xsom.parser.AnnotationParser;
import com.sun.xml.xsom.parser.AnnotationParserFactory;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroup.Compositor;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;

public class XSDAnalyzer {
	
	private Class<?> implementingClass;
	private boolean hasConnection;
	private boolean connectionRequired;
	private boolean hasFunctions;
	private Class<?> connectionInterface;
	private int maxSources = 0;
	private int minSources = 0;
	private boolean treeSourceRequired;
	private boolean simpleSourceElement = false;
	private boolean hasVariables;
	private ComplexField rootWrapperType = new ComplexField();
	private ComplexFieldValue rootWrapperValue = new ComplexFieldValue();
	private XSSchema schema;
	
	
	private class AnnotationFactory implements AnnotationParserFactory{
	    @Override
	    public AnnotationParser create() {
	        return new XsdAnnotationParser();
	    }
	}
	
	private class AnnotationHandler {
		private StringBuilder appinfo = new StringBuilder();
		private StringBuilder documentation = new StringBuilder();
		
		public String getAppinfo() {
			return appinfo.toString();
		}
		
		public String getDocumenttation() {
			return documentation.toString();
		}
	}
	
	private class XsdAnnotationParser extends AnnotationParser {	
		
		private final AnnotationHandler annotation = new AnnotationHandler();
	    
		@Override
	    public ContentHandler getContentHandler(AnnotationContext context, String parentElementName, ErrorHandler handler, EntityResolver resolver) {
	        return new ContentHandler(){
	            private boolean parsingDocumentation = false;
	            private boolean parsingAppinfo = false;
	           
	            @Override
	            public void characters(char[] ch, int start, int length)
	            throws SAXException {
	                if(parsingDocumentation){
	                	annotation.documentation.append(ch,start,length);
	                }
	                if(parsingAppinfo){
	                	annotation.appinfo.append(ch,start,length);
	                }
	            }
	            @Override
	            public void endElement(String uri, String localName, String name) throws SAXException {
	            	
	            	if (parsingDocumentation) { //inline docu html
	            		annotation.documentation.append("</"+localName+">");
	            	}
	                if(localName.equals("documentation")){
	                    parsingDocumentation = false;
	                }
	               
	                if(localName.equals("appinfo")){
	                	parsingAppinfo = false;
	                }
	            }
	            @Override
	            public void startElement(String uri, String localName,String name, Attributes atts) throws SAXException {
	            	if (parsingDocumentation) {  //inline docu html
	            		annotation.documentation.append("<"+localName+">");
	            	}
	                if(localName.equals("documentation")){
	                    parsingDocumentation = true;
	                }
	                if(localName.equals("appinfo")){
	                	parsingAppinfo = true;
	                }
	            }
				@Override
				public void endDocument() throws SAXException {					
				}
				@Override
				public void endPrefixMapping(String prefix) throws SAXException {					
				}
				@Override
				public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {					
				}
				@Override
				public void processingInstruction(String target, String data) throws SAXException {					
				}
				@Override
				public void setDocumentLocator(Locator locator) {					
				}
				@Override
				public void skippedEntity(String name) throws SAXException {					
				}
				@Override
				public void startDocument() throws SAXException {					
				}
				@Override
				public void startPrefixMapping(String prefix, String uri) throws SAXException {					
				}
	        };
	    }
	    @Override
	    public AnnotationHandler getResult(Object existing) {
	        return annotation;
	    }
	}
	
	public class SimpleTypeRestriction{
		private String name;
		private List<String> enumeration = null;
		private Integer maxValue = null;
		private Integer minValue = null;
		private Integer length = null;
		private Integer maxLength = null;
		private Integer minLength = null;
		private String pattern = null;
		private Integer totalDigits = null;
	    
	    public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public List<String> getEnumeration() {
			return enumeration;
		}
		public void setEnumeration(List<String> enumeration) {
			this.enumeration = enumeration;
		}
		public Integer getMaxValue() {
			return maxValue != null ? maxValue : 99999;
		}
		public void setMaxValue(Integer maxValue) {
			this.maxValue = maxValue;
		}
		public Integer getMinValue() {
			return minValue != null ? minValue : -99999;
		}
		public void setMinValue(Integer minValue) {
			this.minValue = minValue;
		}
		public Integer getLength() {
			return length;
		}
		public void setLength(Integer length) {
			this.length = length;
		}
		public Integer getMaxLength() {
			return maxLength;
		}
		public void setMaxLength(Integer maxLength) {
			this.maxLength = maxLength;
		}
		public Integer getMinLength() {
			return minLength;
		}
		public void setMinLength(Integer minLength) {
			this.minLength = minLength;
		}
		public String getPattern() {
			return pattern;
		}
		public void setPattern(String pattern) {
			this.pattern = pattern;
		}
		public Integer getTotalDigits() {
			return totalDigits;
		}
		public void setTotalDigits(Integer totalDigits) {
			this.totalDigits = totalDigits;
		}
	}
	
	public class SimpleField implements IUIField {
		protected String name;
		protected Object value;
		protected boolean required;
		protected SimpleTypeRestriction type;
		protected String defaultValue;
		protected String annotation = "";
		protected String documentation;
		protected boolean visible = true;

		public String getDisplayName() {
			return (name != null && name.length() >= 2) ? name.substring(0,1).toUpperCase()+name.substring(1) : name;
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Object getValue() {
			return value == null ? defaultValue : value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		public boolean isRequired() {
			return required;
		}

		public SimpleTypeRestriction getType() {
			return type;
		}

		public void setType(SimpleTypeRestriction type) {
			this.type = type;
		}
		
		public boolean isString() {
			return !isInteger() && !isEnum() && !isBoolean() && !isFloat();
		}
		
		public boolean isInteger() {
			return (type.name.equals("integer") || type.name.equals("nonPositiveInteger") || type.name.equals("negativeInteger") || type.name.equals("nonNegativeInteger") || type.name.equals("positiveInteger"));
		}
		
		public boolean isFloat() {
			return (type.name.equals("float") || type.name.equals("double") || type.name.equals("decimal")); 
		}
		
		public boolean isEnum() {
			return type.enumeration != null;
		}
		
		public boolean isBoolean() {
			return type.name.equals("boolean") && (value == null || value.toString().equalsIgnoreCase("true") || value.toString().equalsIgnoreCase("false"));
		}
		
		public boolean isScript() {
			return getScriptType() != null;
		}
		
		public String getScriptType() {
			if (annotation.startsWith("script.")) {
				String type = annotation.substring(annotation.indexOf(".")+1);
				if (type.equalsIgnoreCase("xml")) {
					return "text/xml";
				}
				return "text/x-"+annotation.substring(annotation.indexOf(".")+1);
			}
			//HEURISTIC FALLBACKS
			if ("jobscript".equalsIgnoreCase(name) || "script".equalsIgnoreCase(name)) return "text/x-groovy";
			if ("query".equalsIgnoreCase(name) && hasConnection() && IRelationalConnection.class.isAssignableFrom(getConnectionInterface())) return "text/x-sql";
			if ("comment".equalsIgnoreCase(name)) return "text/html";
			return null;
		}
		
		public List<String> getEnumeration() {
			List<String> result = type.getEnumeration();
			if (result == null) result = new ArrayList<String>();
			if (value != null && !value.toString().isEmpty() && !result.contains(value)) result.add(value.toString());
			return result;
		}
		
		public String getAnnotation() {
			return annotation;
		}
		
		public boolean isMetadataField() {
			return annotation.startsWith("metadata");
		}
		
		public boolean isInputField() {
			return annotation.startsWith("input");
		}
		
		public boolean isSourceField() {
			return annotation.startsWith("source");
		}
		
		public boolean isConnectionField() {
			return annotation.startsWith("connection");
		}
		
		public boolean isProjectField() {
			return annotation.startsWith("project");
		}
		
		public String getConnectionCategory() {
			if (!isConnectionField()) return ":NONE";
			if (annotation.endsWith(".all")) return ":ALL";
			if (annotation.startsWith("connection.interface;")) {
				return annotation.split(";")[1];
			}
			return ":MODULE";
		}
		
		public String getSourceCategory() {
			if (!isSourceField()) return ":NONE";
			if (annotation.endsWith(".all")) return ":ALL";
			if (annotation.endsWith(".local")) return ":LOCAL";
			if (annotation.endsWith(".tree")) return ":TREE";
			if (annotation.endsWith(".table")) return ":TABLE";
			return ":MODULE";
		}
		
		public boolean isNormalField() {
			return !isMetadataField() && !isInputField() && !isSourceField() && !isConnectionField() &&!isProjectField();
		}
		
		public String getTypeName() {
			return type.getName();
		}
		
		public boolean isVisible() {
			return visible && !annotation.equals("hidden");
		}
		
		public void setVisible(boolean visible) {
			this.visible = visible;
		}
		
		public String getDocumentation() {
			return documentation;
		}
	}
	
	public class AttributeField extends SimpleField implements Comparable<AttributeField> {
		protected ComplexFieldValue owner;
		protected Integer position = 0;
		
		
		public AttributeField clone() {
			AttributeField result = new AttributeField();
			result.name = name;
			result.required = required;
			result.defaultValue = defaultValue;
			result.type = type;
			result.owner = owner;
			result.annotation = annotation;
			return result;
		}
		
		public ComplexFieldValue getOwner() {
			return owner;
		}

		public int compareTo(AttributeField o) {
			if (o != null) return this.position.compareTo(o.position);
			return -1;
		}
	}
	
	public class ComplexField implements Comparable<ComplexField>{
		protected String name = "";
		protected String typeName;
		protected int minOccurs = 0;
		protected int maxOccurs = Integer.MAX_VALUE;
		protected Map<String,ComplexField> children = new LinkedHashMap<String,ComplexField>();
		protected List<AttributeField> attributes = new ArrayList<AttributeField>();
		protected Compositor compositor = Compositor.ALL;
		protected SimpleTypeRestriction type;
		protected String annotation = "";
		protected String documentation;
		protected boolean hasCustomHandle = false;
		protected List<ChoiceGroup> choiceGroups = new ArrayList<ChoiceGroup>();
		protected Integer position = 0;
		protected String defaultValue;
		
		public boolean isRequired() {
			return minOccurs > 0;
		}
		
		public boolean isSequence() {
			return compositor.equals(Compositor.SEQUENCE);// && children.size() == 1;
		}
		
		public boolean isChoice() {
			return compositor.equals(Compositor.CHOICE);
		}
		
		public boolean isAll() {
			return children.size() >= 0 && compositor.equals(Compositor.ALL);
		}
		
		public boolean hasChildren() {
			return children.size() > 0;
		}
		
		public List<AttributeField> getAttributes() {
			return attributes;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getDisplayName() {
			if (name != null) {
				String displayName = name.replace("_", " ");
				//convert to upper case after spaces
				String[] tokens = displayName.split(" ");
				StringBuffer buffer = new StringBuffer();
				for (String t : tokens) {
					if (t.length() >= 2) {
						buffer.append(t.substring(0,1).toUpperCase());
						buffer.append(t.substring(1));
						buffer.append(" ");
					}
					else buffer.append(t+" ");
				}
				return buffer.toString().trim();
			}
			return name;
		}
		
		public boolean isOptionalSingle() {
			return minOccurs == 0 && maxOccurs == 1;
		}
		
		public SimpleTypeRestriction getType() {
			return type;
		}
		
		public int getMinOccurs() {
			return minOccurs;
		}
		
		public int getMaxOccurs() {
			return maxOccurs;
		}
		
		public String getAnnotation() {
			return annotation;
		}
		
		public boolean hasCustomHandle() {
			return hasCustomHandle;
		}
		
		public Map<String,ComplexField> getChildren() {
			return children;
		}

		@Override
		public int compareTo(ComplexField o) {
			if (o != null) return this.position.compareTo(o.position);
			return -1;
		}
		
		public boolean isExpanded() {
			return isRequired() || annotation.equals("expanded");
		}
		
		public String getDocumentation() {
			return documentation;
		}
	}
	
	public class ChoiceGroup {
		private int minOccours = 0;
		private int maxOccours = 1;
		private List<List<ComplexField>> types = new ArrayList<List<ComplexField>>();
	}
	
	public class ComplexFieldEditComponent extends EditComponent<ComplexFieldValue> {
		
		private ComplexField type;
		private ComplexFieldValue owner;

		public ComplexFieldEditComponent(List<ComplexFieldValue> items,Integer minItems, Integer maxItems, ComplexField type, ComplexFieldValue owner) {
			super(items, ComplexFieldValue.class, minItems, maxItems == -1 ? Integer.MAX_VALUE : maxItems);
			this.type = type;
			this.owner = owner;
		}
		
		protected ComplexFieldValue createNewItem() {
			ComplexFieldValue newItem = new ComplexFieldValue();
			newItem.setParent(owner);
			newItem.setType(type);
			newItem.enabled = true;
			if (newItem.isBoolean()) newItem.setValue(Boolean.TRUE);
			initFields(newItem,true);
			return newItem;
		}
		
		protected boolean checkChoiceGroups() {
			for (ChoiceGroup cg : owner.type.choiceGroups) {
				int occurrences = 0;
				boolean typeContained = false;
				for (List<ComplexField> fl : cg.types) {
					if (fl.contains(type)) {
						typeContained = true; 
						break;
					}
				}
				if (typeContained) {
					for (List<ComplexField> fl : cg.types) {
						for (ComplexField t : fl) {
							 if (!fl.contains(type) && !owner.getChildrenOfType(t.name).isEmpty()) {
								 occurrences += 1;
								 break;
							 }
						}
					}
					if (cg.maxOccours > 0 && occurrences >= cg.maxOccours) return false;
				}
			}
			return true;
		}
		
		@Override
		public boolean canCreateNewItem() {
			return !type.hasCustomHandle && super.canCreateNewItem() && checkChoiceGroups();
		}
		
	}
	
	public class ComplexFieldValue implements IUIField {
		protected ComplexField type;
		protected List<ComplexFieldValue> children = new ArrayList<ComplexFieldValue>();
		protected Map<String,AttributeField> attributes = new LinkedHashMap<String,AttributeField>();
		protected Boolean holdsConstantInput;
		protected String input;
		protected Object value;
		protected boolean enabled;
		protected ComplexFieldValue compactedValue;
		private Map<String,ComplexFieldEditComponent> editorLookup = new LinkedHashMap<String,ComplexFieldEditComponent>();
		private ComplexFieldValue parent;

		public ComplexField getType() {
			return type;
		}
		
		public void setType(ComplexField type) {
			this.type = type;
		}
		
		public List<ComplexFieldValue> getChildren() {
			return children;
		}
		
		public Map<String, AttributeField> getAttributes() {
			return attributes;
		}
		
		public List<AttributeField> getAttributeList() {
			List<AttributeField> result = new ArrayList<AttributeField>();
			result.addAll(getAttributes().values());
			return result;
		}
		
		public List<AttributeField> getVisibleAttributes() {
			List<AttributeField> result = new ArrayList<AttributeField>();
			for (AttributeField f : getAttributes().values()) {
				if (f.isVisible()) result.add(f);
			}
			return result;
		}
		
		public boolean isInputField() {
			return (type.name.equals("input") || getAnnotation().startsWith("input") || (isCompacted() && getCompactedValue().isInputField()));
		}
		
		public boolean isSourceField() {
			return (type.name.equals("source") || getAnnotation().startsWith("source"));
		}
		
		public boolean isMetadataField() {
			return getAnnotation().startsWith("metadata");
		}
		
		public boolean isConnectionField() {
			return (type.name.equals("connection") || getAnnotation().startsWith("connection"));
		}
		
		public boolean isProjectField() {
			return getAnnotation().startsWith("project");
		}
		
		public String getConnectionCategory() {
			return asSimpleField().getConnectionCategory();
		}
		
		public String getSourceCategory() {
			return asSimpleField().getSourceCategory();
		}
		
		public boolean isNormalField() {
			return !isMetadataField() && !isInputField() && !isSourceField() && !isConnectionField() && !isProjectField();
		}
		
		public ComplexFieldEditComponent getEditComponent(ComplexField type) {
			if (type != null) {
				ComplexFieldEditComponent editor = editorLookup.get(type.getName());
				if (editor == null) {
					List<ComplexFieldValue> toEdit = getChildrenOfType(type.getName());
					editor = new ComplexFieldEditComponent(toEdit, type.minOccurs, type.maxOccurs, type, this);
					editorLookup.put(type.getName(), editor);
				}
				return editor;
			}
			return null;
		}
		
		public String getDisplayName() {
			return type.getDisplayName();
		}
		
		public String getName() {
			return type.name;
		}
		
		public List<ComplexField> getChildTypes() {
			List<ComplexField> result = new ArrayList<ComplexField>();
			if (!isCompacted()) {
				for (ComplexField ct : type.children.values()) {
					result.add(ct);
				}
			}
			Collections.sort(result);
			return result;
		}
		
		public List<ComplexFieldValue> getChildrenOfTypes() {
			List<ComplexFieldValue> result = new ArrayList<ComplexFieldValue>();
			for (ComplexFieldValue v : getChildren()) {
				if (type.children.containsKey(v.getName())) result.add(v);
			}
			return result;
		}
		
		public List<ComplexField> getChildTypesInverseOrder() {
			List<ComplexField> result = new ArrayList<ComplexField>();
			result.addAll(getChildTypes());
			Collections.reverse(result);
			return result;
		}
		
		public List<ComplexFieldValue> getChildrenOfType(String typeName) {
			List<ComplexFieldValue> result = new ArrayList<ComplexFieldValue>();
			for (ComplexFieldValue c : children) {
				if (c.type.name.equals(typeName)) result.add(c);
			}
			return result;
		}
		
		public ComplexFieldValue getChildOfType(String typeName) {
			List<ComplexFieldValue> list = getChildrenOfType(typeName);
			return list.isEmpty() ? null : list.get(0);
		}
		
		public boolean mayHoldConstantInput() {
			return (isInputField() && attributes.containsKey("constant"));
		}
		
		public Boolean getHoldsConstantInput() {
			if (holdsConstantInput == null) {
				if (mayHoldConstantInput()) {
					holdsConstantInput = attributes.get("constant").value != null;
				} else {
					holdsConstantInput = false;
				}
			}
			return holdsConstantInput;
		}
		
		public void setHoldsConstantInput(Boolean holdsConstantInput) {
			this.holdsConstantInput = holdsConstantInput;
			if (holdsConstantInput) {
				if (attributes.containsKey("nameref")) attributes.get("nameref").value = null;
			} else {
				if (attributes.containsKey("constant")) attributes.get("constant").value = null;
			}
		}
		
		public String getInput() {
			if (input == null && (isInputField() || isSourceField() || isMetadataField() || isConnectionField()) && Boolean.FALSE.equals(getHoldsConstantInput())) {
				String attribute = isMetadataField() ? "name" : "nameref";
				if (attributes.get(attribute) != null) {
					Object name = attributes.get(attribute).value;
					input = (name == null ? "" : name.toString());
				} else {
					return value != null ? value.toString() : type.defaultValue;
				}
			}
			return input;
		}

		public void setInput(String input) {
			this.input = input;
			String attribute = isMetadataField() ? "name" : "nameref";
			if (attributes.containsKey(attribute) && input != null) {
				attributes.get(attribute).value = input;
			} else {
				value = input;
			}
		}
		
		private boolean isAttributeValue() {
			String attribute = isMetadataField() ? "name" : "nameref";
			return !isValueHolder() && attributes.containsKey(attribute);
		}
		
		public Object getValue() {
			if (isAttributeValue()) {
				return getInput();
			} else {
				return (value == null ? type.defaultValue : value);
			}
		}

		public void setValue(Object value) {
			if (isAttributeValue() && value != null) {
				setInput(value.toString());
			} else {
				this.value = value;
				this.input = value != null ? value.toString() : null;
			}
		}
		
		public boolean isEnabled() {
			return enabled || type.isRequired();
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
			if (!type.isRequired() && enabled && !isCompacted()) {
				initFields(this,false);
			}
		}
		
		public boolean isValueHolder() {
			return type.type != null;
		}
		
		private SimpleField asSimpleField() {
			SimpleField f = new SimpleField() {
				public void setValue(Object innerValue) {
					ComplexFieldValue.this.setValue(innerValue);
				}
				public Object getValue() {
					return ComplexFieldValue.this.getValue();
				}
			};
			f.name = type.name;
			f.type = type.type;
			f.required =  type.compositor.equals(Compositor.ALL) && type.isRequired();
			f.annotation = type.annotation;
			return f;
		}
		
		private boolean hasCommonAttributes(ComplexField type) {
			Map<String,AttributeField> localAttributes = getAttributes();
			for (AttributeField a : type.attributes) {
				if (localAttributes.containsKey(a.getName())) return true;
			}
			return false;
		}
		
		public boolean isCompactable() {
			if (!type.hasCustomHandle()) {
				List<ComplexField> childTypes = getChildTypes();
				if (children.size() == 1 && childTypes.size() == 1 && childTypes.get(0).children.size() == 0 && childTypes.get(0).isRequired() && childTypes.get(0).maxOccurs == 1 && !hasCommonAttributes(childTypes.get(0)) && childTypes.get(0).type == null && (type.getAnnotation().isEmpty() && childTypes.get(0).getAnnotation().isEmpty())) return true;
			}
			return false;
		}
		
		public boolean isCompacted() {
			return compactedValue != null;
		}
		
		public ComplexFieldValue getCompactedValue() {
			return compactedValue;
		}
		
		public List<ComplexFieldValue> getSortedChildren() {
			//order children by type occurrance
			List<ComplexFieldValue> orderedList = new ArrayList<ComplexFieldValue>();
			for (ComplexField type : getChildTypes()) {
				List<ComplexFieldValue> typeList = getChildrenOfType(type.getName());
				orderedList.addAll(typeList);
			}
			return orderedList;
		}
		
		public void clearEditors() {
			editorLookup.clear();
		}
		
		public void init() {
			for (AttributeField f : getAttributes().values()) {
				f.owner = this;
			}
			if (isCompactable() && !isCompacted()) {
				compactedValue = children.get(0);
				attributes.putAll(compactedValue.attributes);
				children.clear();
				editorLookup.clear();
			}
			if (isMetadataField()) {
				AttributeField attribute = getAttributes().get("name");
				if (attribute !=null) attribute.visible = false;
			}
			if (isInputField() || isSourceField() || isConnectionField()) {
				AttributeField attribute = getAttributes().get("nameref");
				if (attribute !=null) attribute.visible = false;
				attribute = getAttributes().get("constant");
				if (attribute !=null) attribute.visible = false;
			}
			if (isEncryptable()) {
				AttributeField attribute = getAttributes().get("encrypted");
				if (attribute !=null) attribute.visible = false;
			}
			List<ComplexFieldValue> orderedList = getSortedChildren();
			children.removeAll(orderedList);
			children.addAll(orderedList); //types with custom handle are now in front followed by ordered Generic-UI managed children
		}
		
		public void deleteChild(ComplexFieldValue toDelete) {
			children.remove(toDelete);
			toDelete.setEnabled(false);
			ComplexFieldEditComponent editor = editorLookup.get(toDelete.getType().getName());
			if (editor != null) {
				editor.delete(toDelete);
			}
		}
		
		private int getPreviousAvailablePosition(int pos) {
			while (pos > 0 && getType().getChildren().get(children.get(pos-1)) == null) {
				pos--;
			}
			return pos--;
		}
		
		private int getNextAvaliablePosition(int pos) {
			while (pos < children.size()-1 && getType().getChildren().get(children.get(pos+1)) == null) {
				pos++;
			}
			return pos++;
		}
		
		public void moveUp(ComplexFieldValue toMove) {
			int index = children.indexOf(toMove);
			int posToMove = getPreviousAvailablePosition(index);
			if (index > 0 && posToMove >= 0) for (int i = 0; i<(index-posToMove); i++) {
				Collections.swap(children, index-i, index-1-i);
			}
		}
		
		public void moveDown(ComplexFieldValue toMove) {
			int index = children.indexOf(toMove);
			int posToMove = getNextAvaliablePosition(index);
			if (index >= 0 && index < children.size()-1 && posToMove < children.size()) for (int i = 0; i<(posToMove-index); i++) {
				Collections.swap(children, index+1, index+1+i);
			}
		}

		@Override
		public boolean isRequired() {
			if (getParent() != null && getParent().getType() != null && getParent().getType().isSequence()) { //check if we have enough elements for the sequence
				return getType().isRequired() && getParent().getChildrenOfType(getTypeName()).size() <= getParent().getType().getMinOccurs();
			}
			return getType().isRequired(); //parent ALL
		}

		@Override
		public boolean isString() {
			return (isValueHolder()  ? asSimpleField().isString() : false);
		}

		@Override
		public boolean isInteger() {
			return (isValueHolder()  ? asSimpleField().isInteger() : false);
		}

		@Override
		public boolean isFloat() {
			return (isValueHolder()  ? asSimpleField().isFloat() : false);
		}

		@Override
		public boolean isEnum() {
			return (isValueHolder()  ? asSimpleField().isEnum() : false);
		}

		@Override
		public boolean isBoolean() {
			return (isValueHolder()  ? asSimpleField().isBoolean() : false);
		}

		@Override
		public boolean isScript() {
			return (isValueHolder()  ? asSimpleField().isScript() : false);
		}
		
		public String getScriptType() {
			return asSimpleField().getScriptType();
		}
		
		public boolean isVisible() {
			return !getAnnotation().equals("hidden");
		}

		@Override
		public String getAnnotation() {
			if (isCompacted() && getType().getAnnotation().isEmpty()) {
				return compactedValue.getType().getAnnotation();
			}
			return getType().getAnnotation();
		}

		@Override
		public String getTypeName() {
			return type.getName();
		}
		
		public Long getId() {
			int hash = hashCode();
			long id = new Long(Integer.MAX_VALUE) + new Long(hash);
			return id;
		}
		
		public boolean isEncrypted() {
			AttributeField attribute = getAttributes().get("encrypted");
			if (attribute != null) {
				Object value = getAttributes().get("encrypted").getValue();
				return value == null ? false : Boolean.valueOf(value.toString());
			}
			return false;
		}

		public void setEncrypted(boolean encrypted) {
			getAttributes().get("encrypted").setValue(String.valueOf(encrypted));
		}

		public boolean isEncryptable() {
			return isValueHolder() && getAttributes().containsKey("encrypted");
		}
		
		public void checkEncrypt() {
			if (isEncryptable() && isEncrypted()) encrypt();
		}
		
		public void encrypt() {
			if (isEncryptable() && value != null) {
				try {
					BasicTextEncryptor crypt = new BasicTextEncryptor();
					crypt.setPassword(Settings.getInstance().getContext(Settings.EncryptionCtx).getProperty("password"));
					value = crypt.encrypt(value.toString());
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public void decrypt() {
			if (isEncryptable() && value != null) {
				try {
					BasicTextEncryptor crypt = new BasicTextEncryptor();
					crypt.setPassword(Settings.getInstance().getContext(Settings.EncryptionCtx).getProperty("password"));
					value = crypt.decrypt(value.toString());
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public void toggleEncryption() {
			if (isEncryptable()) {
				if (!isEncrypted()) decrypt(); else encrypt();
			}
		}

		public ComplexFieldValue getParent() {
			return parent;
		}

		public void setParent(ComplexFieldValue parent) {
			this.parent = parent;
		}
		
		public boolean isExpanded() {
			return isRequired() || getAnnotation().equals("expanded");
		}
	}
	
	
	
	public XSDAnalyzer(Class<?> implementingClass, String componentType) throws Exception {
		this.implementingClass = implementingClass;
		XSOMParser parser = new XSOMParser();
		parser.setAnnotationParser(new AnnotationFactory());
		parser.parse(getSchemaDocumentInput(implementingClass));
		XSSchemaSet set  = parser.getResult();
		schema = set.getSchema(1);
		XSElementDecl rootElement = schema.getElementDecl(componentType);
		XSType rootType = rootElement.getType();
		rootWrapperType.name = rootType.getName();
		rootWrapperType.minOccurs = 1;
		rootWrapperType.maxOccurs = 1;
		rootWrapperType.compositor = Compositor.ALL;
		if (rootElement.getAnnotation() != null) {
			AnnotationHandler ah = (AnnotationHandler)rootElement.getAnnotation().getAnnotation();
			rootWrapperType.documentation = ah.getDocumenttation();
		}
		rootWrapperValue.setType(rootWrapperType);
		parseRoot(rootType);
	}
	
	private InputStream getSchemaDocumentInput(Class<?> implementingClass) throws IOException, SAXException, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
		//Delegate to backend to use the includes
		Document doc = ConfigValidator.getInstance().getSchemaDocument(implementingClass);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Source xmlSource = new DOMSource(doc);
		Result outputTarget = new StreamResult(outputStream);
		TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
		InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
		return is;
	}
	
	private SimpleTypeRestriction getRestriction(XSSimpleType xsSimpleType){
		SimpleTypeRestriction t = new SimpleTypeRestriction();
		t.name = xsSimpleType.getName();
	    XSRestrictionSimpleType restriction = xsSimpleType.asRestriction();
	    if(restriction != null){
	        List<String> enumeration = new ArrayList<String>();
	        Iterator<? extends XSFacet> i = restriction.getDeclaredFacets().iterator();
	        while(i.hasNext()){
	            XSFacet facet = i.next();
	            if(facet.getName().equals(XSFacet.FACET_ENUMERATION)){
	                enumeration.add(facet.getValue().value);
	            }
	            if(facet.getName().equals(XSFacet.FACET_MAXINCLUSIVE)){
	                t.maxValue = Integer.parseInt(facet.getValue().value);
	            }
	            if(facet.getName().equals(XSFacet.FACET_MININCLUSIVE)){
	                t.minValue = Integer.parseInt(facet.getValue().value);
	            }
	            if(facet.getName().equals(XSFacet.FACET_MAXEXCLUSIVE)){
	                t.maxValue = Integer.parseInt(facet.getValue().value) - 1;
	            }
	            if(facet.getName().equals(XSFacet.FACET_MINEXCLUSIVE)){
	                t.minValue = Integer.parseInt(facet.getValue().value) + 1;
	            }
	            if(facet.getName().equals(XSFacet.FACET_LENGTH)){
	                t.length = Integer.parseInt(facet.getValue().value);
	            }
	            if(facet.getName().equals(XSFacet.FACET_MAXLENGTH)){
	                t.maxLength = Integer.parseInt(facet.getValue().value);
	            }
	            if(facet.getName().equals(XSFacet.FACET_MINLENGTH)){
	                t.minLength = Integer.parseInt(facet.getValue().value);
	            }
	            if(facet.getName().equals(XSFacet.FACET_PATTERN)){
	                t.pattern = facet.getValue().value;
	            }
	            if(facet.getName().equals(XSFacet.FACET_TOTALDIGITS)){
	                t.totalDigits = Integer.parseInt(facet.getValue().value);
	            }
	        }
	        if(enumeration.size() > 0){
	            t.enumeration = enumeration;
	        }
	    }
	    return t;
	}
	
	private List<AttributeField> getAttributes(XSComplexType xsComplexType){
		List<AttributeField> attributes = new ArrayList<AttributeField>();
		if (xsComplexType != null) {
		    Collection<? extends XSAttributeUse> c = xsComplexType.getAttributeUses();
		    Iterator<? extends XSAttributeUse> i = c.iterator();
		    while(i.hasNext()){
		    	XSAttributeUse a = i.next();
		        XSAttributeDecl attributeDecl = a.getDecl(); 
		        SimpleTypeRestriction r = getRestriction(attributeDecl.getType());
		        AttributeField af = new AttributeField();
		        af.name = attributeDecl.getName();
		        af.type = r;
		        af.defaultValue = attributeDecl.getDefaultValue() != null ? attributeDecl.getDefaultValue().value : null;
		        af.required = a.isRequired();
		        af.position = a.getLocator().getLineNumber();
		        if (attributeDecl.getAnnotation() != null) {
		        	AnnotationHandler ah = (AnnotationHandler) attributeDecl.getAnnotation().getAnnotation();
		        	af.annotation = ah.getAppinfo();
		        	af.documentation = ah.getDocumenttation();
		        }
		        attributes.add(af);
		    }
		}
		Collections.sort(attributes);
	    return attributes;
	}
	
	private String getMethodName(ITypes.Components component) {
		return "get"+component.toString().substring(0, 1).toUpperCase()+component.toString().substring(1);
	}
	
	private ComplexField findType(ComplexField root, String name, String typeName, Set<ComplexField> visited) {
		if (!visited.contains(root)) {
			visited.add(root);
			for (ComplexField c : root.children.values()) {
				if (c.name.equals(name) && c.typeName.equals(typeName)) return c;
				ComplexField f = findType(c,name,typeName, visited);
				if (f != null) return f;
			}
		}
		return null;
	}
	
	private ComplexField parseComplex(XSParticle tp, XSType type, XSElementDecl element, Compositor compositor, ComplexField parent) {
		String name = element.getName();
		ComplexField existing = findType(rootWrapperType,name,type.getName(),new HashSet<ComplexField>());
		ComplexField cf = new ComplexField();
		cf.typeName = type.getName();
		cf.name = name;
		cf.minOccurs = compositor.equals(Compositor.CHOICE) ? 0 : tp.getMinOccurs().intValue();
		cf.maxOccurs = tp.getMaxOccurs().intValue();
		cf.compositor = compositor;
		cf.position = element.getLocator().getLineNumber();
		if (element.getAnnotation() != null) {
			AnnotationHandler ah = (AnnotationHandler)element.getAnnotation().getAnnotation();
			cf.annotation = ah.getAppinfo();
			cf.documentation = ah.getDocumenttation();
		}
		parent.children.put(cf.name, cf);
		if (existing == null) {
			if (type.isSimpleType()) {
				cf.type = getRestriction(type.asSimpleType());
				cf.defaultValue = (element.getDefaultValue() != null ? element.getDefaultValue().value : null);
			}
			if (type.isComplexType()) {
				if (type.asComplexType().getContentType().asSimpleType() != null) {
					cf.type = getRestriction(type.asComplexType().getContentType().asSimpleType());
				}
				cf.attributes = getAttributes(type.asComplexType());
				XSParticle particle = type.asComplexType().getContentType().asParticle();
				parseParticle(particle, 1, cf);
			}
			return cf;
		} else {
			cf.choiceGroups = existing.choiceGroups;
			cf.attributes = existing.attributes;
			cf.children = existing.children;
			cf.type = existing.type;
			return cf;
		}
	}
	
	private void checkTreeSourceRequired(ComplexField f) {
		treeSourceRequired = !IFunction.class.isAssignableFrom(implementingClass); //functions do not require tree sources by default and have no format attribute
		for (AttributeField a : f.getAttributes()) {
			if (a.getName().equals("format")) {
				treeSourceRequired = f.getAnnotation().equals("source.tree");
				break;
			}
		}
	}

	
	private ComplexField parseElement(XSParticle p, int level, XSModelGroup xsModelGroup, XSParticle particle, ComplexField parent) {
		XSTerm pterm = p.getTerm();
		ComplexField result = null;
		if(pterm.isElementDecl()){ //xs:element inside complex type
        	XSElementDecl element = pterm.asElementDecl();
        	XSType elementType = pterm.asElementDecl().getType();
            if (ITypes.Components.connection.toString().equals(element.getName())) {
            	try {
            		Method method = implementingClass.getMethod(getMethodName(ITypes.Components.connection), new Class<?>[]{});
            		connectionInterface = method.getReturnType();
            		hasConnection = true;
            		connectionRequired = p.getMinOccurs().intValue() > 0;
            	}
            	catch (Exception e) {}
            	result = parseComplex(xsModelGroup.getCompositor().equals(Compositor.CHOICE) ? particle : p,elementType,element,xsModelGroup.getCompositor(),parent);
            	result.hasCustomHandle = true;
            } else if (ITypes.Sources.equals(element.getName())) {
            	result = parseComplex(xsModelGroup.getCompositor().equals(Compositor.CHOICE) ? particle : p,elementType,element,xsModelGroup.getCompositor(),parent);
            	result.hasCustomHandle = true;
            	ComplexField source = result.getChildren().get("source");
            	checkTreeSourceRequired(source);
            	minSources = source.getMinOccurs();
        		maxSources = source.getMaxOccurs();
            } else if (ITypes.Sources.startsWith(element.getName())) {
            	minSources = p.getMinOccurs().intValue();
            	maxSources = 1;
            	simpleSourceElement = true;
            	result = parseComplex(xsModelGroup.getCompositor().equals(Compositor.CHOICE) ? particle : p,elementType,element,xsModelGroup.getCompositor(),parent);
            	result.hasCustomHandle = true;
            	checkTreeSourceRequired(result); 
            } else if ("execution".equals(element.getName())) {
            	minSources = p.getMinOccurs().intValue();
            	maxSources = p.getMaxOccurs().intValue();
            	result = parseComplex(xsModelGroup.getCompositor().equals(Compositor.CHOICE) ? particle : p,elementType,element,xsModelGroup.getCompositor(),parent);
            	result.hasCustomHandle = true;
            } else if (element.getName().equals("comment")) {
            	//allow comments only for function. other components handle then by themselves
            	if (IFunction.class.isAssignableFrom(implementingClass)) {
            		result = parseComplex(xsModelGroup.getCompositor().equals(Compositor.CHOICE) ? particle : p,elementType,element,xsModelGroup.getCompositor(),parent);
            		result.hasCustomHandle = true;
            	}
            } else if (ITypes.Functions.equals(element.getName())) {
            	hasFunctions = true;
            	result = parseComplex(xsModelGroup.getCompositor().equals(Compositor.CHOICE) ? particle : p,elementType,element,xsModelGroup.getCompositor(),parent);
            	result.hasCustomHandle = true;
            } else if (element.getName().equals("variable") && level == 0) {
            	hasVariables = true;
            	result = parseComplex(xsModelGroup.getCompositor().equals(Compositor.CHOICE) ? particle : p,elementType,element,xsModelGroup.getCompositor(),parent);
            	result.hasCustomHandle = true;
            } else if (elementType.asSimpleType() != null || isEncryptableField(elementType.asComplexType(),element.getName())) {
            	result = parseComplex(xsModelGroup.getCompositor().equals(Compositor.CHOICE) ? particle : p,elementType,element,xsModelGroup.getCompositor(),parent);
            } else if (elementType.asComplexType() != null) {
            	result = parseComplex(xsModelGroup.getCompositor().equals(Compositor.CHOICE) ? particle : p,elementType,element,xsModelGroup.getCompositor(),parent);
            }
        }
		return result;
	}

	
	private List<ComplexField> parseParticle(XSParticle particle, int level, ComplexField parent) {
		List<ComplexField> result = new ArrayList<ComplexField>();
		if(particle != null){
	        XSTerm term = particle.getTerm();
	        if(term.isModelGroup() || term.isModelGroupDecl()){
	            XSModelGroup xsModelGroup = term.isModelGroup() ? term.asModelGroup() : term.asModelGroupDecl().getModelGroup();
	            XSParticle[] particles = xsModelGroup.getChildren();
	            ChoiceGroup choiceGroup = new ChoiceGroup();
        		choiceGroup.minOccours = particle.getMinOccurs().intValue();
        		choiceGroup.maxOccours = particle.getMaxOccurs().intValue();
	            for(XSParticle p : particles ){
	                XSTerm pterm = p.getTerm();
	                if(pterm.isElementDecl()){ //xs:element inside complex type
	                	if (level > 0) {
		                	XSElementDecl e = pterm.asElementDecl();
		                	XSType elementType = pterm.asElementDecl().getType();
		                	ComplexField f = parseComplex(xsModelGroup.getCompositor().equals(Compositor.CHOICE) ? particle : p,elementType,e,xsModelGroup.getCompositor(),parent);
		                	if (f != null) result.add(f);
	                	} else {
	                		ComplexField f = parseElement(p,level,xsModelGroup,particle, parent);
	                		if (f != null) result.add(f);
	                	}
	                } else {
	                	List<ComplexField> nextResult = parseParticle(p,level, parent);
	                	result.addAll(nextResult);
	                	if (xsModelGroup.getCompositor().equals(Compositor.CHOICE)) {
	                		choiceGroup.types.add(nextResult);
	                	}
			        }
	            }
	            if (!choiceGroup.types.isEmpty()) {
	            	parent.choiceGroups.add(choiceGroup);
	            }
	        } 		        
	    }
		return result;
	}
	
	
	public void parseRoot(XSType rootType) {
		if (rootType.isComplexType()) {
			XSParticle particle = rootType.asComplexType().getContentType().asParticle();
			parseParticle(particle,0, rootWrapperType);
		}
	}
	
	private boolean isEncryptableField(XSComplexType type, String name) {
		if (type != null && type.getContentType().asSimpleType() != null) {
			for (AttributeField f : getAttributes(type)) {
				if (f.name.equals("encrypted")) return true;
			}
		}
		return false;
	}
	
	public XSSchema getSchema() {
		return schema;
	}
	
	public boolean hasConnection() {
		return hasConnection;
	}
	
	public boolean hasFunctions() {
		return hasFunctions;
	}
	
	public Class<?> getConnectionInterface() {
		return connectionInterface;
	}
	
	public int getMaxSources() {
		return maxSources == -1 ? Integer.MAX_VALUE : maxSources;
	}
	
	public int getMinSources() {
		return minSources;
	}
	
	public ComplexField getRootComplexField() {
		return rootWrapperType;
	}
	
	public ComplexFieldValue getRootComplexFieldValue () {
		return rootWrapperValue;
	}
	
	public ComplexFieldValue getUIFieldValueRoot() {
		ComplexFieldValue result = new ComplexFieldValue();
		result.type = getRootComplexField();
		result.enabled = true;
		result.children = getRootComplexFieldValue().children; //take children list, since we create new elements into this list
		result.init();
		result.type = new ComplexField();
		result.type.choiceGroups.addAll(getRootComplexField().choiceGroups);
		for (String s : getRootComplexField().children.keySet()) { //filter children by available types, simple types first
			ComplexField t = getRootComplexField().children.get(s);
			if (t.getType() != null && !t.hasCustomHandle) result.type.children.put(t.name, t);
		}
		for (String s : getRootComplexField().children.keySet()) { //filter children by available types, simple types first
			ComplexField t = getRootComplexField().children.get(s);
			if (t.getType() == null && !t.hasCustomHandle) result.type.children.put(t.name, t);
		}
		return result;
	}
	
	
	public boolean isTreeSourceRequired() {
		return treeSourceRequired;
	}
	
	public boolean hasSimpleSourceElement() {
		return simpleSourceElement;
	}
	
	public boolean hasVariables() {
		return hasVariables;
	}
	
	public List<ComplexFieldValue> getSources() {
		List<ComplexFieldValue> result = new ArrayList<ComplexFieldValue>();
		if (IJob.class.isAssignableFrom(implementingClass)) {
			result.addAll(getRootComplexFieldValue().getChildrenOfType("execution"));
		}
		else if (hasSimpleSourceElement()) {
			ComplexFieldValue v = getRootComplexFieldValue().getChildOfType("source");
			if (v != null) result.add(v);
		} else {
			ComplexFieldValue sources = getRootComplexFieldValue().getChildOfType(ITypes.Sources);
			if (sources != null) result.addAll(sources.getChildrenOfType("source"));
		}
		return result;
	}
	
	public ComplexFieldValue getSource(String name) {
		if (IJob.class.isAssignableFrom(implementingClass)) {
			for (ComplexFieldValue v : getRootComplexFieldValue().getChildrenOfType("execution")) {
				if (v.getInput().equals(name)) return v;
			}
		}
		else if (hasSimpleSourceElement()) {
			return getRootComplexFieldValue().getChildOfType("source");
		} else {
			ComplexFieldValue sources = getRootComplexFieldValue().getChildOfType(ITypes.Sources);
			if (sources != null) for (ComplexFieldValue v : sources.getChildrenOfType("source")) {
				if (v.getInput().equals(name)) return v;
			}
		}
		return null;
	}
	
	public ComplexFieldValue createSource() {
		if (IJob.class.isAssignableFrom(implementingClass)) {
			ComplexFieldValue existing = getSource("");
			if (existing == null) {
				existing = getRootComplexFieldValue().getEditComponent(getRootComplexField().getChildren().get("execution")).create();
				getRootComplexFieldValue().children.add(existing);
			}
			if (existing != null) {
				existing.setEnabled(true);
			}
			return existing;
		}
		else if (hasSimpleSourceElement()) {
			ComplexField f = getRootComplexField().children.get("source");
			ComplexFieldValue existing = getSource("");
			if (f != null && existing == null) {
				existing = getRootComplexFieldValue().getEditComponent(f).create();
				getRootComplexFieldValue().children.add(existing);
			}
			if (existing != null) {
				existing.setEnabled(true);
			}
			return existing;
		} else {
			ComplexFieldValue sources = getRootComplexFieldValue().getChildOfType(ITypes.Sources);
			ComplexField f = getRootComplexField().getChildren().get(ITypes.Sources);
			if (sources == null && f != null) { //no sources field yet created but creation possible -> create it
				sources = getRootComplexFieldValue().getEditComponent(f).create();
				getRootComplexFieldValue().children.add(sources);
			}
			if (sources != null) {
				ComplexFieldValue existing = getSource("");
				if (existing == null) {
					existing = sources.getEditComponent(sources.getType().getChildren().get("source")).create();
					sources.children.add(existing);
				}
				if (existing != null) {
					existing.setEnabled(true);
				}
				return existing; //anonymous source has been created by parent (minOccurs > 0) deliver it. else create it
			}
			return null;
		}
	}
	
	public void deleteSource(String name) {
		if (hasSimpleSourceElement()) {
			ComplexFieldValue v = getSource(name);
			if (v != null) getRootComplexFieldValue().deleteChild(v);
		} else {
			ComplexFieldValue sources = IJob.class.isAssignableFrom(implementingClass) ? getRootComplexFieldValue() : getRootComplexFieldValue().getChildOfType(ITypes.Sources);
			if (sources != null) sources.deleteChild(getSource(name));
		}
	}
	
	public ComplexFieldValue getConnection() {
		return getRootComplexFieldValue().getChildOfType(ITypes.Components.connection.toString());
	}
	
	public ComplexFieldValue createConnection() {
		ComplexField f = getRootComplexField().children.get(ITypes.Components.connection.toString());
		ComplexFieldValue existing = getConnection();
		if (f != null && existing == null) {
			existing = getRootComplexFieldValue().getEditComponent(f).create();
			getRootComplexFieldValue().children.add(existing);
		}
		if (existing != null) {
			existing.setEnabled(true);
		}
		return existing;
	}
	
	public void deleteConnection() {
		ComplexFieldValue v = getConnection();
		if (v != null) getRootComplexFieldValue().deleteChild(v);
	}
	
	private ComplexFieldValue findExternalProject(ComplexFieldValue node) {
		if (node.isProjectField()) return node;
		for (ComplexFieldValue c : node.getChildren()) {
			ComplexFieldValue p = findExternalProject(c);
			if (p != null) return p;
		}
		return null;
	}
	
	public ComplexFieldValue getExternalProject() {
		return findExternalProject(rootWrapperValue);
	}
	
	public boolean isConnectionRequired() {
		return connectionRequired;
	}
	
	public void setConnectionRequired(boolean connectionRequired) {
		this.connectionRequired = connectionRequired;
	}
	
	
	
	private String enumerate(Collection<String> collection) {
		if (collection == null) return "null";
		StringBuffer buffer = new StringBuffer("[");
		for (String s : collection) {
			buffer.append(",'"+s+"'");
		}
		if (buffer.length() > 1) buffer.deleteCharAt(1);
		buffer.append("]");
		return buffer.toString();
	}
	
	public String getAutoCompletion(Collection<String> inputFields, Collection<String> sources, Collection<String> connections) {
		StringBuffer buffer = new StringBuffer("var tags = {'!top': ");
		List<String> topTags = new ArrayList<String>();
		topTags.addAll(rootWrapperType.children.keySet());
		buffer.append(enumerate(topTags));
		for (String s : rootWrapperType.children.keySet()) {
			buffer.append(", ");
			buffer.append(s);
			buffer.append(": { attrs: { ");
			ComplexField cf = rootWrapperType.children.get(s);
			if (cf.attributes == null || cf.attributes.isEmpty()) {
				buffer.append(",");
			} else for (AttributeField a : cf.attributes) {
				buffer.append(a.name);
				buffer.append(": ");
				if (a.name.equals("nameref") && (cf.name.equals("input") || cf.name.equals("key"))) { //input
					buffer.append(enumerate(inputFields));
				} else if (a.name.equals("nameref") && cf.name.equals("source")) { //input 
					buffer.append(enumerate(sources));
				} else if (a.name.equals("nameref") && cf.name.equals("connection")) { //input 
					buffer.append(enumerate(connections));
				} else {
					buffer.append(enumerate(a.type.enumeration));
				}
				buffer.append(",");
			}
			buffer.insert(buffer.length()-1,"}");
			buffer.append(" children: ");
			List<String> childNames = new ArrayList<String>();
			for (ComplexField ct : cf.children.values()) {
				childNames.add(ct.name);
			}
			buffer.append(enumerate(childNames));
			buffer.append("}");
		}
		buffer.append("};");
		return buffer.toString();
	}
	
	public void applyComplexValues(ComplexFieldValue v, Element e) {
		v.enabled = true;
		if (v.getType().getAttributes() != null) for (AttributeField f : v.getType().attributes) {
			AttributeField vf = f.clone();
			vf.setValue(e.getAttributeValue(f.name));
			v.attributes.put(f.name, vf);
		}
		v.value = e.getTextTrim();
		for (Object o : e.getChildren()) {
			Element c = (Element)o;
			ComplexFieldValue child = new ComplexFieldValue();
			ComplexField type = v.getType().children.get(c.getName());
			if (type != null) {
				child.setType(type);
				child.setParent(v);
				v.children.add(child);
				applyComplexValues(child,c);
			}
		}
		v.init();
	}
	
	public String applyValues(Element root) {
		StringBuffer buffer = new StringBuffer();
		for (Object o : root.getChildren()) {
			Element e = (Element)o;
			if (rootWrapperType.children.keySet().contains(e.getName())) {
				ComplexFieldValue value = new ComplexFieldValue();
				value.setType(rootWrapperType.children.get(e.getName()));
				value.setParent(rootWrapperValue);
				rootWrapperValue.getChildren().add(value);
				applyComplexValues(value, e);
			}
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setOmitDeclaration(true));
			buffer.append(outputter.outputString(e)+"\n");
		}
		return buffer.toString();
	}
	
	private void initFields(ComplexFieldValue v, boolean expand) {
		if (v.getType().getAttributes() != null) for (AttributeField f : v.getType().attributes) {
			if (!v.attributes.containsKey(f.name)) v.attributes.put(f.name,f.clone());
		}
		if (!v.isCompacted()) for (ComplexField t : v.getType().children.values()) {
			boolean criterion = expand ? t.isExpanded() : t.isRequired();
			if (criterion) {
				ComplexFieldValue cv = checkComplexValueByType(v.children,t,v);
				cv.enabled = true;
			}
		}
		v.init();
	}
	
	private ComplexFieldValue checkComplexValueByType(List<ComplexFieldValue> list, ComplexField type, ComplexFieldValue parent) {
		for (ComplexFieldValue v : list) {
			if (v.type.equals(type)) return v;
		}
		//create if not found
		ComplexFieldValue v = null;
		//use editor component, which in turn invokes initComplexFields for children
		if (parent.getEditComponent(type).canCreateNewItem()) {
			v = parent.getEditComponent(type).create();
			v.setType(type);
			list.add(v);
			v.enabled = type.isExpanded();
		}
		return v;
	}
	
	public void initFields(boolean expand) {
		for (String key : rootWrapperType.children.keySet()) {
			ComplexField f = rootWrapperType.children.get(key);
			boolean criterion = expand ? f.isExpanded() : f.isRequired();
			if (criterion) { //create required fields
				ComplexFieldValue v = checkComplexValueByType(rootWrapperValue.children,f,rootWrapperValue);
				if (v != null && v.isEnabled()) {
					initFields(v,expand);
				}
			}
		}
	}
	
	public void initAllFields(ComplexFieldValue value) {
		for (String key : value.getType().children.keySet()) {
			ComplexField f = value.getType().children.get(key);
			ComplexFieldValue v = checkComplexValueByType(value.children,f,value);
			if (v != null && v.isEnabled()) {
				initFields(v,false);
			}
		}
	}
	
	public AttributeField emptyAttributeField() {
		AttributeField result = new AttributeField();
		result.name = "empty";
		result.type = new SimpleTypeRestriction();
		result.type.name = "string";
		return result;
	}
	
	public static void main(String[] args) {
		try {
			XSDAnalyzer analyzer = new XSDAnalyzer(OLAPConnection.class,"connection");
			if (analyzer.hasConnection) System.out.println(analyzer.getConnectionInterface().getSimpleName());
			System.out.println(analyzer.getMaxSources());
			System.out.println(analyzer.isTreeSourceRequired());
			System.out.println(analyzer.getAutoCompletion(Arrays.asList(new String[]{"price","amount"}), new ArrayList<String>(),new ArrayList<String>()));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}

	
