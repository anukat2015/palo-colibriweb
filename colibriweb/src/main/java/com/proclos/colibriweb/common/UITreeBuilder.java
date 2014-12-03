package com.proclos.colibriweb.common;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.jaxen.dom.DOMXPath;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.proclos.colibriweb.entity.component.Component;
import com.proclos.colibriweb.session.common.IEditComponent;

public class UITreeBuilder extends UIBuilder {
	
	public class TreeNodeComparator implements Comparator<TreeNode> {

		@Override
		public int compare(TreeNode some, TreeNode other) {
			if (some.getData() instanceof IUIField && other.getData() instanceof IUIField) {
				IUIField thisData = (IUIField)some.getData();
				IUIField otherData = (IUIField)other.getData();
				return thisData.getTypeName().compareTo(otherData.getTypeName());
			}
			return 0;
		}
		
	}
	
	private TreeNode root;
	private int position;
	private Map<XSDAnalyzer.ComplexFieldValue,TreeNode> lookup = new HashMap<XSDAnalyzer.ComplexFieldValue,TreeNode>();

	public UITreeBuilder(XSDAnalyzer.ComplexFieldValue data, Component instance, int position) {
		super(instance);
		this.position = position;
		root = new DefaultTreeNode(data.getName(),null);
		buildTree(data,root);
		expandTree(getExpandedSet(lookup.get(data)));
	}
	
	private TreeNode buildTree(XSDAnalyzer.ComplexFieldValue field, TreeNode parent) {
		TreeNode node = lookup.get(field);
		if (node == null) {
			node = new DefaultTreeNode(field,parent);
			lookup.put(field, node);
		}
		//node.setExpanded(field.getType().isRequired());
		node.setExpanded(true);
		for (XSDAnalyzer.ComplexFieldValue f : field.getChildren()) {
			if (field.getType().getChildren().get(f.getName()) != null) buildTree(f,node); //ignore fields of types not set
		}
		return node;
	}
	
	public TreeNode getRoot() {
		return root;
	}
	
	public XSDAnalyzer.ComplexFieldValue getRootField() {
		return !root.getChildren().isEmpty() ? (XSDAnalyzer.ComplexFieldValue) root.getChildren().get(0).getData() : null;
	}
	
	public boolean isRootField(XSDAnalyzer.ComplexFieldValue field) {
		return field == null || lookup.get(field).getParent() == null || lookup.get(field).getParent().equals(root);
	}
	
	public boolean isEvenLevel(XSDAnalyzer.ComplexFieldValue field) {
		TreeNode node = lookup.get(field);
		int level = 0;
		while (node.getParent() != null && !node.getParent().equals(root)) {
			level++;
			node = node.getParent();
		}
		return level % 2 == 0;
	}
	
	private void refresh(TreeNode node) {
		XSDAnalyzer.ComplexFieldValue data = (XSDAnalyzer.ComplexFieldValue)node.getData();
		Set<XSDAnalyzer.ComplexFieldValue> newChildren = new LinkedHashSet<XSDAnalyzer.ComplexFieldValue>();
		newChildren.addAll(data.getChildren());
		List<TreeNode> toRemove = new ArrayList<TreeNode>();
		for (TreeNode n : node.getChildren()) {
			if (newChildren.contains(n.getData())) {
				newChildren.remove(n.getData());
			} else {
				toRemove.add(n);
			}
		}
		node.getChildren().removeAll(toRemove); //remove deleted data
		for (XSDAnalyzer.ComplexFieldValue newChild : newChildren) { //add new data
			new DefaultTreeNode(newChild,node);
		}
		for (TreeNode n : node.getChildren()) {
			refresh(n);
		}
	}
	
	public void refresh() {
		refresh(root.getChildren().get(0));
	}
	
	/*
	private void collapseTree(TreeNode root) {
		root.setExpanded(false);
		for (TreeNode n : root.getChildren()) {
			collapseTree(n);
		}
	}
	*/
	
	private Set<TreeNode> getAncestors(TreeNode node) {
		Set<TreeNode> result = new HashSet<TreeNode>();
		while (node.getParent() != null && !node.getParent().equals(root)) {
			result.add(node.getParent());
			node = node.getParent();
		}
		return result;
	}
	
	private Set<TreeNode> getExpandedSet(TreeNode root) {
		Set<TreeNode> result = new HashSet<TreeNode>();
		for (TreeNode node : root.getChildren()) {
			if (node.isExpanded()) result.addAll(getAncestors(node));
			getExpandedSet(node);
		}
		return result;
	}
	
