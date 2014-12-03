package com.proclos.colibriweb.entity.component;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;
import org.jdom.CDATA;
import org.jdom.Element;

import at.adaptive.components.bean.annotation.SearchField;

import com.jedox.etl.core.util.XMLUtil;
import com.proclos.colibriweb.common.IAccessControlled;
import com.proclos.colibriweb.common.ISelector;
import com.proclos.colibriweb.entity.BaseEntity;
import com.proclos.colibriweb.session.system.AccessUtil;

import java.util.List;
import java.util.ArrayList;

@Entity
public class Project extends BaseEntity implements ISelector, IAccessControlled {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5702512326182071146L;
	
	private String name;
	private ProjectType type;
	private Integer publicationState = AccessUtil.USER;
	private String description;
	private String contactPerson;
	private String contactOnError;
	private List<Variable> variables = new ArrayList<Variable>();
	private List<Connection> connections = new ArrayList<Connection>();
	private List<Extract> extracts = new ArrayList<Extract>();
	private List<Transform> transforms = new ArrayList<Transform>();
	private List<Load> loads = new ArrayList<Load>();
	private List<Job> jobs = new ArrayList<Job>();
	
	@SearchField(groups = {"listSearchProperties"})
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@SearchField
	@ManyToOne(fetch = FetchType.LAZY)
	public ProjectType getType() {
		return type;
	}
	public void setType(ProjectType type) {
		this.type = type;
	}
	public Integer getPublicationState() {
		return publicationState;
	}
	public void setPublicationState(Integer publicationState) {
		this.publicationState = publicationState;
	}
	@SearchField(groups = {"listSearchProperties"})
	@Lob
	@Type(type = "org.hibernate.type.StringClobType") 
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@IndexColumn(name = "position")
	@BatchSize(size = 50)
	public List<Variable> getVariables() {
		return variables;
	}
	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}
	@SearchField(groups = {"listSearchProperties"})
	public String getContactPerson() {
		return contactPerson;
	}
	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}
	public String getContactOnError() {
		return contactOnError;
	}
	public void setContactOnError(String contactOnError) {
		this.contactOnError = contactOnError;
	}
	@OneToMany(fetch = FetchType.LAZY, mappedBy="project", cascade={CascadeType.REFRESH,CascadeType.MERGE,CascadeType.DETACH,CascadeType.REMOVE},targetEntity=Component.class)
	@Where(clause="dtype="+ComponentTypes.CONNECTION)
	public List<Connection> getConnections() {
		return connections;
	}
	public void setConnections(List<Connection> connections) {
		this.connections = connections;
	}
	@OneToMany(fetch = FetchType.LAZY, mappedBy="project", cascade={CascadeType.REFRESH,CascadeType.MERGE,CascadeType.DETACH,CascadeType.REMOVE},targetEntity=Component.class)
	@Where(clause="dtype="+ComponentTypes.EXTRACT)
	public List<Extract> getExtracts() {
		return extracts;
	}
	public void setExtracts(List<Extract> extracts) {
		this.extracts = extracts;
	}
	@OneToMany(fetch = FetchType.LAZY, mappedBy="project", cascade={CascadeType.REFRESH,CascadeType.MERGE,CascadeType.DETACH,CascadeType.REMOVE},targetEntity=Component.class)
	@Where(clause="dtype="+ComponentTypes.TRANSFORM)
	public List<Transform> getTransforms() {
		return transforms;
	}
	public void setTransforms(List<Transform> transforms) {
		this.transforms = transforms;
	}
	@OneToMany(fetch = FetchType.LAZY, mappedBy="project", cascade={CascadeType.REFRESH,CascadeType.MERGE,CascadeType.DETACH,CascadeType.REMOVE},targetEntity=Component.class)
	@Where(clause="dtype="+ComponentTypes.LOAD)
	public List<Load> getLoads() {
		return loads;
	}
	public void setLoads(List<Load> loads) {
		this.loads = loads;
	}
	@OneToMany(fetch = FetchType.LAZY, mappedBy="project", cascade={CascadeType.REFRESH,CascadeType.MERGE,CascadeType.DETACH,CascadeType.REMOVE},targetEntity=Component.class)
	@Where(clause="dtype="+ComponentTypes.JOB)
	public List<Job> getJobs() {
		return jobs;
	}
	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}
	
	@Transient
	public Element getVariableXML() {
		Element variables = new Element("variables");
		for (Variable variable : getVariables()) {
			Element v = new Element("variable");
			v.setAttribute("name", variable.getName());
			Element comment = new Element("comment");
			CDATA cdata= new CDATA(variable.getComment());
			comment.addContent(cdata);
			v.addContent(comment);
			Element value = new Element("default");
			value.setText(variable.getValue());
			v.addContent(value);
			variables.addContent(v);
		}
		return variables;
	}
	
	@Transient
	public Element getProjectXML() {
		Element root = new Element("project");
		root.setAttribute("name", getName() != null ? getName() : "newProject");
		Element variables = getVariableXML();
		Element connections = new Element("connections");
		Element extracts = new Element("extracts");
		Element transforms = new Element("transforms");
		Element loads = new Element("loads");
		Element jobs = new Element("jobs");
		root.addContent(variables);
		root.addContent(connections);
		root.addContent(extracts);
		root.addContent(transforms);
		root.addContent(loads);
		root.addContent(jobs);
		try {
			for (Component c: getConnections()) {
				connections.addContent(XMLUtil.stringTojdom(c.getXml()).detach());
			}
			for (Component c: getExtracts()) {
				extracts.addContent(XMLUtil.stringTojdom(c.getXml()).detach());
			}
			for (Component c: getTransforms()) {
				transforms.addContent(XMLUtil.stringTojdom(c.getXml()).detach());
			}
			for (Component c: getLoads()) {
				loads.addContent(XMLUtil.stringTojdom(c.getXml()).detach());
			}
			for (Component c: getJobs()) {
				jobs.addContent(XMLUtil.stringTojdom(c.getXml()).detach());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return root;
	}


}
