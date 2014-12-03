package com.proclos.colibriweb.session.modules.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.proclos.colibriweb.common.UITreeBuilder;
import com.proclos.colibriweb.common.XSDAnalyzer;
import com.proclos.colibriweb.entity.component.Component;
import com.proclos.colibriweb.session.system.ModuleManager;


@Name("helpProvider")
@Scope(ScopeType.CONVERSATION)
public class HelpProvider implements Serializable {
	
	public class TypeTree {
		private TreeNode root;
		
		public TypeTree(XSDAnalyzer.ComplexField data) {
			root = new DefaultTreeNode(data.getDisplayName(),null);
			buildTree(data,root);
		}
		
		private TreeNode buildTree(XSDAnalyzer.ComplexField field, TreeNode parent) {
			TreeNode node = new DefaultTreeNode(field,parent);		
			node.setExpanded(true);
			for (XSDAnalyzer.ComplexField f : field.getChildren().values()) {
				buildTree(f,node); //ignore fields of types not set
			}
			return node;
		}
		
		public TreeNode getRoot() {
			return root;
		}
		
		private int findLevel(TreeNode node, XSDAnalyzer.ComplexField field, int level) {
			if (node.getData().equals(field)) return level;
			for (TreeNode c : node.getChildren()) {
				level = findLevel(c,field,level+1);
			}
			return level;
		}
		
		public boolean isEvenLevel(XSDAnalyzer.ComplexField field) {
			return findLevel(root,field,0) % 2 != 0;
		}
	}
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 292654012303995038L;
	
	@RequestParameter
	private String name;
	
	@RequestParameter
	private String type;
	
	private ComponentModule<Component> module;
	
	public String getName() {
		return name;
	}
	
	public String getDisplayType() {
		return type.substring(0, 1).toUpperCase() + type.substring(1);
	}
	
	@SuppressWarnings("unchecked")
	private ComponentModule<Component> getModule() {
		if (module == null) {
			module = (ComponentModule<Component>)ModuleManager.instance().getModuleForName(type);
		}
		return module;
	}
	
	public String getComponentHelp() {
		return getModule().getAnalyzer().getRootComplexField().getDocumentation();
	}
	
	public List<TypeTree> getUIElementHelp() {
		List<TypeTree> result = new ArrayList<TypeTree>();
		for (UITreeBuilder b : getModule().getComplexRootNodes()) {
			for (XSDAnalyzer.ComplexField f : b.getRootField().getType().getChildren().values()) {
				result.add(new TypeTree(f));
			}
		}
		return result;
	}
	
	public String getElementInfo(XSDAnalyzer.ComplexField field) {
		if (field.getMinOccurs() == 0 && field.getMaxOccurs() == 1) return "optional";
		if (field.getMinOccurs() == 1 && field.getMaxOccurs() == 1) return "required";
		return field.getMinOccurs()+".."+(field.getMaxOccurs() == Integer.MAX_VALUE || field.getMaxOccurs() == -1 ? "∞" : field.getMaxOccurs());
	}
	
	public String getAttributeInfo(XSDAnalyzer.AttributeField attribute) {
		return attribute.isRequired() ? "required" : "optional";
	}
	
}
