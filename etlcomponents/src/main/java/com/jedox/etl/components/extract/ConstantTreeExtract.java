package com.jedox.etl.components.extract;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import com.jedox.etl.core.component.InitializationException;
import com.jedox.etl.core.component.RuntimeException;
import com.jedox.etl.core.config.source.TreeSourceConfigurator;
import com.jedox.etl.core.extract.IExtract;
import com.jedox.etl.core.node.AttributeNode;
import com.jedox.etl.core.node.ColumnNodeFactory;
import com.jedox.etl.core.node.Row;
import com.jedox.etl.core.node.tree.Attribute;
import com.jedox.etl.core.source.TreeSource;
import com.jedox.etl.core.source.IView.Views;
import com.jedox.etl.core.source.processor.ConstantTableProcessor;
import com.jedox.etl.core.source.processor.IProcessor;
import com.jedox.etl.core.source.processor.ITreeProcessor;
import com.jedox.etl.core.source.processor.TreeBuildProcessor;
import com.jedox.etl.core.source.processor.IProcessor.Facets;
import com.jedox.etl.core.util.NamingUtil;
import com.jedox.palojlib.interfaces.IElement.ElementType;

public class ConstantTreeExtract extends TreeSource implements IExtract {

	private Element data;	
	
	public ConstantTreeExtract() {
		setConfigurator(new TreeSourceConfigurator());
	}

	public TreeSourceConfigurator getConfigurator() {
		return super.getConfigurator();
	}
	
	public ITreeProcessor buildTree() throws RuntimeException {		
		IProcessor tableProc = initProcessor(new ConstantTableProcessor(data, 0),Facets.OUTPUT);
		TreeBuildProcessor treeBuilder = new TreeBuildProcessor(tableProc, Views.LEWTA);
		
		List<AttributeNode> attributes = new ArrayList<AttributeNode>();
		Row row = tableProc.getOutputDescription();
		for (int i=4; i< row.size(); i++) {
			attributes.add(ColumnNodeFactory.getInstance().createAttributeNode(new Attribute(row.getColumn(i).getName(),ElementType.ELEMENT_STRING), null));
		}
		treeBuilder.setAttributes(attributes);
		initProcessor(treeBuilder,Facets.HIDDEN).run();
		setTreeManager(treeBuilder.getManager());		
		return treeBuilder;
	}	

	public void init() throws InitializationException {
		super.init();
		try {
			data = getConfigurator().getXML().getChild("data");
			if(data==null){
				data = new Element("data");
				Element header = new Element("header");
				data.addContent(header);
				header.addContent(new Element("value").setText(NamingUtil.getElementnameLevel()));
				header.addContent(new Element("value").setText(NamingUtil.getElementnameElement()));
				header.addContent(new Element("value").setText(NamingUtil.getElementnameWeight()));
				header.addContent(new Element("value").setText(NamingUtil.getElementnameNodeType()));
			}
		} catch (Exception e) {
			throw new InitializationException(e);
		}
	}	
	
}
