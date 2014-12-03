package com.proclos.colibriweb.entity.component;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.Formula;

import at.adaptive.components.bean.annotation.SearchField;

import com.proclos.colibriweb.common.IAccessControlled;
import com.proclos.colibriweb.common.XSDAnalyzer;
import com.proclos.colibriweb.entity.BaseEntity;

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dType", discriminatorType = DiscriminatorType.INTEGER)
@Entity
public abstract class Component extends BaseEntity implements IAccessControlled, Comparable<Component> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6679292749703383892L;
	
	private Project project;
	private String type;
	private String name;
	private Integer dType;
	private String xml;
	private String code;
	private String description;
	private Connection connection;
	private Integer publicationState;
	private List<ComponentOutput> outputDescription = new ArrayList<ComponentOutput>();
	private List<Variable> variables = new ArrayList<Variable>();
	private List<Component> sources = new ArrayList<Component>();
	private List<Execution> executions = new ArrayList<Execution>();
	private XSDAnalyzer analyzer;
	
	@Column(insertable = false, updatable = false)
	public Integer getdType() {
		return dType;
	}

	public void setdType(Integer dType) {
		this.dType = dType;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@SearchField
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@SearchField(groups = {"listSearchProperties"})
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	@Formula("(select p.publicationState from project p where p.id = project_id)")
	public Integer getPublicationState() {
		return getProject().getPublicationState();
	}
	
	public void setPublicationState(Integer publicationState) {
	}
	
	@Transient
	public String getContactPerson() {
		return (project != null) ? project.getContactPerson() : null;
	}
	
	@Transient
	public String getContactOnError() {
		return (project != null) ? project.getContactOnError() : null;
	}

	@SearchField(groups = {"listSearchProperties"})
	@Type(type = "org.hibernate.type.StringClobType") 
	@Lob
	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@OneToMany(mappedBy="component", fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH,CascadeType.REMOVE}, orphanRemoval=true)
	public List<Execution> getExecutions() {
		return executions;
	}

	public void setExecutions(List<Execution> executions) {
		this.executions = executions;
	}
	
	@OneToMany(mappedBy="component", fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH,CascadeType.REMOVE}, orphanRemoval=true)
	public List<ComponentOutput> getOutputDescription() {
		return outputDescription;
	}

	public void setOutputDescription(List<ComponentOutput> outputDescription) {
		this.outputDescription = outputDescription;
	}

	@SearchField(groups = {"listSearchProperties"})
	@Type(type = "org.hibernate.type.StringClobType") 
	@Lob
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Transient
	public List<Variable> getVariables() {
		return variables;
	}
	
	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}

	@Transient
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Transient
	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	@Transient
	public List<Component> getSources() {
		return sources;
	}

	public void setSources(List<Component> sources) {
		this.sources = sources;
	}
	
	@Transient
	public String getLabel() {
		return getName();
	}
	
	@Transient
	public String getIconTitle() {
		return xml;
	}
	
	@Override
	@Transient
	public int compareTo(Component c) {
		return getName().compareTo(c.getName());
	}

	@Transient
	public XSDAnalyzer getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(XSDAnalyzer analyzer) {
		this.analyzer = analyzer;
	}
	
	@Transient
	public String getInternalName() {
		return getName() != null ? getName().replaceAll("[^A-Za-z0-9]", "x") : "newComponent";
	}

}
