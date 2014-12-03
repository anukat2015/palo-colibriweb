package com.proclos.colibriweb.session.modules.component;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;

import at.adaptive.components.validator.ParameterizableUniqueValidator;
import at.adaptive.components.validator.UniqueValidator;

import com.proclos.colibriweb.entity.component.Component;
import com.proclos.colibriweb.entity.component.ComponentTypes;
import com.proclos.colibriweb.entity.component.Project;
import com.proclos.colibriweb.entity.component.ProjectType;

public class CloneHelper {
	
	private int mode = modeCopy;
	private int target = targetCurrent;
	private boolean withDependencies = false;
	private boolean updateDependencies = true;
	private Project targetProject;
	private String newProjectName;
	private String newComponentName;
	private ProjectType newProjectType;
	private UniqueValidator nameValidator;
	private boolean overwrite = false;
	private Project initialProject;
	private Component component;
	private String initialComponentName;
	
	public static final int modeCopy = 1;
	public static final int modeMove = 2;
	public static final int modeDelete = 3;
	public static final int targetCurrent = 1;
	public static final int targetExisting = 2;
	public static final int targetNew = 3;
	
	public CloneHelper(Component component) {
		Project initialProject = component.getProject();
		String initialComponentName = component.getName();
		this.targetProject = initialProject;
		this.initialProject = initialProject;
		this.initialComponentName = initialComponentName;
		this.component = component;
	}
	
	public int getMode() {
		return mode;
	}
	
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	public int getTarget() {
		return target;
	}
	
	public void setTarget(int target) {
		this.target = target;
	}

	public boolean isUpdateDependencies() {
		return updateDependencies;
	}

	public void setUpdateDependencies(boolean updateDependencies) {
		this.updateDependencies = updateDependencies;
	}

	public Project getTargetProject() {
		return targetProject;
	}

	public void setTargetProject(Project targetProject) {
		this.targetProject = targetProject;
	}

	public String getNewProjectName() {
		return newProjectName;
	}

	public void setNewProjectName(String newProjectName) {
		this.newProjectName = newProjectName;
	}

	public ProjectType getNewProjectType() {
		return newProjectType;
	}

	public void setNewProjectType(ProjectType newProjectType) {
		this.newProjectType = newProjectType;
	}
	
	
	public void validateProjectName(FacesContext context, UIComponent c, Object obj) throws ValidatorException
	{
		nameValidator = new ParameterizableUniqueValidator("from Project project where project.name is not null and lower(project.name) like :value", "Name not valid", "Project with this name already exists");
		String name = (String)obj;
		if(name != null)
		{
			name = name.toLowerCase().trim();
		}
		nameValidator.validate(null, context, c, name);
	}
	
	public void validateComponentName(FacesContext context, UIComponent c, Object obj) throws ValidatorException
	{
		StringBuffer cTypes = new StringBuffer("(");
		cTypes.append(component.getdType());
		if (component.getdType() == ComponentTypes.EXTRACT) cTypes.append(","+ComponentTypes.TRANSFORM);
		if (component.getdType() == ComponentTypes.TRANSFORM) cTypes.append(ComponentTypes.EXTRACT);
		cTypes.append(")");
		nameValidator = new ParameterizableUniqueValidator("from Component component where component.name is not null and component.dType in "+cTypes.toString()+" and component.project.id = "+targetProject.getId()+" and lower(component.name) like :value", "Name not valid", "Component with this name already exists");
		String name = (String)obj;
		if(name != null)
		{
			name = name.toLowerCase().trim();
		}
		nameValidator.validate(null, context, c, name);
	}

	public String getNewComponentName() {
		return newComponentName;
	}

	public void setNewComponentName(String newComponentName) {
		this.newComponentName = newComponentName;
	}

	public boolean isOverwrite() {
		return overwrite;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public boolean isWithDependencies() {
		return withDependencies;
	}

	public void setWithDependencies(boolean withDependencies) {
		this.withDependencies = withDependencies;
	}
	
	public void checkSettings() {
		if (mode != modeCopy) {
			targetProject = initialProject;
			target = targetCurrent;
		}
		if (target != targetCurrent && StringUtils.isEmpty(newComponentName)) {
			newComponentName = initialComponentName;
		}
	}

}