	private void expandTree(Set<TreeNode> nodeSet) {
		for (TreeNode node : nodeSet) {
			node.setExpanded(true);
		}
	}
	
	public void create(XSDAnalyzer.ComplexFieldValue parent, XSDAnalyzer.ComplexField type) {
		
		long time = System.currentTimeMillis();
		IEditComponent<XSDAnalyzer.ComplexFieldValue> editor = parent.getEditComponent(type);
		XSDAnalyzer.ComplexFieldValue field = editor.create();
		parent.getChildren().add(field);
		this.buildTree(field,lookup.get(parent));
		//lookup.put(field, node);
		expandTree(getExpandedSet(lookup.get(parent)));
		//move to right position
		List<XSDAnalyzer.ComplexFieldValue> sortedChildren = parent.getSortedChildren();
		for (int i=0; i<sortedChildren.size(); i++) {
			int j = parent.getChildrenOfTypes().indexOf(sortedChildren.get(i));
			for (int k = j; k < i; k++) {
				moveDown(sortedChildren.get(i));
			}
			for (int k = i; k < j; k++) {
				moveUp(sortedChildren.get(i));
			}
		}
		parent.setEnabled(true);
		//System.err.println(System.currentTimeMillis()-time);
	}
	
	public void delete(XSDAnalyzer.ComplexFieldValue toDelete) {
		TreeNode childNode = lookup.get(toDelete);
		TreeNode parentNode = childNode.getParent();
		XSDAnalyzer.ComplexFieldValue parent = (XSDAnalyzer.ComplexFieldValue)parentNode.getData();
		lookup.remove(toDelete);
		parentNode.getChildren().remove(childNode);
		parent.deleteChild(toDelete);
		/*
		if (treeId != null) {
			RequestContext context = RequestContext.getCurrentInstance();
			context.update(treeId);
		} else needUpdate = true;
		*/
	}
	
	public boolean mayDeleteItem(XSDAnalyzer.ComplexFieldValue field) {
		if (field == null) return false;
		TreeNode parent = lookup.get(field).getParent();
		if (parent != null && !parent.equals(root)) {
			return ((XSDAnalyzer.ComplexFieldValue)parent.getData()).getEditComponent(field.getType()).canDeleteItem(field);
		}
		return false;
	}
	
	
	public boolean mayMoveUp(XSDAnalyzer.ComplexFieldValue field) {
		if (field == null) return false;
		TreeNode node = lookup.get(field);
		TreeNode parent = node.getParent();
		if (parent != null && !parent.equals(root)) {
			return (field.getType().isSequence() || field.getType().isChoice()) && parent.getChildren().indexOf(node) > 0;
		}
		return false;
	}
	
	public boolean mayMoveDown(XSDAnalyzer.ComplexFieldValue field) {
		if (field == null) return false;
		TreeNode node = lookup.get(field);
		TreeNode parent = node.getParent();
		if (parent != null && !parent.equals(root)) {
			return (field.getType().isSequence() || field.getType().isChoice()) && parent.getChildren().indexOf(node) >= 0 && parent.getChildren().indexOf(node) < parent.getChildren().size()-1;
		}
		return false;
	}
	
	public void moveUp(XSDAnalyzer.ComplexFieldValue field) {
		TreeNode node = lookup.get(field);
		TreeNode parent = node.getParent();
		XSDAnalyzer.ComplexFieldValue parentField = (XSDAnalyzer.ComplexFieldValue)parent.getData();
		int index = parent.getChildren().indexOf(node);
		TreeNode precessor = parent.getChildren().get(index-1);
		Object data = precessor.getData();
		((DefaultTreeNode)precessor).setData(node.getData());
		((DefaultTreeNode)node).setData(data);
		lookup.remove(node.getData());
		lookup.remove(precessor.getData());
		lookup.put((XSDAnalyzer.ComplexFieldValue)node.getData(), node);
		lookup.put((XSDAnalyzer.ComplexFieldValue)precessor.getData(), precessor);
		List<TreeNode> children = new ArrayList<TreeNode>();
		children.addAll(precessor.getChildren());
		precessor.getChildren().clear();
		precessor.getChildren().addAll(node.getChildren());
		node.getChildren().clear();
		node.getChildren().addAll(children);
		parentField.moveUp(field);
	}
	
	public void moveDown(XSDAnalyzer.ComplexFieldValue field) {
		TreeNode node = lookup.get(field);
		TreeNode parent = node.getParent();
		XSDAnalyzer.ComplexFieldValue parentField = (XSDAnalyzer.ComplexFieldValue)parent.getData();
		int index = parent.getChildren().indexOf(node);
		TreeNode successor = parent.getChildren().get(index+1);
		Object data = successor.getData();
		((DefaultTreeNode)successor).setData(node.getData());
		((DefaultTreeNode)node).setData(data);
		lookup.remove(node.getData());
		lookup.remove(successor.getData());
		lookup.put((XSDAnalyzer.ComplexFieldValue)node.getData(), node);
		lookup.put((XSDAnalyzer.ComplexFieldValue)successor.getData(), successor);
		List<TreeNode> children = new ArrayList<TreeNode>();
		children.addAll(successor.getChildren());
		successor.getChildren().clear();
		successor.getChildren().addAll(node.getChildren());
		node.getChildren().clear();
		node.getChildren().addAll(children);
		parentField.moveDown(field);
	}
	
	
	public void onNodeExpand(NodeExpandEvent event) {
		//event.getTreeNode().setExpanded(true);
	}
	
	public void onNodeCollapse(NodeCollapseEvent event) {
		//event.getTreeNode().setExpanded(false);
	}
	
	private Element getAsElement(Document document, TreeNode node) {
		XSDAnalyzer.ComplexFieldValue field = (XSDAnalyzer.ComplexFieldValue)node.getData();
		Element e = document.createElement(field.getName());
		for (XSDAnalyzer.AttributeField a : field.getAttributeList()) {
			if (a.getValue() != null) {
				e.setAttribute(a.getName(), a.getValue().toString());
			}
		}
		for (TreeNode n : node.getChildren()) {
			Element c = getAsElement(document,n);
			e.appendChild(c);
		}
		if (node.getChildCount() == 0) {
			e.setTextContent(field.getValue() != null ? field.getValue().toString() : null);
		}
		return e;
	}
	
	private Document getAsDocument() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			Document document = factory.newDocumentBuilder().newDocument();
			//function have direct root nodes, other components start with empty root node
			TreeNode documentRoot = (root.getData() == null || root.getData().toString().isEmpty()) ? root.getChildren().get(0) : root;
			for (TreeNode node : documentRoot.getChildren()) {
				document.appendChild(getAsElement(document,node));
			}
			
			StringWriter writer = new StringWriter();
		    StreamResult result = new StreamResult(writer);
			// create an instance of TransformerFactory
			TransformerFactory transFact = TransformerFactory.newInstance();
			Transformer trans = transFact.newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
			trans.transform(new DOMSource(document), result);
			String renderedDoc = writer.toString();
			//System.err.println(renderedDoc);
			
			return document;
		} catch (ParserConfigurationException e) {
			addErrorMessage("XML representation cannot be built: "+e.getMessage());
			return null;
		}
		
		catch (TransformerException e1) {
			e1.printStackTrace();
			return null;
		}
	}
	
	protected String evalXPath(String expression) {
		try {
			Node section = getAsDocument();
			if (section != null) {
				DOMXPath fieldExpression = new DOMXPath(expression);
				List<?> nodes = fieldExpression.selectNodes(section);
				if (nodes.size() == 1) {
					Node node = (Node) nodes.get(0);
					return node.getTextContent();
				} else {
					addErrorMessage("Invalid XPath expression found in component definition: "+expression);
				}
			}
		}
		catch (Exception e) {
			addErrorMessage("XPath "+expression+" cannot be avaluated: "+e.getMessage());
		}
		return super.evalXPath(expression); //fallback to null
	}
	
	private String getNodeName(String prefix, TreeNode node) {
		StringBuffer buf = new StringBuffer();
		while (node.getParent() != null) {
			int index = node.getParent().getChildren().indexOf(node);
			buf.insert(0, "_"+index);
			node = node.getParent();
		}
		if (buf.length() > 0) buf.replace(0,1,":");
		return prefix+":"+position+":root"+buf.toString();
	}
	
	public String getNodeToUpdate(String prefix, XSDAnalyzer.ComplexFieldValue field) {
		TreeNode node = lookup.get(field);
		return getNodeName(prefix,node).substring(1);
	}
	
	public String getParentNodeToUpdate(String prefix, XSDAnalyzer.ComplexFieldValue field) {
		TreeNode node = lookup.get(field);
		return getNodeName(prefix,node.getParent()).substring(1);
	}
	
	public int getPosition() {
		return position;
	}

}
